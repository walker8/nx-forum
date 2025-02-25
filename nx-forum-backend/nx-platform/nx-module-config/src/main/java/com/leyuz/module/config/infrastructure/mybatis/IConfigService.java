package com.leyuz.module.config.infrastructure.mybatis;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyuz.module.config.infrastructure.ConfigPO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author walker
 * @since 2024-08-16
 */
public interface IConfigService extends IService<ConfigPO> {
    List<ConfigPO> listAll();

    boolean updateConfig(String key, String value);
}
