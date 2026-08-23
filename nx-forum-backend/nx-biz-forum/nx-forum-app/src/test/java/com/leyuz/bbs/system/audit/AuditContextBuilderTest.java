package com.leyuz.bbs.system.audit;

import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import com.leyuz.bbs.content.comment.CommentMapper;
import com.leyuz.bbs.content.comment.CommentReplyMapper;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.dto.AuditContentType;
import com.leyuz.uc.user.UserApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AuditContextBuilder 单元测试：覆盖场景分支、历史合并、降级
 */
@ExtendWith(MockitoExtension.class)
class AuditContextBuilderTest {

    @Mock
    private UserApplication userApplication;
    @Mock
    private ForumApplication forumApplication;
    @Mock
    private ThreadGateway threadGateway;
    @Mock
    private CommentGateway commentGateway;
    @Mock
    private ThreadMapper threadMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CommentReplyMapper commentReplyMapper;

    @InjectMocks
    private AuditContextBuilder builder;

    @Test
    void threadScenarioOnlyFillsRecentThreads() {
        Long userId = 1L;
        when(threadGateway.listRecentThreads(userId, 5))
                .thenReturn(List.of(new ThreadHistoryItemV("T1", "概要1", LocalDateTime.now())));

        AuditContext ctx = builder.build(userId, AuditContentType.THREAD, true, 1,
                "标题", "内容", "<p>内容</p>", "127.0.0.1",
                LocalDateTime.now(), 5);

        assertNotNull(ctx.getRecentThreads());
        assertEquals(1, ctx.getRecentThreads().size());
        assertEquals("T1", ctx.getRecentThreads().get(0).getTitle());
        assertNotNull(ctx.getRecentComments());
        assertTrue(ctx.getRecentComments().isEmpty(),
                "主题帖场景下 recentComments 应为空");
        verify(threadGateway).listRecentThreads(userId, 5);
        verifyNoInteractions(commentGateway);
    }

    @Test
    void commentScenarioMergesCommentsAndRepliesByTimeDesc() {
        Long userId = 1L;
        LocalDateTime t1 = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 1, 1, 11, 0);
        LocalDateTime t3 = LocalDateTime.of(2026, 1, 1, 12, 0);
        when(commentGateway.listRecentComments(userId, 5))
                .thenReturn(List.of(
                        new CommentHistoryItemV("评论1", t1),
                        new CommentHistoryItemV("评论2", t3)));
        when(commentGateway.listRecentReplies(userId, 5))
                .thenReturn(List.of(
                        new CommentHistoryItemV("楼中楼1", t2)));

        AuditContext ctx = builder.build(userId, AuditContentType.COMMENT, true, 1,
                null, "内容", null, "127.0.0.1",
                LocalDateTime.now(), 5);

        assertTrue(ctx.getRecentThreads().isEmpty(),
                "评论场景下 recentThreads 应为空");
        assertEquals(3, ctx.getRecentComments().size());
        // 按 createTime desc 合并：t3 > t2 > t1
        assertEquals(t3, ctx.getRecentComments().get(0).getCreateTime());
        assertEquals(t2, ctx.getRecentComments().get(1).getCreateTime());
        assertEquals(t1, ctx.getRecentComments().get(2).getCreateTime());
    }

    @Test
    void replyScenarioMergesCommentsAndRepliesByTimeDesc() {
        Long userId = 1L;
        LocalDateTime t1 = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 1, 1, 10, 0);
        when(commentGateway.listRecentComments(userId, 5))
                .thenReturn(List.of(new CommentHistoryItemV("评论1", t1)));
        when(commentGateway.listRecentReplies(userId, 5))
                .thenReturn(List.of(new CommentHistoryItemV("楼中楼1", t2)));

        AuditContext ctx = builder.build(userId, AuditContentType.REPLY, true, 1,
                null, "内容", null, "127.0.0.1",
                LocalDateTime.now(), 5);

        assertTrue(ctx.getRecentThreads().isEmpty());
        assertEquals(2, ctx.getRecentComments().size());
        assertEquals(t2, ctx.getRecentComments().get(0).getCreateTime());
    }

    @Test
    void mergedListIsTruncatedToHistoryCount() {
        Long userId = 1L;
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        List<CommentHistoryItemV> comments = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            comments.add(new CommentHistoryItemV("C" + i, base.plusHours(i)));
        }
        List<CommentHistoryItemV> replies = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            replies.add(new CommentHistoryItemV("R" + i, base.plusHours(10 + i)));
        }
        when(commentGateway.listRecentComments(userId, 3)).thenReturn(comments);
        when(commentGateway.listRecentReplies(userId, 3)).thenReturn(replies);

        AuditContext ctx = builder.build(userId, AuditContentType.COMMENT, true, 1,
                null, "内容", null, "127.0.0.1",
                LocalDateTime.now(), 3);

        assertEquals(3, ctx.getRecentComments().size());
        // 应取时间最新的 3 条
        assertEquals(base.plusHours(12), ctx.getRecentComments().get(0).getCreateTime());
        assertEquals(base.plusHours(11), ctx.getRecentComments().get(1).getCreateTime());
        assertEquals(base.plusHours(10), ctx.getRecentComments().get(2).getCreateTime());
    }

    @Test
    void historyCountZeroYieldsEmptyLists() {
        AuditContext ctx = builder.build(1L, AuditContentType.THREAD, true, 1,
                "标题", "内容", null, "127.0.0.1",
                LocalDateTime.now(), 0);
        assertTrue(ctx.getRecentThreads().isEmpty());
        assertTrue(ctx.getRecentComments().isEmpty());
        verifyNoInteractions(threadGateway);
        verifyNoInteractions(commentGateway);
    }

    @Test
    void threadGatewayFailureYieldsEmptyThreadsWithoutThrowing() {
        Long userId = 1L;
        when(threadGateway.listRecentThreads(eq(userId), anyInt()))
                .thenThrow(new RuntimeException("DB down"));

        AuditContext ctx = builder.build(userId, AuditContentType.THREAD, true, 1,
                "标题", "内容", null, "127.0.0.1",
                LocalDateTime.now(), 5);

        assertNotNull(ctx.getRecentThreads());
        assertTrue(ctx.getRecentThreads().isEmpty());
    }

    @Test
    void commentGatewayPartialFailureKeepsAvailableData() {
        Long userId = 1L;
        when(commentGateway.listRecentComments(userId, 5))
                .thenReturn(List.of(new CommentHistoryItemV("C1", LocalDateTime.now())));
        when(commentGateway.listRecentReplies(userId, 5))
                .thenThrow(new RuntimeException("reply DB down"));

        AuditContext ctx = builder.build(userId, AuditContentType.COMMENT, true, 1,
                null, "内容", null, "127.0.0.1",
                LocalDateTime.now(), 5);

        assertEquals(1, ctx.getRecentComments().size());
        assertEquals("C1", ctx.getRecentComments().get(0).getContent());
    }

    @Test
    void nullUserIdSkipsHistoryQueries() throws Exception {
        AuditContext ctx = builder.build(null, AuditContentType.THREAD, true, 1,
                "标题", "内容", null, "127.0.0.1",
                LocalDateTime.now(), 5);
        assertTrue(ctx.getRecentThreads().isEmpty());
        assertTrue(ctx.getRecentComments().isEmpty());
        verifyNoInteractions(threadGateway);
        verifyNoInteractions(commentGateway);
    }

    @SuppressWarnings("unused")
    private static void assertFieldEquals(Object obj, String name, Object expected) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        assertEquals(expected, f.get(obj));
    }

    @SuppressWarnings("unused")
    private static List<?> empty() {
        return Collections.emptyList();
    }
}