package com.leyuz.bbs.content.thread.dto;

import com.alibaba.cola.dto.Query;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThreadQuery extends Query {
    @Serial
    private static final long serialVersionUID = 3372400457990252129L;
    private Integer forumId = 0;
    private String forumName;
    private String authorName;
    private String keyword;
    private String ip;
    private String orderBy;
    private Integer pageNo;
    private Integer pageSize;
    private AuditStatusV auditStatusV = AuditStatusV.PASSED;
    private ThreadPropertyTypeV propertyTypeV;
    private Boolean deleted = false;
    private List<Long> excludeThreadIds;
    private Integer days;
}
