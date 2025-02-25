package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.content.comment.CommentApplication;
import com.leyuz.bbs.content.thread.ThreadApplication;
import com.leyuz.bbs.interaction.report.ReportApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 管理接口
 * </p>
 *
 * @author walker
 * @since 2024-09-16
 */
@Tag(name = "后台管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
public class AdminController {


    private final ThreadApplication threadApplication;

    private final CommentApplication commentApplication;

    private final ReportApplication reportApplication;

    @Operation(summary = "查询帖子审核数量")
    @GetMapping("/posts/auditing/count")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:manage')")
    public SingleResponse getAuditingCount(@RequestParam Integer forumId) {
        Map<String, Long> map = new HashMap<>();
        long threadAuditCount = threadApplication.getAuditingCount(forumId);
        map.put("threadAuditCount", threadAuditCount);
        long commentAuditCount = commentApplication.getCommentAuditingCount(forumId);
        map.put("commentAuditCount", commentAuditCount);
        long replyAuditCount = commentApplication.getReplyAuditingCount(forumId);
        map.put("replyAuditCount", replyAuditCount);
        long pendingReportCount = reportApplication.getPendingReportCount(forumId);
        map.put("pendingReportCount", pendingReportCount);
        map.put("totalAuditCount", threadAuditCount + commentAuditCount + replyAuditCount + pendingReportCount);
        return SingleResponse.of(map);
    }

}
