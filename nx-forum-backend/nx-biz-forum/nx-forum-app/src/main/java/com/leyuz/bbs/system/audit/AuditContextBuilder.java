package com.leyuz.bbs.system.audit;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyuz.bbs.common.dataobject.CommentHistoryItemV;
import com.leyuz.bbs.common.dataobject.ThreadHistoryItemV;
import com.leyuz.bbs.content.comment.CommentMapper;
import com.leyuz.bbs.content.comment.CommentPO;
import com.leyuz.bbs.content.comment.CommentReplyMapper;
import com.leyuz.bbs.content.comment.CommentReplyPO;
import com.leyuz.bbs.content.comment.gateway.CommentGateway;
import com.leyuz.bbs.content.thread.ThreadMapper;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.system.audit.context.AuditContext;
import com.leyuz.bbs.system.audit.context.ContentInfo;
import com.leyuz.bbs.system.audit.context.PostInfo;
import com.leyuz.bbs.system.audit.context.PosterInfo;
import com.leyuz.bbs.system.audit.dto.AuditContentType;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.dto.UserVO;
import com.leyuz.common.ip.AddressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 审核上下文构建器：聚合发帖人信息、发帖信息、内容信息与历史发帖/回帖
 *
 * @author Walker
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditContextBuilder {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", Pattern.CASE_INSENSITIVE);

    private final UserApplication userApplication;
    private final ForumApplication forumApplication;
    private final ThreadGateway threadGateway;
    private final CommentGateway commentGateway;
    private final ThreadMapper threadMapper;
    private final CommentMapper commentMapper;
    private final CommentReplyMapper commentReplyMapper;

    /**
     * 构建审核上下文
     *
     * @param userId      发帖人 ID
     * @param contentType 内容类型
     * @param isNew       是否新建（false 为编辑）
     * @param forumId     版块 ID
     * @param subject     标题
     * @param textContent 纯文本内容
     * @param rawContent  原始内容（帖子为 HTML，用于外链检测）
     * @param ip          发帖 IP
     * @param createTime  发帖时间
     * @param historyCount 历史上下文条数
     */
    public AuditContext build(Long userId, AuditContentType contentType, Boolean isNew, Integer forumId,
                              String subject, String textContent, String rawContent, String ip,
                              LocalDateTime createTime, int historyCount) {
        PosterInfo poster = buildPoster(userId, ip);
        PostInfo post = buildPost(contentType, isNew, forumId, subject, ip, createTime);
        ContentInfo content = buildContent(textContent, rawContent);
        List<ThreadHistoryItemV> threads = new ArrayList<>();
        List<CommentHistoryItemV> comments = new ArrayList<>();
        // 历史上下文按内容类型分支：主题帖场景只填 recentThreads，评论/楼中楼场景只填 recentComments
        if (contentType == AuditContentType.THREAD) {
            threads = loadRecentThreads(userId, historyCount);
        } else if (contentType == AuditContentType.COMMENT || contentType == AuditContentType.REPLY) {
            comments = loadRecentComments(userId, historyCount);
        }
        return AuditContext.builder()
                .poster(poster)
                .post(post)
                .content(content)
                .recentThreads(threads)
                .recentComments(comments)
                .build();
    }

    private PosterInfo buildPoster(Long userId, String ip) {
        long postCount = 0;
        long commentCount = 0;
        String userName = "";
        LocalDateTime registerTime = null;
        LocalDateTime lastActiveDate = null;
        if (userId != null) {
            try {
                UserVO userVO = userApplication.getUserInfo(userId);
                userName = userVO.getUserName();
                registerTime = userVO.getCreateTime();
                lastActiveDate = userVO.getLastActiveDate();
            } catch (Exception e) {
                log.warn("获取用户信息失败，userId = {}", userId);
            }
            postCount = threadMapper.selectCount(new LambdaQueryWrapper<ThreadPO>()
                    .eq(ThreadPO::getCreateBy, userId)
                    .eq(ThreadPO::getIsDeleted, false));
            commentCount = commentMapper.selectCount(new LambdaQueryWrapper<CommentPO>()
                    .eq(CommentPO::getCreateBy, userId)
                    .eq(CommentPO::getIsDeleted, false))
                    + commentReplyMapper.selectCount(new LambdaQueryWrapper<CommentReplyPO>()
                    .eq(CommentReplyPO::getCreateBy, userId)
                    .eq(CommentReplyPO::getIsDeleted, false));
        }
        long registerDays = registerTime == null ? 0
                : ChronoUnit.DAYS.between(registerTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        return PosterInfo.builder()
                .userId(userId)
                .userName(userName)
                .registerTime(registerTime)
                .registerDays(registerDays)
                .location(AddressUtils.getRegionByIP(ip))
                .lastActiveDate(lastActiveDate)
                .postCount(postCount)
                .commentCount(commentCount)
                .build();
    }

    private PostInfo buildPost(AuditContentType contentType, Boolean isNew, Integer forumId,
                               String subject, String ip, LocalDateTime createTime) {
        String forumName = "";
        if (forumId != null) {
            ForumPO forumPO = forumApplication.getForumById(forumId);
            if (forumPO != null) {
                forumName = StringUtils.defaultIfBlank(forumPO.getNickName(), forumPO.getName());
            }
        }
        LocalDateTime time = createTime == null ? LocalDateTime.now() : createTime;
        return PostInfo.builder()
                .isNew(isNew)
                .contentType(contentType == null ? null : contentType.name())
                .forumId(forumId)
                .forumName(forumName)
                .subject(subject)
                .ip(ip)
                .ipLocation(AddressUtils.getCityByIP(ip))
                .createTime(time)
                .hour(time.getHour())
                .build();
    }

    private ContentInfo buildContent(String textContent, String rawContent) {
        String text = StringUtils.defaultString(textContent);
        List<String> urls = new ArrayList<>();
        collectUrls(urls, text);
        if (StringUtils.isNotBlank(rawContent)) {
            collectUrls(urls, rawContent);
        }
        int linkCount = urls.size();
        return ContentInfo.builder()
                .text(text)
                .length(text.length())
                .linkCount(linkCount)
                .externalLinkCount(linkCount)
                .hasLink(linkCount > 0)
                .imageCount(0)
                .build();
    }

    private void collectUrls(List<String> urls, String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        Matcher matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
    }

    /**
     * 历史主题帖：直接调 Gateway；查询异常时降级为空列表，不影响 AI 审核整体流程
     */
    private List<ThreadHistoryItemV> loadRecentThreads(Long userId, int n) {
        if (userId == null || n <= 0) {
            return new ArrayList<>();
        }
        try {
            return threadGateway.listRecentThreads(userId, n);
        } catch (Exception e) {
            log.warn("加载用户历史主题帖失败，userId = {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 历史评论（含楼中楼）：调两次 Gateway，合并后按 createTime desc 取 limit
     * 任一查询异常时降级为空列表
     */
    private List<CommentHistoryItemV> loadRecentComments(Long userId, int n) {
        if (userId == null || n <= 0) {
            return new ArrayList<>();
        }
        List<CommentHistoryItemV> merged = new ArrayList<>();
        try {
            List<CommentHistoryItemV> comments = commentGateway.listRecentComments(userId, n);
            if (comments != null) {
                merged.addAll(comments);
            }
        } catch (Exception e) {
            log.warn("加载用户历史评论失败，userId = {}", userId, e);
        }
        try {
            List<CommentHistoryItemV> replies = commentGateway.listRecentReplies(userId, n);
            if (replies != null) {
                merged.addAll(replies);
            }
        } catch (Exception e) {
            log.warn("加载用户历史楼中楼失败，userId = {}", userId, e);
        }
        if (merged.isEmpty()) {
            return merged;
        }
        merged.sort(Comparator.comparing(CommentHistoryItemV::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (merged.size() > n) {
            return new ArrayList<>(merged.subList(0, n));
        }
        return merged;
    }
}