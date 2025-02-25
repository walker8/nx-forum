package com.leyuz.bbs.system.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 通知额外数据对象
 */
@Data
@AllArgsConstructor
public class NotificationExtra {
    private Long commentId;
    private Long replyId;
    private Long authorId;
}

