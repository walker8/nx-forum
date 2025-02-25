package com.leyuz.uc.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志类型值对象
 */
@Getter
@AllArgsConstructor
public enum LogTypeV {

    LOGIN(1, "登录"),
    LOGOUT(2, "登出"),
    REGISTER(3, "注册"),
    INFO_UPDATE(4, "信息修改"),
    PASSWORD_UPDATE(5, "密码修改");

    private final Integer code;
    private final String desc;

    public static LogTypeV fromCode(Integer code) {
        for (LogTypeV typeV : LogTypeV.values()) {
            if (typeV.getCode().equals(code)) {
                return typeV;
            }
        }
        return null;
    }
} 