package com.leyuz.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 支持多条限流规则的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流规则列表
     */
    RateLimitRule[] rules() default {};

    /**
     * 规则匹配模式
     * AND: 所有规则都通过才允许访问
     * OR: 任一规则通过即允许访问
     */
    MatchMode matchMode() default MatchMode.AND;

    /**
     * 单规则限流 - key前缀
     */
    String prefix() default "rate_limit:";

    /**
     * 单规则限流 - key，支持SpEL表达式
     */
    String key() default "";

    /**
     * 单规则限流 - 时间窗口，默认1秒
     */
    int time() default 1;

    /**
     * 单规则限流 - 时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 单规则限流 - 限流次数，默认100次
     */
    int count() default 100;

    /**
     * 单规则限流 - 限流类型，默认按接口限流
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 单规则限流 - 自定义错误提示信息
     */
    String message() default "操作过于频繁，请稍后重试";
}