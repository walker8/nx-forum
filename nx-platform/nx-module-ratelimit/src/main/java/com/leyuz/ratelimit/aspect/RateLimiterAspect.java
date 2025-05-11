package com.leyuz.ratelimit.aspect;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.anno.CacheType;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.module.cache.CacheService;
import com.leyuz.ratelimit.annotation.LimitType;
import com.leyuz.ratelimit.annotation.MatchMode;
import com.leyuz.ratelimit.annotation.RateLimitRule;
import com.leyuz.ratelimit.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class RateLimiterAspect {
    @Autowired
    private CacheService cacheService;
    @Value("${nx.rate-limit.disabled:false}")
    private boolean disableRateLimit;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private Cache<String, Long[]> cache;

    @PostConstruct
    public void init() {
        cache = cacheService.create("system", 60 * 60L, CacheType.REMOTE);
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        if (disableRateLimit) {
            // 不限流，方便测试环境调试
            return;
        }
        RateLimitRule[] rules = rateLimiter.rules();

        // 处理单条规则的情况
        if (rules.length == 0) {
            RateLimitRule singleRule = new RateLimitRule() {
                @Override
                public String prefix() {
                    return rateLimiter.prefix();
                }

                @Override
                public String key() {
                    return rateLimiter.key();
                }

                @Override
                public int time() {
                    return rateLimiter.time();
                }

                @Override
                public TimeUnit timeUnit() {
                    return rateLimiter.timeUnit();
                }

                @Override
                public int count() {
                    return rateLimiter.count();
                }

                @Override
                public LimitType limitType() {
                    return rateLimiter.limitType();
                }

                @Override
                public String message() {
                    return rateLimiter.message();
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return RateLimitRule.class;
                }
            };

            if (!checkRule(singleRule, point)) {
                throw new ValidationException(singleRule.message());
            }
            return;
        }

        // 处理多条规则的情况
        if (rateLimiter.matchMode() == MatchMode.OR) {
            // OR模式：任一规则通过即可，收集所有失败规则的错误信息
            Optional<RateLimitRule> anyMatch = Arrays.stream(rules)
                    .filter(rule -> checkRule(rule, point))
                    .findFirst();

            if (anyMatch.isEmpty()) {
                // 所有规则都失败，组合所有错误信息
                String errorMessage = Arrays.stream(rules)
                        .map(RateLimitRule::message)
                        .distinct()
                        .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                        .orElse("访问过于频繁，请稍后重试");

                throw new ValidationException(errorMessage);
            }
        } else {
            // AND模式：所有规则都要通过，返回第一个失败规则的错误信息
            Optional<RateLimitRule> firstFailed = Arrays.stream(rules)
                    .filter(rule -> !checkRule(rule, point))
                    .findFirst();

            if (firstFailed.isPresent()) {
                throw new ValidationException(firstFailed.get().message());
            }
        }
    }

    private boolean checkRule(RateLimitRule rule, JoinPoint point) {
        String key = buildKey(rule, point);
        long windowMillis = rule.timeUnit().toMillis(rule.time());
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - windowMillis;

        // 获取当前时间戳列表（按时间排序）
        CacheGetResult<Long[]> result = cache.GET(key);
        Long[] timestamps = result.isSuccess() ? result.getValue() : new Long[0];

        // 过滤出在时间窗口内的请求
        long validCount = Arrays.stream(timestamps)
                .filter(ts -> ts >= windowStart)
                .count();

        if (validCount >= rule.count()) {
            log.debug("Rate limit exceeded for key: {}, rule: {}/{} {}, message: {}",
                    key, rule.count(), rule.time(), rule.timeUnit(), rule.message());
            return false;
        }

        // 添加当前时间戳并修剪过期数据
        Long[] updated = Arrays.copyOf(timestamps, timestamps.length + 1);
        updated[timestamps.length] = currentTime;

        // 保留最近N个时间戳（N=限流次数+1）避免存储过多无效数据
        int keepSize = Math.min(updated.length, rule.count() + 1);
        Long[] trimmed = Arrays.copyOfRange(updated, updated.length - keepSize, updated.length);

        // 更新缓存（设置过期时间为窗口时间+1分钟防止数据过早消失）
        cache.put(key, trimmed, windowMillis / 1000 + 60, TimeUnit.SECONDS);
        return true;
    }

    private String buildKey(RateLimitRule rule, JoinPoint point) {
        StringBuilder key = new StringBuilder(rule.prefix());

        // 根据限流类型构建key
        switch (rule.limitType()) {
            case IP -> key.append(HeaderUtils.getIp());
            case USER_ID -> key.append(HeaderUtils.getUserId());
            case DEFAULT -> {
                if (rule.key().isEmpty()) {
                    MethodSignature signature = (MethodSignature) point.getSignature();
                    Method method = signature.getMethod();
                    key.append(method.getDeclaringClass().getName()).append(".").append(method.getName());
                } else {
                    key.append("default");
                }
            }
        }

        // 解析SpEL表达式
        if (StringUtils.hasText(rule.key()) && rule.key().contains("#")) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            Object[] args = point.getArgs();
            Object target = point.getTarget();

            EvaluationContext context = new MethodBasedEvaluationContext(
                    target, method, args, nameDiscoverer);

            String spElValue = parser.parseExpression(rule.key()).getValue(context, String.class);
            if (spElValue != null) {
                key.append(":").append(spElValue);
            }
        } else if (StringUtils.hasText(rule.key())) {
            key.append(":").append(rule.key());
        }

        return key.toString();
    }
} 