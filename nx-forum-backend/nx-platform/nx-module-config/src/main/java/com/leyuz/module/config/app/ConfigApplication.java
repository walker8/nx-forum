package com.leyuz.module.config.app;

import com.alibaba.fastjson2.JSON;
import com.leyuz.module.cache.GenericCache;
import com.leyuz.module.config.infrastructure.ConfigPO;
import com.leyuz.module.config.infrastructure.mybatis.IConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigApplication {
    private final IConfigService configService;
    private final GenericCache<String, List<ConfigPO>> configCache;
    private final ConfigSecretHandler configSecretHandler;

    /**
     * 读取配置值（敏感配置自动解密）
     */
    public String getConfigValueByKey(String configKey) {
        String configValue = getRawConfigValueByKey(configKey);
        return configSecretHandler.decryptConfigValue(configKey, configValue);
    }

    public <T> T getConfigValueByKey(String configKey, Class<T> clazz) {
        String defaultValue = getConfigValueByKey(configKey);
        if (StringUtils.isEmpty(defaultValue)) {
            return null;
        }
        return JSON.parseObject(defaultValue, clazz);
    }

    /**
     * 读取库中原始配置值（不做解密，仅用于迁移等内部场景）
     */
    public String getRawConfigValueByKey(String configKey) {
        Map<String, String> configMap = getConfigMap();
        return configMap.get(configKey);
    }

    private Map<String, String> getConfigMap() {
        List<ConfigPO> configPOList = configCache.get("all");
        if (CollectionUtils.isEmpty(configPOList)) {
            configPOList = configService.listAll();
            configCache.put("all", configPOList);
        }
        Map<String, String> configMap = new HashMap<>();
        configPOList.forEach(configPO -> configMap.put(configPO.getConfigKey(), configPO.getConfigValue()));
        return configMap;
    }

    /**
     * 更新配置值（敏感配置自动加密后落库）
     */
    public boolean updateConfig(String configKey, String configValue) {
        String encryptedValue = configSecretHandler.encryptConfigValue(configKey, configValue);
        configCache.remove("all");
        return configService.updateConfig(configKey, encryptedValue);
    }

}
