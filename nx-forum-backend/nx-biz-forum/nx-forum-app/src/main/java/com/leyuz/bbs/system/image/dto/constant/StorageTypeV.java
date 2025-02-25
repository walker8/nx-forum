package com.leyuz.bbs.system.image.dto.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储类型值对象
 */
@Getter
@AllArgsConstructor
public enum StorageTypeV {
    /**
     * 本地存储
     */
    LOCAL(0, "本地存储"),
    /**
     * 阿里云OSS
     */
    OSS(1, "阿里云OSS"),
    /**
     * 腾讯云COS
     */
    COS(2, "腾讯云COS");

    private final int value;
    private final String desc;

    public static StorageTypeV of(Integer value) {
        if (value == null) {
            return null;
        }
        for (StorageTypeV type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}

