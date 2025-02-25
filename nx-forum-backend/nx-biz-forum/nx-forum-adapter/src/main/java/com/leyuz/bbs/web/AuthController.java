package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户权限管理
 * </p>
 *
 * @author walker
 * @since 2025-02-05
 */
@Tag(name = "用户权限管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {
    private final ForumPermissionResolver forumPermissionResolver;

    @Operation(summary = "查询用户权限")
    @GetMapping("/permissions")
    @PermitAll
    public SingleResponse queryPermissions(@RequestParam(defaultValue = "0") Integer forumId) {
        return SingleResponse.of(forumPermissionResolver.queryPermissions(forumId));
    }

}
