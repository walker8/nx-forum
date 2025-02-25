package com.leyuz.bbs.system.rss;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.thread.ThreadPO;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyTypeV;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.content.thread.strategy.ThreadQueryStrategyFactory;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.system.config.ForumConfigApplication;
import com.leyuz.bbs.system.config.dto.WebsiteBaseInfoVO;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.uc.user.UserApplication;
import com.leyuz.uc.user.UserE;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssApplication {
    private final ThreadGateway threadGateway;
    private final ForumApplication forumApplication;
    private final ThreadQueryStrategyFactory threadQueryStrategyFactory;
    private final UserApplication userApplication;
    private final ForumConfigApplication forumConfigApplication;
    private final ForumPermissionResolver forumPermissionResolver;

    public String getForumRss(String forumName) throws FeedException {
        String forumNickName = forumName;
        ForumPO forumPO = forumApplication.getByName(forumName);
        if (forumPO == null) {
            throw new FeedException("版块不存在");
        }

        Boolean isSystem = forumPO.getIsSystem();
        if (Boolean.FALSE.equals(isSystem)) {
            // 非系统内置版块
            forumPermissionResolver.checkPermission(forumPO.getForumId(), "thread:view");
        }

        if (forumPO.getNickName() != null) {
            forumNickName = forumPO.getNickName();
        }
        WebsiteBaseInfoVO baseInfoVO = forumConfigApplication.getWebsiteBaseInfoByAdmin();
        // 构建查询条件
        ThreadQuery query = ThreadQuery.builder()
                .forumName(Boolean.TRUE.equals(isSystem) ? "" : forumName)
                .auditStatusV(AuditStatusV.PASSED)
                .deleted(false)
                .forumId(Boolean.TRUE.equals(isSystem) ? 0 : forumPO.getForumId())
                .keyword("")
                .orderBy("create_time")
                .pageNo(1)
                .pageSize(20)
                .build();

        String feedTitle = forumNickName + "版块最新文章" + "-" + baseInfoVO.getWebsiteName();
        String feedDescription = forumNickName + "版块最新文章RSS订阅";
        if (Boolean.TRUE.equals(isSystem)) {
            if ("hot".equals(forumName)) {
                feedTitle = "最热文章" + "-" + baseInfoVO.getWebsiteName();
                feedDescription = "最热文章RSS订阅";
                query.setOrderBy("views");
                query.setDays(30);
            } else if ("digest".equals(forumName)) {
                feedTitle = "精华文章" + "-" + baseInfoVO.getWebsiteName();
                feedDescription = "精华文章RSS订阅";
                query.setPropertyTypeV(ThreadPropertyTypeV.DIGEST);
            } else if ("newest".equals(forumName)) {
                feedTitle = "最新文章" + "-" + baseInfoVO.getWebsiteName();
                feedDescription = "最新文章RSS订阅";
            } else if ("recommend".equals(forumName)) {
                feedTitle = "推荐文章" + "-" + baseInfoVO.getWebsiteName();
                feedDescription = "推荐文章RSS订阅";
                query.setPropertyTypeV(ThreadPropertyTypeV.RECOMMEND);
            }
        }

        // 创建RSS Feed
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle(feedTitle);
        // 获取当前域名
        String domain = HeaderUtils.getDomain();
        feed.setLink(domain + "/f/" + forumName);
        feed.setDescription(feedDescription);
        feed.setLanguage("zh-CN");
        feed.setPublishedDate(new Date());

        // 获取文章列表
        Page<ThreadPO> threadPage = threadQueryStrategyFactory.getStrategy("rss").query(query);
        List<Integer> accessibleForumIds = new ArrayList<>(16);
        if (Boolean.TRUE.equals(isSystem)) {
            threadPage.getRecords().stream().map(ThreadPO::getForumId).distinct().forEach(forumId -> {
                if (forumPermissionResolver.hasPermission(forumId, "thread:view")) {
                    accessibleForumIds.add(forumId);
                }
            });
        } else {
            accessibleForumIds.add(forumPO.getForumId());
        }
        List<SyndEntry> entries = getSyndEntries(threadPage, accessibleForumIds);
        feed.setEntries(entries);

        // 输出RSS Feed
        return new SyndFeedOutput().outputString(feed);
    }

    public String getAuthorRss(String authorName) throws FeedException {
        // 创建RSS Feed
        SyndFeed feed = new SyndFeedImpl();
        String domain = HeaderUtils.getDomain();
        feed.setFeedType("rss_2.0");
        feed.setLink(domain);
        WebsiteBaseInfoVO baseInfoVO = forumConfigApplication.getWebsiteBaseInfoByAdmin();
        feed.setTitle(authorName + "用户最新主题" + "-" + baseInfoVO.getWebsiteName());
        feed.setDescription(authorName + "用户最新主题RSS订阅");
        UserE userE = userApplication.getByUserNameFromCache(authorName);
        if (userE != null) {
            feed.setLink(domain + "/user/" + userE.getUserId());
        }
        feed.setLanguage("zh-CN");
        feed.setPublishedDate(new Date());
        // 构建查询条件
        ThreadQuery query = ThreadQuery.builder()
                .auditStatusV(AuditStatusV.PASSED)
                .deleted(false)
                .authorName(authorName)
                .keyword("")
                .orderBy("create_time")
                .pageNo(1)
                .pageSize(20)
                .build();

        // 获取文章列表
        Page<ThreadPO> threadPage = threadQueryStrategyFactory.getStrategy("rss").query(query);

        List<Integer> accessibleForumIds = new ArrayList<>(16);
        threadPage.getRecords().stream().map(ThreadPO::getForumId).distinct().forEach(forumId -> {
            if (forumPermissionResolver.hasPermission(forumId, "thread:view")) {
                accessibleForumIds.add(forumId);
            }
        });

        List<SyndEntry> entries = getSyndEntries(threadPage, accessibleForumIds);
        feed.setEntries(entries);

        // 输出RSS Feed
        return new SyndFeedOutput().outputString(feed);
    }

    private List<SyndEntry> getSyndEntries(Page<ThreadPO> threadPage, List<Integer> accessibleForumIds) {
        // 转换文章为RSS条目
        String domain = HeaderUtils.getDomain();
        List<SyndEntry> entries = new ArrayList<>();

        if (threadPage == null || threadPage.getRecords() == null || threadPage.getRecords().isEmpty()) {
            return entries;
        }

        List<ThreadPO> threads = threadPage.getRecords();

        for (ThreadPO thread : threads) {
            if (thread.getForumId() == null || !accessibleForumIds.contains(thread.getForumId())) {
                // 跳过无权限的版块
                continue;
            }

            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle(thread.getSubject());
            entry.setLink(domain + "/t/" + thread.getThreadId());

            if (thread.getCreateTime() != null) {
                entry.setPublishedDate(Date.from(thread.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                // 如果创建时间为空，设置一个默认的发布日期（例如当前时间）
                // 或者根据业务需求决定是否完全不设置发布日期
                entry.setPublishedDate(new Date());
            }

            SyndContent description = new SyndContentImpl();
            description.setType("text/html");
            String brief = thread.getBrief();
            String content = threadGateway.getThreadContent(thread.getThreadId());

            if (StringUtils.isNotEmpty(content)) {
                Document document = Jsoup.parse(content);
                // 处理图片链接
                Elements images = document.select("img");
                for (Element img : images) {
                    String src = img.attr("src");
                    if (src.startsWith("/") && !src.startsWith("//")) {
                        img.attr("src", domain + src);
                    }
                }
                // 处理a标签链接
                Elements links = document.select("a");
                for (Element link : links) {
                    String href = link.attr("href");
                    if (href.startsWith("/") && !href.startsWith("//")) {
                        link.attr("href", domain + href);
                    }
                }
                description.setValue(document.html());
            } else {
                description.setValue(brief);
            }
            entry.setDescription(description);
            entry.setAuthor(userApplication.getByIdFromCache(thread.getCreateBy()).getUserName());
            entries.add(entry);
        }
        return entries;
    }
}
