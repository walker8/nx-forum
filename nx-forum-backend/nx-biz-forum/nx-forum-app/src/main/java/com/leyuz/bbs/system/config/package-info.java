/**
 * 配置管理模块（Configuration Management）
 * <p>
 * 本模块负责论坛配置相关功能，包括网站配置、审核配置等。采用 MVC 模式开发。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li>网站配置：网站基本信息、SEO 配置</li>
 *   <li>审核配置：敏感词配置、黑白名单配置</li>
 *   <li>默认配置：默认版块等全局配置</li>
 * </ul>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.system.config.ForumConfigApplication} - 论坛配置应用服务</li>
 *   <li>{@link com.leyuz.bbs.system.config.AuditConfigApplication} - 审核配置应用服务</li>
 * </ul>
 *
 * @author walker
 * @since 2024-12-22
 */
package com.leyuz.bbs.system.config;

