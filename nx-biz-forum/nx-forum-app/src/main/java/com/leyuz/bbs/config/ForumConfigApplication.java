package com.leyuz.bbs.config;

import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.config.dto.ConfigConst;
import com.leyuz.bbs.config.dto.WebsiteBaseInfoVO;
import com.leyuz.module.config.app.ConfigApplication;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForumConfigApplication {
    private final ConfigApplication configApplication;
    private final Integer DEFAULT_FORUM_ID = 1;


    /**
     * 获取默认版块id
     *
     * @return
     */
    public Integer getDefaultForumId() {
        String defaultValue = configApplication.getConfigValueByKey(ConfigConst.DEFAULT_FORUM_ID);
        if (StringUtils.isBlank(defaultValue)) {
            return DEFAULT_FORUM_ID;
        }
        return Integer.parseInt(defaultValue);
    }

    public WebsiteBaseInfoVO getWebsiteBaseInfo() {
        String defaultValue = configApplication.getConfigValueByKey(ConfigConst.WEBSITE_BASE_INFO);
        WebsiteBaseInfoVO websiteBaseInfoVO = JSON.parseObject(defaultValue, WebsiteBaseInfoVO.class);
        if (StringUtils.isBlank(websiteBaseInfoVO.getSeoTitle())) {
            websiteBaseInfoVO.setSeoTitle(websiteBaseInfoVO.getWebsiteName());
        }
        return websiteBaseInfoVO;
    }

    public WebsiteBaseInfoVO getWebsiteBaseInfoByAdmin() {
        String defaultValue = configApplication.getConfigValueByKey(ConfigConst.WEBSITE_BASE_INFO);
        return JSON.parseObject(defaultValue, WebsiteBaseInfoVO.class);
    }

    public boolean updateWebsiteBaseInfoByAdmin(WebsiteBaseInfoVO websiteBaseInfoVO) {
        return configApplication.updateConfig(ConfigConst.WEBSITE_BASE_INFO, JSON.toJSONString(websiteBaseInfoVO));
    }

    public boolean updateDefaultForum(Integer forumId) {
        if (forumId != null) {
            return configApplication.updateConfig(ConfigConst.DEFAULT_FORUM_ID, forumId.toString());
        }
        return false;
    }

}
