package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.interaction.report.ReportApplication;
import com.leyuz.bbs.interaction.report.dto.ReportDTO;
import com.leyuz.bbs.interaction.report.dto.ReportPageQuery;
import com.leyuz.bbs.interaction.report.dto.command.ReportHandleCmd;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理后台-举报管理", description = "管理员处理举报相关接口")
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportApplication reportApplication;

    @Operation(summary = "查询举报列表")
    @GetMapping("/list")
    @PreAuthorize("@forumPermissionResolver.hasPermission(#query.forumId,'admin:user:report')")
    public SingleResponse<CustomPage<ReportDTO>> queryReports(ReportPageQuery query) {
        return SingleResponse.of(reportApplication.queryReports(query));
    }

    @Operation(summary = "处理举报")
    @PostMapping("/handle")
    public SingleResponse<Void> handleReport(@Valid @RequestBody ReportHandleCmd cmd) {
        reportApplication.handleReport(cmd);
        return SingleResponse.buildSuccess();
    }
}
