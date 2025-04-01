package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.uc.log.UserLogApplication;
import com.leyuz.uc.log.dto.UserLogQuery;
import com.leyuz.uc.log.dto.UserLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户日志管理控制器
 */
@Tag(name = "用户日志管理")
@RestController
@RequestMapping("/v1/uc/admin/user-logs")
@RequiredArgsConstructor
public class AdminUserLogController {

    private final UserLogApplication userLogApplication;

    @Operation(summary = "查询用户日志")
    @GetMapping("")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse<CustomPage<UserLogVO>> queryUserLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Integer logType,
            @RequestParam(defaultValue = "") String orderBy,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        UserLogQuery query = UserLogQuery.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .logType(logType)
                .orderBy(orderBy)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();

        CustomPage<UserLogVO> logPage = userLogApplication.queryUserLogs(query);

        return SingleResponse.of(logPage);
    }
} 