package com.leyuz.bbs.forum;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyuz.common.dto.BaseEntity;
import lombok.Data;

import java.io.Serial;

/**
 * <p>
 *
 * </p>
 *
 * @author walker
 * @since 2024-08-07
 */
@TableName("bbs_forums")
@Data
public class ForumPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -8995169823160307525L;
    /**
     * 论坛ID
     */
    @TableId(value = "forum_id", type = IdType.AUTO)
    private Integer forumId;

    /**
     * 论坛号，只能用英文或数字
     */
    private String name;

    /**
     * 论坛名称
     */
    private String nickName;

    /**
     * 论坛20字短介绍 纯文本
     */
    private String shortBrief;

    /**
     * 论坛简介 允许HTML
     */
    private String brief;

    /**
     * 访问控制 0 开放 1 关闭
     */
    private Boolean forumAccess;

    /**
     * 论坛图标
     */
    private String iconName;

    /**
     * SEO 标题，如果设置会代替版块名称
     */
    private String seoTitle;

    /**
     * SEO关键字，不要超过6个，关键字之间用英文的,隔开
     */
    private String seoKeywords;

    /**
     * SEO content
     */
    private String seoContent;

    /**
     * SEO帖子后缀，保留
     */
    private String seoExt;

    /**
     * 论坛背景图片
     */
    private String backgroundImage;

    /**
     * 菜单栏是否显示（1显示 0不显示）
     */
    private Boolean showMenu;

    /**
     * 是否是系统内置
     */
    private Boolean isSystem;

    /**
     * 菜单栏排序
     */
    private Integer menuOrder;
}
