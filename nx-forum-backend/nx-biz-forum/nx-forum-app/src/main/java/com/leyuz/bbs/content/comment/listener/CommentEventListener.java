package com.leyuz.bbs.content.comment.listener;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.event.EventType;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.event.*;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.event.ThreadTransferredEvent;
import com.leyuz.bbs.content.thread.event.dto.ThreadTransferredEventData;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.content.thread.service.ThreadDomainService;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.system.notification.NotificationApplication;
import com.leyuz.bbs.user.property.ForumUserPropertyMapper;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@RequiredArgsConstructor
public class CommentEventListener {
    private final ThreadGateway threadGateway;
    private final ThreadDomainService threadDomainService;
    private final CommentGateway commentGateway;
    private final ForumUserPropertyMapper forumUserPropertyService;
    private final NotificationApplication notificationApplication;
    private final ForumApplication forumApplication;

    @EventListener
    public void handleCommentNewEvent(CommentNewEvent event) {
        CommentE commentE = event.getCommentE();
        if (AuditStatusV.PASSED.equals(commentE.getAuditStatus())) {
            threadDomainService.handleNewComment(commentE.getThreadId(), HeaderUtils.getUserId());
            forumUserPropertyService.incrementComments(HeaderUtils.getUserId());
            notificationApplication.sendCommentNotification(commentE);
            notificationApplication.sendMentionNotification(commentE);
        }
    }

    @EventListener
    public void handleCommentReplyNewEvent(CommentReplyNewEvent event) {
        CommentReplyE commentReplyE = event.getCommentReplyE();
        if (AuditStatusV.PASSED.equals(commentReplyE.getAuditStatus())) {
            threadDomainService.handleNewComment(commentReplyE.getThreadId(), HeaderUtils.getUserId());
            notificationApplication.sendCommentReplyNotification(commentReplyE);
            notificationApplication.sendMentionNotification(commentReplyE);
        }
    }

    @EventListener
    public void handleCommentDeletedEvent(CommentDeletedEvent event) {
        CommentE commentE = event.getEventData();
        threadGateway.decreaseComments(commentE.getThreadId(), commentE.getReplyCount() + 1);
        forumUserPropertyService.decrementComments(commentE.getCreateBy());
        sendCommentSystemNotification(EventType.DELETED, commentE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentPassedEvent(CommentPassedEvent event) {
        CommentE commentE = event.getEventData();
        threadGateway.increaseComments(commentE.getThreadId(), commentE.getReplyCount() + 1);
        forumUserPropertyService.incrementComments(commentE.getCreateBy());
        notificationApplication.sendCommentNotification(commentE);
        notificationApplication.sendMentionNotification(commentE);
        sendCommentSystemNotification(EventType.PASSED, commentE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentRejectedEvent(CommentRejectedEvent event) {
        CommentE commentE = event.getEventData();
        // AI 异步复审路径：PASSED -> REJECTED，需要扣减评论数；手动拒绝路径：AUDITING -> REJECTED，无需扣减
        if (AuditStatusV.PASSED.equals(commentE.getAuditStatus())) {
            threadGateway.decreaseComments(commentE.getThreadId(), commentE.getReplyCount() + 1);
            // 该顶层评论被拒后，其下挂载的楼中楼回复也随之不可见，需同步清零 reply_count
            // 防止嵌套回复计数在 DB 与 thread.comments 之间漂移（decreaseReplies 内部已对 number<=0 短路）
            commentGateway.decreaseReplies(commentE.getCommentId(), commentE.getReplyCount());
            forumUserPropertyService.decrementComments(commentE.getCreateBy());
        }
        sendCommentSystemNotification(EventType.REJECTED, commentE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentRestoredEvent(CommentRestoredEvent event) {
        CommentE commentE = event.getEventData();
        threadGateway.increaseComments(commentE.getThreadId(), commentE.getReplyCount() + 1);
        forumUserPropertyService.incrementComments(commentE.getCreateBy());
        sendCommentSystemNotification(EventType.RESTORED, commentE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentReplyDeletedEvent(CommentReplyDeletedEvent event) {
        CommentReplyE commentReplyE = event.getEventData();
        threadGateway.decreaseComments(commentReplyE.getThreadId(), 1);
        sendCommentReplySystemNotification(EventType.DELETED, commentReplyE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentReplyPassedEvent(CommentReplyPassedEvent event) {
        CommentReplyE commentReplyE = event.getEventData();
        threadGateway.increaseComments(commentReplyE.getThreadId(), 1);
        notificationApplication.sendCommentReplyNotification(commentReplyE);
        notificationApplication.sendMentionNotification(commentReplyE);
        sendCommentReplySystemNotification(EventType.PASSED, commentReplyE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentReplyRejectedEvent(CommentReplyRejectedEvent event) {
        CommentReplyE commentReplyE = event.getEventData();
        // AI 异步复审路径：PASSED -> REJECTED，需要扣减评论数；手动拒绝路径：AUDITING -> REJECTED，无需扣减
        if (AuditStatusV.PASSED.equals(commentReplyE.getAuditStatus())) {
            threadGateway.decreaseComments(commentReplyE.getThreadId(), 1);
            // 同步扣减父评论的 reply_count，避免嵌套回复计数漂移
            commentGateway.decreaseReplies(commentReplyE.getCommentId(), 1);
        }
        sendCommentReplySystemNotification(EventType.REJECTED, commentReplyE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleCommentAiReviewEvent(CommentAiReviewEvent event) {
        CommentE commentE = event.getEventData();
        // AI 复审路径：PASSED -> AUDITING，需要扣减评论数
        threadGateway.decreaseComments(commentE.getThreadId(), commentE.getReplyCount() + 1);
        forumUserPropertyService.decrementComments(commentE.getCreateBy());
        if (event.isNotice()) {
            String brief = getShortBrief(commentE.getMessage());
            String message = MessageFormat.format(
                    "您的评论《<a href=\"/c/{0}\">{1}</a>》已被系统转交人工审核",
                    commentE.getCommentId(), brief);
            String reason = event.getReason();
            if (StringUtils.isNotEmpty(reason)) {
                message = message + "，原因：" + reason;
            }
            notificationApplication.sendSystemNotification(commentE.getCreateBy(), "评论审核通知", message);
        }
    }

    @EventListener
    public void handleCommentReplyAiReviewEvent(CommentReplyAiReviewEvent event) {
        CommentReplyE replyE = event.getEventData();
        // AI 复审路径：PASSED -> AUDITING，需要扣减评论数
        threadGateway.decreaseComments(replyE.getThreadId(), 1);
        if (event.isNotice()) {
            String brief = getShortBrief(replyE.getMessage());
            String message = MessageFormat.format(
                    "您的回复《<a href=\"/c/{0}?replyId={1}\">{2}</a>》已被系统转交人工审核",
                    replyE.getCommentId(), replyE.getReplyId(), brief);
            String reason = event.getReason();
            if (StringUtils.isNotEmpty(reason)) {
                message = message + "，原因：" + reason;
            }
            notificationApplication.sendSystemNotification(replyE.getCreateBy(), "回复审核通知", message);
        }
    }

    @EventListener
    public void handleCommentReplyRestoredEvent(CommentReplyRestoredEvent event) {
        CommentReplyE commentReplyE = event.getEventData();
        threadGateway.increaseComments(commentReplyE.getThreadId(), 1);
        sendCommentReplySystemNotification(EventType.RESTORED, commentReplyE, event.getReason(), event.isNotice());
    }

    @EventListener
    public void handleThreadTransferredEvent(ThreadTransferredEvent event) {
        ThreadTransferredEventData eventData = event.getEventData();
        commentGateway.updateForumId(eventData.getThreadId(), eventData.getTargetForumId());
        if (event.isNotice()) {
            ForumPO forumPO = forumApplication.getForumById(eventData.getTargetForumId());
            String message = MessageFormat.format("您的帖子《<a href=\"/t/{0}\">{1}</a>》已被转移至版块《{2}》",
                    eventData.getThreadId(), getSubjectOrBrief(eventData.getSubject(), eventData.getBrief()), forumPO.getNickName());
            notificationApplication.sendSystemNotification(eventData.getUserId(), "帖子转移通知", message);
        }
    }

    private String getSubjectOrBrief(String subject, String brief) {
        if (StringUtils.isBlank(subject)) {
            // 取出摘要的前80个字符
            if (StringUtils.isBlank(brief)) {
                subject = "无文字内容";
            } else if (brief.length() > 80) {
                subject = brief.substring(0, 80) + "...";
            } else {
                subject = brief;
            }
        }
        return subject;
    }

    private void sendCommentSystemNotification(EventType eventType, CommentE commentE, String reason, boolean notice) {
        if (notice) {
            // 拒绝/删除通知使用 30 字短摘要，其他通知保持 80 字
            String brief = (eventType == EventType.REJECTED)
                    ? getShortBrief(commentE.getMessage())
                    : getBrief(commentE.getMessage());
            String pattern = "您的评论《<a href=\"/c/{0}\">{1}</a>》";
            String subject = "";
            switch (eventType) {
                case DELETED:
                    pattern += "已被删除";
                    subject = "评论删除通知";
                    break;
                case PASSED:
                    pattern += "已审核通过";
                    subject = "评论审核通知";
                    break;
                case REJECTED:
                    pattern += "已被审核拒绝";
                    subject = "评论审核通知";
                    break;
                case RESTORED:
                    pattern += "已被恢复";
                    subject = "评论恢复通知";
                    break;
            }
            String message = MessageFormat.format(pattern, commentE.getCommentId(), brief);
            if (StringUtils.isNotEmpty(reason)) {
                message = message + "，原因：" + reason;
            }
            notificationApplication.sendSystemNotification(commentE.getCreateBy(), subject, message);
        }
    }

    private void sendCommentReplySystemNotification(EventType eventType, CommentReplyE commentReplyE, String reason, boolean notice) {
        if (notice) {
            // 拒绝/删除通知使用 30 字短摘要，其他通知保持 80 字
            String brief = (eventType == EventType.REJECTED)
                    ? getShortBrief(commentReplyE.getMessage())
                    : getBrief(commentReplyE.getMessage());
            String pattern = "您的回复《<a href=\"/c/{0}?replyId={1}\">{2}</a>》";
            String subject = "";
            switch (eventType) {
                case DELETED:
                    pattern += "已被删除";
                    subject = "回复删除通知";
                    break;
                case PASSED:
                    pattern += "已审核通过";
                    subject = "回复审核通知";
                    break;
                case REJECTED:
                    pattern += "已被审核拒绝";
                    subject = "回复审核通知";
                    break;
                case RESTORED:
                    pattern += "已被恢复";
                    subject = "回复恢复通知";
                    break;
            }
            String message = MessageFormat.format(pattern, commentReplyE.getCommentId(), commentReplyE.getReplyId(), brief);
            if (StringUtils.isNotEmpty(reason)) {
                message = message + "，原因：" + reason;
            }
            notificationApplication.sendSystemNotification(commentReplyE.getCreateBy(), subject, message);
        }
    }

    private String getBrief(String message) {
        return truncateBrief(message, 80);
    }

    /**
     * 截取短摘要（AI 拒绝/转人工通知专用，限 30 字 + 省略号）
     */
    private String getShortBrief(String message) {
        return truncateBrief(message, 30);
    }

    private String truncateBrief(String message, int maxLen) {
        if (StringUtils.isBlank(message)) {
            return "无文字内容";
        } else if (message.length() > maxLen) {
            return message.substring(0, maxLen) + "...";
        } else {
            return message;
        }
    }
}
