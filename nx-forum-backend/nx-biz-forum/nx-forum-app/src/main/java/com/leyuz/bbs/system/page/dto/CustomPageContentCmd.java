package com.leyuz.bbs.system.page.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomPageContentCmd {
    @NotNull(message = "页面ID不能为空", groups = UpdateGroup.class)
    private Long pageId;

    @NotBlank(message = "页面名称不能为空")
    @Size(max = 50, message = "页面名称最长50个字符")
    private String pageName;

    @NotBlank(message = "页面编码不能为空")
    @Pattern(regexp = "^[a-z0-9-]{4,50}$", message = "页面编码格式不正确（小写字母、数字、短横线，4-50位）")
    private String pageCode;

    @NotNull(message = "页面状态不能为空")
    private Integer pageStatus;

    @NotNull(message = "访问权限不能为空")
    private Integer accessLevel;

    // 验证分组
    public interface UpdateGroup {
    }
} 