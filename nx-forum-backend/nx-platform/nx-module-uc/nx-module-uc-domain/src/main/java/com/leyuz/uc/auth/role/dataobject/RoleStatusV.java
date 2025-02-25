package com.leyuz.uc.auth.role.dataobject;

import lombok.Getter;

@Getter
public enum RoleStatusV {
    NORMAL(0, "正常"),
    DISABLED(1, "停用");

    private final Integer value;
    private final String desc;

    RoleStatusV(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static RoleStatusV of(Integer value) {
        for (RoleStatusV status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return NORMAL;
    }
} 