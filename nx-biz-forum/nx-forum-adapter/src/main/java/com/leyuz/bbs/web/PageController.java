package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.page.CustomPageApplication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/v1/pages")
public class PageController {
    @Autowired
    private CustomPageApplication customPageApplication;

    @Operation(summary = "")
    @GetMapping("/{pageCode}/content")
    @PermitAll
    public SingleResponse getContentByPageCode(@PathVariable String pageCode) {
        String content = customPageApplication.getContentByPageCode(pageCode).getContent();
        return SingleResponse.of(content);
    }

}
