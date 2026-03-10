package com.leyuz.bbs.content.thread.plugin;

import com.leyuz.bbs.content.thread.ThreadE;
import com.leyuz.bbs.content.thread.dto.ThreadQuery;
import com.leyuz.bbs.content.thread.dto.ThreadVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 主题插件管理器
 * 负责管理和执行所有的ThreadPlugin
 */
@Component
@Slf4j
public class ThreadPluginManager {

    private List<ThreadPlugin> plugins = Collections.emptyList();

    @Autowired(required = false)
    public void setPlugins(List<ThreadPlugin> plugins) {
        // 按照优先级排序
        if (plugins != null && !plugins.isEmpty()) {
            this.plugins = plugins.stream()
                    .sorted(Comparator.comparingInt(ThreadPlugin::getOrder))
                    .toList();
            log.info("已加载{}个主题插件: {}", plugins.size(),
                    plugins.stream().map(p -> p.getClass().getSimpleName()).toList());
        }
    }

    /**
     * 执行所有插件的beforeSave方法
     *
     * @param threadE 主题实体
     * @return 处理后的主题实体
     */
    public ThreadE executeBeforeSave(ThreadE threadE) {
        ThreadE result = threadE;
        for (ThreadPlugin plugin : plugins) {
            try {
                plugin.beforeSave(result);
            } catch (Exception e) {
                log.error("执行插件[{}]的beforeSave方法时发生异常", plugin.getClass().getSimpleName(), e);
            }
        }
        return result;
    }

    /**
     * 执行所有插件的beforeUpdate方法
     *
     * @param threadE 主题实体
     * @return 处理后的主题实体
     */
    public ThreadE executeBeforeUpdate(ThreadE threadE) {
        ThreadE result = threadE;
        for (ThreadPlugin plugin : plugins) {
            try {
                plugin.beforeUpdate(result);
            } catch (Exception e) {
                log.error("执行插件[{}]的beforeUpdate方法时发生异常", plugin.getClass().getSimpleName(), e);
            }
        }
        return result;
    }

    /**
     * 执行所有插件的getAdditionalThreads方法，收集额外的主题并合并到现有列表
     *
     * @param existingThreads 现有主题列表
     * @param query           查询条件
     * @return 合并后的主题列表（现有主题 + 额外主题，自动去重）
     */
    public List<ThreadVO> executeEnhanceThreadList(List<ThreadVO> existingThreads, ThreadQuery query) {
        if (plugins.isEmpty()) {
            return existingThreads;
        }

        // 收集已存在的threadId用于去重
        Set<Long> existingIds = existingThreads.stream()
                .map(ThreadVO::getThreadId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ThreadVO> additionalThreads = new ArrayList<>();
        for (ThreadPlugin plugin : plugins) {
            try {
                List<ThreadVO> pluginThreads = plugin.getAdditionalThreads(query);
                if (pluginThreads != null) {
                    for (ThreadVO thread : pluginThreads) {
                        if (thread.getThreadId() != null && !existingIds.contains(thread.getThreadId())) {
                            existingIds.add(thread.getThreadId());
                            additionalThreads.add(thread);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("执行插件[{}]的getAdditionalThreads方法时发生异常", plugin.getClass().getSimpleName(), e);
            }
        }

        // 合并：现有主题 + 额外主题
        List<ThreadVO> result = new ArrayList<>(existingThreads);
        result.addAll(additionalThreads);
        return result;
    }
} 