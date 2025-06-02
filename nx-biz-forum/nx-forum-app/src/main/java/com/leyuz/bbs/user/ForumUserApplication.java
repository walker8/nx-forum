package com.leyuz.bbs.user;

import com.leyuz.bbs.thread.dto.AuthorVO;
import com.leyuz.bbs.user.dto.ForumUserVO;
import com.leyuz.bbs.user.mybatis.ForumUserPropertyService;
import com.leyuz.uc.domain.user.UserE;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumUserApplication {
    private final UserApplication userApplication;
    private final ForumUserPropertyService forumUserPropertyService;
    private final UserFollowApplication userFollowApplication;

    public AuthorVO getAuthorWithProperties(Long userId) {
        UserE userE = userApplication.getByIdFromCache(userId);
        boolean followed = userFollowApplication.isFollowing(userId);
        AuthorVO authorVO = AuthorVO.builder()
                .authorId(userId)
                .authorName(userE.getUserName())
                .avatarUrl(userE.getAvatar())
                .followed(followed)
                .intro(userE.getIntro())
                .build();
        ForumUserPropertyDO userPropertyDO = forumUserPropertyService.getByUserId(userId);
        if (userPropertyDO != null) {
            authorVO.setThreads(userPropertyDO.getThreads());
            authorVO.setComments(userPropertyDO.getComments());
            authorVO.setFans(userPropertyDO.getFans());
        }
        return authorVO;
    }

    public AuthorVO getAuthor(Long userId) {
        UserE userE = userApplication.getByIdFromCache(userId);
        return AuthorVO.builder()
                .authorId(userId)
                .authorName(userE.getUserName())
                .avatarUrl(userE.getAvatar())
                .build();
    }

    public ForumUserVO getForumUserInfo(Long userId) {
        UserVO userVO = userApplication.getUserInfo(userId);
        return ForumUserVO.builder()
                .userId(userVO.getUserId())
                .userName(userVO.getUserName())
                .avatar(userVO.getAvatar())
                .intro(userVO.getIntro())
                .createTime(userVO.getCreateTime())
                .lastActiveDate(userVO.getLastActiveDate())
                .followed(userFollowApplication.isFollowing(userId))
                .build();
    }
}
