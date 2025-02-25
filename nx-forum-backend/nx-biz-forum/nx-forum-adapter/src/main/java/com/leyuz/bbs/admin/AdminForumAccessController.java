package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.dto.ForumAccessDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台版块权限管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/forum-access")
public class AdminForumAccessController {
    private final ForumApplication forumApplication;

    @Operation(summary = "获取版块权限配置")
    @GetMapping("/{forumId}")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<List<ForumAccessDTO>> getForumAccess(@PathVariable Integer forumId) {
        return SingleResponse.of(forumApplication.getForumAccess(forumId));
    }

    @Operation(summary = "批量更新版块权限")
    @PutMapping("/batch")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> batchUpdateForumAccess(@RequestBody List<ForumAccessDTO> accessList,
                                                         @RequestParam Integer forumId,
                                                         @RequestParam(defaultValue = "true") boolean enableForumAccess) {
        forumApplication.updateForumAccess(forumId, accessList, enableForumAccess);
        return SingleResponse.of("更新成功");
    }

    @Operation(summary = "删除版块权限")
    @DeleteMapping("/{forumId}")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> deleteForumAccess(@PathVariable Integer forumId) {
        forumApplication.deleteForumAccess(forumId);
        return SingleResponse.of("删除成功");
    }
} 