package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentE;

import java.io.Serial;

public class CommentPassedEvent extends BaseEvent<CommentE> {

    @Serial
    private static final long serialVersionUID = 6060527256726337303L;

    public CommentPassedEvent(Object source, CommentE eventData, Boolean notice) {
        super(source, eventData, "", notice);
    }
}
