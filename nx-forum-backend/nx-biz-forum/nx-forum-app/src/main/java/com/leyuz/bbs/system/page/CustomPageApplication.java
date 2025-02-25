package com.leyuz.bbs.system.page;

import com.alibaba.cola.exception.BizException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.system.page.converter.CustomPageConverter;
import com.leyuz.bbs.system.page.dto.CustomPageCmd;
import com.leyuz.bbs.system.page.dto.CustomPageVO;
import com.leyuz.bbs.system.page.CustomPageMapper;
import com.leyuz.bbs.system.page.CustomPageContentMapper;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomPageApplication {
    private final CustomPageMapper customPageMapper;
    private final CustomPageContentMapper customPageContentMapper;

    @Transactional
    public void createPage(CustomPageCmd cmd) {
        // 校验页面编码唯一性
        if (customPageMapper.existsByPageCode(cmd.getPageCode())) {
            throw new BizException("页面编码已存在");
        }
        CustomPagePO po = CustomPageConverter.toPO(cmd);
        customPageMapper.saveCustomPage(po);
    }

    public void saveContent(Long pageId, String content) {
        if (!customPageMapper.existsByPageId(pageId)) {
            throw new BizException("页面不存在");
        }
        customPageContentMapper.saveContent(pageId, content);
    }

    public CustomPageContentPO getContentByPageId(Long pageId) {
        if (!customPageMapper.existsByPageId(pageId)) {
            throw new BizException("页面不存在");
        }
        return customPageContentMapper.getContentByPageId(pageId);
    }

    public CustomPageContentPO getContentByPageCode(String pageCode) {
        CustomPagePO customPagePO = customPageMapper.getByPageCode(pageCode);
        if (customPagePO == null) {
            throw new BizException("页面不存在");
        }
        return getContentByPageId(customPagePO.getPageId());
    }

    public CustomPage<CustomPageVO> queryPages(PageQuery query) {
        Page<CustomPagePO> customPagePOPage = customPageMapper.queryPage(query);
        return DataBaseUtils.createCustomPage(customPagePOPage, CustomPageConverter::toVO);
    }


    @Transactional
    public void deletePage(Long pageId) {
        boolean existsByPageId = customPageMapper.existsByPageId(pageId);
        if (!existsByPageId) {
            throw new BizException("页面不存在");
        }
        customPageMapper.removeByPageId(pageId);
    }

    public void updatePage(@Valid CustomPageCmd cmd) {
        Long pageId = cmd.getPageId();
        CustomPagePO pagePO = customPageMapper.getByPageId(pageId);
        if (cmd.getPageCode() != null && !cmd.getPageCode().equals(pagePO.getPageCode())) {
            if (customPageMapper.existsByPageCode(cmd.getPageCode())) {
                throw new BizException("页面编码已存在");
            }
        }
        pagePO.setPageCode(cmd.getPageCode());
        pagePO.setPageName(cmd.getPageName());
        pagePO.setPageStatus(cmd.getPageStatus());
        pagePO.setAccessLevel(cmd.getAccessLevel());
        customPageMapper.updatePage(pagePO);
    }
}
