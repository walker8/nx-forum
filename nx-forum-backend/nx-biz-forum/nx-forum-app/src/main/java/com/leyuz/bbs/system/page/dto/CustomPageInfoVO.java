package com.leyuz.bbs.system.page.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义页面信息（包含页面名称和内容），用于前端展示和SEO
 */
@Data
@Schema(description = "自定义页面信息")
public class CustomPageInfoVO {

    @Schema(description = "页面名称")
    private String pageName;

    @Schema(description = "页面内容")
    private String content;

    @Schema(description = "布局类型 default默认 simple简洁 empty无布局")
    private String layout;
}
