package com.leyuz.bbs.admin;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.image.ImageApplication;
import com.leyuz.bbs.system.image.dto.ImagePageQuery;
import com.leyuz.bbs.system.image.dto.ImageVO;
import com.leyuz.common.mybatis.CustomPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台图片管理")
@RestController
@RequestMapping("/v1/admin/images")
@RequiredArgsConstructor
public class AdminImageController {

    private final ImageApplication imageApplication;

    @Operation(summary = "分页查询图片")
    @GetMapping
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:image')")
    public SingleResponse<CustomPage<ImageVO>> queryPage(
            @RequestParam(required = false) Integer imageType,
            @RequestParam(required = false) Integer storageType,
            @RequestParam(required = false) Long createBy,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        ImagePageQuery query = new ImagePageQuery();
        query.setImageType(imageType);
        query.setStorageType(storageType);
        query.setCreateBy(createBy);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return SingleResponse.of(imageApplication.queryPage(query));
    }

    @Operation(summary = "批量删除图片")
    @DeleteMapping
    @PreAuthorize("@permissionResolver.hasPermission('admin:system:image')")
    public SingleResponse<Void> deleteBatch(@RequestBody List<Long> imageIds) {
        imageApplication.deleteBatch(imageIds);
        return SingleResponse.buildSuccess();
    }
} 