package com.leyuz.bbs.system.audit.dto;

import lombok.Data;

/**
 * 规则表达式测试命令
 *
 * @author Walker
 */
@Data
public class RuleTestCmd {
    /**
     * QLExpress 表达式
     */
    private String expression;
}
