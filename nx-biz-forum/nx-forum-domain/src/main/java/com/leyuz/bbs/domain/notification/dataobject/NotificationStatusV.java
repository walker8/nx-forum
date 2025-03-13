package com.leyuz.bbs.domain.notification.dataobject;

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
