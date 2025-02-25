package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.audit.AuditApplication;
import com.leyuz.bbs.system.audit.dto.AuditConfigBlackWhiteUsersVO;
import com.leyuz.bbs.system.config.AuditConfigApplication;
import com.leyuz.bbs.system.config.dto.AuditConfigBlackWhiteUsersDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigSensitiveWordsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台审核设置")
@RestController
@RequestMapping("/v1/admin/audits")
@RequiredArgsConstructor
public class AdminAuditController {
    private final AuditConfigApplication auditConfigApplication;
    private final AuditApplication auditApplication;

    @Operation(summary = "获取敏感词配置", description = "获取系统当前设置的敏感词列表")
    @GetMapping("/sensitive-words")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AuditConfigSensitiveWordsDTO> getAuditConfigSensitiveWords() {
        return SingleResponse.of(auditConfigApplication.getAuditConfigSensitiveWords());
    }

    @Operation(summary = "更新敏感词配置", description = "更新系统敏感词配置（全量更新）")
    @PutMapping("/sensitive-words")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<Void> updateAuditConfigSensitiveWords(
            @RequestBody @Valid AuditConfigSensitiveWordsDTO config) {
        auditConfigApplication.updateAuditConfigSensitiveWords(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取黑白名单配置", description = "获取用户黑白名单配置信息")
    @GetMapping("/black-white-users")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AuditConfigBlackWhiteUsersVO> getAuditConfigBlackWhiteUsersVO() {
        return SingleResponse.of(auditApplication.getAuditConfigBlackWhiteUsersVO());
    }

    @Operation(summary = "更新黑白名单配置", description = "更新用户黑白名单配置（全量更新）")
    @PutMapping("/black-white-users")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<Void> updateAuditConfigBlackWhiteUsers(
            @RequestBody @Valid AuditConfigBlackWhiteUsersDTO config) {
        auditConfigApplication.updateAuditConfigBlackWhiteUsers(config);
        return SingleResponse.buildSuccess();
    }
}
