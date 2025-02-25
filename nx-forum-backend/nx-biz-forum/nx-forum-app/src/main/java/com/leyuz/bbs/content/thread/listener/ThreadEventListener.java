package com.leyuz.bbs.content.thread.listener;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.event.*;
import com.leyuz.bbs.system.notification.NotificationApplication;
import com.leyuz.bbs.user.property.ForumUserPropertyMapper;
import com.leyuz.common.utils.HeaderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@RequiredArgsConstructor
@Slf4j
public class ThreadEventListener {
    private final NotificationApplication notificationApplication;
    private final ForumUserPropertyMapper forumUserPropertyMapper;

    @EventListener
    public void handleThreadNewEvent(ThreadNewEvent event) {
        ThreadE threadE = event.getEventData();
        if (AuditStatusV.PASSED.equals(threadE.getAuditStatus())) {
            forumUserPropertyMapper.incrementThreads(HeaderUtils.getUserId());
            notificationApplication.sendMentionNotification(threadE);
        }
    }

    @EventListener
    public void handleThreadUpdateEvent(ThreadUpdateEvent event) {
        ThreadE threadE = event.getEventData();
        if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus())) {
            forumUserPropertyMapper.decrementThreads(threadE.getCreateBy());
        } else if (AuditStatusV.PASSED.equals(threadE.getAuditStatus())) {
            notificationApplication.sendMentionNotification(threadE);
        }
    }

    @EventListener
    public void handleThreadDeletedEvent(ThreadDeletedEvent event) {
        ThreadE threadE = event.getEventData();
        if (event.isNotice()) {
            String subject = getSubjectOrBrief(threadE.getSubject(), threadE.getBrief());
            String message = MessageFormat.format("您的帖子《<a href=\"/t/{0}\">{1}</a>》已被删除", threadE.getThreadId(), subject);
            String reason = event.getReason();
            if (StringUtils.isNotEmpty(reason)) {
                message = message + "，原因：" + reason;
            }
            notificationApplication.sendSystemNotification(threadE.getCreateBy(), "帖子删除通知", message);
        }
        forumUserPropertyMapper.decrementThreads(threadE.getCreateBy());
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

    @EventListener
    public void handleThreadRejectedEvent(ThreadRejectedEvent event) {
        ThreadE threadE = event.getEventData();
        if (event.isNotice()) {
            String subject = getSubjectOrBrief(threadE.getSubject(), threadE.getBrief());
            String message = MessageFormat.format("您的帖子《<a href=\"/t/{0}\">{1}</a>》已被审核拒绝", threadE.getThreadId(), subject);
            String reason = event.getReason();
            if (StringUtils.isNotEmpty(reason)) {
                message = message + "，原因：" + reason;
            }
            notificationApplication.sendSystemNotification(threadE.getCreateBy(), "帖子审核通知", message);
        }
    }

    @EventListener
    public void handleThreadPassedEvent(ThreadPassedEvent event) {
        ThreadE threadE = event.getEventData();
        forumUserPropertyMapper.incrementThreads(threadE.getCreateBy());
        notificationApplication.sendMentionNotification(threadE);
        if (event.isNotice()) {
            String subject = getSubjectOrBrief(threadE.getSubject(), threadE.getBrief());
            String message = MessageFormat.format("您的帖子《<a href=\"/t/{0}\">{1}</a>》已审核通过", threadE.getThreadId(), subject);
            notificationApplication.sendSystemNotification(threadE.getCreateBy(), "帖子审核通知", message);
        }
    }

    @EventListener
    public void handleThreadRestoredEvent(ThreadRestoredEvent event) {
        ThreadE threadE = event.getEventData();
        forumUserPropertyMapper.incrementThreads(threadE.getCreateBy());
        if (event.isNotice()) {
            String subject = getSubjectOrBrief(threadE.getSubject(), threadE.getBrief());
            String message = MessageFormat.format("您的帖子《<a href=\"/t/{0}\">{1}</a>》已被恢复", threadE.getThreadId(), subject);
            notificationApplication.sendSystemNotification(threadE.getCreateBy(), "帖子恢复通知", message);
        }
    }
}
