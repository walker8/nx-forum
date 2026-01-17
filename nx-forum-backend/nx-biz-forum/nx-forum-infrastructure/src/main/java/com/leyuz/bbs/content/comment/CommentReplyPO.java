package com.leyuz.bbs.content.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 * 楼中楼评论
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
@TableName("bbs_comment_replies")
@Data
public class CommentReplyPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 8814738144444904440L;
    /**
     * 楼中楼帖子id (唯一)
     */
    @TableId(value = "reply_id", type = IdType.AUTO)
    private Long replyId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 回复id （bbs_comments的主键id）
     */
    private Long commentId;

    /**
     * 帖子id
     */
    private Long threadId;

    /**
     * 回复的用户id
     */
    private Long replyUserId;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 楼中楼内容，不超过500个字符，纯文本
     */
    private String message;

    /**
     * 发帖时用户ip地址
     */
    private String userIp;

    /**
     * 客户端useragent
     */
    private String userAgent;

    /**
     * 终端类型（PC/MOBILE/APP）
     */
    private String terminalType;

    /**
     * 平台（Windows/Mac/Android/iPhone等）
     */
    private String platform;

    /**
     * 审核原因
     */
    private String auditReason;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    private Byte auditStatus;
}
