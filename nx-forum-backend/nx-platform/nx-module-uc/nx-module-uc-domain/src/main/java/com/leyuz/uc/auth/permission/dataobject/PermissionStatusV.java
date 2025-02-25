package com.leyuz.uc.auth.permission.dataobject;

public enum PermissionStatusV {
    ENABLED(0),
    DISABLED(1);

    private final int code;

    PermissionStatusV(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


    public static PermissionStatusV of(Integer code) {
        if (code == null) {
            return DISABLED;
        }
        for (PermissionStatusV status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return DISABLED;
    }
} 