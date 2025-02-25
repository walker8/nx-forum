package com.leyuz.bbs.content.comment.service;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.comment.BaseComment;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.event.*;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CommentDomainService {
    private final CommentGateway commentGateway;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public void saveComment(CommentE commentE) {
        commentE.create();
        commentGateway.saveComment(commentE);
        // 发布评论事件
        eventPublisher.publishEvent(new CommentNewEvent(this, commentE));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveCommentReply(CommentReplyE commentReplyE) {
        commentGateway.saveCommentReply(commentReplyE);
        if (AuditStatusV.PASSED.equals(commentReplyE.getAuditStatus())) {
            // 更新评论回复数
            commentGateway.increaseReplies(commentReplyE.getCommentId(), 1);
        }
        // 发布评论回复事件
        eventPublisher.publishEvent(new CommentReplyNewEvent(this, commentReplyE));
    }

    public void deleteComments(Integer forumId, List<Long> commentIds, String reason, Boolean notice) {
        operate(forumId, commentIds, commentGateway::getComment, commentE -> {
            if (AuditStatusV.PASSED.equals(commentE.getAuditStatus())) {
                // 已通过的回复才能删除
                boolean deleted = commentGateway.deleteComment(commentE.getCommentId());
                if (deleted) {
                    eventPublisher.publishEvent(new CommentDeletedEvent(this, commentE, reason, notice));
                }
            }
        });
    }

    public void passComments(Integer forumId, List<Long> commentIds, Boolean notice) {
        operate(forumId, commentIds, commentGateway::getComment, commentE -> {
            if (AuditStatusV.AUDITING.equals(commentE.getAuditStatus())) {
                // 待审核的回复才能通过
                boolean passed = commentGateway.passComment(commentE.getCommentId());
                if (passed) {
                    eventPublisher.publishEvent(new CommentPassedEvent(this, commentE, notice));
                }
            }
        });
    }

    public void rejectComments(Integer forumId, List<Long> commentIds, String reason, Boolean notice) {
        operate(forumId, commentIds, commentGateway::getComment, commentE -> {
            if (AuditStatusV.AUDITING.equals(commentE.getAuditStatus())) {
                // 待审核的回复才能被拒绝
                boolean rejected = commentGateway.rejectComment(commentE.getCommentId(), reason);
                if (rejected) {
                    eventPublisher.publishEvent(new CommentRejectedEvent(this, commentE, reason, notice));
                }
            }
        });
    }

    public void restoreComments(Integer forumId, List<Long> commentIds, Boolean notice) {
        if (CollectionUtils.isEmpty(commentIds)) {
            return;
        }
        if (forumId == null) {
            forumId = 0;
        }
        for (Long commentId : commentIds) {
            CommentE commentE = commentGateway.getDeletedComment(commentId);
            if (commentE != null) {
                if (forumId <= 0 || commentE.getForumId().equals(forumId)) {
                    // 已删除的回复才能被恢复
                    boolean restored = commentGateway.restoreComment(commentE.getCommentId());
                    if (restored) {
                        eventPublisher.publishEvent(new CommentRestoredEvent(this, commentE, notice));
                    }
                }
            }
        }
    }

    public void deleteCommentReplies(Integer forumId, List<Long> replyIds, String reason, Boolean notice) {
        operate(forumId, replyIds, commentGateway::getCommentReply, commentReplyE -> {
            if (AuditStatusV.PASSED.equals(commentReplyE.getAuditStatus())) {
                // 已通过的评论才能删除
                boolean deleted = commentGateway.deleteCommentReply(commentReplyE.getReplyId());
                if (deleted) {
                    // 更新评论回复数
                    commentGateway.decreaseReplies(commentReplyE.getCommentId(), 1);
                    eventPublisher.publishEvent(new CommentReplyDeletedEvent(this, commentReplyE, reason, notice));
                }
            }
        });
    }

    public void passCommentReplies(Integer forumId, List<Long> replyIds, Boolean notice) {
        operate(forumId, replyIds, commentGateway::getCommentReply, commentReplyE -> {
            if (AuditStatusV.AUDITING.equals(commentReplyE.getAuditStatus())) {
                // 待审核的评论才能通过
                boolean passed = commentGateway.passCommentReply(commentReplyE.getReplyId());
                if (passed) {
                    // 更新评论回复数
                    commentGateway.increaseReplies(commentReplyE.getCommentId(), 1);
                    eventPublisher.publishEvent(new CommentReplyPassedEvent(this, commentReplyE, notice));
                }
            }
        });
    }

    public void rejectCommentReplies(Integer forumId, List<Long> replyIds, String reason, Boolean notice) {
        operate(forumId, replyIds, commentGateway::getCommentReply, commentReplyE -> {
            if (AuditStatusV.AUDITING.equals(commentReplyE.getAuditStatus())) {
                // 待审核的评论才能被拒绝
                boolean rejected = commentGateway.rejectCommentReply(commentReplyE.getReplyId(), reason);
                if (rejected) {
                    eventPublisher.publishEvent(new CommentReplyRejectedEvent(this, commentReplyE, reason, notice));
                }
            }
        });
    }

    public void restoreCommentReplies(Integer forumId, List<Long> replyIds, Boolean notice) {
        if (CollectionUtils.isEmpty(replyIds)) {
            return;
        }
        if (forumId == null) {
            forumId = 0;
        }
        for (Long replyId : replyIds) {
            CommentReplyE commentReplyE = commentGateway.getDeletedCommentReply(replyId);
            if (commentReplyE != null) {
                if (forumId <= 0 || commentReplyE.getForumId().equals(forumId)) {
                    // 已删除的评论才能被恢复
                    boolean restored = commentGateway.restoreCommentReply(commentReplyE.getReplyId());
                    if (restored) {
                        // 更新评论回复数
                        commentGateway.increaseReplies(commentReplyE.getCommentId(), 1);
                        eventPublisher.publishEvent(new CommentReplyRestoredEvent(this, commentReplyE, notice));
                    }
                }
            }
        }
    }

    private <T extends BaseComment> void operate(Integer forumId, List<Long> ids,
                                                 Function<Long, T> getDataFunction, Consumer<T> consumer) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        if (forumId == null) {
            forumId = 0;
        }
        for (Long id : ids) {
            T t = getDataFunction.apply(id);
            if (t != null) {
                if (forumId <= 0 || t.getForumId().equals(forumId)) {
                    consumer.accept(t);
                }
            }
        }
    }
}
