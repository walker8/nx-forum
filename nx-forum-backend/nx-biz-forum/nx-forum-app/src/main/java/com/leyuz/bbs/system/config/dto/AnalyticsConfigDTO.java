package com.leyuz.bbs.system.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计配置数据传输对象
 *
 * @author walker
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsConfigDTO {
    /**
     * 统计代码
     */
    private String analyticsCode;

    /**
     * 是否启用
     */
    private Boolean enabled;
}

