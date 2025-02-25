package com.leyuz.bbs.domain.image.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StorageTypeV {
    LOCAL(0, "本地存储"),
    OSS(1, "阿里云OSS"),
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