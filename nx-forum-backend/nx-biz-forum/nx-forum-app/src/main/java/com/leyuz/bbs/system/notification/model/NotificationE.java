package com.leyuz.bbs.system.notification.model;

import com.leyuz.bbs.system.notification.dto.NotificationExtra;
import com.leyuz.bbs.system.notification.dto.constant.NotificationStatusV;
import com.leyuz.bbs.system.notification.dto.constant.NotificationTypeV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体模型
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationE {
    private Long id;
    private Long postId;
    private NotificationStatusV notificationStatusV;
    private NotificationTypeV notificationTypeV;
    private LocalDateTime createTime;
    private String subject;
    private String message;
    /**
     * 被通知人的id
     */
    private Long userId;
    private NotificationExtra notificationExtra;
}

