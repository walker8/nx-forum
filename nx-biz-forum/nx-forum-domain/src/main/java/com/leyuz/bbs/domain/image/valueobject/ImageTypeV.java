package com.leyuz.bbs.domain.image.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageTypeV {
    THREAD(0, "帖子图片"),
    AVATAR(1, "用户头像");

    private final int value;
    private final String desc;

    public static ImageTypeV of(Integer value) {
        if (value == null) {
            return null;
        }
        for (ImageTypeV type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
} 