package com.leyuz.bbs.interaction.like.dto;

public enum LikeTargetType {

    THREAD(0), COMMENT(1), REPLY(2);

    public static LikeTargetType of(Integer value) {
        return switch (value) {
            case 0 -> THREAD;
            case 1 -> COMMENT;
            case 2 -> REPLY;
            default -> null;
        };
    }

    private final int value;

    LikeTargetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
