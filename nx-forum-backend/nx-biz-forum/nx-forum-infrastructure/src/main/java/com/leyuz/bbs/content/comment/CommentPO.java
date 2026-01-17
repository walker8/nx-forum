package com.leyuz.bbs.content.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 * 评论
 * </p>
 *
 * @author walker
 * @since 2024-07-28
 */
@TableName("bbs_comments")
@Data
public class CommentPO extends BaseEntity {
    @Serial
    private static final long serialVersionUID = -4291760500152991573L;
    /**
     * 回复id （唯一）
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 主题id（和bbs_threads表一致）
     */
    private Long threadId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 发帖时用户ip地址
     */
    private String userIp;

    /**
     * 类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown
     */
    private Byte docType;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 楼中楼数 0为没有
     */
    private Integer replyCount;

    /**
     * 内容，用户提示的原始数据
     */
    private String message;

    /**
     * 正文中的图片，分隔符为,
     */
    private String images;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    private Byte auditStatus;

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
}
