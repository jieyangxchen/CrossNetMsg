package com.github.chen.dingtalkrobotmsg.msg;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CallBackMsg implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String msg;
}
