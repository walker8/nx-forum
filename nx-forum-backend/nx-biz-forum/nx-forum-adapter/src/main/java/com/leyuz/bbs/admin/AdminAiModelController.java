package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.audit.ai.AiModerationClient;
import com.leyuz.bbs.system.audit.ai.AiModerationException;
import com.leyuz.bbs.system.config.AiModelConfigApplication;
import com.leyuz.bbs.system.config.dto.AiModelConfigsDTO;
import com.leyuz.bbs.system.config.dto.AiModelTestCmd;
import com.leyuz.common.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 共享 AI 模型管理（厂商 → 模型，与业务场景解耦、可复用）
 *
 * @author Walker
 */
@Tag(name = "AI 模型管理")
@RestController
@RequestMapping("/v1/admin/ai-models")
@RequiredArgsConstructor
public class AdminAiModelController {
    private final AiModelConfigApplication aiModelConfigApplication;
    private final AiModerationClient aiModerationClient;

    @Operation(summary = "获取共享模型库", description = "获取所有厂商及其模型")
    @GetMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<AiModelConfigsDTO> getAiModels() {
        return SingleResponse.of(aiModelConfigApplication.getAiModels());
    }

    @Operation(summary = "更新共享模型库", description = "全量更新厂商及其模型")
    @PutMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<Void> updateAiModels(@RequestBody AiModelConfigsDTO config) {
        aiModelConfigApplication.updateAiModels(config);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "测试模型连通性", description = "用最小请求验证某个模型 ID 能否正常请求")
    @PostMapping("/test")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:audit')")
    public SingleResponse<String> testModel(@RequestBody AiModelTestCmd cmd) {
        if (cmd == null || StringUtils.isBlank(cmd.getModel())) {
            throw new ValidationException("模型 ID 不能为空");
        }
        if (StringUtils.isBlank(cmd.getApiUrl())) {
            throw new ValidationException("模型地址不能为空");
        }
        // API Key 留空或为脱敏值时，由服务端按厂商 ID 解析已保存的 Key，避免明文 Key 在前后端间往返
        String apiKey = cmd.getApiKey();
        if (StringUtils.isBlank(apiKey) || apiKey.contains("****")) {
            apiKey = aiModelConfigApplication.getProviderApiKey(cmd.getProviderId());
        }
        try {
            String resp = aiModerationClient.checkConnectivity(cmd.getApiUrl(), apiKey, cmd.getTimeoutSeconds(), cmd.getModel());
            return SingleResponse.of("连接成功：" + StringUtils.abbreviate(resp, 120));
        } catch (AiModerationException e) {
            throw new ValidationException("模型测试失败：" + e.getMessage());
        }
    }
}
