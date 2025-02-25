package com.leyuz.bbs.domain.notification.gateway;

import com.leyuz.bbs.domain.notification.NotificationE;
import com.leyuz.bbs.domain.notification.dataobject.NotificationTypeV;
import com.leyuz.common.mybatis.CustomPage;

import java.util.List;
import java.util.Map;

public interface NotificationGateway {
    boolean save(NotificationE notificationE);

    List<NotificationE> queryNotificationByPostId(Long threadId, NotificationTypeV notificationTypeV);

    List<Map<String, Object>> getUnreadCountByUserId(Long userId);

    void deleteNotificationById(Long id);

    CustomPage<NotificationE> queryNotifications(Long userId, NotificationTypeV notificationTypeV, int pageNo, int pageSize);

    boolean clearNotifications(Long userId, NotificationTypeV of);
}
