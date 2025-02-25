package com.leyuz.uc.log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作状态值对象
 */
@Getter
@AllArgsConstructor
public enum OperationStatusV {

    SUCCESS(0, "成功"),
    FAILURE(1, "失败");

    private final Integer code;
    private final String desc;

    public static OperationStatusV fromCode(Integer code) {
        for (OperationStatusV statusV : OperationStatusV.values()) {
            if (statusV.getCode().equals(code)) {
                return statusV;
            }
        }
        return null;
    }
} 