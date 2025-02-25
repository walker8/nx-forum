package com.leyuz.bbs.domain.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.domain.comment.CommentE;

import java.io.Serial;

public class CommentDeletedEvent extends BaseEvent<CommentE> {

    @Serial
    private static final long serialVersionUID = 120165834479746183L;

    public CommentDeletedEvent(Object source, CommentE eventData, String reason, Boolean notice) {
        super(source, eventData, reason, notice);
    }
}
