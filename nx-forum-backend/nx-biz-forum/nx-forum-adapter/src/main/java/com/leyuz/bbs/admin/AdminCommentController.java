package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.comment.CommentApplication;
import com.leyuz.bbs.content.comment.dto.AdminCommentReplyVO;
import com.leyuz.bbs.content.comment.dto.AdminCommentVO;
import com.leyuz.bbs.content.comment.dto.CommentQuery;
import com.leyuz.bbs.content.comment.dto.CommentReplyQuery;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 回帖
 * </p>
 *
 * @author walker
 * @since 2024-09-01
 */
@Tag(name = "后台回帖管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/comments")
public class AdminCommentController {

    private final CommentApplication commentApplication;

    @Operation(summary = "查询帖子")
    @GetMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission(#forumId, 'admin:comment:search')")
    public SingleResponse queryCommentsByAdmin(@RequestParam(defaultValue = "0") Integer forumId,
                                               @RequestParam(defaultValue = "") String authorName,
                                               @RequestParam(defaultValue = "") String ip,
                                               @RequestParam(defaultValue = "") Long threadId,
                                               @RequestParam(defaultValue = "") Byte auditStatus,
                                               @RequestParam(defaultValue = "") Boolean deleted,
                                               @RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        CommentQuery query = CommentQuery.builder().forumId(forumId).authorName(authorName)
                .auditStatusV(AuditStatusV.of(auditStatus)).deleted(deleted)
                .threadId(threadId).ip(ip).pageNo(pageNo).pageSize(pageSize).build();
        CustomPage<AdminCommentVO> commentVOCustomPage = commentApplication.queryCommentsByAdmin(query);
        return SingleResponse.of(commentVOCustomPage);
    }

    @Operation(summary = "查询帖子评论")
    @GetMapping("/replies")
    @PreAuthorize("@forumPermissionResolver.hasPermission(#forumId, 'admin:comment:search')")
    public SingleResponse queryCommentRepliesByAdmin(@RequestParam(defaultValue = "0") Integer forumId,
                                                     @RequestParam(defaultValue = "") String authorName,
                                                     @RequestParam(defaultValue = "") String ip,
                                                     @RequestParam(defaultValue = "") Long threadId,
                                                     @RequestParam(defaultValue = "") Byte auditStatus,
                                                     @RequestParam(defaultValue = "") Boolean deleted,
                                                     @RequestParam(defaultValue = "1") int pageNo,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        CommentReplyQuery query = CommentReplyQuery.builder().forumId(forumId).authorName(authorName)
                .auditStatusV(AuditStatusV.of(auditStatus)).deleted(deleted)
                .threadId(threadId).ip(ip).pageNo(pageNo).pageSize(pageSize).build();
        CustomPage<AdminCommentReplyVO> commentReplyVOCustomPage = commentApplication.queryCommentRepliesByAdmin(query);
        return SingleResponse.of(commentReplyVOCustomPage);
    }

    @Operation(summary = "批量操作回复")
    @PutMapping("/batch/operation")
    public SingleResponse operateCommentsByAdmin(@RequestBody List<Long> commentIds,
                                                 @RequestParam(defaultValue = "0") Integer forumId,
                                                 @RequestParam(defaultValue = "") String reason,
                                                 @RequestParam(defaultValue = "") String operation,
                                                 @RequestParam(defaultValue = "") Boolean notice) {
        commentApplication.operateCommentsByAdmin(forumId, commentIds, operation, reason, notice);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "批量操作评论")
    @PutMapping("/replies/batch/operation")
    public SingleResponse operateCommentRepliesByAdmin(@RequestBody List<Long> replyIds,
                                                       @RequestParam(defaultValue = "0") Integer forumId,
                                                       @RequestParam(defaultValue = "") String reason,
                                                       @RequestParam(defaultValue = "") String operation,
                                                       @RequestParam(defaultValue = "") Boolean notice) {
        commentApplication.operateCommentRepliesByAdmin(forumId, replyIds, operation, reason, notice);
        return SingleResponse.buildSuccess();
    }

}
