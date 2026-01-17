package com.leyuz.bbs.system.stats;

import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.system.access.AccessLogGateway;
import com.leyuz.bbs.system.stats.dto.DailyStatsQuery;
import com.leyuz.bbs.system.stats.dto.DailyStatsVO;
import com.leyuz.bbs.system.stats.dto.StatsOverviewVO;
import com.leyuz.bbs.system.stats.dto.StatsTrendVO;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserStatsOverviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计聚合应用服务
 *
 * @author Walker
 * @since 2025-01-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsApplication {

    private final DailyStatsGateway dailyStatsGateway;
    private final AccessLogGateway accessLogGateway;
    private final UserApplication userApplication;
    private final ThreadGateway threadGateway;
    private final CommentGateway commentGateway;

    /**
     * 每日凌晨聚合前一天的统计数据
     * cron: "0 0 2 * * ?" - 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async("statsAggregationExecutor")
    public void aggregateDailyStats() {
        log.info("开始聚合每日统计数据");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startTime = yesterday.atStartOfDay();
            LocalDateTime endTime = yesterday.atTime(23, 59, 59);

            aggregateDailyData(yesterday, startTime, endTime);
            log.info("每日统计数据聚合完成：date={}", yesterday);

        } catch (Exception e) {
            log.error("聚合每日统计数据失败", e);
        }
    }

    /**
     * 清理3个月前的访问日志和统计数据
     * cron: "0 0 2 * * ?" - 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async("statsAggregationExecutor")
    public void cleanupOldAccessLogs() {
        log.info("开始清理旧的访问日志和统计数据");
        try {
            // 清理365天前的统计数据
            int deletedDailyStats = dailyStatsGateway.deleteOldStats(365);
            int deletedAccessLogs = accessLogGateway.deleteOldLogs(90);

            log.info("清理完成：dailyStats={}, accessLogs={}",
                    deletedDailyStats, deletedAccessLogs);

        } catch (Exception e) {
            log.error("清理旧数据失败", e);
        }
    }

    // ==================== 查询方法 ====================

    /**
     * 查询每日统计数据
     *
     * @param query 查询参数
     * @return 统计数据列表
     */
    public List<DailyStatsVO> queryDailyStats(DailyStatsQuery query) {
        LocalDate startDate = query.getStartDate() != null ? query.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = query.getEndDate() != null ? query.getEndDate() : LocalDate.now();
        String terminalType = query.getTerminalType();
        String platform = query.getPlatform();

        List<DailyStatsPO> pos = dailyStatsGateway.queryByDateRange(startDate, endDate, terminalType, platform);
        return pos.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取统计概览
     *
     * @return 统计概览数据
     */
    public StatsOverviewVO getOverview() {
        StatsOverviewVO overview = dailyStatsGateway.getOverview();
        UserStatsOverviewVO userStatsOverview = userApplication.getUserStatsOverview();
        overview.setTotalUsers(userStatsOverview.getTotalUsers());
        overview.setTodayNewUsers(userStatsOverview.getTodayNewUsers());
        return overview;
    }

    /**
     * 获取趋势数据
     *
     * @param days         天数
     * @param terminalType 终端类型
     * @param platform     平台
     * @return 趋势数据
     */
    public StatsTrendVO getTrend(int days, String terminalType, String platform) {
        return dailyStatsGateway.getTrend(days, terminalType, platform);
    }

    /**
     * 按终端类型分组统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 终端类型统计映射
     */
    public Map<String, DailyStatsVO> getStatsByTerminal(LocalDate startDate, LocalDate endDate) {
        return dailyStatsGateway.getStatsByTerminal(startDate, endDate);
    }

    /**
     * 按平台分组统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 平台统计映射
     */
    public Map<String, DailyStatsVO> getStatsByPlatform(LocalDate startDate, LocalDate endDate) {
        return dailyStatsGateway.getStatsByPlatform(startDate, endDate);
    }

    // ==================== 私有方法 ====================

    @Transactional
    protected void aggregateDailyData(LocalDate statDate, LocalDateTime startTime,
                                      LocalDateTime endTime) {
        // 一次性聚合所有统计数据（访问 + 内容）
        String[] terminalTypes = {"ALL", "PC", "MOBILE", "APP"};
        String[] platforms = {"ALL", "Windows", "Mac", "Linux", "Android", "iPhone"};

        for (String terminalType : terminalTypes) {
            for (String platform : platforms) {
                // 一次性收集所有统计数据
                long uniqueIpCount = accessLogGateway.countUniqueIps(startTime, endTime, terminalType, platform);
                long guestIpCount = accessLogGateway.countGuestIps(startTime, endTime, terminalType, platform);
                long userIpCount = accessLogGateway.countUserIps(startTime, endTime, terminalType, platform);
                long threadCount = threadGateway.countThreadsCreatedBetween(startTime, endTime, terminalType, platform);
                long commentCount = commentGateway.countCommentsCreatedBetween(startTime, endTime, terminalType, platform);

                // 如果所有统计数据都为 0，跳过写入
                if (uniqueIpCount == 0 && guestIpCount == 0 && userIpCount == 0
                        && threadCount == 0 && commentCount == 0) {
                    continue;
                }

                // 一次性保存所有数据
                DailyStatsPO stats = DailyStatsPO.builder()
                        .statDate(statDate)
                        .uniqueIpCount((int) uniqueIpCount)
                        .guestIpCount((int) guestIpCount)
                        .userIpCount((int) userIpCount)
                        .threadCount((int) threadCount)
                        .commentCount((int) commentCount)
                        .terminalType(terminalType)
                        .platform(platform)
                        .build();

                dailyStatsGateway.saveOrUpdate(stats);
            }
        }
    }

    /**
     * 将PO转换为VO
     */
    private DailyStatsVO convertToVO(DailyStatsPO po) {
        return DailyStatsVO.builder()
                .statDate(po.getStatDate())
                .uniqueIpCount(po.getUniqueIpCount())
                .guestIpCount(po.getGuestIpCount())
                .userIpCount(po.getUserIpCount())
                .threadCount(po.getThreadCount())
                .commentCount(po.getCommentCount())
                .terminalType(po.getTerminalType())
                .platform(po.getPlatform())
                .build();
    }
}

