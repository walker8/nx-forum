package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.page.CustomPageApplication;
import com.leyuz.bbs.system.page.CustomPageContentPO;
import com.leyuz.bbs.system.page.dto.CustomPageCmd;
import com.leyuz.bbs.system.page.dto.CustomPageVO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台自定义页面管理")
@RestController
@RequestMapping("/v1/admin/pages")
@RequiredArgsConstructor
public class AdminCustomPageController {

    private final CustomPageApplication customPageApplication;

    @Operation(summary = "创建页面")
    @PostMapping
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:page')")
    public SingleResponse<Void> createPage(@RequestBody @Valid CustomPageCmd cmd) {
        customPageApplication.createPage(cmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "分页查询页面")
    @GetMapping
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:page')")
    public SingleResponse<CustomPage<CustomPageVO>> queryPages(
            @RequestParam(defaultValue = "") String pageName,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageQuery query = PageQuery.builder().pageNo(pageNo).pageSize(pageSize).build();
        return SingleResponse.of(customPageApplication.queryPages(query));
    }

    @Operation(summary = "更新页面")
    @PutMapping("/{pageId}")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:page')")
    public SingleResponse<Void> updatePage(
            @PathVariable Long pageId,
            @RequestBody @Valid CustomPageCmd cmd) {
        cmd.setPageId(pageId);
        customPageApplication.updatePage(cmd);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "保存页面内容")
    @PutMapping("/{pageId}/content")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:page')")
    public SingleResponse<Void> saveContent(@PathVariable Long pageId, @RequestBody String content) {
        customPageApplication.saveContent(pageId, content);
        return SingleResponse.buildSuccess();
    }

    @Operation(summary = "获取页面内容")
    @GetMapping("/{pageId}/content")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:page')")
    public SingleResponse<String> getContent(@PathVariable Long pageId) {
        CustomPageContentPO contentPO = customPageApplication.getContentByPageId(pageId);
        return SingleResponse.of(contentPO == null ? "" : contentPO.getContent());
    }

    @Operation(summary = "删除页面")
    @DeleteMapping("/{pageId}")
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:page')")
    public SingleResponse<Void> deletePage(@PathVariable Long pageId) {
        customPageApplication.deletePage(pageId);
        return SingleResponse.buildSuccess();
    }
} 