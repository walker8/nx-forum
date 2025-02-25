package com.leyuz.bbs.interaction.report.dto.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 举报目标类型枚举
 */
@Getter
@AllArgsConstructor
public enum ReportTargetTypeEnum {
    /**
     * 主题
     */
    THREAD(1, "主题"),
    /**
     * 评论
     */
    COMMENT(2, "评论"),
    /**
     * 楼中楼回复
     */
    REPLY(3, "楼中楼回复");

    private final Integer value;
    private final String description;

    public static ReportTargetTypeEnum of(Integer value) {
        return Arrays.stream(ReportTargetTypeEnum.values())
                .filter(item -> item.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }
}
