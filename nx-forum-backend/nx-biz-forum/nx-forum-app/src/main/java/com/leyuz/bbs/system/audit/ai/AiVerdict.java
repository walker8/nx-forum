package com.leyuz.bbs.system.audit.ai;

/**
 * AI 审核结论
 *
 * @author Walker
 */
public enum AiVerdict {
    /**
     * 正常放行
     */
    PASS,

    /**
     * 存疑，转人工复审
     */
    REVIEW,

    /**
     * 明显违规，转人工复审
     */
    REJECT
}
