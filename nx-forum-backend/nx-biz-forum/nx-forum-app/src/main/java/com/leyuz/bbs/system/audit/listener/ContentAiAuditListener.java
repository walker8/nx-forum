package com.leyuz.bbs.system.audit.listener;

import com.leyuz.bbs.system.audit.AiAuditReviewService;
import com.leyuz.bbs.system.audit.event.ContentAiAuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 异步 AI 审核监听器
 *
 * <p>使用 {@link TransactionalEventListener} + {@code AFTER_COMMIT}，确保发布事件的事务
 * （例如 ThreadApplication.createThread、CommentDomainService.saveComment）已提交后再触发
 * AI 审核，避免读到事务未提交的脏数据或缺失的行。</p>
 *
 * @author Walker
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentAiAuditListener {
    private final AiAuditReviewService aiAuditReviewService;

    /**
     * {@code fallbackExecution = true} 兜底：若事件在非事务上下文发布（例如某些测试 / 工具脚本），
     * 仍以普通同步事件方式执行，避免被默认的「无事务则跳过」行为静默吞掉。
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async("aiAuditExecutor")
    public void onContentAiAuditEvent(ContentAiAuditEvent event) {
        try {
            aiAuditReviewService.review(event.getContentType(), event.getContentId(), event.getForumId(), event.getIsNew());
        } catch (Exception e) {
            log.error("AI 审核异步处理失败，contentType={}, contentId={}", event.getContentType(), event.getContentId(), e);
        }
    }
}
