package com.leyuz.bbs.interaction.report.dto.command;

import com.leyuz.bbs.interaction.report.dto.constant.ReportHandleStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "处理举报命令")
public class ReportHandleCmd {

    @NotNull
    @Schema(description = "举报ID")
    private Long reportId;

    @NotNull
    @Schema(description = "处理状态 (1:已处理-违规, 2:已处理-驳回)")
    private ReportHandleStatusEnum handleStatus;

    @Schema(description = "处理结果说明")
    private String handleReason;

    @Schema(description = "是否通知用户", defaultValue = "false")
    private Boolean notifyUser = false;
} 