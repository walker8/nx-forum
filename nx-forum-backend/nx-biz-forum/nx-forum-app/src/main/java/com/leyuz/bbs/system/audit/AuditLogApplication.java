package com.leyuz.bbs.system.audit;

import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.domain.gateway.AuditLogGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审核记录 Application Service
 * <p>
 * 提供统一的 record 入口；写入失败不影响主业务流。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogApplication {

    private final AuditLogGateway auditLogGateway;

    /**
     * 记录一条审核事件
     *
     * @param auditLogE 审核记录实体
     * @return 已保存的实体（含 logId / createTime）
     */
    public AuditLogE record(AuditLogE auditLogE) {
        if (auditLogE == null) {
            return null;
        }
        try {
            auditLogGateway.save(auditLogE);
            return auditLogE;
        } catch (Exception e) {
            // 写入审计日志失败不应阻塞主业务
            log.error("保存审核记录失败，contentType={}, contentId={}", auditLogE.getContentType(), auditLogE.getContentId(), e);
            return null;
        }
    }

    /**
     * 内容入库后回填同步阶段审核记录的 contentId
     *
     * <p>同步审核（黑白名单/敏感词/规则引擎）在内容入库前写日志时拿不到 contentId，
     * 会话内统一使用 sessionId 串联；主流程拿到真实 ID 后调用本方法批量回填，
     * 避免审核日志列表展示「未保存」。
     *
     * @param sessionId 审核会话ID
     * @param contentId 内容入库后的主键
     * @return 受影响行数
     */
    public int updateContentIdBySessionId(String sessionId, Long contentId) {
        if (sessionId == null || sessionId.isEmpty() || contentId == null) {
            return 0;
        }
        try {
            return auditLogGateway.updateContentIdBySessionId(sessionId, contentId);
        } catch (Exception e) {
            // 回填失败不应阻塞主业务
            log.error("回填审核记录 contentId 失败，sessionId={}, contentId={}", sessionId, contentId, e);
            return 0;
        }
    }

    /**
     * 生成新的审核会话 ID（用于串联同一审核事件的多次记录）
     */
    public static String newSessionId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
