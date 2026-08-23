package com.leyuz.bbs.system.audit.rule;

import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.context.ContentInfo;
import com.leyuz.bbs.system.audit.context.PostInfo;
import com.leyuz.bbs.system.audit.context.PosterInfo;
import com.leyuz.bbs.system.config.dto.AuditRuleAction;
import com.leyuz.bbs.system.config.dto.AuditRuleDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * QLExpress 规则引擎单元测试
 */
class RuleEngineTest {

    private final RuleEngine engine = new RuleEngine();

    private AuditContext context() {
        return AuditContext.builder()
                .poster(PosterInfo.builder().userId(1L).userName("测试用户")
                        .registerDays(3).postCount(1).commentCount(2).build())
                .post(PostInfo.builder().isNew(true).contentType("THREAD").forumId(1)
                        .forumName("测试版块").subject("标题").hour(3).build())
                .content(ContentInfo.builder().text("加微信 xxxx 联系").length(10)
                        .linkCount(0).hasLink(false).imageCount(0).build())
                .recentThreads(List.of(new ThreadHistoryItemV("历史帖标题", "历史帖概要", LocalDateTime.now())))
                .recentComments(List.of(new CommentHistoryItemV("历史评论内容", LocalDateTime.now())))
                .build();
    }

    private AuditRuleDTO rule(String expression, AuditRuleAction action) {
        AuditRuleDTO rule = new AuditRuleDTO();
        rule.setId("1");
        rule.setName("测试规则");
        rule.setExpression(expression);
        rule.setAction(action);
        rule.setEnabled(true);
        rule.setPriority(1);
        return rule;
    }

    @Test
    void numericComparisonShouldMatch() {
        assertNotNull(engine.check(context(), List.of(rule("poster.registerDays < 7", AuditRuleAction.REVIEW))));
        assertNotNull(engine.check(context(), List.of(rule("post.hour < 6", AuditRuleAction.REVIEW))));
        assertNull(engine.check(context(), List.of(rule("poster.registerDays < 2", AuditRuleAction.REVIEW))));
    }

    @Test
    void booleanPropertyShouldMatch() {
        assertNotNull(engine.check(context(), List.of(rule("post.isNew == true", AuditRuleAction.REVIEW))));
        assertNull(engine.check(context(), List.of(rule("post.isNew == false", AuditRuleAction.REVIEW))));
    }

    @Test
    void containsFunctionShouldMatch() {
        assertNotNull(engine.check(context(), List.of(rule("contains(content.text, \"微信\")", AuditRuleAction.REVIEW))));
        assertNull(engine.check(context(), List.of(rule("contains(content.text, \"不存在\")", AuditRuleAction.REVIEW))));
    }

    @Test
    void priorityShouldEvaluateFirstMatch() {
        AuditRuleDTO first = rule("poster.registerDays < 7", AuditRuleAction.REVIEW);
        first.setPriority(1);
        AuditRuleDTO second = rule("post.hour < 6", AuditRuleAction.REJECT);
        second.setPriority(2);
        AuditRuleDTO hit = engine.check(context(), List.of(second, first));
        assertNotNull(hit);
    }

    @Test
    void validateShouldReturnNullForValidExpression() {
        assertNull(engine.validate("poster.registerDays < 7"));
        assertNull(engine.validate("content.linkCount > 0 && post.isNew == true"));
    }

    @Test
    void validateShouldReturnErrorForInvalidExpression() {
        assertNotNull(engine.validate("poster.registerDays <"));
        assertNotNull(engine.validate("poster.registerDays + 1"));
    }
}
