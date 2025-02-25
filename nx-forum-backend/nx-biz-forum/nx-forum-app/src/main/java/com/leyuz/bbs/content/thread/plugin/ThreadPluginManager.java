package com.leyuz.bbs.content.thread.plugin;

import com.leyuz.bbs.content.thread.ThreadE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
} 