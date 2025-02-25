package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.content.comment.CommentApplication;
import com.leyuz.bbs.content.comment.dto.CommentVO;
import com.leyuz.bbs.content.thread.ThreadApplication;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import com.leyuz.bbs.interaction.favorite.FavoriteApplication;
import com.leyuz.bbs.interaction.favorite.dto.FavoriteCmd;
import com.leyuz.bbs.interaction.follow.UserFollowApplication;
import com.leyuz.bbs.interaction.follow.UserFollowCmd;
import com.leyuz.bbs.interaction.follow.UserFollowVO;
import com.leyuz.bbs.interaction.like.LikeApplication;
import com.leyuz.bbs.interaction.like.dto.LikeCmd;
import com.leyuz.bbs.user.ForumUserApplication;
import com.leyuz.bbs.user.dto.ForumUserVO;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final ThreadApplication threadApplication;
    private final CommentApplication commentApplication;
    private final LikeApplication likeApplication;
    private final FavoriteApplication favoriteApplication;
    private final UserFollowApplication userFollowApplication;
    private final ForumUserApplication forumUserApplication;

    @Operation(summary = "查询用户帖子")
    @GetMapping("/{userId}/threads")
    @PermitAll
    public SingleResponse queryThreadsByUserId(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "1") int pageNo,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        CustomPage<ThreadVO> threadRespPage = threadApplication.queryThreadsByUserId(userId, pageNo, pageSize);
        return SingleResponse.of(threadRespPage);
    }

    @Operation(summary = "获取用户基本信息")
    @GetMapping("/{userId}/info")
    @PermitAll
    public SingleResponse getForumUserInfo(@PathVariable Long userId) {
        ForumUserVO userVO = forumUserApplication.getForumUserInfo(userId);
        return SingleResponse.of(userVO);
    }

    @Operation(summary = "查询用户评论")
    @GetMapping("/{userId}/comments")
    @PermitAll
    public SingleResponse queryCommentsByUserId(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "1") int pageNo,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        CustomPage<CommentVO> comments = commentApplication.queryCommentsByUserId(userId, pageNo, pageSize);
        return SingleResponse.of(comments);
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like")
    public SingleResponse<Integer> toggleLike(@RequestBody LikeCmd cmd) {
        return SingleResponse.of(likeApplication.toggleLike(cmd));
    }

    @Operation(summary = "分页查询用户点赞的帖子")
    @GetMapping("/{userId}/likes")
    public SingleResponse<CustomPage<ThreadVO>> queryThreadsByUserLikes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return SingleResponse.of(likeApplication.queryThreadsByUserLikes(userId, pageNo, pageSize));
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/favorite")
    public SingleResponse<Integer> toggleFavorite(@RequestBody FavoriteCmd cmd) {
        return SingleResponse.of(favoriteApplication.toggleFavorite(cmd));
    }

    @Operation(summary = "分页查询用户收藏的帖子")
    @GetMapping("/{userId}/favorites")
    public SingleResponse<CustomPage<ThreadVO>> queryThreadsByUserFavorites(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return SingleResponse.of(favoriteApplication.queryThreadsByUserFavorites(userId, pageNo, pageSize));
    }

    @Operation(summary = "关注用户")
    @PostMapping("/follow")
    public SingleResponse<Boolean> follow(@RequestBody UserFollowCmd cmd) {
        return SingleResponse.of(userFollowApplication.follow(cmd));
    }

    @Operation(summary = "取消关注")
    @PostMapping("/unfollow/{followUserId}")
    public SingleResponse<Boolean> unfollow(@PathVariable Long followUserId) {
        return SingleResponse.of(userFollowApplication.unfollow(followUserId));
    }

    @Operation(summary = "查询用户关注列表")
    @GetMapping("/{userId}/following")
    @PermitAll
    public SingleResponse<CustomPage<UserFollowVO>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return SingleResponse.of(userFollowApplication.getFollowingList(userId, pageNo, pageSize));
    }

    @Operation(summary = "查询用户粉丝列表")
    @GetMapping("/{userId}/followers")
    @PermitAll
    public SingleResponse<CustomPage<UserFollowVO>> getFollowerList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return SingleResponse.of(userFollowApplication.getFollowerList(userId, pageNo, pageSize));
    }
}
