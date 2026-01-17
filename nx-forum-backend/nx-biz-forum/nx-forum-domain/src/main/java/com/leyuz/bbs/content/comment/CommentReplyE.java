package com.leyuz.bbs.content.comment;

import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.common.exception.ValidationException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

import static com.leyuz.bbs.common.utils.HtmlUtils.convertToSafeHtml;

@Getter
@NoArgsConstructor
public class CommentReplyE extends BaseComment {
    @Setter
    private Long replyId;

    private Long replyUserId;

    @Builder
    public CommentReplyE(Long threadId, Long commentId, Integer forumId, String userIp, String userAgent, String terminalType, String platform, Integer likes, AuditStatusV auditStatus, String auditReason, String message, LocalDateTime createTime, LocalDateTime updateTime, Long createBy, Long updateBy, Long replyId, Long replyUserId) {
        super(threadId, commentId, forumId, userIp, userAgent, terminalType, platform, likes, auditStatus, auditReason, message, createTime, updateTime, createBy, updateBy);
        this.replyId = replyId;
        this.replyUserId = replyUserId;
    }

    public String getMessageHtml() {
        return convertToSafeHtml(message, false);
    }

    @Override
    protected void checkMessage() {
        if (StringUtils.isBlank(message)) {
            throw new ValidationException("回复不能为空！");
        }
        if (StringUtils.isNotBlank(message) && message.length() > 500) {
            throw new ValidationException("回复内容过长！");
        }
    }
}
