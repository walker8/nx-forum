package com.leyuz.bbs.content.thread;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.content.thread.dto.AdminThreadVO;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import com.leyuz.common.dto.UserClientInfo;
import com.leyuz.common.ip.AddressUtils;
import com.leyuz.common.utils.UserAgentUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreadConvert {
    private final UserApplication userApplication;
    private final ForumApplication forumApplication;

    public ThreadVO convertThreadPO2VO(ThreadPO threadPO, boolean convertProperty) {
        if (threadPO != null) {
            ThreadVO threadVO = BeanUtil.toBean(threadPO, ThreadVO.class);
            if (convertProperty) {
                ThreadPropertyV threadPropertyV = JSON.parseObject(threadPO.getProperty(), ThreadPropertyV.class);
                threadVO.setProperty(threadPropertyV);
            }
            Long authorId = threadPO.getCreateBy();
            threadVO.setAuthorId(authorId);
            UserE author = userApplication.getByIdFromCache(authorId);
            threadVO.setAuthorName(author.getUserName());
            threadVO.setAvatarUrl(author.getAvatar());
            ForumPO forum = forumApplication.getForumById(threadPO.getForumId());
            if (forum != null) {
                // 版块未删除
                threadVO.setForumNickName(forum.getNickName());
                threadVO.setForumName(forum.getName());
            }
            return threadVO;
        }
        return null;
    }

    public ThreadVO convertThreadPO2VO(ThreadPO threadPO) {
        return convertThreadPO2VO(threadPO, true);
    }

    public ThreadVO convertThreadPO2VO(ThreadPO threadPO, String orderBy) {
        ThreadVO threadVO = convertThreadPO2VO(threadPO, true);
        if (threadVO != null) {
            // 根据排序方式计算显示时间
            if ("last_comment_time".equals(orderBy)) {
                // 取 createTime、updateTime、lastCommentTime 中的最大值
                LocalDateTime displayTime = threadVO.getCreateTime();
                if (threadVO.getUpdateTime() != null && threadVO.getUpdateTime().isAfter(displayTime)) {
                    displayTime = threadVO.getUpdateTime();
                }
                // 需要从 threadPO 获取 lastCommentTime
                if (threadPO.getLastCommentTime() != null && threadPO.getLastCommentTime().isAfter(displayTime)) {
                    displayTime = threadPO.getLastCommentTime();
                }
                threadVO.setDisplayTime(displayTime);
            } else {
                // 默认使用 createTime
                threadVO.setDisplayTime(threadVO.getCreateTime());
            }
        }
        return threadVO;
    }

    public AdminThreadVO convertThreadPO2AdminVO(ThreadPO threadPO) {
        if (threadPO != null) {
            AdminThreadVO threadVO = BeanUtil.toBean(threadPO, AdminThreadVO.class);
            ThreadPropertyV threadPropertyV = JSON.parseObject(threadPO.getProperty(), ThreadPropertyV.class);
            threadVO.setProperty(threadPropertyV);
            Long authorId = threadPO.getCreateBy();
            threadVO.setAuthorId(authorId);
            UserE author = userApplication.getByIdFromCache(authorId);
            threadVO.setAuthorName(author.getUserName());
            threadVO.setAvatarUrl(author.getAvatar());
            ForumPO forumPO = forumApplication.getForumById(threadPO.getForumId());
            if (forumPO != null) {
                threadVO.setForumNickName(forumPO.getNickName());
            }
            threadVO.setLocation(AddressUtils.getCityByIP(threadPO.getUserIp()));
            UserClientInfo clientInfo = UserAgentUtils.getClientInfo(threadPO.getUserAgent());
            threadVO.setOs(clientInfo.getOs());
            threadVO.setBrowser(clientInfo.getBrowser());
            return threadVO;
        }
        return null;
    }
}
