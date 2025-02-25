package com.leyuz.uc.auth.permission.dataobject;

import lombok.Getter;

/**
 * 权限类型枚举
 */
@Getter
public enum PermissionTypeV {
    DIRECTORY(1, "目录"),
    MENU(2, "菜单"),
    BUTTON(3, "按钮");

    private final int code;
    private final String desc;

    PermissionTypeV(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PermissionTypeV of(Integer code) {
        if (code == null) {
            return null;
        }
        for (PermissionTypeV value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
} 