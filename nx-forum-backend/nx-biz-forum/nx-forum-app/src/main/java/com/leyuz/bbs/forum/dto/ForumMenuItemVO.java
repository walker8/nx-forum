package com.leyuz.bbs.forum.dto;

import lombok.Data;

@Data
public class ForumMenuItemVO {
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
     * 论坛图标
     */
    private String iconName;

    /**
     * 论坛20字短介绍 纯文本
     */
    private String shortBrief;
}
