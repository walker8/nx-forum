package com.leyuz.uc.user.dto;

public enum VerifyType {
    REGISTER("register"), LOGIN("login"), RESET_PASSWORD("reset-password");

    public static VerifyType of(String value) {
        return switch (value) {
            case "register" -> REGISTER;
            case "reset-password" -> RESET_PASSWORD;
            default -> LOGIN;
        };
    }

    private final String value;

    VerifyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
