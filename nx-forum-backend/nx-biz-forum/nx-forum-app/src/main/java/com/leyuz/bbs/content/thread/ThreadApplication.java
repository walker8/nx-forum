package com.leyuz.bbs.content.thread;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Table;
import com.leyuz.bbs.auth.ForumPermissionResolver;
import com.leyuz.bbs.common.constant.OperationConstant;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.common.dataobject.DocTypeV;
import com.leyuz.bbs.common.utils.HtmlUtils;
import com.leyuz.bbs.content.thread.dataobject.ThreadPropertyV;
import com.leyuz.bbs.content.thread.dto.*;
import com.leyuz.bbs.content.thread.gateway.ThreadGateway;
import com.leyuz.bbs.content.thread.plugin.ThreadPluginManager;
import com.leyuz.bbs.content.thread.service.ThreadDomainService;
import com.leyuz.bbs.content.thread.strategy.ForumThreadQueryStrategy;
import com.leyuz.bbs.content.thread.strategy.ThreadQueryStrategyFactory;
import com.leyuz.bbs.forum.ForumApplication;
import com.leyuz.bbs.forum.ForumPO;
import com.leyuz.bbs.interaction.favorite.FavoriteApplication;
import com.leyuz.bbs.interaction.like.LikeApplication;
import com.leyuz.bbs.interaction.like.dto.LikeTargetType;
import com.leyuz.bbs.system.attach.AttachApplication;
import com.leyuz.bbs.system.audit.AuditApplication;
import com.leyuz.bbs.system.audit.dto.AuditDTO;
import com.leyuz.bbs.system.config.ForumConfigApplication;
import com.leyuz.bbs.user.ForumUserApplication;
import com.leyuz.common.exception.AuditException;
import com.leyuz.common.exception.ValidationException;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.common.mybatis.DataBaseUtils;
import com.leyuz.common.mybatis.PageQuery;
import com.leyuz.common.utils.HeaderUtils;
import com.leyuz.module.cache.GenericCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreadApplication {
    private final ThreadDomainService threadDomainService;
    private final ThreadGateway threadGateway;
    private final ThreadMapper threadMapper;
    private final ForumApplication forumApplication;
    private final ForumConfigApplication forumConfigApplication;
    private final ForumUserApplication forumUserApplication;
    private final AuditApplication auditApplication;
    private final AttachApplication attachApplication;
    private final LikeApplication likeApplication;
    private final FavoriteApplication favoriteApplication;
    private final ThreadConvert threadConvert;
    private final ForumPermissionResolver forumPermissionResolver;
    private final GenericCache<String, Boolean> threadViewCache;
    private final GenericCache<String, List<ThreadVO>> threadsHotCache;
    private final ThreadQueryStrategyFactory threadQueryStrategyFactory;
    private final ForumThreadQueryStrategy forumThreadQueryStrategy;
    private final ThreadPluginManager threadPluginManager;

    public void createThread(Integer forumId, ThreadCmd threadCmd) {
        forumPermissionResolver.checkPermission(forumId, "thread:new");
        // 初始化帖子
        ThreadE threadE = ThreadE.builder()
                .forumId(forumId)
                .subject(threadCmd.getSubject())
                .categoryId(threadCmd.getCategoryId())
                .remark(threadCmd.getRemark())
                .docType(threadCmd.getContentType() != null ? threadCmd.getContentType() : DocTypeV.HTML)
                .commentOrder(threadCmd.getCommentOrder())
                .build();

        // 设置内容，如果 docType 为 MARKDOWN，会自动转换为 HTML
        threadE.setContent(threadCmd.getContent());

        // 下载远程图片到本地（转换后的 HTML 内容）
        threadE.setContent(downloadImages(threadE.getContent()));

        // 审核主题
        auditThread(threadE);
        // 执行用户自定义的插件方法
        threadPluginManager.executeBeforeSave(threadE);

        // 保存到数据库
        threadDomainService.save(threadE);
        if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus())) {
            throw new AuditException("主题审核中，请耐心等待审核通过！");
        }
    }

    private void auditThread(ThreadE threadE) {
        AuditDTO auditDTO = AuditDTO.builder()
                .userId(HeaderUtils.getUserId())
                .message(MessageFormat.format("{0} {1}", threadE.getSubject(), HtmlUtils.convertHtmlToText(threadE.getContent())))
                .build();
        Pair<AuditStatusV, String> result = auditApplication.check(auditDTO);
        threadE.setAuditResult(result.getKey(), result.getValue());
    }

    public void updateThread(Long threadId, ThreadCmd threadCmd) {
        // 只有通过的帖子才允许更新
        ThreadE thread = threadGateway.getThread(threadId);
        checkThreadUpdateAuth(thread.getForumId(), thread.getCreateBy());
        if (!thread.getAuditStatus().equals(AuditStatusV.PASSED)) {
            throw new ValidationException("帖子未通过审核，不允许修改");
        }

        ThreadE threadE = ThreadE.builder()
                .threadId(threadId)
                .subject(threadCmd.getSubject())
                .categoryId(threadCmd.getCategoryId())
                .remark(threadCmd.getRemark())
                .docType(threadCmd.getContentType() != null ? threadCmd.getContentType() : DocTypeV.HTML)
                .createBy(thread.getCreateBy())
                .commentOrder(threadCmd.getCommentOrder())
                .forumId(threadCmd.getForumId())
                .build();

        // 设置内容，如果 docType 为 MARKDOWN，会自动转换为 HTML
        threadE.setContent(threadCmd.getContent());

        // 下载远程图片到本地（转换后的 HTML 内容）
        threadE.setContent(downloadImages(threadE.getContent()));

        // 审核主题
        auditThread(threadE);
        // 执行用户自定义的插件方法
        threadPluginManager.executeBeforeUpdate(threadE);

        threadDomainService.update(threadE);
        if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus())) {
            throw new AuditException("主题审核中，请耐心等待审核通过！");
        }
    }

    private void checkThreadUpdateAuth(Integer forumId, Long createBy) {
        if (forumPermissionResolver.hasPermission(forumId, "thread:edit") && HeaderUtils.getUserId().equals(createBy)) {
            // 普通用户只能编辑自己的帖子
            return;
        }
        if (forumPermissionResolver.hasPermission(forumId, "admin:thread:edit")) {
            // 管理员可以编辑所有帖子
            return;
        }
        throw new ValidationException("您没有权限修改此帖子");
    }

    private String downloadImages(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        Document document = Jsoup.parse(content);
        document.select("img").forEach(img -> {
            String src = img.attr("src");
            if (StringUtils.isNotEmpty(src) && src.startsWith("http")) {
                // 获取域名
                String host = HeaderUtils.getHost();
                if (StringUtils.isNotEmpty(host) && src.contains(host)) {
                    // 本地图片无需重复下载
                    return;
                }
                // 下载图片到本地
                String localSrc = attachApplication.downloadImage(src);
                if (StringUtils.isNotEmpty(localSrc)) {
                    log.info("下载图片成功，src = {}, localSrc = {}", src, localSrc);
                    img.attr("src", localSrc);
                }
            }
        });
        return document.body().toString();
    }

    public ThreadDetailVO getThreadForView(Long threadId) {
        ThreadE threadE = threadGateway.getThreadDetail(threadId);
        if (threadE == null) {
            throw new ValidationException("文章不存在或已删除");
        }
        // 处理已删除帖子
        if (Boolean.TRUE.equals(threadE.getIsDeleted())) {
            // 检查用户是否有 admin:thread 权限
            if (!forumPermissionResolver.hasPermission(threadE.getForumId(), "admin:thread")) {
                throw new ValidationException("文章不存在或已删除");
            }
            // 管理员可以查看已删除帖子
            threadE.outputForView();
            ThreadDetailVO threadDetailVO = buildThreadDetailVO(threadE);
            threadDetailVO.setDeleted(true);
            return threadDetailVO;
        }
        if (!threadE.getCreateBy().equals(HeaderUtils.getUserId())) {
            // 自己发表的帖子有权限查看
            forumPermissionResolver.checkPermission(threadE.getForumId(), "thread:view");
        }
        if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus()) && !forumPermissionResolver.hasPermission(threadE.getForumId(), "admin:thread")) {
            throw new ValidationException("文章审核中，请耐心等待审核通过！");
        }
        threadE.outputForView();
        ThreadDetailVO threadDetailVO = buildThreadDetailVO(threadE);
        // 点赞
        Table<Long, Integer, Boolean> userLikes = likeApplication.getUserLikesByThreadId(threadId);
        if (userLikes.contains(threadId, LikeTargetType.THREAD.getValue())) {
            threadDetailVO.setLiked(true);
        }
        // 收藏
        threadDetailVO.setCollected(favoriteApplication.isFavorite(threadId));
        // 异步更新浏览量
        updateThreadViewsAsync(threadId);

        return threadDetailVO;
    }

    private ThreadDetailVO buildThreadDetailVO(ThreadE threadE) {
        ThreadDetailVO threadDetailVO = BeanUtil.toBean(threadE, ThreadDetailVO.class);
        threadDetailVO.setSeoContent(threadE.getBrief());
        threadDetailVO.setSeoTitle(threadE.getSubject() + " - " + forumConfigApplication.getWebsiteBaseInfo().getWebsiteName());
        // 发帖者信息
        AuthorVO authorVO = forumUserApplication.getAuthorWithProperties(threadE.getCreateBy());
        threadDetailVO.setAuthor(authorVO);
        if (threadE.getUpdateTime() != null && threadE.getUpdateTime().isAfter(threadE.getCreateTime().plusMinutes(1))) {
            // 更新者信息
            threadDetailVO.setUpdateTime(threadE.getUpdateTime());
            AuthorVO updateAuthorVO = forumUserApplication.getAuthor(threadE.getUpdateBy());
            threadDetailVO.setUpdateAuthor(updateAuthorVO);
        }
        ThreadPropertyV property = threadE.getProperty();
        threadDetailVO.setBlocked(property.isBlocked());
        threadDetailVO.setClosed(property.isClosed());
        threadDetailVO.setDigest(property.isDigest());
        threadDetailVO.setTop(property.isTop());
        return threadDetailVO;
    }

    private void updateThreadViewsAsync(Long threadId) {
        // 获取当前线程的ip
        String ip = HeaderUtils.getIp();
        if (StringUtils.isBlank(ip)) {
            return;
        }
        threadViewCache.computeIfAbsent(threadId + ":" + ip, key -> {
            CompletableFuture.runAsync(() -> threadMapper.updateViews(threadId, 1));
            return true;
        });
    }

    public ThreadDetailVO getThreadForEdit(Long threadId) {
        ThreadDetailVO threadDetailResp = new ThreadDetailVO();
        ThreadE threadE = threadGateway.getThreadDetail(threadId);
        // 禁止编辑已删除的帖子
        if (Boolean.TRUE.equals(threadE.getIsDeleted())) {
            throw new ValidationException("文章不存在或已删除");
        }
        forumPermissionResolver.checkPermission(threadE.getForumId(), "thread:view");
        if (AuditStatusV.AUDITING.equals(threadE.getAuditStatus()) && !forumPermissionResolver.hasPermission(threadE.getForumId(), "admin:thread")) {
            throw new ValidationException("文章审核中，请等待审核通过再编辑！");
        }
        BeanUtil.copyProperties(threadE, threadDetailResp);
        ThreadPropertyV property = threadE.getProperty();
        threadDetailResp.setBlocked(property.isBlocked());
        threadDetailResp.setClosed(property.isClosed());
        threadDetailResp.setDigest(property.isDigest());
        threadDetailResp.setTop(property.isTop());
        return threadDetailResp;
    }

    public CustomPage<ThreadVO> queryThreads(ThreadQuery threadQuery) {
        log.info("queryThreads forumName={}", threadQuery.getForumName());
        ForumPO forumPO = forumApplication.getForumByNameWithDefault(threadQuery.getForumName());
        Page<ThreadPO> threadPOPage;

        if (Boolean.TRUE.equals(forumPO.getIsSystem())) {
            forumPermissionResolver.checkPermission(0, "forum:visit:section");
            // 使用策略模式查询帖子
            threadPOPage = threadQueryStrategyFactory.getStrategy(forumPO.getName()).query(threadQuery);
        } else {
            forumPermissionResolver.checkPermission(forumPO.getForumId(), "forum:visit:section");
            threadQuery.setForumId(forumPO.getForumId());
            threadPOPage = forumThreadQueryStrategy.query(threadQuery);
        }

        return DataBaseUtils.createCustomPage(threadPOPage, threadConvert::convertThreadPO2VO);
    }

    public CustomPage<AdminThreadVO> queryThreadsByAdmin(ThreadQuery threadQuery) {
        log.info("queryAdminThreads forumId={}", threadQuery.getForumId());
        Page<ThreadPO> threadPOPage = threadQueryStrategyFactory.getStrategy("admin").query(threadQuery);
        return DataBaseUtils.createCustomPage(threadPOPage, threadConvert::convertThreadPO2AdminVO);
    }

    public long getAuditingCount(Integer forumId) {
        return threadGateway.getAuditingCount(forumId);
    }

    public String operateThreadsByAdmin(Integer forumId, List<Long> threadIds, String reason, String operation, boolean notice) {
        return switch (operation) {
            case OperationConstant.PASS -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:pass");
                threadDomainService.passThreads(forumId, threadIds, notice);
                yield "通过成功";
            }
            case OperationConstant.REJECT -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:reject");
                threadDomainService.rejectThreads(forumId, threadIds, reason, notice);
                yield "拒绝成功";
            }
            case OperationConstant.RESTORE -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:restore");
                threadDomainService.restoreThreads(forumId, threadIds, notice);
                yield "恢复成功";
            }
            case OperationConstant.DELETE -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:delete");
                threadDomainService.deleteThreads(forumId, threadIds, reason, notice);
                yield "删除成功";
            }
            case OperationConstant.TOP -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:top");
                threadDomainService.topThreads(forumId, threadIds);
                yield "版块置顶成功";
            }
            case OperationConstant.CANCEL_TOP -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:top");
                threadDomainService.cancelTopThreads(forumId, threadIds);
                yield "取消置顶成功";
            }
            case OperationConstant.GLOBAL_TOP -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:top");
                threadDomainService.globalTopThreads(forumId, threadIds);
                yield "全局置顶成功";
            }
            case OperationConstant.DIGEST -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:digest");
                threadDomainService.digestThreads(forumId, threadIds);
                yield "设置精华成功";
            }
            case OperationConstant.CANCEL_DIGEST -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:digest");
                threadDomainService.cancelDigestThreads(forumId, threadIds);
                yield "取消精华成功";
            }
            case OperationConstant.RECOMMEND -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:recommend");
                threadDomainService.recommendThreads(forumId, threadIds);
                yield "推荐成功";
            }
            case OperationConstant.CANCEL_RECOMMEND -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:recommend");
                threadDomainService.cancelRecommendThreads(forumId, threadIds);
                yield "取消推荐成功";
            }
            case OperationConstant.OPEN -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:close");
                threadDomainService.openThreads(forumId, threadIds);
                yield "打开成功";
            }
            case OperationConstant.CLOSE -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:close");
                threadDomainService.closeThreads(forumId, threadIds);
                yield "关闭成功";
            }
            case OperationConstant.TRANSFER -> {
                forumPermissionResolver.checkPermission(forumId, "admin:thread:transfer");
                threadDomainService.transferThreads(forumId, threadIds, notice);
                yield "转移版块成功";
            }
            default -> throw new ValidationException("操作类型不正确");
        };
    }

    public void deleteThread(Long threadId, String reason, boolean notice) {
        threadDomainService.deleteThreads(0, Collections.singletonList(threadId), reason, notice);
    }

    public CustomPage<ThreadVO> queryThreadsByUserId(Long userId, int pageNo, int pageSize) {
        PageQuery pageQuery = PageQuery.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .orderByColumn("create_time")
                .isAsc(false)
                .build();
        Map<String, Object> params = pageQuery.getParams();
        params.put("auditStatus", AuditStatusV.PASSED.getValue());
        params.put("isDeleted", false);
        params.put("userId", userId);
        Page<ThreadPO> threadPOPage = threadMapper.queryThreads(0, pageQuery);
        return DataBaseUtils.createCustomPage(threadPOPage, threadConvert::convertThreadPO2VO);
    }

    public CustomPage<ThreadVO> queryThreadsByKeyword(ThreadQuery threadQuery) {
        String keyword = threadQuery.getKeyword();
        // 关键词不能为空
        if (keyword.isEmpty()) {
            throw new ValidationException("关键词不能为空！");
        }

        threadQuery.setForumId(threadQuery.getForumId());
        // 关键词搜索时不包含置顶帖子，直接查询匹配关键词的帖子
        Page<ThreadPO> threadPOPage = forumThreadQueryStrategy.queryWithoutTop(threadQuery);

        CustomPage<ThreadVO> threadVOCustomPage = DataBaseUtils.createCustomPage(threadPOPage, threadConvert::convertThreadPO2VO);
        String regex = "(?i)" + Pattern.quote(keyword); // (?i) 表示忽略大小写
        threadVOCustomPage.getRecords().forEach(threadVO -> {
            if (StringUtils.isNotEmpty(threadVO.getSubject())) {
                threadVO.setSubject(threadVO.getSubject().replaceAll(regex, "<em>$0</em>"));
            }
            if (StringUtils.isNotEmpty(threadVO.getBrief())) {
                threadVO.setBrief(threadVO.getBrief().replaceAll(regex, "<em>$0</em>"));
            }
        });
        return threadVOCustomPage;
    }

    public List<ThreadVO> queryHotThreads(Integer days, Integer limit) {
        return threadsHotCache.computeIfAbsent(MessageFormat.format("{0}-{1}", days, limit), key -> {
            ThreadQuery query = ThreadQuery.builder()
                    .days(days)
                    .auditStatusV(AuditStatusV.PASSED)
                    .deleted(false)
                    .orderBy("views")
                    .pageNo(1)
                    .pageSize(limit)
                    .build();

            // 使用热门帖子查询策略
            Page<ThreadPO> threadPOPage = threadQueryStrategyFactory.getStrategy("hot").query(query);

            threadPOPage.getRecords().forEach(threadPO -> {
                threadPO.setCreateTime(null);
                threadPO.setUpdateTime(null);
                threadPO.setBrief(null);
            });
            return threadPOPage.getRecords().stream()
                    .filter(m -> StringUtils.isNotEmpty(m.getSubject()))
                    .map(threadConvert::convertThreadPO2VO)
                    .toList();
        });
    }
}
