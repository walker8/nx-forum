package com.leyuz.module.config.infrastructure;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 * 系统配置表
 * </p>
 *
 * @author walker
 * @since 2024-08-16
 */
@TableName("common_configs")
@Data
public class ConfigPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -8272966760359000827L;
    /**
     * 配置id
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;

    /**
     * 配置键名
     */
    private String configKey;

    /**
     * 配置键值
     */
    private String configValue;

    /**
     * 系统内置（0是 1否）
     */
    private Boolean configType;

    /**
     * 说明
     */
    private String remark;
}
