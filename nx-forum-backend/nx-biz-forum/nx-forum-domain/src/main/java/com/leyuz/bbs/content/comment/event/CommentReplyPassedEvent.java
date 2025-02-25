package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentReplyE;

import java.io.Serial;

public class CommentReplyPassedEvent extends BaseEvent<CommentReplyE> {

    @Serial
    private static final long serialVersionUID = 6060527256726337303L;

    public CommentReplyPassedEvent(Object source, CommentReplyE eventData, Boolean notice) {
        super(source, eventData, "", notice);
    }
}
