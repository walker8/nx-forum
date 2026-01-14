package com.leyuz.bbs.system.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 每日统计数据视图对象
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsVO {

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 独立IP访问数
     */
    private Integer uniqueIpCount;

    /**
     * 游客独立IP数
     */
    private Integer guestIpCount;

    /**
     * 登录用户独立IP数
     */
    private Integer userIpCount;

    /**
     * 发帖数量
     */
    private Integer threadCount;

    /**
     * 回帖数量
     */
    private Integer commentCount;

    /**
     * 终端类型（ALL/PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台名称（ALL表示全部）
     */
    private String platform;
}
