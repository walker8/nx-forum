package com.leyuz.bbs.system.config.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 审核规则引擎配置
 *
 * @author Walker
 */
@Data
public class AuditConfigRulesDTO {
    /**
     * 是否启用规则引擎
     */
    private boolean enabled = true;

    /**
     * 规则列表
     */
    private List<AuditRuleDTO> rules = new ArrayList<>();
}
