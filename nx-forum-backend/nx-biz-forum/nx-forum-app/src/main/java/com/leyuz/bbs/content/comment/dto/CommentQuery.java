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
public class CommentQuery extends Query {
    @Serial
    private static final long serialVersionUID = 1534402571408506960L;
    private Integer forumId;
    private String authorName;
    private String ip;
    private Long threadId;
    private Integer pageNo;
    private Integer pageSize;
    private AuditStatusV auditStatusV;
    private Boolean deleted;
}
