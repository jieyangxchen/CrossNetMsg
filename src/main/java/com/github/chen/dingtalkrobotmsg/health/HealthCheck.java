package com.github.chen.dingtalkrobotmsg.health;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheck {

    @RequestMapping("/health/ping")
    public String health() {
        return "success";
    }
}
