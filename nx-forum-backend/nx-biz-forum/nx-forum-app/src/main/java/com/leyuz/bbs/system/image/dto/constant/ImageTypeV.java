package com.leyuz.bbs.system.image.dto.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片类型值对象
 */
@Getter
@AllArgsConstructor
public enum ImageTypeV {
    /**
     * 帖子图片
     */
    THREAD(0, "帖子图片"),
    /**
     * 用户头像
     */
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

