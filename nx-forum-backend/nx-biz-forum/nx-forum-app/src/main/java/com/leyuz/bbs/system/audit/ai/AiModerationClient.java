package com.leyuz.bbs.system.audit.ai;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.context.ContentInfo;
import com.leyuz.bbs.system.audit.context.PostInfo;
import com.leyuz.bbs.system.audit.context.PosterInfo;
import com.leyuz.bbs.system.config.dto.AiModelProviderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OpenAI 兼容接口的内容审核客户端
 *
 * @author Walker
 */
@Component
@Slf4j
public class AiModerationClient {

    private static final String DEFAULT_PROMPT = "你是论坛内容安全审核助手。请根据给定内容及其上下文（发帖人信息、发帖信息、历史发帖/回帖）判断内容是否违规。判断维度：违法违规、色情低俗、暴力恐怖、人身攻击辱骂、广告营销垃圾信息、政治敏感等。请只输出一个 JSON 对象（不要输出任何其他文字），格式：{\"verdict\":\"PASS|REVIEW|REJECT\",\"reason\":\"详细理由（仅供管理员审计，可包含内部推理细节）\",\"userReason\":\"面向用户的简短违规说明（不超过 50 字，只描述违规事实，不要暴露判断规则、历史行为、推理过程）\",\"confidence\":0.0~1.0}。verdict 含义：PASS=正常放行，REVIEW=存疑转人工复审，REJECT=明显违规。";

    private static final DateTimeFormatter HISTORY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int THREAD_SUMMARY_MAX = 60;
    private static final int COMMENT_CONTENT_MAX = 120;

    /**
     * 带完整上下文审核内容（发帖人信息 + 发帖信息 + 历史发帖/回帖）
     */
    public AiModerationResult moderate(AiModelProviderDTO provider, String model, String prompt, AuditContext context) {
        return call(provider, model, prompt, buildUserMessage(context));
    }

    /**
     * 仅用文本内容审核（用于业务场景测试）
     */
    public AiModerationResult moderate(AiModelProviderDTO provider, String model, String prompt, String content) {
        return call(provider, model, prompt, StringUtils.defaultString(content));
    }

    /**
     * 模型连通性测试：用最小请求验证该模型 ID 能否正常请求
     */
    public String checkConnectivity(String apiUrl, String apiKey, int timeoutSeconds, String model) {
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("max_tokens", 16);
        payload.put("stream", false);
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "请回复 OK");
        messages.add(userMessage);
        payload.put("messages", messages);

        HttpRequest request = HttpRequest.post(buildChatUrl(apiUrl))
                .timeout(timeoutSeconds * 1000)
                .header("Content-Type", "application/json")
                .body(payload.toJSONString());
        if (StringUtils.isNotBlank(apiKey)) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        try {
            try (HttpResponse response = request.execute()) {
                if (!response.isOk()) {
                    throw new AiModerationException("HTTP " + response.getStatus());
                }
                return response.body();
            }
        } catch (AiModerationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiModerationException("请求失败：" + e.getMessage(), e);
        }
    }

    private AiModerationResult call(AiModelProviderDTO provider, String model, String prompt, String userContent) {
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("temperature", 0);
        payload.put("stream", false);
        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", StringUtils.defaultIfBlank(prompt, DEFAULT_PROMPT));
        messages.add(systemMessage);
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);
        payload.put("messages", messages);

        HttpRequest request = HttpRequest.post(buildChatUrl(provider.getApiUrl()))
                .timeout(provider.getTimeoutSeconds() * 1000)
                .header("Content-Type", "application/json")
                .body(payload.toJSONString());
        if (StringUtils.isNotBlank(provider.getApiKey())) {
            request.header("Authorization", "Bearer " + provider.getApiKey());
        }

        try {
            try (HttpResponse response = request.execute()) {
                if (!response.isOk()) {
                    throw new AiModerationException("AI 接口返回异常状态码：" + response.getStatus());
                }
                return parseResponse(response.body());
            }
        } catch (AiModerationException e) {
            throw e;
        } catch (Exception e) {
            throw new AiModerationException("AI 接口调用失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将 base URL 拼接为 chat/completions 地址（兼容直接填完整地址）。
     * <p>apiUrl 为空时抛出带 actionable 提示的异常，避免在调用栈深处 NPE。</p>
     */
    private String buildChatUrl(String apiUrl) {
        String url = StringUtils.trim(apiUrl);
        if (StringUtils.isBlank(url)) {
            throw new AiModerationException("AI 提供方 API 地址未配置（apiUrl 为空），请在 AI 模型管理中补全");
        }
        if (url.endsWith("/chat/completions")) {
            return url;
        }
        return url.replaceAll("/+$", "") + "/chat/completions";
    }

    private String buildUserMessage(AuditContext context) {
        ContentInfo content = context.getContent();
        PosterInfo poster = context.getPoster();
        PostInfo post = context.getPost();
        StringBuilder sb = new StringBuilder();
        sb.append("待审核内容：\n").append(StringUtils.defaultString(content.getText())).append("\n\n");

        sb.append("【发帖人信息】\n");
        if (poster != null) {
            sb.append("用户名：").append(StringUtils.defaultString(poster.getUserName())).append("\n");
            sb.append("注册时间：").append(poster.getRegisterTime()).append("（注册 ").append(poster.getRegisterDays()).append(" 天）\n");
            sb.append("所在地：").append(StringUtils.defaultString(poster.getLocation())).append("\n");
            sb.append("历史发帖数：").append(poster.getPostCount()).append("，历史评论数：").append(poster.getCommentCount()).append("\n");
        }

        sb.append("\n【发帖信息】\n");
        if (post != null) {
            sb.append("类型：").append(StringUtils.defaultString(post.getContentType())).append("\n");
            sb.append("操作：").append(Boolean.FALSE.equals(post.getIsNew()) ? "编辑" : "新建").append("\n");
            sb.append("版块：").append(StringUtils.defaultString(post.getForumName())).append("\n");
            sb.append("标题：").append(StringUtils.defaultString(post.getSubject())).append("\n");
            sb.append("发帖时间：").append(post.getCreateTime()).append("\n");
            sb.append("发帖地址：").append(StringUtils.defaultString(post.getIpLocation())).append("\n");
        }

        appendThreads(sb, "\n【历史发帖】", context.getRecentThreads());
        appendComments(sb, "\n【历史评论】", context.getRecentComments());
        return sb.toString();
    }

    private void appendThreads(StringBuilder sb, String title, List<ThreadHistoryItemV> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(title).append("\n");
        for (ThreadHistoryItemV item : items) {
            sb.append("- [").append(formatTime(item.getCreateTime())).append("] 标题：")
                    .append(StringUtils.defaultString(item.getTitle())).append(" 概要：")
                    .append(formatSummary(item.getSummary())).append("\n");
        }
    }

    private void appendComments(StringBuilder sb, String title, List<CommentHistoryItemV> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        sb.append(title).append("\n");
        for (CommentHistoryItemV item : items) {
            sb.append("- [").append(formatTime(item.getCreateTime())).append("] 内容：")
                    .append(StringUtils.abbreviate(StringUtils.defaultString(item.getContent()), COMMENT_CONTENT_MAX))
                    .append("\n");
        }
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : HISTORY_TIME_FORMAT.format(time);
    }

    private String formatSummary(String summary) {
        if (StringUtils.isBlank(summary)) {
            return "[无概要]";
        }
        return StringUtils.abbreviate(summary.trim(), THREAD_SUMMARY_MAX);
    }

    private AiModerationResult parseResponse(String body) {
        JSONObject root = JSON.parseObject(body);
        if (root == null) {
            throw new AiModerationException("AI 接口返回内容为空");
        }
        JSONArray choices = root.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new AiModerationException("AI 接口返回缺少 choices");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (firstChoice == null) {
            throw new AiModerationException("AI 接口返回 choices[0] 为空");
        }
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            throw new AiModerationException("AI 接口返回缺少 message 字段（可能 finish_reason 非 stop）");
        }
        String content = message.getString("content");
        if (StringUtils.isBlank(content)) {
            throw new AiModerationException("AI 接口返回缺少判定内容");
        }
        return parseVerdict(content);
    }

    /**
     * 解析模型输出（允许被 markdown 代码块包裹）
     */
    private AiModerationResult parseVerdict(String raw) {
        String content = raw.trim();
        // 去掉 markdown 代码块
        if (content.startsWith("```")) {
            int firstNewline = content.indexOf('\n');
            if (firstNewline >= 0) {
                content = content.substring(firstNewline + 1);
            }
            int closing = content.lastIndexOf("```");
            if (closing >= 0) {
                content = content.substring(0, closing);
            }
            content = content.trim();
        }
        // 去掉 <think>...</think> 推理块（推理型模型）
        content = content.replaceAll("(?s)<think>.*?</think>", "").trim();

        JSONObject json = parseJsonObject(content);
        AiModerationResult result = new AiModerationResult();
        if (json == null) {
            // 无法解析则按存疑处理，转人工复审
            result.setVerdict(AiVerdict.REVIEW);
            result.setReason(StringUtils.abbreviate(raw, 200));
            result.setConfidence(0);
            return result;
        }
        String verdict = json.getString("verdict");
        result.setVerdict(parseVerdictValue(verdict));
        result.setReason(json.getString("reason"));
        result.setUserReason(json.getString("userReason"));
        Double confidence = json.getDouble("confidence");
        result.setConfidence(confidence == null ? 0 : confidence);
        return result;
    }

    private JSONObject parseJsonObject(String content) {
        try {
            return JSON.parseObject(content);
        } catch (Exception ignore) {
            // 尝试提取第一个 { 到最后一个 } 之间的 JSON
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                try {
                    return JSON.parseObject(content.substring(start, end + 1));
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }
    }

    private AiVerdict parseVerdictValue(String value) {
        if (StringUtils.isBlank(value)) {
            return AiVerdict.REVIEW;
        }
        return switch (value.trim().toUpperCase()) {
            case "PASS", "SAFE", "正常", "通过" -> AiVerdict.PASS;
            case "REJECT", "VIOLATION", "违规", "拒绝" -> AiVerdict.REJECT;
            default -> AiVerdict.REVIEW;
        };
    }
}
