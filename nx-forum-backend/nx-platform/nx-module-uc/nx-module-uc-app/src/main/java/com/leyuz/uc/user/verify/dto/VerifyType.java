package com.leyuz.uc.user.verify.dto;

public enum VerifyType {
    REGISTER("register"), LOGIN("login"), RESET_PASSWORD("reset_password"), CHANGE_EMAIL("change_email");

    public static VerifyType of(String value) {
        return switch (value) {
            case "register" -> REGISTER;
            case "reset_password" -> RESET_PASSWORD;
            case "change_email" -> CHANGE_EMAIL;
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
