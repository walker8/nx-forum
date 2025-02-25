package com.leyuz.ratelimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 单条限流规则定义
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitRule {
    /**
     * 限流key前缀
     */
    String prefix() default "rate_limit:";

    /**
     * 限流key，支持SpEL表达式
     */
    String key() default "";

    /**
     * 限流时间窗口，默认1秒
     */
    int time() default 1;

    /**
     * 时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 限流次数，默认100次
     */
    int count() default 100;

    /**
     * 限流类型，默认按接口限流
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 自定义错误提示信息
     */
    String message() default "操作过于频繁，请稍后重试";
} 