package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.content.comment.CommentApplication;
import com.leyuz.bbs.content.comment.dto.CommentCmd;
import com.leyuz.bbs.content.comment.dto.CommentReplyCmd;
import com.leyuz.bbs.content.comment.dto.CommentReplyVO;
import com.leyuz.bbs.content.comment.dto.CommentVO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.ratelimit.annotation.LimitType;
import com.leyuz.ratelimit.annotation.RateLimitRule;
import com.leyuz.ratelimit.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 评论 前端控制器
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
@Tag(name = "评论管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/comments")
public class CommentController {
    
    private final CommentApplication commentApplication;

    @Operation(summary = "新增评论")
    @PostMapping("")
    @RateLimiter(rules = {
            @RateLimitRule(key = "comment-1", time = 5, count = 1, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID),
            @RateLimitRule(key = "comment-2", time = 60, count = 3, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID)
    })
    public SingleResponse createComment(@RequestBody CommentCmd commentCmd) {
        commentApplication.createComment(commentCmd.getThreadId(), commentCmd);
        return SingleResponse.of("回帖成功！");
    }

    @Operation(summary = "查询帖子下的评论")
    @GetMapping("")
    @PermitAll
    public SingleResponse queryComments(@RequestParam Long threadId,
                                        @RequestParam(required = false) Long commentId,
                                        @RequestParam(defaultValue = "0") int order,
                                        @RequestParam(defaultValue = "1") int pageNo,
                                        @RequestParam(defaultValue = "10") int pageSize) {
        CustomPage<CommentVO> comments = commentApplication.queryComments(threadId, commentId, order, pageNo, pageSize);
        return SingleResponse.of(comments);
    }

    @Operation(summary = "查询评论详情")
    @GetMapping("/{commentId}")
    @PermitAll
    public SingleResponse getCommentVOById(@PathVariable("commentId") Long commentId, @RequestParam(required = false) Long replyId) {
        CommentVO commentVO = commentApplication.getCommentVOById(commentId, replyId);
        return SingleResponse.of(commentVO);
    }

    @Operation(summary = "查询评论下的回复")
    @GetMapping("/{commentId}/replies")
    @PermitAll
    public SingleResponse queryCommentReplies(@PathVariable("commentId") Long commentId,
                                              @RequestParam(required = false) Long replyId,
                                              @RequestParam(defaultValue = "0") int order,
                                              @RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        CustomPage<CommentReplyVO> commentReply = commentApplication.queryCommentReplies(commentId, replyId, order, pageNo, pageSize);
        return SingleResponse.of(commentReply);
    }

    @Operation(summary = "新增评论的回复")
    @PostMapping("/{commentId}/replies")
    @RateLimiter(rules = {
            @RateLimitRule(key = "comment-1", time = 5, count = 1, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID),
            @RateLimitRule(key = "comment-2", time = 60, count = 3, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID)
    })
    public SingleResponse createCommentReply(@PathVariable("commentId") Long commentId, @RequestBody CommentReplyCmd commentReplyCmd) {
        commentApplication.createCommentReply(commentId, commentReplyCmd);
        return SingleResponse.of("评论回复成功！");
    }
}
