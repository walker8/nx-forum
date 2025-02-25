package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.config.ForumConfigApplication;
import com.leyuz.bbs.system.config.dto.WebsiteBaseInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "网站配置项")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/configs")
public class ConfigController {
    private final ForumConfigApplication forumConfigApplication;


    @Operation(summary = "获取网站基础信息")
    @GetMapping("/website")
    @PermitAll
    public SingleResponse<WebsiteBaseInfoVO> getWebsiteBaseInfo() {
        return SingleResponse.of(forumConfigApplication.getWebsiteBaseInfo());
    }

    @Operation(summary = "获取统计代码")
    @GetMapping("/analytics")
    @PermitAll
    public SingleResponse<String> getAnalyticsCode() {
        return SingleResponse.of(forumConfigApplication.getAnalyticsCode());
    }
}
