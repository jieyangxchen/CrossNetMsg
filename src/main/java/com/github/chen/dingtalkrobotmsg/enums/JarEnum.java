package com.github.chen.dingtalkrobotmsg.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JarEnum {


    ;

    /**
     * 待匹配的名称
     */
    private final String jarName;
    /**
     * 环境参数
     */
    private final String env;
    /**
     * 健康检查地址
     */
    private final String healthUrl;
    /**
     * 消息内容
     */
    private final String msg;

    /**
     * 根据 jarName 和 env 获取对应的 healthUrl
     *
     * @param jarName jar 包名称
     * @param env     环境
     * @return 对应的 healthUrl，如果找不到匹配的返回 null
     */
    public static JarEnum getHealthUrl(String jarName, String env) {
        for (JarEnum jar : values()) {
            if (jar.jarName.equals(jarName) && jar.env.equals(env)) {
                return jar;
            }
        }
        return null;
    }
}
