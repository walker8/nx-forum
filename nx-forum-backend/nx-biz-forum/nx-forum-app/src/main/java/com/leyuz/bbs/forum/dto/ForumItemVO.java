package com.leyuz.bbs.forum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForumItemVO {
    /**
     * 论坛ID
     */
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    /**
     * 是否是默认版块
     */
    private boolean defaultForum;
}
