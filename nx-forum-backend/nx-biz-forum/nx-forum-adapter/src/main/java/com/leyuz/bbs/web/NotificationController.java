package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.notification.NotificationApplication;
import com.leyuz.bbs.system.notification.dto.NotificationVO;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 消息通知中心
 * </p>
 *
 * @author walker
 * @since 2024-10-20
 */
@Tag(name = "消息通知中心")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/notifications")
public class NotificationController {
    private final NotificationApplication notificationApplication;

    @Operation(summary = "查询当前用户的通知数量")
    @GetMapping("/count")
    public SingleResponse getNotificationCount() {
        return SingleResponse.of(notificationApplication.getNotificationCount());
    }

    @Operation(summary = "查询帖子")
    @GetMapping("")
    public SingleResponse queryNotifications(@RequestParam(defaultValue = "reply") String type,
                                             @RequestParam(defaultValue = "1") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize) {
        CustomPage<NotificationVO> notificationRespPage = notificationApplication.queryNotifications(type, pageNo, pageSize);
        return SingleResponse.of(notificationRespPage);
    }

    @Operation(summary = "清除当前用户的通知")
    @PutMapping("/clear")
    public SingleResponse clearNotifications(@RequestParam(defaultValue = "reply") String type) {
        notificationApplication.clearNotifications(type);
        return SingleResponse.buildSuccess();
    }

}
