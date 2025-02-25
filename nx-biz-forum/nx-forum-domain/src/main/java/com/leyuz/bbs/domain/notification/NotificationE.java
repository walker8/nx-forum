package com.leyuz.bbs.domain.notification;

import com.leyuz.bbs.domain.notification.dataobject.NotificationExtra;
import com.leyuz.bbs.domain.notification.dataobject.NotificationStatusV;
import com.leyuz.bbs.domain.notification.dataobject.NotificationTypeV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
