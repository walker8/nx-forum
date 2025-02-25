package com.leyuz.bbs.forum.dto;

import lombok.Data;

@Data
public class UserForumVO {
    private Integer forumId;
    /**
     * 主题数
     */
    private Integer threadCount;

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
     * 论坛图标
     */
    private String iconName;

    /**
     * 是否是版块管理员
     */
    private Boolean isAdmin = false;
}
