package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserRegistrationTrendVO;
import com.leyuz.uc.user.dto.UserStatsOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * User Statistics Controller
 *
 * @author Walker
 * @since 2025-01-11
 */
@Tag(name = "用户统计", description = "用户中心统计数据查询接口")
@RestController
@RequestMapping("/v1/uc/admin/stats")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserApplication userApplication;

    /**
     * Get user statistics overview
     *
     * @return UserStatsOverviewVO containing total users, today's new users, and today's active users
     */
    @Operation(summary = "获取用户统计概览", description = "获取用户中心总体统计数据概览，包括总用户数、今日新增用户数、今日活跃用户数")
    @GetMapping("/overview")
    public SingleResponse<UserStatsOverviewVO> getOverview() {
        UserStatsOverviewVO overview = userApplication.getUserStatsOverview();
        return SingleResponse.of(overview);
    }

    /**
     * Get user registration trend
     *
     * @param days Number of days to look back (default: 30)
     * @return UserRegistrationTrendVO containing dates and registration counts
     */
    @Operation(summary = "获取用户注册趋势", description = "获取最近N天的用户注册趋势数据，用于图表展示")
    @GetMapping("/registration-trend")
    public SingleResponse<UserRegistrationTrendVO> getRegistrationTrend(
            @RequestParam(required = false, defaultValue = "30") int days) {

        UserRegistrationTrendVO trend = userApplication.getUserRegistrationTrend(days);
        return SingleResponse.of(trend);
    }
}
