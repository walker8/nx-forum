package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentE;

import java.io.Serial;

public class CommentRestoredEvent extends BaseEvent<CommentE> {

    @Serial
    private static final long serialVersionUID = -7317227856300765377L;

    public CommentRestoredEvent(Object source, CommentE eventData, Boolean notice) {
        super(source, eventData, "", notice);
    }
}
