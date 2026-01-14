package com.leyuz.bbs.system.stats;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.system.stats.dto.DailyStatsVO;
import com.leyuz.bbs.system.stats.dto.StatsOverviewVO;
import com.leyuz.bbs.system.stats.dto.StatsTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 每日统计网关实现
 *
 * @author Walker
 * @since 2025-01-11
 */
@Repository
@RequiredArgsConstructor
public class DailyStatsGatewayImpl implements DailyStatsGateway {

    private final DailyStatsMapper dailyStatsMapper;
    private final ThreadGateway threadGateway;
    private final CommentGateway commentGateway;
    private final HourlyStatsGateway hourlyStatsGateway;

    @Override
    public void saveOrUpdate(DailyStatsPO stats) {
        DailyStatsPO existing = getByDateAndTerminal(
                stats.getStatDate(),
                stats.getTerminalType(),
                stats.getPlatform()
        );

        if (existing == null) {
            dailyStatsMapper.insert(stats);
        } else {
            LambdaUpdateWrapper<DailyStatsPO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(DailyStatsPO::getStatId, existing.getStatId())
                    .set(DailyStatsPO::getUniqueIpCount, stats.getUniqueIpCount())
                    .set(DailyStatsPO::getGuestIpCount, stats.getGuestIpCount())
                    .set(DailyStatsPO::getUserIpCount, stats.getUserIpCount())
                    .set(DailyStatsPO::getThreadCount, stats.getThreadCount())
                    .set(DailyStatsPO::getCommentCount, stats.getCommentCount());

            dailyStatsMapper.update(null, wrapper);
        }
    }

    @Override
    public DailyStatsPO getByDateAndTerminal(LocalDate date, String terminalType, String platform) {
        return dailyStatsMapper.selectOne(
                new LambdaQueryWrapper<DailyStatsPO>()
                        .eq(DailyStatsPO::getStatDate, date)
                        .eq(DailyStatsPO::getTerminalType, terminalType)
                        .eq(DailyStatsPO::getPlatform, platform)
        );
    }

    @Override
    public void updateById(DailyStatsPO stats) {
        dailyStatsMapper.updateById(stats);
    }

    @Override
    public int deleteOldStats(int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return dailyStatsMapper.delete(
                new LambdaQueryWrapper<DailyStatsPO>()
                        .lt(DailyStatsPO::getStatDate, cutoffDate)
        );
    }

    @Override
    public List<DailyStatsPO> queryByDateRange(LocalDate startDate, LocalDate endDate,
                                               String terminalType, String platform) {
        LambdaQueryWrapper<DailyStatsPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(DailyStatsPO::getStatDate, startDate)
                .le(DailyStatsPO::getStatDate, endDate);

        if (!"ALL".equals(terminalType)) {
            wrapper.eq(DailyStatsPO::getTerminalType, terminalType);
        }
        if (!"ALL".equals(platform)) {
            wrapper.eq(DailyStatsPO::getPlatform, platform);
        }

        wrapper.orderByAsc(DailyStatsPO::getStatDate);

        return dailyStatsMapper.selectList(wrapper);
    }

    @Override
    public StatsOverviewVO getOverview() {
        // Get today's date
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // Get today's hourly stats for IP counts
        var hourlyStats = hourlyStatsGateway.getStatsByDate(today);
        int todayUniqueIps = hourlyStats.stream().mapToInt(HourlyStatsPO::getUniqueIpCount).sum();
        int todayGuestIps = hourlyStats.stream().mapToInt(HourlyStatsPO::getGuestIpCount).sum();
        int todayUserIps = hourlyStats.stream().mapToInt(HourlyStatsPO::getUserIpCount).sum();

        // Build overview statistics
        return StatsOverviewVO.builder()
                .totalThreads(threadGateway.countTotalThreads())
                .todayThreads(threadGateway.countThreadsCreatedBetween(startOfDay, endOfDay))
                .totalComments(commentGateway.countTotalComments())
                .todayComments(commentGateway.countCommentsCreatedBetween(startOfDay, endOfDay))
                // For user statistics, we'll fetch from UC module
                // Set to 0 for now (will be overwritten in StatsApplication)
                .totalUsers(0L)
                .todayNewUsers(0L)
                // Get IP statistics from hourly stats table
                .todayUniqueIps(todayUniqueIps)
                .todayGuestIps(todayGuestIps)
                .todayUserIps(todayUserIps)
                .build();
    }

    @Override
    public StatsTrendVO getTrend(int days, String terminalType, String platform) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<DailyStatsPO> stats = queryByDateRange(startDate, endDate, terminalType, platform);

        List<LocalDate> dates = new ArrayList<>();
        List<Integer> uniqueIps = new ArrayList<>();
        List<Integer> guestIps = new ArrayList<>();
        List<Integer> userIps = new ArrayList<>();
        List<Integer> threadCounts = new ArrayList<>();
        List<Integer> commentCounts = new ArrayList<>();

        for (DailyStatsPO stat : stats) {
            dates.add(stat.getStatDate());
            uniqueIps.add(stat.getUniqueIpCount());
            guestIps.add(stat.getGuestIpCount());
            userIps.add(stat.getUserIpCount());
            threadCounts.add(stat.getThreadCount());
            commentCounts.add(stat.getCommentCount());
        }

        return StatsTrendVO.builder()
                .dates(dates)
                .uniqueIps(uniqueIps)
                .guestIps(guestIps)
                .userIps(userIps)
                .threadCounts(threadCounts)
                .commentCounts(commentCounts)
                .build();
    }

    @Override
    public Map<String, DailyStatsVO> getStatsByTerminal(LocalDate startDate, LocalDate endDate) {
        List<DailyStatsPO> allStats = queryByDateRange(startDate, endDate, "ALL", "ALL");

        // 按终端类型分组聚合
        Map<String, List<DailyStatsPO>> grouped = allStats.stream()
                .collect(Collectors.groupingBy(DailyStatsPO::getTerminalType));

        Map<String, DailyStatsVO> result = new HashMap<>();

        for (Map.Entry<String, List<DailyStatsPO>> entry : grouped.entrySet()) {
            String terminalType = entry.getKey();
            List<DailyStatsPO> stats = entry.getValue();

            int totalUniqueIps = stats.stream().mapToInt(DailyStatsPO::getUniqueIpCount).sum();
            int totalGuestIps = stats.stream().mapToInt(DailyStatsPO::getGuestIpCount).sum();
            int totalUserIps = stats.stream().mapToInt(DailyStatsPO::getUserIpCount).sum();
            int totalThreads = stats.stream().mapToInt(DailyStatsPO::getThreadCount).sum();
            int totalComments = stats.stream().mapToInt(DailyStatsPO::getCommentCount).sum();

            result.put(terminalType, DailyStatsVO.builder()
                    .statDate(null) // 聚合数据，无具体日期
                    .uniqueIpCount(totalUniqueIps)
                    .guestIpCount(totalGuestIps)
                    .userIpCount(totalUserIps)
                    .threadCount(totalThreads)
                    .commentCount(totalComments)
                    .terminalType(terminalType)
                    .platform("ALL")
                    .build());
        }

        result.remove("ALL");
        return result;
    }

    @Override
    public Map<String, DailyStatsVO> getStatsByPlatform(LocalDate startDate, LocalDate endDate) {
        List<DailyStatsPO> allStats = queryByDateRange(startDate, endDate, "ALL", "ALL");

        // 按平台分组聚合
        Map<String, List<DailyStatsPO>> grouped = allStats.stream()
                .collect(Collectors.groupingBy(DailyStatsPO::getPlatform));

        Map<String, DailyStatsVO> result = new HashMap<>();

        for (Map.Entry<String, List<DailyStatsPO>> entry : grouped.entrySet()) {
            String platform = entry.getKey();
            List<DailyStatsPO> stats = entry.getValue();

            int totalUniqueIps = stats.stream().mapToInt(DailyStatsPO::getUniqueIpCount).sum();
            int totalGuestIps = stats.stream().mapToInt(DailyStatsPO::getGuestIpCount).sum();
            int totalUserIps = stats.stream().mapToInt(DailyStatsPO::getUserIpCount).sum();
            int totalThreads = stats.stream().mapToInt(DailyStatsPO::getThreadCount).sum();
            int totalComments = stats.stream().mapToInt(DailyStatsPO::getCommentCount).sum();

            result.put(platform, DailyStatsVO.builder()
                    .statDate(null) // 聚合数据，无具体日期
                    .uniqueIpCount(totalUniqueIps)
                    .guestIpCount(totalGuestIps)
                    .userIpCount(totalUserIps)
                    .threadCount(totalThreads)
                    .commentCount(totalComments)
                    .terminalType("ALL")
                    .platform(platform)
                    .build());
        }

        result.remove("ALL");
        return result;
    }

    @Override
    public long countThreadsCreatedBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return threadGateway.countThreadsCreatedBetween(startTime, endTime);
    }

    @Override
    public long countCommentsCreatedBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return commentGateway.countCommentsCreatedBetween(startTime, endTime);
    }
}
