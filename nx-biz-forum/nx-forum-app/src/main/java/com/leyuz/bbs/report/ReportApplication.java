package com.leyuz.bbs.report;

import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.comment.CommentApplication;
import com.leyuz.bbs.domain.comment.CommentE;
import com.leyuz.bbs.domain.comment.CommentReplyE;
import com.leyuz.bbs.domain.comment.gateway.CommentGateway;
import com.leyuz.bbs.domain.report.ReportE;
import com.leyuz.bbs.domain.report.constant.ReportHandleStatusEnum;
import com.leyuz.bbs.domain.report.constant.ReportTargetTypeEnum;
import com.leyuz.bbs.domain.report.gateway.ReportGateway;
import com.leyuz.bbs.domain.report.query.ReportPageQuery;
import com.leyuz.bbs.domain.thread.ThreadE;
import com.leyuz.bbs.domain.thread.gateway.ThreadGateway;
import com.leyuz.bbs.notification.NotificationApplication;
import com.leyuz.bbs.report.dto.ReportDTO;
import com.leyuz.bbs.report.dto.command.ReportCreateCmd;
import com.leyuz.bbs.report.dto.command.ReportHandleCmd;
import com.leyuz.bbs.thread.ThreadApplication;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.domain.user.UserE;
import com.leyuz.uc.user.UserApplication;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportApplication {

    private final ReportGateway reportGateway;
    private final ThreadGateway threadGateway;
    private final ThreadApplication threadApplication;
    private final CommentGateway commentGateway;
    private final CommentApplication commentApplication;
    private final UserApplication userApplication;
    private final ForumPermissionResolver forumPermissionResolver;
    private final NotificationApplication notificationApplication;

    public void createReport(ReportCreateCmd cmd) {
        ReportE report = new ReportE();
        report.setReportReason(cmd.getReportReason());
        report.setTargetId(cmd.getTargetId());
        ReportTargetTypeEnum targetType = cmd.getTargetType();
        report.setTargetType(targetType);
        report.setForumId(cmd.getForumId());
        report.setReportType(cmd.getReportType());
        String reportedContent = getReportedContent(cmd, targetType);
        report.setReportedContent(reportedContent);
        report.setHandleStatus(ReportHandleStatusEnum.PENDING);
        report.setCreateBy(HeaderUtils.getUserId());
        report.setUserIp(HeaderUtils.getIp());
        report.setUserAgent(HeaderUtils.getUserAgent());
        reportGateway.save(report);
    }

    private String getReportedContent(ReportCreateCmd cmd, ReportTargetTypeEnum targetType) {
        String reportedContent = "";
        switch (targetType) {
            case THREAD:
                ThreadE thread = threadGateway.getThread(cmd.getTargetId());
                if (thread == null) {
                    throw new ValidationException("主题不存在");
                }
                reportedContent = StringUtils.isNotEmpty(thread.getSubject()) ? thread.getSubject() : thread.getBrief();
                break;
            case COMMENT:
                CommentE comment = commentGateway.getComment(cmd.getTargetId());
                if (comment == null) {
                    throw new ValidationException("评论不存在");
                }
                reportedContent = comment.getMessage();
                break;
            case REPLY:
                CommentReplyE commentReply = commentGateway.getCommentReply(cmd.getTargetId());
                if (commentReply == null) {
                    throw new ValidationException("回复不存在");
                }
                reportedContent = commentReply.getMessage();
                break;
        }
        // 截取前80个字符，超出的部分用...代替
        reportedContent = StringUtils.abbreviate(reportedContent, 80);
        return reportedContent;
    }

    public void handleReport(ReportHandleCmd cmd) {
        ReportE report = reportGateway.findById(cmd.getReportId());
        if (report == null) {
            throw new ValidationException("举报不存在");
        }
        forumPermissionResolver.checkPermission(report.getForumId(), "admin:user:report");
        if (cmd.getHandleStatus() == ReportHandleStatusEnum.PENDING) {
            throw new ValidationException("处理状态不能为待处理");
        }
        report.setHandleStatus(cmd.getHandleStatus());
        report.setHandleReason(cmd.getHandleReason());
        report.setUpdateBy(HeaderUtils.getUserId());
        if (ReportHandleStatusEnum.APPROVED.equals(cmd.getHandleStatus())) {
            String reason = report.getHandleReason();
            switch (report.getTargetType()) {
                case THREAD:
                    threadApplication.deleteThread(report.getTargetId(), reason, cmd.getNotifyUser());
                    break;
                case COMMENT:
                    commentApplication.deleteComment(report.getTargetId(), reason, cmd.getNotifyUser());
                    break;
                case REPLY:
                    commentApplication.deleteCommentReply(report.getTargetId(), reason, cmd.getNotifyUser());
                    break;
            }
        }
        reportGateway.updateById(report);

        // 如果需要通知用户
        if (Boolean.TRUE.equals(cmd.getNotifyUser())) {
            sendNotificationToReporter(report);
        }
    }

    /**
     * 向举报人发送系统通知
     *
     * @param report 举报实体
     */
    private void sendNotificationToReporter(ReportE report) {
        String status = report.getHandleStatus() == ReportHandleStatusEnum.APPROVED ? "违规" : "驳回";
        String subject = "您的举报已处理";

        // 构建通知内容
        StringBuilder message = new StringBuilder();
        message.append("您举报的内容已被管理员处理为【").append(status).append("】。<br />");

        // 添加被举报内容摘要
        if (StringUtils.isNotEmpty(report.getReportedContent())) {
            message.append("被举报内容: ").append(report.getReportedContent()).append("<br />");
        }

        // 添加处理说明（如果有）
        if (report.getHandleStatus() == ReportHandleStatusEnum.REJECTED
                && StringUtils.isNotEmpty(report.getHandleReason())) {
            // 驳回原因
            message.append("处理说明: ").append(report.getHandleReason());
        }

        // 发送系统通知
        notificationApplication.sendSystemNotification(report.getCreateBy(), subject, message.toString());
    }

    public CustomPage<ReportDTO> queryReports(ReportPageQuery query) {
        CustomPage<ReportE> page = reportGateway.findPage(query);
        return page.map(this::toDTO);
    }

    public long getPendingReportCount(Integer forumId) {
        return reportGateway.countPendingReports(forumId);
    }

    private ReportDTO toDTO(ReportE report) {
        ReportDTO dto = new ReportDTO();
        BeanUtils.copyProperties(report, dto);
        if (StringUtils.isEmpty(report.getReportedContent())) {
            dto.setReportedContent("无文字内容");
        }
        UserE reporter = userApplication.getByIdFromCache(report.getCreateBy());
        dto.setReporterName(reporter.getUserName());
        UserE handler = userApplication.getByIdFromCache(report.getUpdateBy());
        dto.setHandlerName(handler.getUserName());
        setRedirectUrl(dto);
        return dto;
    }

    private void setRedirectUrl(ReportDTO dto) {
        switch (dto.getTargetType()) {
            case THREAD:
                dto.setRedirectUrl("/t/" + dto.getTargetId());
                break;
            case COMMENT:
                dto.setRedirectUrl("/c/" + dto.getTargetId());
                break;
            case REPLY:
                CommentReplyE commentReply = commentGateway.getCommentReply(dto.getTargetId());
                if (commentReply != null) {
                    dto.setRedirectUrl("/c/" + commentReply.getCommentId() + "?replyId=" + commentReply.getReplyId());
                }
                break;
        }
    }
} 