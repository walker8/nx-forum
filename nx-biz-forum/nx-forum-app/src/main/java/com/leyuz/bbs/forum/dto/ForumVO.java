package com.leyuz.bbs.forum.dto;

import lombok.Data;

@Data
public class ForumVO {
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
     * 主题数
     */
    private Integer threadCount;

    /**
     * 关注人数
     */
    private Integer followCount;

    /**
     * 论坛20字短介绍 纯文本
     */
    private String shortBrief;

    /**
     * 论坛简介 允许HTML
     */
    private String brief;

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
     * 论坛背景图片
     */
    private String backgroundImage;
}
