package com.leyuz.bbs.content.comment.event;

import com.leyuz.bbs.content.comment.CommentE;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

public class CommentNewEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 8993598971539920426L;

    private final CommentE commentE;

    public CommentNewEvent(Object source, CommentE commentE) {
        super(source);
        this.commentE = commentE;
    }

    public CommentE getCommentE() {
        return commentE;
    }
}
