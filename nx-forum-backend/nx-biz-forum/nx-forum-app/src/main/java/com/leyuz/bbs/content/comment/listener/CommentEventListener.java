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
        sendCommentSystemNotification(EventType.REJECTED, event.getEventData(), event.getReason(), event.isNotice());
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
        sendCommentReplySystemNotification(EventType.REJECTED, event.getEventData(), event.getReason(), event.isNotice());
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
            String brief = getBrief(commentE.getMessage());
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
            String brief = getBrief(commentReplyE.getMessage());
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
        // 取出摘要的前80个字符
        if (StringUtils.isBlank(message)) {
            return "无文字内容";
        } else if (message.length() > 80) {
            return message.substring(0, 80) + "...";
        } else {
            return message;
        }
    }
}
