package com.leyuz.bbs.content.comment.gateway;

import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.common.mybatis.CustomPage;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentGateway {

    void saveComment(CommentE commentE);

    CommentE getComment(Long commentId);

    CommentE getCommentFromCache(Long commentId);

    CommentE getDeletedComment(Long commentId);

    CommentReplyE getCommentReply(Long replyId);

    CommentReplyE getDeletedCommentReply(Long replyId);

    void saveCommentReply(CommentReplyE commentReplyE);

    boolean increaseReplies(Long commentId, int number);

    boolean decreaseReplies(Long commentId, int number);

    /**
     * 查询帖子下的评论，如果指定了commentId，则优先查询该评论
     *
     * @param threadId  帖子ID
     * @param commentId 指定的评论ID，可以为null
     * @param orderV    排序方式
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 评论列表分页结果
     */
    CustomPage<CommentE> queryComments(Long threadId, Long commentId, CommentOrderV orderV, int pageNo, int pageSize);

    CustomPage<CommentE> queryCommentsByUserId(Long userId, int pageNo, int pageSize);

    /**
     * 查询评论下的回复，如果指定了replyId，则优先查询该回复
     *
     * @param commentId 评论ID
     * @param replyId   指定的回复ID，可以为null
     * @param orderV    排序方式
     * @param pageNo    页码
     * @param pageSize  每页大小
     * @return 回复列表分页结果
     */
    CustomPage<CommentReplyE> queryCommentRelies(Long commentId, Long replyId, CommentOrderV orderV, int pageNo, int pageSize);

    long getCommentAuditingCount(Integer forumId);

    long getReplyAuditingCount(Integer forumId);

    boolean deleteComment(Long commentId);

    boolean deleteCommentReply(Long replyId);

    boolean restoreComment(Long commentId);

    boolean restoreCommentReply(Long replyId);

    boolean passComment(Long commentId);

    boolean passCommentReply(Long replyId);

    boolean rejectComment(Long commentId, String reason);

    boolean rejectCommentReply(Long replyId, String reason);

    /**
     * 仅更新评论审核原因（保持审核中状态）
     */
    boolean updateCommentAuditReason(Long commentId, String reason);

    /**
     * 仅更新楼中楼审核原因（保持审核中状态）
     */
    boolean updateCommentReplyAuditReason(Long replyId, String reason);

    /**
     * 将评论从 PASSED 退回审核中（AI 存疑时使用），更新状态和原因
     */
    boolean revertCommentToAuditing(Long commentId, String reason);

    /**
     * 将楼中楼回复从 PASSED 退回审核中（AI 存疑时使用），更新状态和原因
     */
    boolean revertCommentReplyToAuditing(Long replyId, String reason);

    void updateForumId(Long threadId, Integer targetForumId);

    void incrementLikeCount(Long targetId, int delta);

    void incrementReplyLikeCount(Long targetId, int delta);

    /**
     * Count total comments (approved, not deleted)
     */
    Long countTotalComments();

    /**
     * Count comments created between start and end date
     */
    Long countCommentsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 统计指定时间段内创建的评论和楼中楼数量（按终端和平台）
     *
     * @param startDate     开始时间
     * @param endDate       结束时间
     * @param terminalType  终端类型（PC/MOBILE/APP/ALL）
     * @param platform      平台（Windows/Mac/Android/iPhone/ALL等）
     * @return 评论数量（包含楼中楼）
     */
    Long countCommentsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
                                     String terminalType, String platform);

    /**
     * 获取用户最近的顶层评论（按评论时间倒序，取 limit 条）
     *
     * @param userId 用户 ID
     * @param limit  最大条数；<=0 返回空列表
     * @return 历史评论条目列表
     */
    List<CommentHistoryItemV> listRecentComments(Long userId, int limit);

    /**
     * 获取用户最近的楼中楼回复（按回复时间倒序，取 limit 条）
     *
     * @param userId 用户 ID
     * @param limit  最大条数；<=0 返回空列表
     * @return 历史楼中楼条目列表
     */
    List<CommentHistoryItemV> listRecentReplies(Long userId, int limit);
}
