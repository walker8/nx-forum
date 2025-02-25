package com.leyuz.bbs.web;

import com.leyuz.bbs.system.rss.RssApplication;
import com.rometools.rome.io.FeedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * RSS 订阅源
 * </p>
 *
 * @author walker
 * @since 2025-05-15
 */

@Tag(name = "RSS订阅")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/rss")
public class RssController {
    
    private final RssApplication rssApplication;

    @Operation(summary = "获取版块RSS订阅")
    @GetMapping(value = "/forum/{forumName}", produces = MediaType.APPLICATION_XML_VALUE)
    @PermitAll
    public String getForumRss(@PathVariable String forumName) throws FeedException {
        return rssApplication.getForumRss(forumName);
    }

    @Operation(summary = "获取社区RSS订阅")
    @GetMapping(value = "/author/{authorName}", produces = MediaType.APPLICATION_XML_VALUE)
    @PermitAll
    public String getAuthorRss(@PathVariable String authorName) throws FeedException {
        return rssApplication.getAuthorRss(authorName);
    }
}
