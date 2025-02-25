package com.leyuz.bbs.system.notification.dto;

import com.alibaba.cola.dto.DTO;
import lombok.Data;

import java.io.Serial;

@Data
public class NotificationVO extends DTO {
    @Serial
    private static final long serialVersionUID = 3214366716696557616L;
    /**
     * 同主题id，方便索引
     */
    private Long postId;
    private String postTitle;

    /**
     * 所属板块信息
     */
    private Integer forumId;
    private String forumName;
    private String forumNickName;

    /**
     * 通知类型（如：回复、点赞、系统消息等）
     */
    private Integer notificationType;

    /**
     * 通知的状态（如：未读、已读等）
     */
    private Integer notificationStatus;

    /**
     * 通知的标题
     */
    private String subject;

    /**
     * 通知的详细内容
     */
    private String message;

    /**
     * 通知的链接
     */
    private String url;

    /**
     * 通知的作者
     */
    private Long authorId;
    private String authorName;

    /**
     * 创建时间
     */
    private String createTime;
}
