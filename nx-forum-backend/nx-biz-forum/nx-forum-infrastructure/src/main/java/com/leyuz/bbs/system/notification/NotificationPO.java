package com.leyuz.bbs.system.notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author walker
 * @since 2024-09-18
 */
@TableName("bbs_notifications")
@Data
public class NotificationPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收通知的用户ID
     */
    private Long userId;

    /**
     * 同主题id，方便索引
     */
    private Long postId;

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
     * 额外的数据，可以用来存储JSON格式的数据，例如发送者的UID、关联的帖子ID等
     */
    private String extra;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
