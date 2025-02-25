package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.forum.dto.ForumCmd;
import com.leyuz.bbs.forum.dto.ForumItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 版块
 * </p>
 *
 * @author walker
 * @since 2024-09-01
 */
@Tag(name = "后台版块管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/forums")
public class AdminForumController {

    private final ForumApplication forumApplication;

    @Operation(summary = "新建版块")
    @PostMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> createForum(@RequestBody ForumCmd forumCmd) {
        forumApplication.createForum(forumCmd);
        return SingleResponse.of("创建版块成功");
    }

    @Operation(summary = "更新版块")
    @PutMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> updateForum(@RequestBody ForumCmd forumCmd) {
        forumApplication.updateForum(forumCmd);
        return SingleResponse.of("更新版块成功");
    }

    @Operation(summary = "查询所有版块")
    @GetMapping("")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<List<ForumItemVO>> getAllForumByAdmin() {
        List<ForumItemVO> forumItemVOList = forumApplication.getAllForumByAdmin();
        return SingleResponse.of(forumItemVOList);
    }

    @Operation(summary = "更新默认版块")
    @PutMapping("/default")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> updateDefaultForum(@RequestParam Integer forumId) {
        forumApplication.updateDefaultForum(forumId);
        return SingleResponse.of("success");
    }

    @Operation(summary = "更新版块基础信息")
    @PutMapping("/base")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> updateBaseForum(@RequestParam ForumItemVO forumItemVO) {
        forumApplication.updateBaseForum(forumItemVO);
        return SingleResponse.of("更新版块成功");
    }

    @Operation(summary = "获取版块信息")
    @GetMapping("/{forumId}")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<ForumPO> getForumById(@PathVariable("forumId") Integer forumId) {
        return SingleResponse.of(forumApplication.getForumById(forumId));
    }

    @Operation(summary = "删除版块")
    @DeleteMapping("/{forumId}")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<String> deleteForumById(@PathVariable("forumId") Integer forumId) {
        forumApplication.deleteForumById(forumId);
        return SingleResponse.of("删除版块成功");
    }

    @Operation(summary = "获取管理员版块菜单（包含归档版块）")
    @GetMapping("/menu")
    @PreAuthorize("@forumPermissionResolver.hasPermission('admin:system:forum')")
    public SingleResponse<List<com.leyuz.bbs.forum.dto.ForumMenuItemVO>> getAdminForumMenu() {
        return SingleResponse.of(forumApplication.getAdminForumMenu());
    }
}
