package com.leyuz.uc.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.AdminUserVO;
import com.leyuz.uc.user.dto.UserCmd;
import com.leyuz.uc.user.dto.UserQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户中心
 * </p>
 *
 * @author walker
 * @since 2024-01-06
 */
@Tag(name = "用户中心")
@RestController
@RequestMapping("/v1/uc/admin/users")
public class AdminUserController {

    @Autowired
    private UserApplication userApplication;

    @Operation(summary = "查询用户")
    @GetMapping("")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse queryUsersByAdmin(@RequestParam(defaultValue = "") Long userId,
                                            @RequestParam(defaultValue = "") String userName,
                                            @RequestParam(defaultValue = "") String lastActiveIp,
                                            @RequestParam(defaultValue = "") String orderBy,
                                            @RequestParam(defaultValue = "1") int pageNo,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        UserQuery query = UserQuery.builder().userId(userId).userName(userName)
                .orderBy(orderBy).lastActiveIp(lastActiveIp).pageNo(pageNo).pageSize(pageSize).build();
        CustomPage<AdminUserVO> threadRespPage = userApplication.queryUsersByAdmin(query);
        return SingleResponse.of(threadRespPage);
    }

    @Operation(summary = "注册用户")
    @PostMapping
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse saveUserByAdmin(@RequestBody UserCmd userCmd) {
        userApplication.saveUserByAdmin(userCmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "更新用户")
    @PutMapping()
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse updateUserByAdmin(@RequestBody UserCmd userCmd) {
        userApplication.updateUserByAdmin(userCmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "删除用户")
    @PutMapping("/{userId}/delete")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse deleteUserByAdmin(@PathVariable("userId") Long userId) {
        userApplication.deleteUserByAdmin(userId);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "批量删除用户")
    @PutMapping("/batch/delete")
    @PreAuthorize("@permissionResolver.hasRole('UC_ADMIN')")
    public SingleResponse deleteUsersByAdmin(@RequestBody List<Long> userIds) {
        userApplication.deleteUsersByAdmin(userIds);
        return SingleResponse.buildSuccess();
    }

}
