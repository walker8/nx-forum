package com.leyuz.bbs.interaction.report.dto.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 举报原因类型枚举
 */
@Getter
@AllArgsConstructor
public enum ReportTypeEnum {
    /**
     * 广告或垃圾信息
     */
    SPAM(1, "垃圾广告"),
    /**
     * 色情或不当内容
     */
    PORN(2, "色情低俗"),
    /**
     * 仇恨言论或歧视
     */
    HATE_SPEECH(3, "仇恨言论"),
    /**
     * 侵犯版权或隐私
     */
    COPYRIGHT(4, "涉嫌侵权"),
    /**
     * 扰乱社区秩序
     */
    DISORDERLY(5, "扰乱社区秩序"),
    /**
     * 违法违规
     */
    VIOLATION(6, "违法违规"),
    /**
     * 其他
     */
    OTHER(99, "其他原因");

    private final Integer value;
    private final String description;

    public static ReportTypeEnum of(Integer reportType) {
        return Arrays.stream(values())
                .filter(item -> item.value.equals(reportType))
                .findFirst()
                .orElse(null);
    }
}
