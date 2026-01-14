package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.stats.StatsApplication;
import com.leyuz.bbs.system.stats.dto.DailyStatsQuery;
import com.leyuz.bbs.system.stats.dto.DailyStatsVO;
import com.leyuz.bbs.system.stats.dto.StatsOverviewVO;
import com.leyuz.bbs.system.stats.dto.StatsTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统计数据控制器
 *
 * @author Walker
 * @since 2025-01-11
 */
@Tag(name = "统计数据", description = "论坛统计数据查询接口")
@RestController
@RequestMapping("/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsApplication statsApplication;

    /**
     * 获取每日统计数据
     *
     * @param startDate    开始日期（可选，默认30天前）
     * @param endDate      结束日期（可选，默认今天）
     * @param terminalType 终端类型（可选，默认ALL）
     * @param platform     平台（可选，默认ALL）
     * @return 统计数据列表
     */
    @Operation(summary = "获取每日统计数据", description = "根据日期范围和筛选条件获取每日统计数据")
    @GetMapping("/daily")
    @PermitAll
    public SingleResponse<List<DailyStatsVO>> getDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "ALL") String terminalType,
            @RequestParam(required = false, defaultValue = "ALL") String platform) {

        DailyStatsQuery query = new DailyStatsQuery();
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setTerminalType(terminalType);
        query.setPlatform(platform);

        List<DailyStatsVO> stats = statsApplication.queryDailyStats(query);
        return SingleResponse.of(stats);
    }

    /**
     * 获取统计概览
     *
     * @return 统计概览数据（总帖数、总评论数、总用户数、今日IP数等）
     */
    @Operation(summary = "获取统计概览", description = "获取论坛总体统计数据概览")
    @GetMapping("/overview")
    @PermitAll
    public SingleResponse<StatsOverviewVO> getOverview() {
        StatsOverviewVO overview = statsApplication.getOverview();
        return SingleResponse.of(overview);
    }

    /**
     * 获取统计趋势数据
     *
     * @param days         天数（默认30天）
     * @param terminalType 终端类型（默认ALL）
     * @param platform     平台（默认ALL）
     * @return 趋势数据
     */
    @Operation(summary = "获取统计趋势", description = "获取最近N天的统计数据趋势，用于图表展示")
    @GetMapping("/trend")
    @PermitAll
    public SingleResponse<StatsTrendVO> getTrend(
            @RequestParam(required = false, defaultValue = "30") int days,
            @RequestParam(required = false, defaultValue = "ALL") String terminalType,
            @RequestParam(required = false, defaultValue = "ALL") String platform) {

        StatsTrendVO trend = statsApplication.getTrend(days, terminalType, platform);
        return SingleResponse.of(trend);
    }

    /**
     * 按终端类型分组统计
     *
     * @param startDate 开始日期（可选，默认30天前）
     * @param endDate   结束日期（可选，默认今天）
     * @return 按终端类型分组的统计数据（PC/MOBILE/APP）
     */
    @Operation(summary = "按终端类型统计", description = "按终端类型（PC/MOBILE/APP）分组统计访问数据")
    @GetMapping("/by-terminal")
    @PermitAll
    public SingleResponse<Map<String, DailyStatsVO>> getStatsByTerminal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Set default values if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, DailyStatsVO> stats = statsApplication.getStatsByTerminal(startDate, endDate);
        return SingleResponse.of(stats);
    }

    /**
     * 按平台分组统计
     *
     * @param startDate 开始日期（可选，默认30天前）
     * @param endDate   结束日期（可选，默认今天）
     * @return 按平台分组的统计数据（Windows/Android/iOS/macOS等）
     */
    @Operation(summary = "按平台统计", description = "按平台（Windows/Android/iOS/macOS等）分组统计访问数据")
    @GetMapping("/by-platform")
    @PermitAll
    public SingleResponse<Map<String, DailyStatsVO>> getStatsByPlatform(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Set default values if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, DailyStatsVO> stats = statsApplication.getStatsByPlatform(startDate, endDate);
        return SingleResponse.of(stats);
    }
}
