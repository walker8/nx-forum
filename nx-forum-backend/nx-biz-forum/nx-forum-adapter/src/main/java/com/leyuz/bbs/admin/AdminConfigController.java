package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.config.ForumConfigApplication;
import com.leyuz.bbs.system.config.dto.AnalyticsConfigDTO;
import com.leyuz.bbs.system.config.dto.WebsiteBaseInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台网站配置项")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/configs")
public class AdminConfigController {
    private final ForumConfigApplication forumConfigApplication;

    @Operation(summary = "获取网站基础信息")
    @GetMapping("/website")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:basic')")
    public SingleResponse<WebsiteBaseInfoVO> getWebsiteBaseInfoByAdmin() {
        return SingleResponse.of(forumConfigApplication.getWebsiteBaseInfoByAdmin());
    }

    @Operation(summary = "更新网站基础信息")
    @PutMapping("/website")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:basic')")
    public SingleResponse<String> updateWebsiteBaseInfoByAdmin(@RequestBody WebsiteBaseInfoVO websiteBaseInfoVO) {
        forumConfigApplication.updateWebsiteBaseInfoByAdmin(websiteBaseInfoVO);
        return SingleResponse.of("更新成功");
    }

    @Operation(summary = "获取统计配置")
    @GetMapping("/analytics")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:basic')")
    public SingleResponse<AnalyticsConfigDTO> getAnalyticsConfigByAdmin() {
        return SingleResponse.of(forumConfigApplication.getAnalyticsConfigByAdmin());
    }

    @Operation(summary = "更新统计配置")
    @PutMapping("/analytics")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:basic')")
    public SingleResponse<String> updateAnalyticsConfigByAdmin(@RequestBody AnalyticsConfigDTO config) {
        forumConfigApplication.updateAnalyticsConfig(config);
        return SingleResponse.of("更新成功");
    }
}
