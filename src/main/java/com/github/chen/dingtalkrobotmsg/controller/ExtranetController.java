package com.github.chen.dingtalkrobotmsg.controller;

import com.github.chen.dingtalkrobotmsg.msg.CallBackMsg;
import com.github.chen.dingtalkrobotmsg.service.SendRobotMsgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 外网接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/robot/extranet/")
@Profile("extranet")
public class ExtranetController {

    private final SendRobotMsgService sendRobotMsgService;

    @PostMapping("/callback")
    public void callBack(@RequestBody CallBackMsg msg) {
        log.info("外网回调, msg={}", msg);
        sendRobotMsgService.sendMsg(msg.getMsg());
    }

}
