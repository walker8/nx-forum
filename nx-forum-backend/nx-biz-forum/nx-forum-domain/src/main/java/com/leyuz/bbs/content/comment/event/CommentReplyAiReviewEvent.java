package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentReplyE;

import java.io.Serial;

/**
 * 楼中楼 AI 复审存疑事件：用于将楼中楼从 PASSED 退回人工审核时触发通知
 *
 * @author Walker
 */
public class CommentReplyAiReviewEvent extends BaseEvent<CommentReplyE> {

    @Serial
    private static final long serialVersionUID = 821608561031351901L;

    public CommentReplyAiReviewEvent(Object source, CommentReplyE eventData, String reason, boolean notice) {
        super(source, eventData, reason, notice);
    }
}
