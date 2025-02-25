package com.leyuz.bbs.system.notification.dto.constant;

/**
 * 通知类型值对象
 */
public enum NotificationTypeV {
    /**
     * 1回复消息 2系统消息 3@提醒
     */
    REPLY(1), SYSTEM(2), AT(3);

    public static NotificationTypeV of(Integer value) {
        return switch (value) {
            case 1 -> REPLY;
            case 2 -> SYSTEM;
            case 3 -> AT;
            default -> null;
        };
    }

    public static NotificationTypeV of(String value) {
        return switch (value) {
            case "system" -> SYSTEM;
            case "at" -> AT;
            default -> REPLY;
        };
    }

    private final int value;

    NotificationTypeV(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

