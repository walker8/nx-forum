package com.leyuz.module.config.infrastructure.mybatis;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyuz.common.utils.BaseEntityUtils;
import com.leyuz.module.config.infrastructure.ConfigPO;
import com.leyuz.module.config.infrastructure.mybatis.mapper.ConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author walker
 * @since 2024-08-16
 */
@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, ConfigPO> implements IConfigService {
    @Override
    public List<ConfigPO> listAll() {
        QueryWrapper<ConfigPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", false);
        return list(queryWrapper);
    }

    @Override
    public boolean updateConfig(String key, String value) {
        ConfigPO configPO = getOne(new QueryWrapper<ConfigPO>().eq("config_key", key));
        if (configPO == null) {
            // 不存在则新增配置项
            configPO = new ConfigPO();
            configPO.setConfigKey(key);
            configPO.setConfigType(false);
            configPO.setConfigValue(value);
            BaseEntityUtils.setCreateBaseEntity(configPO);
            return save(configPO);
        }
        configPO.setConfigValue(value);
        BaseEntityUtils.setUpdateBaseEntity(configPO);
        return updateById(configPO);
    }
}
