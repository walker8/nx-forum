package com.leyuz.bbs.notifcation;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.domain.notification.NotificationE;
import com.leyuz.bbs.domain.notification.dataobject.NotificationExtra;
import com.leyuz.bbs.domain.notification.dataobject.NotificationStatusV;
import com.leyuz.bbs.domain.notification.dataobject.NotificationTypeV;
import com.leyuz.bbs.domain.notification.gateway.NotificationGateway;
import com.leyuz.bbs.notifcation.mybatis.INotificationService;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class NotificationGatewayImpl implements NotificationGateway {
    private final INotificationService notificationService;

    @Override
    public boolean save(NotificationE notificationE) {
        if (notificationE != null) {
            NotificationPO notificationPO = new NotificationPO();
            notificationPO.setNotificationStatus(notificationE.getNotificationStatusV().getValue());
            notificationPO.setNotificationType(notificationE.getNotificationTypeV().getValue());
            notificationPO.setCreateTime(notificationE.getCreateTime());
            notificationPO.setMessage(notificationE.getMessage());
            notificationPO.setSubject(notificationE.getSubject());
            notificationPO.setUserId(notificationE.getUserId());
            notificationPO.setPostId(notificationE.getPostId());
            notificationPO.setExtra(JSON.toJSONString(notificationE.getNotificationExtra()));
            return notificationService.save(notificationPO);
        }
        return false;
    }

    @Override
    public List<NotificationE> queryNotificationByPostId(Long threadId, NotificationTypeV notificationTypeV) {
        QueryWrapper<NotificationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", threadId).eq("notification_type", notificationTypeV.getValue());
        List<NotificationPO> notificationPOList = notificationService.list(queryWrapper);
        return notificationPOList.stream().map(this::toNotificationE).toList();
    }

    @Override
    public CustomPage<NotificationE> queryNotifications(Long userId, NotificationTypeV notificationTypeV, int pageNo, int pageSize) {
        // 创建分页对象
        Page<NotificationPO> page = new Page<>(pageNo, pageSize);
        // 执行分页查询
        QueryWrapper<NotificationPO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("notification_type", notificationTypeV.getValue());
        wrapper.orderByDesc("id");
        Page<NotificationPO> notificationPOPage = notificationService.page(page, wrapper);
        return DataBaseUtils.createCustomPage(notificationPOPage, this::toNotificationE);
    }

    @Override
    public boolean clearNotifications(Long userId, NotificationTypeV notificationTypeV) {
        UpdateWrapper<NotificationPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("notification_type", notificationTypeV.getValue())
                .set("notification_status", NotificationStatusV.READ.getValue());
        return notificationService.update(null, updateWrapper);
    }

    @Override
    public void deleteNotificationById(Long id) {
        notificationService.removeById(id);
    }

    @Override
    public List<Map<String, Object>> getUnreadCountByUserId(Long userId) {
        QueryWrapper<NotificationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("notification_type", "count(id) as unread_count")
                .groupBy("notification_type")
                .eq("user_id", userId)
                .eq("notification_status", NotificationStatusV.UNREAD.getValue());
        return notificationService.getBaseMapper().selectMaps(queryWrapper);
    }

    private NotificationE toNotificationE(NotificationPO notificationPO) {
        return NotificationE.builder()
                .id(notificationPO.getId())
                .createTime(notificationPO.getCreateTime())
                .message(notificationPO.getMessage())
                .notificationExtra(JSON.parseObject(notificationPO.getExtra(), NotificationExtra.class))
                .notificationStatusV(NotificationStatusV.of(notificationPO.getNotificationStatus()))
                .notificationTypeV(NotificationTypeV.of(notificationPO.getNotificationType()))
                .postId(notificationPO.getPostId())
                .subject(notificationPO.getSubject())
                .userId(notificationPO.getUserId())
                .build();
    }
}
