package com.leyuz.bbs.common.dataobject;

public enum DocTypeV {
    /**
     * 类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown
     */
    TEXT, HTML, MARKDOWN, UBB;

    public static DocTypeV of(Byte value) {
        return switch (value) {
            case 0 -> TEXT;
            case 1 -> HTML;
            case 2 -> MARKDOWN;
            case 3 -> UBB;
            default -> null;
        };
    }
}
