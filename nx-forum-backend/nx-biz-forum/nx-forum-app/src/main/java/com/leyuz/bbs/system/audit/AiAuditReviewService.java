package com.leyuz.bbs.system.audit;

import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.common.dataobject.AuditStageV;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.AuditTriggerSourceV;
import com.leyuz.bbs.common.dataobject.OperatorTypeV;
import com.leyuz.bbs.common.utils.HtmlUtils;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.comment.service.CommentDomainService;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.content.thread.service.ThreadDomainService;
import com.leyuz.bbs.system.audit.ai.AiModerationClient;
import com.leyuz.bbs.system.audit.ai.AiModerationResult;
import com.leyuz.bbs.system.audit.ai.AiVerdict;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.dto.AuditContentType;
import com.leyuz.bbs.system.config.AiModelConfigApplication;
import com.leyuz.bbs.system.config.AuditConfigApplication;
import com.leyuz.bbs.system.config.dto.AiModelProviderDTO;
import com.leyuz.bbs.system.config.dto.AiModelRef;
import com.leyuz.bbs.system.config.dto.AuditConfigAiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 异步复审服务：加载内容 → 构建上下文（含历史）→ AI 判定 → 自动放行或留人工复审
 *
 * @author Walker
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAuditReviewService {
    private final AuditConfigApplication auditConfigApplication;
    private final AiModelConfigApplication aiModelConfigApplication;
    private final AiModerationClient aiModerationClient;
    private final AuditContextBuilder auditContextBuilder;
    private final ThreadGateway threadGateway;
    private final CommentGateway commentGateway;
    private final ThreadDomainService threadDomainService;
    private final CommentDomainService commentDomainService;
    private final AuditLogApplication auditLogApplication;

    /**
     * 执行 AI 复审
     */
    public void review(AuditContentType contentType, Long contentId, Integer forumId, Boolean isNew) {
        long startMs = System.currentTimeMillis();
        AuditConfigAiDTO config = auditConfigApplication.getAuditConfigAi();
        if (!config.isEnabled()) {
            return;
        }
        AiModelRef modelRef = resolveActiveModel(config);
        if (modelRef == null) {
            log.warn("AI 审核已启用但未配置可用模型");
            return;
        }
        AiModelProviderDTO provider = modelRef.provider();

        ContentData data = loadContent(contentType, contentId);
        if (data == null) {
            return;
        }
        AuditContext context = auditContextBuilder.build(
                data.userId(), contentType, isNew, forumId,
                data.subject(), data.textContent(), data.rawContent(), data.ip(), data.createTime(),
                config.getHistoryCount());

        AiModerationResult result;
        try {
            result = aiModerationClient.moderate(provider, modelRef.model(), config.getPrompt(), context);
        } catch (Exception e) {
            log.error("AI 审核调用失败，contentType={}, contentId={}", contentType, contentId, e);
            int costMs = (int) (System.currentTimeMillis() - startMs);
            AuditStatusV statusBefore = loadCurrentStatus(contentType, contentId);
            recordAiLog(contentType, contentId, forumId, data, provider, modelRef.model(),
                    null, null, "AI 调用失败：" + e.getMessage(), costMs, false,
                    statusBefore, statusBefore);
            updateReason(contentType, contentId, "AI 审核失败，转人工复审");
            return;
        }

        int costMs = (int) (System.currentTimeMillis() - startMs);
        AiVerdict verdict = result.getVerdict();
        // 审计场景的完整原因（包含内部推理细节），仅用于审计日志与 DB 字段
        String aiReasonText = StringUtils.defaultIfBlank(result.getReason(), "").trim();
        String fullReason = aiReasonText.isEmpty() ? verdictLabel(verdict) : aiReasonText;
        // 面向用户通知的简短原因：优先取 AI 显式返回的 userReason；缺失则回退到 verdict 标签，避免暴露判断规则
        String aiUserReasonText = StringUtils.defaultIfBlank(result.getUserReason(), "").trim();
        String userFacingReason = truncateForUser(aiUserReasonText.isEmpty() ? verdictLabel(verdict) : aiUserReasonText);
        // 记录 AI 决策时刻的状态。AI 调用期间管理员可能已手工处理（人工通过/拒绝），
        // 后续 applyDecision 会重新校验 DB 状态是否仍与该值一致，防止 AI 覆盖人工决策。
        AuditStatusV statusBefore = loadCurrentStatus(contentType, contentId);
        AuditStatusV statusAfter;

        if (result.isPass()) {
            statusAfter = AuditStatusV.PASSED;
            recordAiLog(contentType, contentId, forumId, data, provider, modelRef.model(),
                    result, verdict, fullReason, costMs, true, statusBefore, statusAfter);
            if (contentType == AuditContentType.THREAD) {
                // 主题帖：AUDITING -> PASSED
                applyDecision(contentType, contentId, statusBefore,
                        () -> threadDomainService.passThreads(forumId, List.of(contentId), true),
                        "AI 放行");
            }
            // 评论/楼中楼已是 PASSED，无需任何操作
        } else if (verdict == AiVerdict.REVIEW) {
            statusAfter = AuditStatusV.AUDITING;
            recordAiLog(contentType, contentId, forumId, data, provider, modelRef.model(),
                    result, verdict, fullReason, costMs, true, statusBefore, statusAfter);
            if (contentType == AuditContentType.THREAD) {
                // 主题帖：状态保持 AUDITING，仅更新原因（不通知）— 保留完整原因供管理员查看
                applyDecision(contentType, contentId, statusBefore,
                        () -> updateReason(contentType, contentId, fullReason),
                        "AI 存疑更新原因");
            } else {
                // 评论/楼中楼：PASSED -> AUDITING，发通知（仅告知违规事实）
                applyDecision(contentType, contentId, statusBefore,
                        () -> aiReviewContent(contentType, contentId, forumId, userFacingReason),
                        "AI 存疑转人工");
            }
        } else if (verdict == AiVerdict.REJECT) {
            statusAfter = AuditStatusV.REJECTED;
            recordAiLog(contentType, contentId, forumId, data, provider, modelRef.model(),
                    result, verdict, fullReason, costMs, true, statusBefore, statusAfter);
            // 自动删除 + 通知（现有 *RejectedEvent 监听器发送通知）— 仅告知违规事实
            applyDecision(contentType, contentId, statusBefore,
                    () -> rejectContent(contentType, contentId, forumId, userFacingReason),
                    "AI 拒绝");
        }
    }

    /**
     * 在应用 AI 决策前重新读取 DB 状态，确保仍与 AI 决策时刻看到的一致，
     * 避免覆盖管理员在 AI 调用期间的人工决策（如人工通过/拒绝）。
     * <p>该二次读取必要：AI 调用通常耗时数百毫秒到数秒，期间管理员可能已改状态，
     * 复用首次读取（review() 入口处的 statusBefore）会重新引入 TOCTOU 漏洞。</p>
     */
    private void applyDecision(AuditContentType contentType, Long contentId,
                               AuditStatusV expectedBefore, Runnable action, String actionDesc) {
        AuditStatusV currentStatus = loadCurrentStatus(contentType, contentId);
        if (currentStatus != expectedBefore) {
            log.warn("AI 审核决策被跳过：{} contentId={}，AI 决策时状态 {}，当前 DB 状态 {}，"
                    + "可能已被管理员人工处理", actionDesc, contentId, expectedBefore, currentStatus);
            return;
        }
        action.run();
    }

    private AiModelRef resolveActiveModel(AuditConfigAiDTO config) {
        return aiModelConfigApplication.resolveModel(config.getActiveModelId());
    }

    private ContentData loadContent(AuditContentType contentType, Long contentId) {
        return switch (contentType) {
            case THREAD -> {
                ThreadE threadE = threadGateway.getThreadDetail(contentId);
                if (threadE == null) {
                    yield null;
                }
                yield new ContentData(threadE.getCreateBy(), threadE.getSubject(),
                        HtmlUtils.convertHtmlToText(threadE.getContent()), threadE.getContent(),
                        threadE.getUserIp(), threadE.getCreateTime());
            }
            case COMMENT -> {
                CommentE commentE = commentGateway.getComment(contentId);
                if (commentE == null) {
                    yield null;
                }
                yield new ContentData(commentE.getCreateBy(), null, commentE.getMessage(), null,
                        commentE.getUserIp(), commentE.getCreateTime());
            }
            case REPLY -> {
                CommentReplyE replyE = commentGateway.getCommentReply(contentId);
                if (replyE == null) {
                    yield null;
                }
                yield new ContentData(replyE.getCreateBy(), null, replyE.getMessage(), null,
                        replyE.getUserIp(), replyE.getCreateTime());
            }
        };
    }

    /**
     * 写入 AI 审核记录
     */
    private void recordAiLog(AuditContentType contentType, Long contentId, Integer forumId,
                             ContentData data, AiModelProviderDTO provider, String modelId,
                             AiModerationResult result, AiVerdict verdict, String reason,
                             int costMs, boolean isNew,
                             AuditStatusV statusBefore, AuditStatusV statusAfter) {
        try {
            Map<String, Object> hitDetail = new HashMap<>();
            hitDetail.put("modelProvider", provider == null ? "" : provider.getName());
            hitDetail.put("modelId", modelId);
            if (verdict != null) {
                hitDetail.put("verdict", verdict.name());
            }
            if (result != null) {
                hitDetail.put("confidence", result.getConfidence());
                hitDetail.put("rawResponse", truncate(result.getReason(), 2000));
            }

            auditLogApplication.record(AuditLogE.builder()
                    .contentType(contentType.ordinal() + 1)
                    .contentId(contentId)
                    .forumId(forumId)
                    .userId(data.userId())
                    .userIp(data.ip())
                    .auditSessionId(null)
                    .triggerSource(Boolean.TRUE.equals(isNew) ? AuditTriggerSourceV.NEW : AuditTriggerSourceV.EDIT)
                    .auditStage(AuditStageV.AI)
                    .auditStatusBefore(statusBefore)
                    .auditStatusAfter(statusAfter)
                    .hitDetail(JSON.toJSONString(hitDetail))
                    .operatorType(OperatorTypeV.SYSTEM)
                    .reason(reason)
                    .costMs(costMs)
                    .contentSnapshot(truncate(data.textContent(), 2000))
                    .build());
        } catch (Exception e) {
            log.error("写入 AI 审核记录失败", e);
        }
    }

    /**
     * 截断字符串
     */
    private String truncate(String text, int maxLen) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen);
    }

    /**
     * 面向用户的说明限长：超过 80 字直接截断，避免通知过长
     */
    private String truncateForUser(String text) {
        return truncate(text, 80);
    }

    private void updateReason(AuditContentType contentType, Long contentId, String reason) {
        switch (contentType) {
            case THREAD -> threadGateway.updateAuditReason(contentId, reason);
            case COMMENT -> commentGateway.updateCommentAuditReason(contentId, reason);
            case REPLY -> commentGateway.updateCommentReplyAuditReason(contentId, reason);
        }
    }

    /**
     * 加载内容当前实际审核状态（用于审计日志记录准确的状态流转）
     */
    private AuditStatusV loadCurrentStatus(AuditContentType contentType, Long contentId) {
        return switch (contentType) {
            case THREAD -> {
                ThreadE t = threadGateway.getThread(contentId);
                yield t != null ? t.getAuditStatus() : null;
            }
            case COMMENT -> {
                CommentE c = commentGateway.getComment(contentId);
                yield c != null ? c.getAuditStatus() : null;
            }
            case REPLY -> {
                CommentReplyE r = commentGateway.getCommentReply(contentId);
                yield r != null ? r.getAuditStatus() : null;
            }
        };
    }

    /**
     * AI 判定违规时自动删除 + 通知作者
     */
    private void rejectContent(AuditContentType contentType, Long contentId, Integer forumId, String reason) {
        switch (contentType) {
            case THREAD -> threadDomainService.rejectThreads(forumId, List.of(contentId), reason, true);
            case COMMENT -> commentDomainService.rejectComments(forumId, List.of(contentId), reason, true);
            case REPLY -> commentDomainService.rejectCommentReplies(forumId, List.of(contentId), reason, true);
        }
    }

    /**
     * AI 判定存疑时将评论/楼中楼从 PASSED 退回审核中 + 通知作者
     */
    private void aiReviewContent(AuditContentType contentType, Long contentId, Integer forumId, String reason) {
        switch (contentType) {
            case COMMENT -> commentDomainService.aiReviewComments(forumId, List.of(contentId), reason, true);
            case REPLY -> commentDomainService.aiReviewCommentReplies(forumId, List.of(contentId), reason, true);
            default -> throw new IllegalArgumentException("aiReviewContent 不支持 contentType: " + contentType);
        }
    }

    private String verdictLabel(AiVerdict verdict) {
        return switch (verdict) {
            case PASS -> "正常";
            case REVIEW -> "存疑";
            case REJECT -> "违规";
        };
    }

    private record ContentData(Long userId, String subject, String textContent, String rawContent,
                               String ip, LocalDateTime createTime) {
    }
}
