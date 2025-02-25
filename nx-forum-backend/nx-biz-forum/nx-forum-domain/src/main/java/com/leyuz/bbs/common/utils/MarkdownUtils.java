package com.leyuz.bbs.common.utils;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * Markdown 工具类
 * 用于将 Markdown 格式的文本转换为 HTML
 *
 * @author system
 */
public class MarkdownUtils {

    /**
     * Flexmark 解析器配置
     * 支持常用的 Markdown 特性：表格、任务列表、自动链接等
     */
    private static final DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(
            Extensions.ALL & ~Extensions.ANCHORLINKS // 支持所有扩展，除了锚点链接
    );

    /**
     * Markdown 解析器
     */
    private static final Parser PARSER = Parser.builder(OPTIONS).build();

    /**
     * HTML 渲染器
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    /**
     * 将 Markdown 格式的文本转换为 HTML
     *
     * @param markdown Markdown 格式的文本
     * @return 转换后的 HTML 字符串
     */
    public static String convertMarkdownToHtml(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return "";
        }

        // 使用 flexmark 解析 Markdown 并转换为 HTML
        var document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }
}

