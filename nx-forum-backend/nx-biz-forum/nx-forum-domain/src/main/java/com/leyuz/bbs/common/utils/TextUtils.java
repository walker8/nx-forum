package com.leyuz.bbs.common.utils;

import io.micrometer.common.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    private static final String AT_REGEX = "@([^@<&\\[\\s]{2,32})";

    public static Set<String> getMentions(String message) {
        Set<String> mentions = new HashSet<>();
        if (StringUtils.isBlank(message)) {
            return mentions;
        }
        Pattern pattern = Pattern.compile(AT_REGEX);
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }
}
