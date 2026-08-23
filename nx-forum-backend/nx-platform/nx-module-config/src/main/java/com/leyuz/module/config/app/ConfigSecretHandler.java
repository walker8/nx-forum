package com.leyuz.module.config.app;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.leyuz.common.security.SecretCrypto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 敏感配置值处理器
 *
 * <p>维护「配置键 → 敏感字段」注册表，对写入的配置 JSON 中敏感字段做 AES-GCM 加密、
 * 对读取的配置 JSON 中加密字段做解密、对管理端回显做脱敏。所有操作对
 * 未加密的历史明文值幂等透传，保证平滑升级。</p>
 *
 * @author Walker
 */
@Slf4j
@Component
public class ConfigSecretHandler {

    /**
     * 加密密钥（由 nx.config.secret-key 派生，生产环境务必通过环境变量/配置注入随机值）
     */
    @Value("${nx.config.secret-key:}")
    private String secretKey;

    /**
     * 敏感字段定义：数组字段名（可为空，表示根对象直接包含该字段）+ 叶子字段名
     */
    private record SensitiveField(String arrayField, String leafField) {
    }

    /**
     * 敏感配置键注册表（键 → 敏感字段位置）
     */
    private static final Map<String, SensitiveField> SENSITIVE_FIELDS = Map.of(
            // 共享 AI 模型库：providers[].apiKey
            "ai_models", new SensitiveField("providers", "apiKey"),
            // 邮件 SMTP 配置：password
            "mail_smtp_config", new SensitiveField(null, "password"),
            "mail_config", new SensitiveField(null, "password"),
            // 短信配置：accessKeySecret
            "sms_config", new SensitiveField(null, "accessKeySecret")
    );

    /**
     * 是否敏感配置键
     */
    public boolean isSensitive(String configKey) {
        return SENSITIVE_FIELDS.containsKey(configKey);
    }

    /**
     * 全部敏感配置键
     */
    public Set<String> sensitiveKeys() {
        return Collections.unmodifiableSet(SENSITIVE_FIELDS.keySet());
    }

    /**
     * 写入前加密敏感字段（已加密/空值跳过，幂等）
     */
    public String encryptConfigValue(String configKey, String json) {
        return process(configKey, json, true);
    }

    /**
     * 读取后解密敏感字段（未加密明文透传，兼容历史数据）
     */
    public String decryptConfigValue(String configKey, String json) {
        return process(configKey, json, false);
    }

    private String process(String configKey, String json, boolean encrypt) {
        if (!isSensitive(configKey) || StringUtils.isBlank(json)) {
            return json;
        }
        JSONObject root;
        try {
            root = JSON.parseObject(json);
        } catch (Exception e) {
            log.warn("敏感配置解析失败，跳过处理，configKey={}", configKey);
            return json;
        }
        if (root == null) {
            return json;
        }
        SensitiveField field = SENSITIVE_FIELDS.get(configKey);
        if (StringUtils.isNotBlank(field.arrayField())) {
            JSONArray array = root.getJSONArray(field.arrayField());
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    if (item != null) {
                        processField(item, field.leafField(), encrypt);
                    }
                }
            }
        } else {
            processField(root, field.leafField(), encrypt);
        }
        return root.toJSONString();
    }

    private void processField(JSONObject obj, String field, boolean encrypt) {
        String value = obj.getString(field);
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (encrypt) {
            // 已加密的值跳过，避免重复加密
            if (SecretCrypto.isEncrypted(value)) {
                return;
            }
            obj.put(field, SecretCrypto.encrypt(value, secretKey));
        } else {
            // 仅解密加密格式，历史明文透传
            if (SecretCrypto.isEncrypted(value)) {
                obj.put(field, SecretCrypto.decrypt(value, secretKey));
            }
        }
    }
}
