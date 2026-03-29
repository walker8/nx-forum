package com.leyuz.bbs.system.dashboard;

import com.leyuz.bbs.content.comment.CommentApplication;
import com.leyuz.bbs.content.thread.ThreadApplication;
import com.leyuz.bbs.interaction.report.ReportApplication;
import com.leyuz.bbs.system.dashboard.dto.DashboardOverviewVO;
import com.leyuz.bbs.system.stats.StatsApplication;
import com.leyuz.bbs.system.stats.dto.StatsOverviewVO;
import com.leyuz.bbs.system.stats.dto.StatsTrendVO;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserRegistrationTrendVO;
import com.leyuz.uc.user.dto.UserStatsOverviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 管理后台首页 Dashboard 聚合服务
 *
 * @author Walker
 * @since 2026-03-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardApplication {

    private final StatsApplication statsApplication;
    private final ThreadApplication threadApplication;
    private final CommentApplication commentApplication;
    private final ReportApplication reportApplication;
    private final UserApplication userApplication;
    private final DataSource dataSource;

    @Value("${nx.cache.redis.enabled:false}")
    private boolean cacheEnabled;

    private static final String APP_VERSION = "Beta";
    private static final String GITHUB_URL = "https://github.com/walker8/nx-forum";
    private static final String LICENSE = "MIT";
    private static final String PROJECT_NAME = "NX Forum";

    /**
     * 获取 Dashboard 首页概览数据
     */
    public DashboardOverviewVO getOverview() {
        DashboardOverviewVO.DashboardOverviewVOBuilder builder = DashboardOverviewVO.builder();

        // 1. 核心统计
        fillStats(builder);

        // 2. 待处理事项
        fillAuditCounts(builder);

        // 3. 趋势数据
        fillTrend(builder);

        // 4. 系统信息
        fillSystemInfo(builder);

        // 5. 项目信息
        fillProjectInfo(builder);

        return builder.build();
    }

    private void fillStats(DashboardOverviewVO.DashboardOverviewVOBuilder builder) {
        try {
            // statsApplication.getOverview() already includes totalUsers and todayNewUsers from userApplication
            StatsOverviewVO stats = statsApplication.getOverview();
            // todayActiveUsers is only available in UserStatsOverviewVO, fetch separately
            UserStatsOverviewVO userStats = userApplication.getUserStatsOverview();

            builder.totalUsers(stats.getTotalUsers())
                    .todayNewUsers(stats.getTodayNewUsers())
                    .todayActiveUsers(userStats.getTodayActiveUsers())
                    .totalThreads(stats.getTotalThreads())
                    .todayThreads(stats.getTodayThreads())
                    .totalComments(stats.getTotalComments())
                    .todayComments(stats.getTodayComments())
                    .todayUniqueIps(stats.getTodayUniqueIps());
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            builder.totalUsers(0L).todayNewUsers(0L).todayActiveUsers(0L)
                    .totalThreads(0L).todayThreads(0L)
                    .totalComments(0L).todayComments(0L)
                    .todayUniqueIps(0);
        }
    }

    private void fillAuditCounts(DashboardOverviewVO.DashboardOverviewVOBuilder builder) {
        try {
            long threadAuditCount = threadApplication.getAuditingCount(null);
            long commentAuditCount = commentApplication.getCommentAuditingCount(null);
            long replyAuditCount = commentApplication.getReplyAuditingCount(null);
            long pendingReportCount = reportApplication.getPendingReportCount(null);
            long totalAuditCount = threadAuditCount + commentAuditCount + replyAuditCount + pendingReportCount;

            builder.threadAuditCount(threadAuditCount)
                    .commentAuditCount(commentAuditCount)
                    .replyAuditCount(replyAuditCount)
                    .pendingReportCount(pendingReportCount)
                    .totalAuditCount(totalAuditCount);
        } catch (Exception e) {
            log.error("获取审核数据失败", e);
            builder.threadAuditCount(0L).commentAuditCount(0L).replyAuditCount(0L)
                    .pendingReportCount(0L).totalAuditCount(0L);
        }
    }

    private void fillTrend(DashboardOverviewVO.DashboardOverviewVOBuilder builder) {
        try {
            StatsTrendVO trend = statsApplication.getTrend(7, "ALL", "ALL");
            UserRegistrationTrendVO userTrend = userApplication.getUserRegistrationTrend(7);

            List<String> dates = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
            if (trend.getDates() != null) {
                for (LocalDate date : trend.getDates()) {
                    dates.add(date.format(formatter));
                }
            }

            builder.trendDates(dates)
                    .trendThreads(trend.getThreadCounts() != null ? trend.getThreadCounts() : List.of())
                    .trendComments(trend.getCommentCounts() != null ? trend.getCommentCounts() : List.of())
                    .trendNewUsers(userTrend.getNewUsersCounts() != null ? userTrend.getNewUsersCounts() : List.of());
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            builder.trendDates(List.of()).trendThreads(List.of())
                    .trendComments(List.of()).trendNewUsers(List.of());
        }
    }

    private void fillSystemInfo(DashboardOverviewVO.DashboardOverviewVOBuilder builder) {
        // JVM 信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        // JVM 运行时间
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptime = formatUptime(uptimeMillis);

        // 磁盘空间
        File root = new File("/");
        long diskTotal = root.getTotalSpace();
        long diskFree = root.getUsableSpace();
        long diskUsed = diskTotal - diskFree;

        builder.appVersion(APP_VERSION)
                .javaVersion(System.getProperty("java.version"))
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .osArch(System.getProperty("os.arch"))
                .jvmTotalMemory(totalMemory)
                .jvmUsedMemory(usedMemory)
                .jvmMaxMemory(maxMemory)
                .jvmUptime(uptime)
                .diskTotal(diskTotal)
                .diskUsed(diskUsed)
                .diskFree(diskFree)
                .cacheEnabled(cacheEnabled);

        // Spring Boot 版本
        try {
            String springBootVersion = org.springframework.boot.SpringBootVersion.getVersion();
            builder.springBootVersion(springBootVersion);
        } catch (Exception e) {
            builder.springBootVersion("Unknown");
        }

        // 数据库信息
        fillDatabaseInfo(builder);
    }

    private void fillDatabaseInfo(DashboardOverviewVO.DashboardOverviewVOBuilder builder) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            builder.databaseType(metaData.getDatabaseProductName())
                    .databaseVersion(metaData.getDatabaseProductVersion());
        } catch (Exception e) {
            log.warn("获取数据库信息失败", e);
            builder.databaseType("Unknown").databaseVersion("Unknown");
        }
    }

    private void fillProjectInfo(DashboardOverviewVO.DashboardOverviewVOBuilder builder) {
        builder.githubUrl(GITHUB_URL)
                .license(LICENSE)
                .projectName(PROJECT_NAME);
    }

    private String formatUptime(long uptimeMillis) {
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天");
        }
        if (hours > 0) {
            sb.append(hours).append("小时");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟");
        }
        return !sb.isEmpty() ? sb.toString() : "不到1分钟";
    }
}
