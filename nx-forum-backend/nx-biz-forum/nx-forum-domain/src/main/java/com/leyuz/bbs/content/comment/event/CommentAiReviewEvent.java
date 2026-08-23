package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentE;

import java.io.Serial;

/**
 * 评论 AI 复审存疑事件：用于将评论从 PASSED 退回人工审核时触发通知
 *
 * @author Walker
 */
public class CommentAiReviewEvent extends BaseEvent<CommentE> {

    @Serial
    private static final long serialVersionUID = 821608561031351900L;

    public CommentAiReviewEvent(Object source, CommentE eventData, String reason, boolean notice) {
        super(source, eventData, reason, notice);
    }
}
