package com.leyuz.bbs.system.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理后台首页概览数据视图对象
 *
 * @author Walker
 * @since 2026-03-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO {

    // ==================== 核心统计 ====================

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日新注册用户数
     */
    private Long todayNewUsers;

    /**
     * 今日活跃用户数
     */
    private Long todayActiveUsers;

    /**
     * 总主题数
     */
    private Long totalThreads;

    /**
     * 今日新主题数
     */
    private Long todayThreads;

    /**
     * 总评论数
     */
    private Long totalComments;

    /**
     * 今日新评论数
     */
    private Long todayComments;

    /**
     * 今日独立IP数
     */
    private Integer todayUniqueIps;

    // ==================== 待处理事项 ====================

    /**
     * 待审核主题数
     */
    private Long threadAuditCount;

    /**
     * 待审核评论数
     */
    private Long commentAuditCount;

    /**
     * 待审核回复数
     */
    private Long replyAuditCount;

    /**
     * 待处理举报数
     */
    private Long pendingReportCount;

    /**
     * 待处理总数
     */
    private Long totalAuditCount;

    // ==================== 趋势数据（近7天） ====================

    /**
     * 趋势日期列表
     */
    private List<String> trendDates;

    /**
     * 趋势主题数列表
     */
    private List<Integer> trendThreads;

    /**
     * 趋势评论数列表
     */
    private List<Integer> trendComments;

    /**
     * 趋势新注册用户数列表
     */
    private List<Integer> trendNewUsers;

    // ==================== 系统信息 ====================

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * Spring Boot 版本
     */
    private String springBootVersion;

    /**
     * Java 版本
     */
    private String javaVersion;

    /**
     * 操作系统名称
     */
    private String osName;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 操作系统架构
     */
    private String osArch;

    /**
     * JVM 总内存（字节）
     */
    private Long jvmTotalMemory;

    /**
     * JVM 已用内存（字节）
     */
    private Long jvmUsedMemory;

    /**
     * JVM 最大内存（字节）
     */
    private Long jvmMaxMemory;

    /**
     * JVM 运行时间（格式化字符串）
     */
    private String jvmUptime;

    /**
     * 磁盘总空间（字节）
     */
    private Long diskTotal;

    /**
     * 磁盘已用空间（字节）
     */
    private Long diskUsed;

    /**
     * 磁盘可用空间（字节）
     */
    private Long diskFree;

    /**
     * 数据库类型
     */
    private String databaseType;

    /**
     * 数据库版本
     */
    private String databaseVersion;

    /**
     * 缓存是否启用（Redis）
     */
    private boolean cacheEnabled;

    // ==================== 项目信息 ====================

    /**
     * GitHub 仓库地址
     */
    private String githubUrl;

    /**
     * 开源协议
     */
    private String license;

    /**
     * 项目名称
     */
    private String projectName;
}
