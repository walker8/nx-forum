package com.leyuz.uc.user.dataobject;

public enum UserStatusV {
    /**
     * 帐号状态（0正常 1停用 2已注销）
     */
    NORMAL(0), DISABLED(1), CANCELLED(2);

    public static UserStatusV of(Byte value) {
        return switch (value) {
            case 0 -> NORMAL;
            case 1 -> DISABLED;
            case 2 -> CANCELLED;
            default -> null;
        };
    }

    private final int value;

    UserStatusV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
