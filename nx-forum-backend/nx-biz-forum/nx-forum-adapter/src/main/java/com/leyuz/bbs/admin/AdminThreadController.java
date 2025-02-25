package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.thread.ThreadApplication;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.bbs.content.thread.dto.AdminThreadVO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 主题
 * </p>
 *
 * @author walker
 * @since 2024-09-01
 */
@Tag(name = "后台主题帖管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/threads")
public class AdminThreadController {

    private final ThreadApplication threadApplication;

    @Operation(summary = "查询帖子")
    @GetMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission(#forumId,'admin:thread:search')")
    public SingleResponse queryThreadsByAdmin(@RequestParam(defaultValue = "0") Integer forumId,
                                              @RequestParam(defaultValue = "") String authorName,
                                              @RequestParam(defaultValue = "") String keyword,
                                              @RequestParam(defaultValue = "") String ip,
                                              @RequestParam(defaultValue = "") String orderBy,
                                              @RequestParam(defaultValue = "") Byte auditStatus,
                                              @RequestParam(defaultValue = "-1") Byte propertyType,
                                              @RequestParam(defaultValue = "") Boolean deleted,
                                              @RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        ThreadQuery query = ThreadQuery.builder().forumId(forumId).authorName(authorName).keyword(keyword).ip(ip)
                .auditStatusV(AuditStatusV.of(auditStatus)).deleted(deleted).propertyTypeV(ThreadPropertyTypeV.of(propertyType))
                .orderBy(orderBy).pageNo(pageNo).pageSize(pageSize).build();
        CustomPage<AdminThreadVO> threadRespPage = threadApplication.queryThreadsByAdmin(query);
        return SingleResponse.of(threadRespPage);
    }

    @Operation(summary = "批量操作帖子")
    @PutMapping("/batch/operation")
    public SingleResponse operateThreadsByAdmin(@RequestBody List<Long> threadIds,
                                                @RequestParam(defaultValue = "0") Integer forumId,
                                                @RequestParam(defaultValue = "") String reason,
                                                @RequestParam(defaultValue = "") String operation,
                                                @RequestParam(defaultValue = "true") boolean notice) {
        String result = threadApplication.operateThreadsByAdmin(forumId, threadIds, reason, operation, notice);
        return SingleResponse.of(result);
    }
}
