package com.leyuz.bbs.domain.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.domain.comment.CommentReplyE;

import java.io.Serial;

public class CommentReplyRejectedEvent extends BaseEvent<CommentReplyE> {

    @Serial
    private static final long serialVersionUID = -6327861946485824964L;

    public CommentReplyRejectedEvent(Object source, CommentReplyE eventData, String reason, Boolean notice) {
        super(source, eventData, reason, notice);
    }
}
