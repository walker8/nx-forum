package com.leyuz.bbs.content.comment.dto;


import com.alibaba.cola.dto.Command;
import lombok.Data;

import java.io.Serial;
import java.util.List;

@Data
public class CommentCmd extends Command {
    @Serial
    private static final long serialVersionUID = 184408722954178118L;
    private String message;
    private Long threadId;
    private Long commentId;
    private List<String> images;
}
