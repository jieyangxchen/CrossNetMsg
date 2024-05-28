package com.github.chen.dingtalkrobotmsg.controller;

import com.github.chen.dingtalkrobotmsg.service.IntranetService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内网接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/robot/intranet/")
@Profile("intranet")
public class IntranetController {

    private final IntranetService intranetService;

    @PostMapping("/signal")
    public void receiveSignal(@RequestParam String jarName, @RequestParam String environment) {
        if (StringUtils.isBlank(jarName) || StringUtils.isBlank(environment)) {
            return;
        }
        intranetService.receiveSignal(jarName, environment);
    }

}
