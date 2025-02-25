package com.leyuz.bbs.system.notification.dto.constant;

/**
 * 通知状态值对象
 */
public enum NotificationStatusV {
    /**
     * 0未读 1已读
     */
    UNREAD(0), READ(1);

    public static NotificationStatusV of(Integer value) {
        return switch (value) {
            case 0 -> UNREAD;
            case 1 -> READ;
            default -> null;
        };
    }

    private final int value;

    NotificationStatusV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

