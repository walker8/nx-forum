package com.leyuz.bbs.system.notification;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.common.utils.HtmlUtils;
import com.leyuz.bbs.common.utils.TextUtils;
import com.leyuz.bbs.content.comment.CommentE;
import com.leyuz.bbs.content.comment.CommentReplyE;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.system.notification.dto.NotificationExtra;
import com.leyuz.bbs.system.notification.dto.NotificationVO;
import com.leyuz.bbs.system.notification.dto.constant.NotificationStatusV;
import com.leyuz.bbs.system.notification.dto.constant.NotificationTypeV;
import com.leyuz.bbs.system.notification.model.NotificationE;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.common.utils.TimeUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationApplication {
    private final NotificationMapper notificationMapper;
    private final ThreadGateway threadGateway;
    private final CommentGateway commentsGateway;
    private final UserApplication userApplication;
    private final ForumApplication forumApplication;

    public void sendNotification(NotificationE notificationE) {
        NotificationPO po = entityToPo(notificationE);
        po.setCreateTime(java.time.LocalDateTime.now());
        notificationMapper.insert(po);
    }

    /**
     * 实体转PO
     */
    private NotificationPO entityToPo(NotificationE entity) {
        NotificationPO po = new NotificationPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setPostId(entity.getPostId());
        if (entity.getNotificationTypeV() != null) {
            po.setNotificationType(entity.getNotificationTypeV().getValue());
        }
        if (entity.getNotificationStatusV() != null) {
            po.setNotificationStatus(entity.getNotificationStatusV().getValue());
        }
        po.setSubject(entity.getSubject());
        po.setMessage(entity.getMessage());
        po.setCreateTime(entity.getCreateTime());
        if (entity.getNotificationExtra() != null) {
            po.setExtra(JSONUtil.toJsonStr(entity.getNotificationExtra()));
        }
        return po;
    }

    /**
     * PO转实体
     */
    private NotificationE poToEntity(NotificationPO po) {
        if (po == null) {
            return null;
        }
        NotificationExtra extra = null;
        if (po.getExtra() != null) {
            extra = JSONUtil.toBean(po.getExtra(), NotificationExtra.class);
        }
        return NotificationE.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .postId(po.getPostId())
                .notificationTypeV(NotificationTypeV.of(po.getNotificationType()))
                .notificationStatusV(NotificationStatusV.of(po.getNotificationStatus()))
                .subject(po.getSubject())
                .message(po.getMessage())
                .createTime(po.getCreateTime())
                .notificationExtra(extra)
                .build();
    }

    public void sendSystemNotification(Long userId, String subject, String message) {
        NotificationE notificationE = NotificationE.builder()
                .notificationStatusV(NotificationStatusV.UNREAD)
                .notificationTypeV(NotificationTypeV.SYSTEM)
                .createTime(LocalDateTime.now())
                .userId(userId)
                .postId(0L)
                .subject(subject)
                .message(message)
                .notificationExtra(new NotificationExtra(null, null, null))
                .build();
        sendNotification(notificationE);
    }

    public void sendCommentNotification(CommentE commentE) {
        ThreadE threadE = threadGateway.getThreadFromCache(commentE.getThreadId());
        if (threadE != null && !threadE.getCreateBy().equals(commentE.getCreateBy())) {
            NotificationE notificationE = NotificationE.builder()
                    .notificationStatusV(NotificationStatusV.UNREAD)
                    .notificationTypeV(NotificationTypeV.REPLY)
                    .createTime(LocalDateTime.now())
                    .userId(threadE.getCreateBy())
                    .postId(commentE.getThreadId())
                    .message(HtmlUtils.convertHtmlToText(commentE.getMessageHtmlWithoutImages()))
                    .notificationExtra(new NotificationExtra(commentE.getCommentId(), null, commentE.getCreateBy()))
                    .build();
            sendNotification(notificationE);
        }
    }

    public void sendCommentReplyNotification(CommentReplyE commentReplyE) {
        CommentE commentE = commentsGateway.getCommentFromCache(commentReplyE.getCommentId());
        if (commentE == null) {
            return;
        }
        Long replyUserId = commentReplyE.getReplyUserId();
        if (replyUserId == null || replyUserId <= 0) {
            replyUserId = commentE.getCreateBy();
        }
        // 回复的是自己不发消息
        if (Objects.equals(replyUserId, commentReplyE.getCreateBy())) {
            return;
        }
        NotificationE notificationE = NotificationE.builder()
                .notificationStatusV(NotificationStatusV.UNREAD)
                .notificationTypeV(NotificationTypeV.REPLY)
                .createTime(LocalDateTime.now())
                .userId(replyUserId)
                .postId(commentReplyE.getThreadId())
                .message(HtmlUtils.convertHtmlToText(commentReplyE.getMessageHtml()))
                .notificationExtra(new NotificationExtra(commentReplyE.getCommentId(), commentReplyE.getReplyId(), commentReplyE.getCreateBy()))
                .build();
        sendNotification(notificationE);
    }

    /**
     * 评论发送@通知
     *
     * @param commentE
     */
    public void sendMentionNotification(CommentE commentE) {
        Set<String> mentions = TextUtils.getMentions(commentE.getMessage());
        for (String mention : mentions) {
            UserE userE = userApplication.getByUserNameFromCache(mention);
            if (userE == null) {
                // 当前@用户不存在
                continue;
            }
            NotificationE notificationE = NotificationE.builder()
                    .notificationStatusV(NotificationStatusV.UNREAD)
                    .notificationTypeV(NotificationTypeV.AT)
                    .createTime(LocalDateTime.now())
                    .userId(userE.getUserId())
                    .postId(commentE.getThreadId())
                    .message(HtmlUtils.convertHtmlToText(commentE.getMessageHtmlWithoutImages()))
                    .notificationExtra(new NotificationExtra(commentE.getCommentId(), null, commentE.getCreateBy()))
                    .build();
            sendNotification(notificationE);
        }
    }

    /**
     * 回复发送@通知
     *
     * @param commentReplyE
     */
    public void sendMentionNotification(CommentReplyE commentReplyE) {
        Set<String> mentions = TextUtils.getMentions(commentReplyE.getMessage());
        for (String mention : mentions) {
            UserE userE = userApplication.getByUserNameFromCache(mention);
            if (userE == null) {
                // 当前@用户不存在
                continue;
            }
            NotificationE notificationE = NotificationE.builder()
                    .notificationStatusV(NotificationStatusV.UNREAD)
                    .notificationTypeV(NotificationTypeV.AT)
                    .createTime(LocalDateTime.now())
                    .userId(userE.getUserId())
                    .postId(commentReplyE.getThreadId())
                    .message(HtmlUtils.convertHtmlToText(commentReplyE.getMessage()))
                    .notificationExtra(new NotificationExtra(commentReplyE.getCommentId(), commentReplyE.getReplyId(), commentReplyE.getCreateBy()))
                    .build();
            sendNotification(notificationE);
        }
    }

    /**
     * 主题发送@通知
     *
     * @param threadE
     */
    public void sendMentionNotification(ThreadE threadE) {
        Set<String> mentions = TextUtils.getMentions(threadE.getContent());
        // 主题帖可以反复编辑，所以需要判断是否已经提醒过
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getPostId, threadE.getThreadId())
                .eq(NotificationPO::getNotificationType, NotificationTypeV.AT.getValue());
        List<NotificationPO> notificationPOs = notificationMapper.selectList(wrapper);
        List<NotificationE> notifications = notificationPOs.stream().map(this::poToEntity).toList();
        List<NotificationE> threadMentions = notifications.stream()
                .filter(n -> n.getNotificationExtra().getCommentId() == null && n.getNotificationExtra().getReplyId() == null)
                .toList();
        List<Long> oldMentionUserIds = threadMentions.stream().map(n -> n.getUserId()).toList();
        List<Long> newMentionUserIds = new ArrayList<>();
        for (String mention : mentions) {
            UserE userE = userApplication.getByUserNameFromCache(mention);
            if (userE != null) {
                newMentionUserIds.add(userE.getUserId());
                if (!oldMentionUserIds.contains(userE.getUserId())) {
                    // 新增提醒
                    NotificationE notificationE = NotificationE.builder()
                            .notificationStatusV(NotificationStatusV.UNREAD)
                            .notificationTypeV(NotificationTypeV.AT)
                            .createTime(LocalDateTime.now())
                            .userId(userE.getUserId())
                            .postId(threadE.getThreadId())
                            .message(HtmlUtils.convertHtmlToText(threadE.getContent()))
                            .notificationExtra(new NotificationExtra(null, null, threadE.getCreateBy()))
                            .build();
                    sendNotification(notificationE);
                }
            }
        }
        // 删除不再提醒的人
        for (Long oldMentionUserId : oldMentionUserIds) {
            if (!newMentionUserIds.contains(oldMentionUserId)) {
                NotificationE notificationE = threadMentions.stream().filter(n -> n.getUserId().equals(oldMentionUserId)).findFirst().get();
                notificationMapper.deleteById(notificationE.getId());
            }
        }
    }

    public Object getNotificationCount() {
        Long userId = HeaderUtils.getUserId();
        if (userId == null || userId <= 0) {
            throw new ValidationException("用户未登录");
        }

        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getUserId, userId)
                .eq(NotificationPO::getNotificationStatus, NotificationStatusV.UNREAD.getValue());
        List<NotificationPO> unreadNotifications = notificationMapper.selectList(wrapper);

        Map<String, Long> map = new HashMap<>();
        long mentionCount = 0L;
        long replyCount = 0L;
        long systemCount = 0L;

        for (NotificationPO po : unreadNotifications) {
            NotificationTypeV type = NotificationTypeV.of(po.getNotificationType());
            switch (type) {
                case AT:
                    mentionCount++;
                    break;
                case REPLY:
                    replyCount++;
                    break;
                case SYSTEM:
                    systemCount++;
                    break;
            }
        }

        map.put("mentionCount", mentionCount);
        map.put("replyCount", replyCount);
        map.put("systemCount", systemCount);
        map.put("totalCount", mentionCount + replyCount + systemCount);
        return map;
    }

    public CustomPage<NotificationVO> queryNotifications(String type, int pageNo, int pageSize) {
        Long userId = HeaderUtils.getUserId();

        Page<NotificationPO> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getUserId, userId)
                .eq(NotificationPO::getNotificationType, NotificationTypeV.of(type).getValue())
                .orderByDesc(NotificationPO::getCreateTime);
        notificationMapper.selectPage(page, wrapper);

        return DataBaseUtils.createCustomPage(page, po -> toNotificationVO(poToEntity(po)));
    }

    private NotificationVO toNotificationVO(NotificationE notificationE) {
        NotificationVO notificationVO = new NotificationVO();
        BeanUtils.copyProperties(notificationE, notificationVO);
        notificationVO.setNotificationStatus(notificationE.getNotificationStatusV().getValue());
        notificationVO.setNotificationType(notificationE.getNotificationTypeV().getValue());
        NotificationExtra notificationExtra = notificationE.getNotificationExtra();
        Long postId = notificationVO.getPostId();
        if (postId != null && postId > 0) {
            ThreadE threadE = threadGateway.getThreadFromCache(postId);
            if (threadE == null) {
                // 找到回收站里的文章
                threadE = threadGateway.getDeletedThread(postId);
            }
            notificationVO.setPostTitle(threadE.getTitle());
            notificationVO.setForumId(threadE.getForumId());
            ForumPO forumPO = forumApplication.getForumById(threadE.getForumId());
            if (forumPO != null) {
                notificationVO.setForumName(forumPO.getName());
                notificationVO.setForumNickName(forumPO.getNickName());
            }
        }
        if (notificationExtra != null) {
            Long userId = notificationExtra.getAuthorId();
            if (userId != null && userId > 0) {
                notificationVO.setAuthorId(userId);
                notificationVO.setAuthorName(userApplication.getByIdFromCache(userId).getUserName());
            }
            Long commentId = notificationExtra.getCommentId();
            Long replyId = notificationExtra.getReplyId();
            if (replyId != null && replyId > 0) {
                notificationVO.setUrl("/c/" + commentId + "?replyId=" + replyId);
            } else if (commentId != null && commentId > 0) {
                notificationVO.setUrl("/t/" + postId + "?commentId=" + commentId);
            }
        }
        notificationVO.setCreateTime(TimeUtils.formatDateTime(notificationE.getCreateTime()));
        return notificationVO;
    }

    public void clearNotifications(String type) {
        Long userId = HeaderUtils.getUserId();
        LambdaQueryWrapper<NotificationPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationPO::getUserId, userId)
                .eq(NotificationPO::getNotificationType, NotificationTypeV.of(type).getValue());

        NotificationPO updatePO = new NotificationPO();
        updatePO.setNotificationStatus(NotificationStatusV.READ.getValue());
        notificationMapper.update(updatePO, wrapper);
    }
}
