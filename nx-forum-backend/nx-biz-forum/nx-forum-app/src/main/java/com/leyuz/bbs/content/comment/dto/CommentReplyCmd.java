package com.leyuz.bbs.content.comment.dto;


import com.alibaba.cola.dto.Command;
import lombok.Data;

import java.io.Serial;

@Data
public class CommentReplyCmd extends Command {

    @Serial
    private static final long serialVersionUID = -8084099572286823746L;
    private String message;
    private Long replyId;
    private Long replyUserId;
}
