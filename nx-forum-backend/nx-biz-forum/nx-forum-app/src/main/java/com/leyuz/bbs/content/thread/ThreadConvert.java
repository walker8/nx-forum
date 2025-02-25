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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            threadVO.setAuthorName(userApplication.getByIdFromCache(authorId).getUserName());
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

    public AdminThreadVO convertThreadPO2AdminVO(ThreadPO threadPO) {
        if (threadPO != null) {
            AdminThreadVO threadVO = BeanUtil.toBean(threadPO, AdminThreadVO.class);
            ThreadPropertyV threadPropertyV = JSON.parseObject(threadPO.getProperty(), ThreadPropertyV.class);
            threadVO.setProperty(threadPropertyV);
            Long authorId = threadPO.getCreateBy();
            threadVO.setAuthorId(authorId);
            threadVO.setAuthorName(userApplication.getByIdFromCache(authorId).getUserName());
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
