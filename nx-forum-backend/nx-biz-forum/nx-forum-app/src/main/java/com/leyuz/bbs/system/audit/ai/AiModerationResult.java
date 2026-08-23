package com.leyuz.bbs.system.audit.ai;

import lombok.Data;

/**
 * AI 审核结果
 *
 * @author Walker
 */
@Data
public class AiModerationResult {
    /**
     * 审核结论
     */
    private AiVerdict verdict;

    /**
     * 详细理由（仅供管理员审计使用，可能包含内部推理细节）
     */
    private String reason;

    /**
     * 面向用户的简短违规说明（用于通知用户，不超过 80 字）
     */
    private String userReason;

    /**
     * 置信度 0.0 ~ 1.0
     */
    private double confidence;

    /**
     * 是否放行
     */
    public boolean isPass() {
        return AiVerdict.PASS.equals(verdict);
    }
}
