package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.audit.AuditApplication;
import com.leyuz.bbs.system.audit.ai.AiModerationClient;
import com.leyuz.bbs.system.audit.ai.AiModerationException;
import com.leyuz.bbs.system.audit.ai.AiModerationResult;
import com.leyuz.bbs.system.audit.dto.AiAuditTestCmd;
import com.leyuz.bbs.system.audit.dto.AuditConfigBlackWhiteUsersVO;
import com.leyuz.bbs.system.audit.dto.RuleTestCmd;
import com.leyuz.bbs.system.audit.rule.RuleEngine;
import com.leyuz.bbs.system.config.AiModelConfigApplication;
import com.leyuz.bbs.system.config.AuditConfigApplication;
import com.leyuz.bbs.system.config.dto.AiModelRef;
import com.leyuz.bbs.system.config.dto.AuditConfigAiDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigBlackWhiteUsersDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigRulesDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigSensitiveWordsDTO;
import com.leyuz.bbs.system.config.dto.AuditRuleDTO;
import com.leyuz.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台审核设置")
@RestController
@RequestMapping("/v1/admin/audits")
@RequiredArgsConstructor
public class AdminAuditController {
    private final AuditConfigApplication auditConfigApplication;
    private final AuditApplication auditApplication;
    private final RuleEngine ruleEngine;
    private final AiModelConfigApplication aiModelConfigApplication;
    private final AiModerationClient aiModerationClient;

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

    @Operation(summary = "获取审核规则配置", description = "获取审核规则引擎配置")
    @GetMapping("/rules")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AuditConfigRulesDTO> getAuditConfigRules() {
        return SingleResponse.of(auditConfigApplication.getAuditConfigRules());
    }

    @Operation(summary = "更新审核规则配置", description = "更新审核规则引擎配置（全量更新）")
    @PutMapping("/rules")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<Void> updateAuditConfigRules(@RequestBody AuditConfigRulesDTO config) {
        if (config != null && config.getRules() != null) {
            for (AuditRuleDTO rule : config.getRules()) {
                if (rule.isEnabled()) {
                    String error = ruleEngine.validate(rule.getExpression());
                    if (StringUtils.isNotBlank(error)) {
                        throw new ValidationException("规则【" + rule.getName() + "】校验失败：" + error);
                    }
                }
            }
        }
        auditConfigApplication.updateAuditConfigRules(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "校验规则表达式", description = "校验 QLExpress 表达式语法，返回错误信息（空串表示合法）")
    @PostMapping("/rules/test")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<String> testRule(@RequestBody RuleTestCmd cmd) {
        String error = ruleEngine.validate(cmd == null ? null : cmd.getExpression());
        return SingleResponse.of(StringUtils.defaultString(error));
    }

    @Operation(summary = "获取 AI 审核配置", description = "获取 AI 审核配置")
    @GetMapping("/ai-config")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AuditConfigAiDTO> getAuditConfigAi() {
        return SingleResponse.of(auditConfigApplication.getAuditConfigAi());
    }

    @Operation(summary = "更新 AI 审核配置", description = "更新 AI 审核场景配置（全量更新）")
    @PutMapping("/ai-config")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<Void> updateAuditConfigAi(@RequestBody AuditConfigAiDTO config) {
        auditConfigApplication.updateAuditConfigAi(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "测试 AI 审核", description = "用选中模型与审核提示词对测试内容做一次审核判定")
    @PostMapping("/ai-config/test")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AiModerationResult> testAiAudit(@RequestBody AiAuditTestCmd cmd) {
        if (cmd == null || StringUtils.isBlank(cmd.getModelId())) {
            throw new ValidationException("请先选择生效模型");
        }
        AiModelRef ref = aiModelConfigApplication.resolveModel(cmd.getModelId());
        if (ref == null) {
            throw new ValidationException("模型不存在或未启用");
        }
        String content = StringUtils.defaultIfBlank(cmd.getContent(), "你好，这是一条用于测试审核的内容");
        try {
            return SingleResponse.of(aiModerationClient.moderate(ref.provider(), ref.model(), cmd.getPrompt(), content));
        } catch (AiModerationException e) {
            throw new ValidationException("AI 审核测试失败：" + e.getMessage());
        }
    }
}
