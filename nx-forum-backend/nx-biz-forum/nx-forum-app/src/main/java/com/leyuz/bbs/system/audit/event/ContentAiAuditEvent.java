package com.leyuz.bbs.system.audit.event;

import com.leyuz.bbs.system.audit.dto.AuditContentType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 待异步 AI 审核事件
 *
 * @author Walker
 */
@Getter
public class ContentAiAuditEvent extends ApplicationEvent {
    private final AuditContentType contentType;
    private final Long contentId;
    private final Integer forumId;
    private final Boolean isNew;

    public ContentAiAuditEvent(Object source, AuditContentType contentType, Long contentId, Integer forumId, Boolean isNew) {
        super(source);
        this.contentType = contentType;
        this.contentId = contentId;
        this.forumId = forumId;
        this.isNew = isNew;
    }
}
