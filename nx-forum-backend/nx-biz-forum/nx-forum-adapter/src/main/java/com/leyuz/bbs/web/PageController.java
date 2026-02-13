package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.system.page.CustomPageApplication;
import com.leyuz.bbs.system.page.dto.CustomPageInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 自定义页面
 * </p>
 *
 * @author walker
 * @since 2025-02-20
 */
@Tag(name = "自定义页面")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/pages")
public class PageController {
    private final CustomPageApplication customPageApplication;

    @Operation(summary = "获取自定义页面信息")
    @GetMapping("/{pageCode}")
    @PermitAll
    public SingleResponse getPageInfo(@PathVariable String pageCode) {
        CustomPageInfoVO pageInfo = customPageApplication.getPageInfoByPageCode(pageCode);
        return SingleResponse.of(pageInfo);
    }

}
