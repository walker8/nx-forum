package com.leyuz.bbs.system.stats.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 每日统计查询参数
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
public class DailyStatsQuery {

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 终端类型（ALL/PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台（ALL/Windows/Android/iOS/macOS/Linux等）
     */
    private String platform;
}
