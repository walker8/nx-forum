package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.content.comment.CommentReplyE;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class CommentReplyNewEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = -808232341604216261L;
    private final CommentReplyE commentReplyE;

    public CommentReplyNewEvent(Object source, CommentReplyE commentReplyE) {
        super(source);
        this.commentReplyE = commentReplyE;
    }

    public CommentReplyE getCommentReplyE() {
        return commentReplyE;
    }
}
