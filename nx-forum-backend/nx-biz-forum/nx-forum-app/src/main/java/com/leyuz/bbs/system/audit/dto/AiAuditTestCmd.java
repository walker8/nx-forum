package com.leyuz.bbs.system.audit.dto;

import lombok.Data;

/**
 * AI 审核场景测试命令
 *
 * @author Walker
 */
@Data
public class AiAuditTestCmd {
    /**
     * 模型唯一键（引用共享模型库中的模型）
     */
    private String modelId;

    /**
     * 审核场景系统提示词
     */
    private String prompt;

    /**
     * 测试内容
     */
    private String content;
}
