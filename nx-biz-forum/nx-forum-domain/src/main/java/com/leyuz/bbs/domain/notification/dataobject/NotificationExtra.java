package com.leyuz.bbs.domain.notification.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationExtra {
    private Long commentId;
    private Long replyId;
    private Long authorId;
}
