package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.dto.ForumMenuItemVO;
import com.leyuz.bbs.forum.dto.ForumMenuVO;
import com.leyuz.bbs.forum.dto.ForumVO;
import com.leyuz.bbs.forum.dto.UserForumVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 版块管理控制器
 *
 * @author walker
 * @since 2024-04-03
 */
@Tag(name = "版块")
@RestController
@RequestMapping("/v1/forums")
@RequiredArgsConstructor
public class ForumController {

    private final ForumApplication forumApplication;
    private final ForumPermissionResolver forumPermissionResolver;

    @Operation(summary = "获取版块菜单")
    @GetMapping("/menu")
    @PermitAll
    public SingleResponse<ForumMenuVO> getForumShowMenu() {
        forumPermissionResolver.checkPermission("forum:visit");
        ForumMenuVO forumMenuVO = forumApplication.getForumShowMenu();
        return SingleResponse.of(forumMenuVO);
    }

    @Operation(summary = "获取所有用户版块")
    @GetMapping("/user")
    @PermitAll
    public SingleResponse<List<UserForumVO>> getUserForumList() {
        forumPermissionResolver.checkPermission("forum:visit");
        return SingleResponse.of(forumApplication.getUserForumList());
    }

    @Operation(summary = "获取用户可显示的版块菜单")
    @GetMapping("/menu/user")
    public SingleResponse<List<ForumMenuItemVO>> getUserForumMenu() {
        List<ForumMenuItemVO> forumMenuItemVOList = forumApplication.getUserForumMenu();
        return SingleResponse.of(forumMenuItemVOList);
    }

    @Operation(summary = "获取版块基础信息")
    @GetMapping("/info")
    @PermitAll
    public SingleResponse<ForumVO> getForumInfoByName(@RequestParam(required = false) String forumName) {
        return SingleResponse.of(forumApplication.getForumInfoByName(forumName));
    }

}
