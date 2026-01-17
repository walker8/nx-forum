package com.leyuz.bbs.content.thread;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author walker
 * @since 2024-03-31
 */
@TableName("bbs_threads")
@Data
public class ThreadPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -6284217858797273049L;
    /**
     * 主题id （唯一）
     */
    @TableId(value = "thread_id", type = IdType.AUTO)
    private Long threadId;

    /**
     * 版块ID
     */
    private Integer forumId;

    /**
     * 分类 id 0 是未分类
     */
    private Integer categoryId;

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
     * 主题名称
     */
    private String subject;

    /**
     * 主题简介，自动提取自主题内容
     */
    private String brief;

    /**
     * 类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown
     */
    private Byte docType;

    /**
     * 查看次数
     */
    private Integer views;

    /**
     * 评论数
     */
    private Integer comments;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 不喜欢数
     */
    private Integer dislikes;

    /**
     * 收藏数
     */
    private Integer collections;

    /**
     * 0 审核通过 1 审核中 2 审核拒绝
     */
    private Byte auditStatus;

    /**
     * 审核原因
     */
    private String auditReason;

    /**
     * 默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序
     */
    private Byte commentOrder;

    /**
     * 最近参与的用户（最后回帖的用户名）
     */
    private Long lastCommentUserId;

    /**
     * 最后回复时间
     */
    private LocalDateTime lastCommentTime;

    /**
     * 正文中的图片，分隔符为#
     */
    private String images;

    /**
     * 正文中的图片数量
     */
    private Integer imageCount;

    /**
     * 主题扩展属性
     */
    private String property;
}
