package com.leyuz.bbs.system.config;

import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.system.config.dto.AiModelConfigsDTO;
import com.leyuz.bbs.system.config.dto.AiModelDTO;
import com.leyuz.bbs.system.config.dto.AiModelProviderDTO;
import com.leyuz.bbs.system.config.dto.AiModelRef;
import com.leyuz.bbs.system.config.dto.ConfigConst;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.security.SecretCrypto;
import com.leyuz.module.config.app.ConfigApplication;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 共享 AI 模型库配置管理（厂商 → 模型，与业务场景解耦、可复用）
 *
 * <p>API Key 在库中加密存储（见 ConfigSecretHandler），对外管理接口仅回显脱敏值，
 * 服务端内部使用（审核调用、连通性测试）时按需解密。</p>
 *
 * @author Walker
 */
@Service
@RequiredArgsConstructor
public class AiModelConfigApplication {
    private final ConfigApplication configApplication;

    /**
     * 获取共享模型库（管理端视图，API Key 脱敏回显）
     */
    public AiModelConfigsDTO getAiModels() {
        return loadAiModels(false);
    }

    /**
     * 更新共享模型库（全量；API Key 留空或为脱敏值则保留原 Key）
     */
    public void updateAiModels(AiModelConfigsDTO config) {
        if (config == null) {
            throw new ValidationException("模型配置不能为空");
        }
        normalize(config);
        keepUnchangedApiKeys(config);
        configApplication.updateConfig(ConfigConst.AI_MODELS, JSON.toJSONString(config));
    }

    /**
     * 按模型唯一键解析出「厂商连接信息 + 模型 ID」（API Key 已解密，仅服务端内部使用）
     */
    public AiModelRef resolveModel(String modelId) {
        if (StringUtils.isBlank(modelId)) {
            return null;
        }
        for (AiModelProviderDTO provider : loadAiModels(true).getProviders()) {
            if (!provider.isEnabled()) {
                continue;
            }
            for (AiModelDTO model : provider.getModels()) {
                if (StringUtils.equals(model.getId(), modelId)) {
                    return new AiModelRef(provider, model.getModel());
                }
            }
        }
        return null;
    }

    /**
     * 按厂商 ID 获取解密后的 API Key（供连通性测试等服务端场景使用）
     */
    public String getProviderApiKey(String providerId) {
        if (StringUtils.isBlank(providerId)) {
            return null;
        }
        for (AiModelProviderDTO provider : loadAiModels(true).getProviders()) {
            if (StringUtils.equals(provider.getId(), providerId)) {
                return provider.getApiKey();
            }
        }
        return null;
    }

    /**
     * 加载模型库；withPlainKeys=true 时返回解密后的原始 Key（仅限服务端内部），
     * 否则对 Key 做脱敏（管理端回显）
     */
    private AiModelConfigsDTO loadAiModels(boolean withPlainKeys) {
        AiModelConfigsDTO config = configApplication.getConfigValueByKey(ConfigConst.AI_MODELS, AiModelConfigsDTO.class);
        if (config == null) {
            config = new AiModelConfigsDTO();
            configApplication.updateConfig(ConfigConst.AI_MODELS, JSON.toJSONString(config));
        }
        normalize(config);
        if (!withPlainKeys) {
            for (AiModelProviderDTO provider : config.getProviders()) {
                provider.setApiKey(SecretCrypto.mask(provider.getApiKey()));
            }
        }
        return config;
    }

    /**
     * 提交的 API Key 为空或为脱敏展示值时，沿用库中已保存的 Key，避免误覆盖
     */
    private void keepUnchangedApiKeys(AiModelConfigsDTO incoming) {
        AiModelConfigsDTO stored = loadAiModels(true);
        for (AiModelProviderDTO provider : incoming.getProviders()) {
            if (StringUtils.isBlank(provider.getApiKey())) {
                continue;
            }
            for (AiModelProviderDTO storedProvider : stored.getProviders()) {
                if (StringUtils.equals(storedProvider.getId(), provider.getId())) {
                    if (SecretCrypto.isMaskedForm(provider.getApiKey(), storedProvider.getApiKey())) {
                        // 未修改，沿用原 Key（落库时由 ConfigSecretHandler 重新加密，幂等）
                        provider.setApiKey(storedProvider.getApiKey());
                    }
                    break;
                }
            }
        }
    }

    private void normalize(AiModelConfigsDTO config) {
        if (config.getProviders() == null) {
            config.setProviders(new ArrayList<>());
        }
        for (AiModelProviderDTO provider : config.getProviders()) {
            if (provider.getModels() == null) {
                provider.setModels(new ArrayList<>());
            }
        }
    }
}
