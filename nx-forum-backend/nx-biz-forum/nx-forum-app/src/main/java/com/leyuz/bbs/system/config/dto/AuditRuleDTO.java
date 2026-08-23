package com.leyuz.bbs.system.config.dto;

import lombok.Data;

/**
 * 审核规则（QLExpress 表达式规则）
 *
 * @author Walker
 */
@Data
public class AuditRuleDTO {
    /**
     * 规则唯一标识
     */
    private String id;

    /**
     * 规则名称
     */
    private String name;

    /**
     * QLExpress 布尔表达式，在 AuditContext 上求值
     * 可用变量：poster / post / content，函数：contains / matchRegex
     */
    private String expression;

    /**
     * 命中动作
     */
    private AuditRuleAction action;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 优先级（数值越小越先执行）
     */
    private int priority;
}
