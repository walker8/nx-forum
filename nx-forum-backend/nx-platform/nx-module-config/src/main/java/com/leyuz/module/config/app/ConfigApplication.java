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

    public String getConfigValueByKey(String configKey) {
        Map<String, String> configMap = getConfigMap();
        return configMap.get(configKey);
    }

    public <T> T getConfigValueByKey(String configKey, Class<T> clazz) {
        String defaultValue = getConfigValueByKey(configKey);
        if (StringUtils.isEmpty(defaultValue)) {
            return null;
        }
        return JSON.parseObject(defaultValue, clazz);
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

    public boolean updateConfig(String configKey, String configValue) {
        configCache.remove("all");
        return configService.updateConfig(configKey, configValue);
    }

}
