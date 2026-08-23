package com.leyuz.bbs.system.audit.dto;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import lombok.Data;

/**
 * 审核结果
 *
 * @author Walker
 */
@Data
public class AuditResult {
    /**
     * 审核状态
     */
    private AuditStatusV status;

    /**
     * 审核原因
     */
    private String reason;

    /**
     * 是否需要异步 AI 复审
     */
    private boolean aiPending;

    /**
     * 审核会话ID（用于在内容入库后回填审核记录的 contentId）
     */
    private String sessionId;

    public static AuditResult of(AuditStatusV status, String reason) {
        AuditResult result = new AuditResult();
        result.status = status;
        result.reason = reason;
        return result;
    }

    public static AuditResult passed() {
        return of(AuditStatusV.PASSED, "");
    }

    public static AuditResult auditing(String reason) {
        return of(AuditStatusV.AUDITING, reason);
    }

    public static AuditResult rejected(String reason) {
        return of(AuditStatusV.REJECTED, reason);
    }

    /**
     * 进入审核中并标记待异步 AI 复审
     */
    public static AuditResult auditingPendingAi(String reason) {
        AuditResult result = auditing(reason);
        result.aiPending = true;
        return result;
    }

    /**
     * 已通过但标记待异步 AI 复审（用于评论/楼中楼的默认通过场景）
     */
    public static AuditResult passedPendingAi(String reason) {
        AuditResult result = of(AuditStatusV.PASSED, reason);
        result.aiPending = true;
        return result;
    }
}
