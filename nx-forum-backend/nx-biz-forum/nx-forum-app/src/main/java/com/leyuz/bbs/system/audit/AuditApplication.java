package com.leyuz.bbs.system.audit;

import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.common.dataobject.AuditStageV;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.AuditTriggerSourceV;
import com.leyuz.bbs.common.dataobject.OperatorTypeV;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.domain.AuditLogE;
import com.leyuz.bbs.system.audit.dto.AuditConfigBlackWhiteUsersVO;
import com.leyuz.bbs.system.audit.dto.AuditContentType;
import com.leyuz.bbs.system.audit.dto.AuditDTO;
import com.leyuz.bbs.system.audit.dto.AuditResult;
import com.leyuz.bbs.system.audit.event.ContentAiAuditEvent;
import com.leyuz.bbs.system.audit.rule.RuleEngine;
import com.leyuz.bbs.system.config.AuditConfigApplication;
import com.leyuz.bbs.system.config.dto.AuditConfigAiDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigBlackWhiteUsersDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigRulesDTO;
import com.leyuz.bbs.system.config.dto.AuditConfigSensitiveWordsDTO;
import com.leyuz.bbs.system.config.dto.AuditRuleAction;
import com.leyuz.bbs.system.config.dto.AuditRuleDTO;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditApplication {
    private final UserApplication userApplication;
    private final AuditConfigApplication auditConfigApplication;
    private final AuditContextBuilder auditContextBuilder;
    private final RuleEngine ruleEngine;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogApplication auditLogApplication;

    public AuditConfigBlackWhiteUsersVO getAuditConfigBlackWhiteUsersVO() {
        AuditConfigBlackWhiteUsersDTO auditConfigBlackWhiteUsers = auditConfigApplication.getAuditConfigBlackWhiteUsers();
        AuditConfigBlackWhiteUsersVO auditConfigBlackWhiteUsersVO = new AuditConfigBlackWhiteUsersVO();
        auditConfigBlackWhiteUsers.getWhiteListUsers().forEach(userId -> {
            try {
                UserVO userVO = userApplication.getUserInfo(userId);
                auditConfigBlackWhiteUsersVO.getWhiteListUsers().add(userVO);
            } catch (Exception e) {
                log.error("获取用户信息失败，userId = {}", userId);
            }
        });
        auditConfigBlackWhiteUsers.getBlackListUsers().forEach(userId -> {
            try {
                UserVO userVO = userApplication.getUserInfo(userId);
                auditConfigBlackWhiteUsersVO.getBlackListUsers().add(userVO);
            } catch (Exception e) {
                log.error("获取用户信息失败，userId = {}", userId);
            }
        });
        return auditConfigBlackWhiteUsersVO;
    }

    // 检查文本是否包含敏感词，返回包含的敏感词
    private String hasSensitiveWords(String text) {
        AuditConfigSensitiveWordsDTO config = auditConfigApplication.getAuditConfigSensitiveWords();
        if (!config.isEnableSensitiveWordsAudit()) {
            return "";
        }
        String lowerText = StringUtils.defaultString(text).toLowerCase();
        return config.getSensitiveWords().stream()
                .filter(word -> lowerText.contains(word.toLowerCase()))
                .findFirst().orElse("");
    }

    /**
     * 审核策略：黑白名单 → 敏感词 → 规则引擎（QLExpress） → AI（异步）
     *
     * @param auditDTO 审核入参
     * @return 审核结果
     */
    public AuditResult check(AuditDTO auditDTO) {
        String sessionId = AuditLogApplication.newSessionId();
        // 黑白名单检查
        AuditConfigBlackWhiteUsersDTO config = auditConfigApplication.getAuditConfigBlackWhiteUsers();
        if (config.getWhiteListUsers().contains(auditDTO.getUserId())) {
            recordAuditLog(auditDTO, AuditStageV.BLACK_WHITE, null, AuditStatusV.PASSED,
                    "白名单用户免审", sessionId, Map.of("listType", "WHITE", "matchedUserId", auditDTO.getUserId()));
            return attachSessionId(AuditResult.passed(), sessionId);
        }
        if (config.getBlackListUsers().contains(auditDTO.getUserId())) {
            recordAuditLog(auditDTO, AuditStageV.BLACK_WHITE, null, AuditStatusV.AUDITING,
                    "黑名单审核", sessionId, Map.of("listType", "BLACK", "matchedUserId", auditDTO.getUserId()));
            return attachSessionId(AuditResult.auditing("黑名单审核"), sessionId);
        }

        // 敏感词检查
        String sensitiveWord = hasSensitiveWords(auditDTO.getMessage());
        if (StringUtils.isNotEmpty(sensitiveWord)) {
            recordAuditLog(auditDTO, AuditStageV.SENSITIVE_WORD, null, AuditStatusV.AUDITING,
                    "包含敏感词：" + sensitiveWord, sessionId, Map.of("word", sensitiveWord, "mode", "contains"));
            return attachSessionId(AuditResult.auditing("包含敏感词：" + sensitiveWord), sessionId);
        }

        // 规则引擎检查（同步阶段不加载历史上下文）
        AuditContext context = auditContextBuilder.build(
                auditDTO.getUserId(), auditDTO.getContentType(), auditDTO.getIsNew(), auditDTO.getForumId(),
                auditDTO.getSubject(), auditDTO.getMessage(), auditDTO.getContent(),
                HeaderUtils.getIp(), null, 0);
        AuditConfigRulesDTO rulesConfig = auditConfigApplication.getAuditConfigRules();
        if (rulesConfig.isEnabled()) {
            AuditRuleDTO hitRule = ruleEngine.check(context, rulesConfig.getRules());
            if (hitRule != null) {
                AuditStatusV afterStatus = mapActionToStatus(hitRule.getAction());
                Map<String, Object> hitDetail = new HashMap<>();
                hitDetail.put("ruleId", hitRule.getId());
                hitDetail.put("ruleName", hitRule.getName());
                hitDetail.put("action", hitRule.getAction().name());
                hitDetail.put("priority", hitRule.getPriority());
                hitDetail.put("expression", hitRule.getExpression());
                recordAuditLog(auditDTO, AuditStageV.RULE_ENGINE, null, afterStatus,
                        "规则命中：" + hitRule.getName(), sessionId, hitDetail);
                return switch (hitRule.getAction()) {
                    case PASS -> attachSessionId(AuditResult.passed(), sessionId);
                    case REVIEW -> attachSessionId(AuditResult.auditing("规则命中：" + hitRule.getName()), sessionId);
                    case REJECT -> attachSessionId(AuditResult.rejected("规则命中：" + hitRule.getName()), sessionId);
                };
            }
        }

        // AI 审核（异步）
        AuditConfigAiDTO aiConfig = auditConfigApplication.getAuditConfigAi();
        if (aiConfig.isEnabled() && StringUtils.isNotBlank(aiConfig.getActiveModelId())) {
            // AI 异步阶段本身由 AiAuditReviewService 写入审计记录
            // 此处仅返回结果 + 发布异步事件
            AuditContentType type = auditDTO.getContentType();
            if (type == AuditContentType.COMMENT || type == AuditContentType.REPLY) {
                // 评论/楼中楼：默认通过 + 异步 AI 复审
                return attachSessionId(AuditResult.passedPendingAi("AI异步审核中（评论默认通过）"), sessionId);
            }
            // 主题帖：进入审核中 + 异步 AI 复审
            return attachSessionId(AuditResult.auditingPendingAi("AI审核中"), sessionId);
        }

        // 无需审核：直接放行（记录一条通过日志，便于追溯）
        recordAuditLog(auditDTO, AuditStageV.BLACK_WHITE, null, AuditStatusV.PASSED,
                "自动通过", sessionId, Map.of("reason", "no_rule_triggered"));
        return attachSessionId(AuditResult.passed(), sessionId);
    }

    /**
     * 将本次审核会话ID附到审核结果上，便于调用方在内容入库后回填 contentId
     */
    private AuditResult attachSessionId(AuditResult result, String sessionId) {
        if (result != null) {
            result.setSessionId(sessionId);
        }
        return result;
    }

    /**
     * 将规则的 action 映射为审核后状态
     */
    private AuditStatusV mapActionToStatus(AuditRuleAction action) {
        return switch (action) {
            case PASS -> AuditStatusV.PASSED;
            case REVIEW -> AuditStatusV.AUDITING;
            case REJECT -> AuditStatusV.REJECTED;
        };
    }

    /**
     * 统一构造并写入审核记录
     */
    private void recordAuditLog(AuditDTO dto, AuditStageV stage, AuditStatusV before, AuditStatusV after,
                                String reason, String sessionId, Map<String, Object> hitDetail) {
        try {
            Integer contentTypeVal = dto.getContentType() == null ? null : dto.getContentType().ordinal() + 1;
            AuditTriggerSourceV trigger = Boolean.TRUE.equals(dto.getIsNew()) ? AuditTriggerSourceV.NEW : AuditTriggerSourceV.EDIT;
            auditLogApplication.record(AuditLogE.builder()
                    .contentType(contentTypeVal)
                    .contentId(null)
                    .forumId(dto.getForumId())
                    .userId(dto.getUserId())
                    .userIp(HeaderUtils.getIp())
                    .auditSessionId(sessionId)
                    .triggerSource(trigger)
                    .auditStage(stage)
                    .auditStatusBefore(before)
                    .auditStatusAfter(after)
                    .hitDetail(hitDetail == null ? null : JSON.toJSONString(hitDetail))
                    .operatorType(OperatorTypeV.SYSTEM)
                    .reason(reason)
                    .contentSnapshot(buildContentSnapshot(dto))
                    .build());
        } catch (Exception e) {
            log.error("写入审核记录异常", e);
        }
    }

    /**
     * 构造内容快照：帖子场景为「标题 + 正文文本」，评论/楼中楼为评论文本
     * 长度限制 2000 字符，避免超长内容撑爆字段
     */
    private String buildContentSnapshot(AuditDTO dto) {
        if (dto == null) {
            return null;
        }
        String subject = dto.getSubject();
        String message = dto.getMessage();
        String snapshot;
        if (subject != null && !subject.isEmpty()) {
            snapshot = subject + "\n" + (message == null ? "" : message);
        } else {
            snapshot = message;
        }
        if (snapshot == null) {
            return null;
        }
        return snapshot.length() > 2000 ? snapshot.substring(0, 2000) : snapshot;
    }

    /**
     * 触发异步 AI 复审
     */
    public void scheduleAiReview(AuditContentType contentType, Long contentId, Integer forumId, Boolean isNew) {
        eventPublisher.publishEvent(new ContentAiAuditEvent(this, contentType, contentId, forumId, isNew));
    }
}
