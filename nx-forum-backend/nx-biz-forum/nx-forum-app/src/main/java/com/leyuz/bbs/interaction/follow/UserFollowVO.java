package com.leyuz.bbs.interaction.follow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注视图对象
 *
 * @author walker
 * @since 2025-03-01
 */
@Data
@Schema(description = "用户关注信息")
public class UserFollowVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户简介")
    private String intro;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关注时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime followTime;

    @Schema(description = "当前用户是否已关注")
    private Boolean followed = false;
} 