package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.user.BanApplication;
import com.leyuz.bbs.user.dto.BanCreateCmd;
import com.leyuz.bbs.user.dto.BanPageQuery;
import com.leyuz.bbs.user.dto.BanVO;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户禁言管理")
@RestController
@RequestMapping("/v1/admin/bans")
@RequiredArgsConstructor
public class AdminBanController {

    private final BanApplication banApplication;

    @Operation(summary = "禁言用户")
    @PostMapping
    //@PreAuthorize("@permissionResolver.hasPermission('admin:system:ban')")
    public SingleResponse<Boolean> createBan(@RequestBody BanCreateCmd cmd) {
        return SingleResponse.of(banApplication.createBan(cmd));
    }

    @Operation(summary = "解除禁言")
    @PostMapping("/unban")
    //@PreAuthorize("@permissionResolver.hasPermission('admin:system:ban')")
    public SingleResponse<Boolean> unbanUser(@RequestBody BanCreateCmd cmd) {
        return SingleResponse.of(banApplication.unbanUser(cmd));
    }

    @Operation(summary = "分页查询禁言记录")
    @GetMapping
    //@PreAuthorize("@permissionResolver.hasPermission('admin:system:ban')")
    public SingleResponse<CustomPage<BanVO>> queryBans(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") Integer forumId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        BanPageQuery query = new BanPageQuery(userId, forumId, pageNo, pageSize);
        return SingleResponse.of(banApplication.queryBans(query));
    }
} 