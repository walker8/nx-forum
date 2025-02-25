package com.leyuz.uc.log.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户日志展示对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "用户日志展示对象")
public class UserLogVO {

    @Schema(description = "日志ID")
    private Long logId;

    @Schema(description = "日志类型（1登录 2注册 3信息修改 4密码修改）")
    private Integer logType;

    @Schema(description = "日志类型描述")
    private String logTypeDesc;

    @Schema(description = "日志内容")
    private String logContent;

    @Schema(description = "操作IP地址")
    private String ipAddress;

    private String location;

    private String os;

    private String browser;

    @Schema(description = "操作状态（0成功 1失败）")
    private Integer operationStatus;

    @Schema(description = "操作状态描述")
    private String operationStatusDesc;

    @Schema(description = "创建者（用户ID）")
    private Long userId;

    @Schema(description = "创建者用户名")
    private String userName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
} 