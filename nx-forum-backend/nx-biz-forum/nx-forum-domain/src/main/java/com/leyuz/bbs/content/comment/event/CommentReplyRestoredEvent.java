package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.common.event.BaseEvent;
import com.leyuz.bbs.content.comment.CommentReplyE;

import java.io.Serial;

public class CommentReplyRestoredEvent extends BaseEvent<CommentReplyE> {

    @Serial
    private static final long serialVersionUID = 3715035301714189636L;

    public CommentReplyRestoredEvent(Object source, CommentReplyE eventData, Boolean notice) {
        super(source, eventData, "", notice);
    }
}
