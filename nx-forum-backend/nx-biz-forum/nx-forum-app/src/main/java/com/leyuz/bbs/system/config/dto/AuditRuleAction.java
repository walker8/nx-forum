package com.leyuz.bbs.system.config.dto;

/**
 * 审核规则命中动作
 *
 * @author Walker
 */
public enum AuditRuleAction {
    /**
     * 放行（命中后直接通过，跳过 AI）
     */
    PASS,

    /**
     * 转人工复审
     */
    REVIEW,

    /**
     * 直接拒绝
     */
    REJECT
}
