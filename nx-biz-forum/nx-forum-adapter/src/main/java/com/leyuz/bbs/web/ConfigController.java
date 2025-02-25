package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.config.ForumConfigApplication;
import com.leyuz.bbs.config.dto.WebsiteBaseInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "网站配置项")
@RestController
@RequestMapping("/v1/configs")
public class ConfigController {
    @Autowired
    ForumConfigApplication forumConfigApplication;


    @Operation(summary = "获取网站基础信息")
    @GetMapping("/website")
    @PermitAll
    public SingleResponse<WebsiteBaseInfoVO> getWebsiteBaseInfo() {
        return SingleResponse.of(forumConfigApplication.getWebsiteBaseInfo());
    }
}
