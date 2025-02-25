package com.leyuz.uc.user.dataobject;

/**
 * 账号类型
 */
public enum AccountTypeV {
    PHONE("phone"), EMAIL("email"), USERNAME("username");

    public static AccountTypeV of(String value) {
        return switch (value) {
            case "mail" -> EMAIL;
            case "phone" -> PHONE;
            default -> USERNAME;
        };
    }

    private final String value;

    AccountTypeV(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
