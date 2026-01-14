package com.leyuz.bbs.system.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计趋势数据视图对象
 *
 * @author Walker
 * @since 2025-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsTrendVO {

    /**
     * 日期列表
     */
    private List<LocalDate> dates;

    /**
     * 独立IP数列表
     */
    private List<Integer> uniqueIps;

    /**
     * 游客独立IP数列表
     */
    private List<Integer> guestIps;

    /**
     * 登录用户独立IP数列表
     */
    private List<Integer> userIps;

    /**
     * 发帖数列表
     */
    private List<Integer> threadCounts;

    /**
     * 回帖数列表
     */
    private List<Integer> commentCounts;
}
