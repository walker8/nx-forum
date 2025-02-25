package com.leyuz.bbs.domain.comment.gateway;

import com.leyuz.bbs.common.dataobject.CommentOrderV;
import com.leyuz.bbs.domain.comment.CommentE;
import com.leyuz.bbs.domain.comment.CommentReplyE;
import com.leyuz.common.mybatis.CustomPage;

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

    CustomPage<CommentE> queryComments(Long threadId, CommentOrderV orderV, int pageNo, int pageSize);

    CustomPage<CommentE> queryCommentsByUserId(Long userId, int pageNo, int pageSize);

    CustomPage<CommentReplyE> queryCommentRelies(Long commentId, CommentOrderV orderV, int pageNo, int pageSize);

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

    void updateForumId(Long threadId, Integer targetForumId);

    void incrementLikeCount(Long targetId, int delta);

    void incrementReplyLikeCount(Long targetId, int delta);
}
