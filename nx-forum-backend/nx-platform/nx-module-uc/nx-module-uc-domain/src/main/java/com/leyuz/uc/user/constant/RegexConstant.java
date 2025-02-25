package com.leyuz.uc.user.constant;

public class RegexConstant {
    public static final String EMAIL_REGEX = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    public static final String PHONE_REGEX = "^$|^1(3\\d|4[5-9]|5[0-35-9]|6[2567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$";
    public static final String NUMBER_REGEX = "^\\d+";

    /**
     * 中文英文数字下划线而且3-16字符
     */
    public static final String USER_NAME_REGEX = "^[\\x{4e00}-\\x{9fa5}A-Za-z0-9_]+$";

    /**
     * 英文、数字、特殊字符6-40位
     */

    public static final String PASSWORD_REGEX = "^[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{6,40}$";
}
