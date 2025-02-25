package com.leyuz.bbs.page;

import com.alibaba.cola.exception.BizException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.page.converter.CustomPageConverter;
import com.leyuz.bbs.page.dto.CustomPageCmd;
import com.leyuz.bbs.page.dto.CustomPageVO;
import com.leyuz.bbs.page.mybatis.CustomPageService;
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
    private final CustomPageService customPageService;

    @Transactional
    public void createPage(CustomPageCmd cmd) {
        // 校验页面编码唯一性
        if (customPageService.existsByPageCode(cmd.getPageCode())) {
            throw new BizException("页面编码已存在");
        }
        CustomPagePO po = CustomPageConverter.toPO(cmd);
        customPageService.save(po);
    }

    public void saveContent(Long pageId, String content) {
        if (!customPageService.existsByPageId(pageId)) {
            throw new BizException("页面不存在");
        }
        customPageService.saveContent(pageId, content);
    }

    public CustomPageContentPO getContentByPageId(Long pageId) {
        if (!customPageService.existsByPageId(pageId)) {
            throw new BizException("页面不存在");
        }
        return customPageService.getContentByPageId(pageId);
    }

    public CustomPageContentPO getContentByPageCode(String pageCode) {
        CustomPagePO customPagePO = customPageService.getByPageCode(pageCode);
        if (customPagePO == null) {
            throw new BizException("页面不存在");
        }
        return getContentByPageId(customPagePO.getPageId());
    }

    public CustomPage<CustomPageVO> queryPages(PageQuery query) {
        Page<CustomPagePO> customPagePOPage = customPageService.page(query);
        return DataBaseUtils.createCustomPage(customPagePOPage, CustomPageConverter::toVO);
    }


    @Transactional
    public void deletePage(Long pageId) {
        boolean existsByPageId = customPageService.existsByPageId(pageId);
        if (!existsByPageId) {
            throw new BizException("页面不存在");
        }
        customPageService.removeById(pageId);
    }

    public void updatePage(@Valid CustomPageCmd cmd) {
        Long pageId = cmd.getPageId();
        CustomPagePO pagePO = customPageService.getById(pageId);
        if (cmd.getPageCode() != null && !cmd.getPageCode().equals(pagePO.getPageCode())) {
            if (customPageService.existsByPageCode(cmd.getPageCode())) {
                throw new BizException("页面编码已存在");
            }
        }
        pagePO.setPageCode(cmd.getPageCode());
        pagePO.setPageName(cmd.getPageName());
        pagePO.setPageStatus(cmd.getPageStatus());
        pagePO.setAccessLevel(cmd.getAccessLevel());
        customPageService.update(pagePO);
    }
}
