package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.interaction.report.ReportApplication;
import com.leyuz.bbs.interaction.report.dto.command.ReportCreateCmd;
import com.leyuz.ratelimit.annotation.LimitType;
import com.leyuz.ratelimit.annotation.RateLimitRule;
import com.leyuz.ratelimit.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Tag(name = "举报管理", description = "用户举报相关接口")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplication reportApplication;

    @Operation(summary = "创建举报")
    @PostMapping("/create")
    @RateLimiter(rules = {
            @RateLimitRule(key = "report-1", time = 5, count = 1, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID),
            @RateLimitRule(key = "report-2", time = 24, count = 10, timeUnit = TimeUnit.HOURS, limitType = LimitType.USER_ID)
    })
    public SingleResponse<Void> createReport(@Valid @RequestBody ReportCreateCmd cmd) {
        reportApplication.createReport(cmd);
        return SingleResponse.buildSuccess();
    }
}
