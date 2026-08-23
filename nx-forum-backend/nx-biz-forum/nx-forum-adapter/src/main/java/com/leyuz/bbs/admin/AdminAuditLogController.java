package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.audit.AuditLogApplication;
import com.leyuz.bbs.system.audit.AuditLogConvert;
import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.domain.gateway.AuditLogGateway;
import com.leyuz.bbs.system.audit.dto.AuditLogVO;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台审核记录查询
 */
@Tag(name = "后台审核记录")
@RestController
@RequestMapping("/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogGateway auditLogGateway;
    private final AuditLogApplication auditLogApplication;
    private final AuditLogConvert auditLogConvert;

    @Operation(summary = "分页查询审核记录")
    @GetMapping
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<CustomPage<AuditLogVO>> pageAuditLogs(
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Long contentId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer forumId,
            @RequestParam(required = false) Byte auditStage,
            @RequestParam(required = false) Byte triggerSource,
            @RequestParam(required = false) Byte operatorType,
            @RequestParam(required = false) Byte auditStatusAfter,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "20") int pageSize) {
        CustomPage<AuditLogE> page = auditLogGateway.pageAuditLogs(
                contentType, contentId, userId, forumId,
                auditStage, triggerSource, operatorType, auditStatusAfter,
                pageNo, pageSize);
        CustomPage<AuditLogVO> voPage = CustomPage.<AuditLogVO>builder()
                .records(auditLogConvert.convertToVOs(page.getRecords()))
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .hasNext(page.isHasNext())
                .build();
        return SingleResponse.of(voPage);
    }

    @Operation(summary = "查询单条审核记录详情")
    @GetMapping("/{logId}")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AuditLogVO> getAuditLog(@PathVariable Long logId) {
        AuditLogE e = auditLogGateway.getById(logId);
        return SingleResponse.of(auditLogConvert.convertToVO(e));
    }

    @Operation(summary = "查询某内容的所有审核记录")
    @GetMapping("/by-content")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<List<AuditLogVO>> listByContent(
            @RequestParam Integer contentType,
            @RequestParam Long contentId) {
        List<AuditLogE> records = auditLogGateway.listByContent(contentType, contentId);
        return SingleResponse.of(auditLogConvert.convertToVOs(records));
    }
}
