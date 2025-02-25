package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.config.ForumConfigApplication;
import com.leyuz.bbs.config.dto.WebsiteBaseInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台网站配置项")
@RestController
@RequestMapping("/v1/admin/configs")
public class AdminConfigController {
    @Autowired
    ForumConfigApplication forumConfigApplication;

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
}
