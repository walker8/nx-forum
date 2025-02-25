package com.leyuz.bbs.interaction.report.dto.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 举报处理状态枚举
 */
@Getter
@AllArgsConstructor
public enum ReportHandleStatusEnum {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    /**
     * 已处理-违规
     */
    APPROVED(1, "已处理-违规"),
    /**
     * 已处理-驳回
     */
    REJECTED(2, "已处理-驳回");

    private final Integer value;
    private final String description;

    public static ReportHandleStatusEnum of(Integer handleStatus) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(handleStatus))
                .findFirst()
                .orElse(null);
    }
}

