package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentReplyE;

import java.io.Serial;

public class CommentReplyDeletedEvent extends BaseEvent<CommentReplyE> {

    @Serial
    private static final long serialVersionUID = 120165834479746183L;

    public CommentReplyDeletedEvent(Object source, CommentReplyE eventData, String reason, Boolean notice) {
        super(source, eventData, reason, notice);
    }
}
