package com.leyuz.bbs.system.audit.rule;

import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.context.ContentInfo;
import com.leyuz.bbs.system.audit.context.PostInfo;
import com.leyuz.bbs.system.audit.context.PosterInfo;
import com.leyuz.bbs.system.config.dto.AuditRuleDTO;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * QLExpress 规则引擎
 *
 * @author Walker
 */
@Component
@Slf4j
public class RuleEngine {
    private final ExpressRunner runner;

    public RuleEngine() {
        this.runner = new ExpressRunner();
        try {
            // 注册内置函数
            this.runner.addFunctionOfClassMethod("contains", AuditFunctions.class.getName(), "contains",
                    new Class[]{String.class, String.class}, null);
            this.runner.addFunctionOfClassMethod("matchRegex", AuditFunctions.class.getName(), "matchRegex",
                    new Class[]{String.class, String.class}, null);
        } catch (Exception e) {
            throw new IllegalStateException("初始化规则引擎失败", e);
        }
    }

    /**
     * 按优先级评估规则，返回首个命中的规则，未命中返回 null
     */
    public AuditRuleDTO check(AuditContext context, List<AuditRuleDTO> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        DefaultContext<String, Object> qlContext = new DefaultContext<>();
        qlContext.put("poster", context.getPoster());
        qlContext.put("post", context.getPost());
        qlContext.put("content", context.getContent());
        return rules.stream()
                .filter(AuditRuleDTO::isEnabled)
                .filter(rule -> StringUtils.isNotBlank(rule.getExpression()))
                .sorted(Comparator.comparingInt(AuditRuleDTO::getPriority))
                .filter(rule -> evaluate(rule.getExpression(), qlContext))
                .findFirst()
                .orElse(null);
    }

    private boolean evaluate(String expression, DefaultContext<String, Object> context) {
        try {
            Object result = runner.execute(expression, context, new ArrayList<>(), false, false);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("规则表达式执行失败: {}", expression, e);
            return false;
        }
    }

    /**
     * 校验表达式语法与返回类型，返回错误信息，合法返回 null
     */
    public String validate(String expression) {
        if (StringUtils.isBlank(expression)) {
            return "表达式不能为空";
        }
        DefaultContext<String, Object> context = new DefaultContext<>();
        context.put("poster", PosterInfo.builder().userId(1L).userName("测试用户").registerDays(10).postCount(5).commentCount(5).build());
        context.put("post", PostInfo.builder().isNew(true).contentType("THREAD").forumId(1).forumName("测试版块").subject("测试标题").hour(12).build());
        context.put("content", ContentInfo.builder().text("测试内容").length(4).linkCount(0).hasLink(false).imageCount(0).build());
        try {
            Object result = runner.execute(expression, context, new ArrayList<>(), false, false);
            if (!(result instanceof Boolean)) {
                return "表达式结果必须是布尔值（true/false）";
            }
            return null;
        } catch (Exception e) {
            return "表达式语法错误：" + e.getMessage();
        }
    }
}
