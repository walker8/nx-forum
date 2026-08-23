package com.leyuz.bbs.system.audit.rule;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * QLExpress 规则内置函数
 *
 * @author Walker
 */
public final class AuditFunctions {
    private AuditFunctions() {
    }

    /**
     * 判断文本是否包含关键词
     */
    public static boolean contains(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.contains(keyword);
    }

    /**
     * 判断文本是否匹配正则
     */
    public static boolean matchRegex(String text, String pattern) {
        if (text == null || pattern == null) {
            return false;
        }
        try {
            return Pattern.compile(pattern).matcher(text).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
