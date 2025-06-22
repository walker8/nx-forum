package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.report.ReportApplication;
import com.leyuz.bbs.report.dto.command.ReportCreateCmd;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "举报管理", description = "用户举报相关接口")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportApplication reportApplication;

    @Operation(summary = "创建举报")
    @PostMapping("/create")
    public SingleResponse<Void> createReport(@Valid @RequestBody ReportCreateCmd cmd) {
        reportApplication.createReport(cmd);
        return SingleResponse.buildSuccess();
    }
}
