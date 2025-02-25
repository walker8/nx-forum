package com.leyuz.bbs.content.comment.dto;

import com.alibaba.cola.dto.Query;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentReplyQuery extends Query {
    @Serial
    private static final long serialVersionUID = 375343543950677409L;
    private Integer forumId;
    private String authorName;
    private String ip;
    private Long threadId;
    private Integer pageNo;
    private Integer pageSize;
    private AuditStatusV auditStatusV;
    private Boolean deleted;
}
