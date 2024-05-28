package com.github.chen.dingtalkrobotmsg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chen.dingtalkrobotmsg.enums.JarEnum;
import com.github.chen.dingtalkrobotmsg.msg.CallBackMsg;
import com.github.chen.dingtalkrobotmsg.service.IntranetService;
import io.micrometer.common.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Profile("intranet")
public class IntranetServiceImpl implements IntranetService {

    @Value("${nginx-mapping-url}")
    private String nginxMappingUrl;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .build();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void receiveSignal(String jarName, String environment) {
        log.info("received signal {} {}", jarName, environment);

        executor.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                checkHealth(jarName, environment);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                send2Extranet("Deployment interrupted: " + e.getMessage());
            }
        });

        log.info("Signal received for jar: " + jarName + " in environment: " + environment);

    }

    @SneakyThrows
    private void checkHealth(String jarName, String env) {
        // 根据枚举获取健康检查url
        JarEnum jarEnum = JarEnum.getHealthUrl(jarName, env);
        if (jarEnum == null || StringUtils.isBlank(jarEnum.getHealthUrl())) {
            log.info("healthUrl is blank");
            return;
        }
        String healthUrl = jarEnum.getHealthUrl();

        // 发送消息到外网，表示项目开始部署
        String msgStart = "[监控网" + env + "环境]开始部署: \"" + jarEnum.getMsg() + "\"";
        String msgSuccess = "[监控网" + env + "环境]部署: \"" + jarEnum.getMsg() + "\" 成功";
        String msgTimeOut = "[监控网" + env + "环境]部署: \"" + jarEnum.getMsg() + "\" 超时";
        send2Extranet(msgStart);
        long startTime = System.currentTimeMillis();

        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("sleep error", e);
        }

        while (true) {
            try {
                log.info("start request health, url={}", healthUrl);
                Request request = new Request.Builder()
                        .url(healthUrl)
                        .build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        log.info("health request success");
                        send2Extranet(msgSuccess);
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("health request error", e);
            }

            // 超时 300 秒
            if (System.currentTimeMillis() - startTime > 300000) {
                log.info("health request timeout");
                send2Extranet(msgTimeOut + ", 部署时间: " + (System.currentTimeMillis() - startTime) / 1000 + "秒");
                break;
            }

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("health request error", e);
                break;
            }
        }
    }

    /**
     * 请求nginx路由接口转发到外网
     *
     * @param message
     */
    private void send2Extranet(String message) {
        String url = nginxMappingUrl;

        CallBackMsg msg = new CallBackMsg();
        msg.setMsg(message);

        try {
            // 将对象转换为 JSON 字符串
            String json = objectMapper.writeValueAsString(msg);

            // 创建请求体
            RequestBody body = RequestBody.create(json, JSON);

            // 构建 POST 请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // 发送请求
            try (Response response = client.newCall(request).execute()) {
                log.info("send message to extranet: {} ", response);
            } catch (IOException e) {
                log.error("Failed to send message to extranet: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("Failed to convert message to JSON: " + e.getMessage(), e);
        }
    }
}
