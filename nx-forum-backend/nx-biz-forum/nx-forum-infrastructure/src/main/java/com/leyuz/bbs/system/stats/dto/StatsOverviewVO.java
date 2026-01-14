package com.leyuz.bbs.system.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计概览数据视图对象
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewVO {

    /**
     * 总帖数
     */
    private Long totalThreads;

    /**
     * 总评论数
     */
    private Long totalComments;

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日独立IP数
     */
    private Integer todayUniqueIps;

    /**
     * 今日游客独立IP数
     */
    private Integer todayGuestIps;

    /**
     * 今日登录用户独立IP数
     */
    private Integer todayUserIps;

    /**
     * 今日贴数
     */
    private Long todayThreads;

    /**
     * 今日评论数
     */
    private Long todayComments;

    /**
     * 今日注册用户数
     */
    private Long todayNewUsers;
}
