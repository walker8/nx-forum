package com.leyuz.bbs.common.utils;

import io.micrometer.common.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    private static Pattern urlPattern = Pattern.compile(
            "(http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+)"
    );

    /**
     * 将文本中的url转换为html超链接
     *
     * @param text 文本
     * @return 转换后的文本
     */
    public static String convertLinksToHtml(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        // Create a matcher for the input text
        Matcher matcher = urlPattern.matcher(text);

        // Use StringBuffer to hold the result
        StringBuffer result = new StringBuffer();

        // Replace all occurrences of the URL pattern with HTML links
        while (matcher.find()) {
            String url = matcher.group(1);
            matcher.appendReplacement(result, "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 将纯文本转换为安全的HTML，包括转义HTML特殊字符，将URL转换为超链接，并添加换行符。
     *
     * @param text           纯文本
     * @param allowLineBreak 是否允许换行符
     * @return
     */
    public static String convertToSafeHtml(String text, boolean allowLineBreak) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        // Encode the HTML to prevent XSS attacks
        String html = Encode.forHtml(text);
        if (allowLineBreak) {
            return convertLinksToHtml(html.replace("\n", "<br>"));
        } else {
            return convertLinksToHtml(html.replace("\n", " "));
        }
    }

    /**
     * 将HTML转换为纯文本，包括移除HTML标签和样式信息。
     *
     * @param html
     * @return
     */
    public static String convertHtmlToText(String html) {
        if (StringUtils.isEmpty(html)) {
            return "";
        }
        Document doc = Jsoup.parse(html);
        return doc.text();
    }
}
