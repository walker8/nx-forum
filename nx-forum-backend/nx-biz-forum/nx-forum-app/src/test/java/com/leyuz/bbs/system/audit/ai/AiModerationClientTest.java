package com.leyuz.bbs.system.audit.ai;

import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.context.ContentInfo;
import com.leyuz.bbs.system.audit.context.PostInfo;
import com.leyuz.bbs.system.audit.context.PosterInfo;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AiModerationClient.buildUserMessage 单元测试：验证新历史上下文格式
 */
class AiModerationClientTest {

    private final AiModerationClient client = new AiModerationClient();

    @Test
    void threadHistoryContainsTitleSummaryAndTime() throws Exception {
        LocalDateTime t = LocalDateTime.of(2026, 8, 1, 12, 30);
        AuditContext ctx = AuditContext.builder()
                .poster(PosterInfo.builder().userId(1L).userName("u").build())
                .post(PostInfo.builder().contentType("THREAD").subject("当前帖").build())
                .content(ContentInfo.builder().text("当前内容").build())
                .recentThreads(List.of(
                        new ThreadHistoryItemV("历史标题A", "历史概要A", t),
                        new ThreadHistoryItemV("历史标题B", null, t.plusHours(1))))
                .recentComments(List.of())
                .build();

        String msg = invokeBuildUserMessage(ctx);

        assertTrue(msg.contains("【历史发帖】"), "应包含【历史发帖】小节");
        assertTrue(msg.contains("[2026-08-01 12:30] 标题：历史标题A 概要：历史概要A"),
                "历史发帖应输出 [time] 标题：x 概要：y");
        assertTrue(msg.contains("[2026-08-01 13:30] 标题：历史标题B 概要：[无概要]"),
                "summary 为空时输出 [无概要]");
        assertFalse(msg.contains("【历史评论】"), "评论列表为空时不应输出【历史评论】小节");
    }

    @Test
    void commentHistoryContainsContentAndTimeWithMixedReplies() throws Exception {
        LocalDateTime t1 = LocalDateTime.of(2026, 8, 1, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 8, 1, 10, 0);
        AuditContext ctx = AuditContext.builder()
                .poster(PosterInfo.builder().userId(1L).userName("u").build())
                .post(PostInfo.builder().contentType("COMMENT").build())
                .content(ContentInfo.builder().text("当前评论").build())
                .recentThreads(List.of())
                .recentComments(List.of(
                        new CommentHistoryItemV("评论A", t1),
                        new CommentHistoryItemV("楼中楼B", t2)))
                .build();

        String msg = invokeBuildUserMessage(ctx);

        assertTrue(msg.contains("【历史评论】"), "应包含【历史评论】小节");
        assertTrue(msg.contains("[2026-08-01 09:00] 内容：评论A"), "顶层评论格式正确");
        assertTrue(msg.contains("[2026-08-01 10:00] 内容：楼中楼B"),
                "楼中楼与顶层评论使用同一格式输出");
        assertFalse(msg.contains("【历史发帖】"), "评论列表为空时不应输出【历史发帖】小节");
    }

    @Test
    void longContentIsTruncated() throws Exception {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longContent.append("x");
        }
        AuditContext ctx = AuditContext.builder()
                .poster(PosterInfo.builder().userId(1L).userName("u").build())
                .post(PostInfo.builder().contentType("COMMENT").build())
                .content(ContentInfo.builder().text("当前评论").build())
                .recentThreads(List.of())
                .recentComments(List.of(new CommentHistoryItemV(longContent.toString(), LocalDateTime.now())))
                .build();

        String msg = invokeBuildUserMessage(ctx);

        // StringUtils.abbreviate 截断后会加 ...
        assertTrue(msg.contains("..."), "长内容应被截断");
    }

    private String invokeBuildUserMessage(AuditContext ctx) throws Exception {
        Method m = AiModerationClient.class.getDeclaredMethod("buildUserMessage", AuditContext.class);
        m.setAccessible(true);
        return (String) m.invoke(client, ctx);
    }
}