package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.comment.CommentApplication;
import com.leyuz.bbs.thread.ThreadApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/v1/admin")
public class AdminController {

    @Autowired
    private ThreadApplication threadApplication;
    @Autowired
    private CommentApplication commentApplication;

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
        map.put("totalAuditCount", threadAuditCount + commentAuditCount + replyAuditCount);
        return SingleResponse.of(map);
    }

}
