package com.leyuz.bbs.system.page.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "自定义页面视图对象")
public class CustomPageVO {
    @Schema(description = "页面ID")
    private Long pageId;

    @Schema(description = "页面名称")
    private String pageName;

    @Schema(description = "页面编码")
    private String pageCode;

    @Schema(description = "页面状态 0启用 1停用")
    private Integer pageStatus;

    @Schema(description = "访问权限 0公开 1登录用户 2指定角色")
    private Integer accessLevel;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "最新内容版本")
    private String content;
} 