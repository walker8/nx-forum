package com.leyuz.bbs.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyuz.bbs.common.dataobject.AuditStatusV;
import com.leyuz.bbs.content.thread.ThreadApplication;
import com.leyuz.bbs.content.thread.dto.ThreadCmd;
import com.leyuz.bbs.content.thread.dto.ThreadDetailVO;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import com.leyuz.common.mybatis.CustomPage;
import com.leyuz.ratelimit.annotation.LimitType;
import com.leyuz.ratelimit.annotation.RateLimitRule;
import com.leyuz.ratelimit.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 主题帖管理控制器
 * 
 * @author walker
 * @since 2024-07-07
 */
@Tag(name = "主题帖")
@RestController
@RequestMapping("/v1/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreadApplication threadApplication;

    @Operation(summary = "新增帖子")
    @PostMapping("")
    @RateLimiter(rules = {
            @RateLimitRule(key = "thread-1", time = 10, count = 1, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID),
            @RateLimitRule(key = "thread-2", time = 60, count = 2, timeUnit = TimeUnit.SECONDS, limitType = LimitType.USER_ID)
    })
    public SingleResponse createThread(@RequestBody ThreadCmd threadCmd) {
        threadApplication.createThread(threadCmd.getForumId(), threadCmd);
        return SingleResponse.of("发帖成功！");
    }

    @Operation(summary = "更新帖子")
    @PutMapping("/{threadId}")
    public SingleResponse updateThread(@PathVariable("threadId") Long threadId, @RequestBody ThreadCmd threadCmd) {
        threadApplication.updateThread(threadId, threadCmd);
        return SingleResponse.of("更新成功！");
    }

    @Operation(summary = "查询帖子-查看态")
    @GetMapping("/view/{threadId}")
    @PermitAll
    public SingleResponse getThreadForView(@PathVariable("threadId") Long threadId) {
        ThreadDetailVO threadDetailResp = threadApplication.getThreadForView(threadId);
        return SingleResponse.of(threadDetailResp);
    }

    @Operation(summary = "查询帖子-编辑态")
    @GetMapping("/edit/{threadId}")
    public SingleResponse getThreadForEdit(@PathVariable("threadId") Long threadId) {
        ThreadDetailVO threadDetailResp = threadApplication.getThreadForEdit(threadId);
        return SingleResponse.of(threadDetailResp);
    }

    @Operation(summary = "查询版块下的帖子")
    @GetMapping("")
    @PermitAll
    public SingleResponse queryThreads(@RequestParam(defaultValue = "") String forumName,
                                       @RequestParam(defaultValue = "") String authorName,
                                       @RequestParam(defaultValue = "") String orderBy,
                                       @RequestParam(defaultValue = "1") int pageNo,
                                       @RequestParam(defaultValue = "10") int pageSize) {
        ThreadQuery query = ThreadQuery.builder().forumName(forumName).authorName(authorName)
                .auditStatusV(AuditStatusV.PASSED).deleted(false)
                .keyword("").orderBy(orderBy).pageNo(pageNo).pageSize(pageSize).build();
        CustomPage<ThreadVO> threadRespPage = threadApplication.queryThreads(query);
        return SingleResponse.of(threadRespPage);
    }

    @Operation(summary = "根据关键词查询版块帖子")
    @PostMapping("/search")
    @PermitAll
    public SingleResponse queryThreadsByKeyword(@RequestBody ThreadQuery threadQuery) {
        CustomPage<ThreadVO> threadRespPage = threadApplication.queryThreadsByKeyword(threadQuery);
        return SingleResponse.of(threadRespPage);
    }

    @Operation(summary = "获取热门帖子")
    @GetMapping("/hot")
    @PermitAll
    public SingleResponse queryHotThreads(
            @RequestParam(defaultValue = "14") Integer days,
            @RequestParam(defaultValue = "10") Integer limit) {
        return SingleResponse.of(threadApplication.queryHotThreads(days, limit));
    }

}
