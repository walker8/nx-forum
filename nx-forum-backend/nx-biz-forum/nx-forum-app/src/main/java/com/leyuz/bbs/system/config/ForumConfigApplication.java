package com.leyuz.bbs.system.config;

import com.alibaba.fastjson2.JSON;
import com.leyuz.bbs.system.config.dto.AnalyticsConfigDTO;
import com.leyuz.bbs.system.config.dto.ConfigConst;
import com.leyuz.bbs.system.config.dto.WebsiteBaseInfoVO;
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

    /**
     * 获取统计代码（前端访问）
     *
     * @return 统计代码（如果启用）
     */
    public String getAnalyticsCode() {
        String configJson = configApplication.getConfigValueByKey(ConfigConst.ANALYTICS_CONFIG);
        if (StringUtils.isBlank(configJson)) {
            // 配置不存在时，默认启用但代码为空，返回空字符串
            return "";
        }
        
        try {
            AnalyticsConfigDTO config = JSON.parseObject(configJson, AnalyticsConfigDTO.class);
            if (config == null) {
                return "";
            }
            // enabled 为 null 时默认为 true
            boolean enabled = config.getEnabled() != null ? config.getEnabled() : true;
            if (enabled) {
                return StringUtils.isNotBlank(config.getAnalyticsCode()) ? config.getAnalyticsCode() : "";
            }
        } catch (Exception e) {
            // JSON 解析失败，返回空字符串
            return "";
        }
        
        return "";
    }

    /**
     * 获取统计配置（管理员）
     *
     * @return 统计配置（包含代码和启用状态）
     */
    public AnalyticsConfigDTO getAnalyticsConfigByAdmin() {
        String configJson = configApplication.getConfigValueByKey(ConfigConst.ANALYTICS_CONFIG);
        if (StringUtils.isBlank(configJson)) {
            return new AnalyticsConfigDTO("", true);
        }
        
        try {
            AnalyticsConfigDTO config = JSON.parseObject(configJson, AnalyticsConfigDTO.class);
            if (config == null) {
                return new AnalyticsConfigDTO("", true);
            }
            // 如果 enabled 为 null，默认为 true
            if (config.getEnabled() == null) {
                config.setEnabled(true);
            }
            // 如果 analyticsCode 为 null，默认为空字符串
            if (config.getAnalyticsCode() == null) {
                config.setAnalyticsCode("");
            }
            return config;
        } catch (Exception e) {
            // JSON 解析失败，返回默认值（启用状态默认为 true）
            return new AnalyticsConfigDTO("", true);
        }
    }

    /**
     * 更新统计配置
     *
     * @param config 统计配置
     * @return 是否更新成功
     */
    public boolean updateAnalyticsConfig(AnalyticsConfigDTO config) {
        if (config == null) {
            config = new AnalyticsConfigDTO("", true);
        }
        
        // 确保字段不为 null
        if (config.getAnalyticsCode() == null) {
            config.setAnalyticsCode("");
        }
        // enabled 为 null 时默认为 true
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        
        String configJson = JSON.toJSONString(config);
        return configApplication.updateConfig(ConfigConst.ANALYTICS_CONFIG, configJson);
    }

}
