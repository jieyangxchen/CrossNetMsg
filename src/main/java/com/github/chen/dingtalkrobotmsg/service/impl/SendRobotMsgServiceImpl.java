package com.github.chen.dingtalkrobotmsg.service.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.github.chen.dingtalkrobotmsg.service.SendRobotMsgService;
import com.taobao.api.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendRobotMsgServiceImpl implements SendRobotMsgService {
    /**
     * 只需要token
     */
    public static final String CUSTOM_ROBOT_TOKEN = "<your dingtalk robot token>";

    public static final String USER_ID = "<you need @ group user's userId>";

    public static final String SECRET = "<your secret>";


    @SneakyThrows
    @Override
    public void sendMsg(String msg) {
        try {
            Long timestamp = System.currentTimeMillis();
            log.info("timestamp: {}", timestamp);
            String secret = SECRET;
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), StandardCharsets.UTF_8);
            log.info("sign: {}", sign);

            //sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?sign=" + sign + "&timestamp=" + timestamp);
            OapiRobotSendRequest req = new OapiRobotSendRequest();
            /**
             * 发送文本消息
             */
            //定义文本内容
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            text.setContent(msg);
            //定义 @ 对象
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtUserIds(Collections.singletonList(USER_ID));
            //设置消息类型
            req.setMsgtype("text");
            req.setText(text);
            req.setAt(at);
            OapiRobotSendResponse rsp = client.execute(req, CUSTOM_ROBOT_TOKEN);
            log.info("sendMsg response: {}", rsp.getBody());
        } catch (ApiException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("sendMsg error", e);
        }
    }
}
