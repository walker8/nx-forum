package com.leyuz.module.config.app;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 敏感配置明文存量迁移器
 *
 * <p>启动时扫描注册表中的敏感配置键，将历史明文敏感字段加密落库。
 * 对已加密/空值幂等跳过，可重复执行，失败仅告警不阻断启动。</p>
 *
 * @author Walker
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensitiveConfigEncryptRunner implements ApplicationRunner {

    private final ConfigApplication configApplication;

    private final ConfigSecretHandler configSecretHandler;

    @Override
    public void run(ApplicationArguments args) {
        try {
            for (String configKey : configSecretHandler.sensitiveKeys()) {
                String rawValue = configApplication.getRawConfigValueByKey(configKey);
                if (StringUtils.isBlank(rawValue)) {
                    continue;
                }
                String encryptedValue = configSecretHandler.encryptConfigValue(configKey, rawValue);
                if (!encryptedValue.equals(rawValue)) {
                    configApplication.updateConfig(configKey, encryptedValue);
                    log.info("敏感配置已加密落库，configKey={}", configKey);
                }
            }
        } catch (Exception e) {
            // 迁移失败不阻断启动，后续写入时会自动加密
            log.warn("敏感配置明文存量迁移失败，将在下次保存时自动加密：{}", e.getMessage());
        }
    }
}
