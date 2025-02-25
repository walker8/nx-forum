package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentE;

import java.io.Serial;

public class CommentRejectedEvent extends BaseEvent<CommentE> {

    @Serial
    private static final long serialVersionUID = 821608561031351897L;

    public CommentRejectedEvent(Object source, CommentE eventData, String reason, boolean notice) {
        super(source, eventData, reason, notice);
    }
}
