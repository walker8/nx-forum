package com.leyuz.bbs.system.stats;

import com.leyuz.bbs.system.stats.dto.DailyStatsVO;
import com.leyuz.bbs.system.stats.dto.StatsOverviewVO;
import com.leyuz.bbs.system.stats.dto.StatsTrendVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 每日统计网关接口
 *
 * @author Walker
 * @since 2025-01-11
 */
public interface DailyStatsGateway {

    /**
     * 保存或更新每日统计
     *
     * @param stats 统计实体
     */
    void saveOrUpdate(DailyStatsPO stats);

    /**
     * 根据日期和终端类型获取统计
     *
     * @param date         日期
     * @param terminalType 终端类型
     * @param platform     平台
     * @return 统计实体
     */
    DailyStatsPO getByDateAndTerminal(LocalDate date, String terminalType, String platform);

    /**
     * 更新统计实体
     *
     * @param stats 统计实体
     */
    void updateById(DailyStatsPO stats);

    /**
     * 清理指定天数之前的统计数据
     *
     * @param days 天数
     * @return 删除的记录数
     */
    int deleteOldStats(int days);

    /**
     * 根据日期范围查询每日统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param terminalType 终端类型
     * @param platform     平台
     * @return 统计实体列表
     */
    List<DailyStatsPO> queryByDateRange(LocalDate startDate, LocalDate endDate,
                                        String terminalType, String platform);

    /**
     * 获取统计概览
     *
     * @return 统计概览数据
     */
    StatsOverviewVO getOverview();

    /**
     * 获取趋势数据
     *
     * @param days         天数
     * @param terminalType 终端类型
     * @param platform     平台
     * @return 趋势数据
     */
    StatsTrendVO getTrend(int days, String terminalType, String platform);

    /**
     * 按终端类型分组统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 终端类型统计映射
     */
    Map<String, DailyStatsVO> getStatsByTerminal(LocalDate startDate, LocalDate endDate);

    /**
     * 按平台分组统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 平台统计映射
     */
    Map<String, DailyStatsVO> getStatsByPlatform(LocalDate startDate, LocalDate endDate);

    /**
     * 统计指定时间段内创建的主题数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 主题数量
     */
    long countThreadsCreatedBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定时间段内创建的评论数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 评论数量
     */
    long countCommentsCreatedBetween(LocalDateTime startTime, LocalDateTime endTime);
}
