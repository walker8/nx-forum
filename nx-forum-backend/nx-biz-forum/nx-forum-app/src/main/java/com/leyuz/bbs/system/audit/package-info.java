/**
 * 审核管理模块（Audit Management）
 * <p>
 * 本模块负责内容审核相关功能，包括敏感词过滤、黑白名单管理等。采用 MVC 模式开发。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li>内容审核：对帖子、评论内容进行审核</li>
 *   <li>敏感词过滤：配置和检测敏感词</li>
 *   <li>黑白名单：用户黑白名单管理</li>
 *   <li>审核配置：审核规则和策略配置</li>
 * </ul>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.system.audit.AuditApplication} - 审核应用服务</li>
 * </ul>
 *
 * @author walker
 * @since 2024-12-22
 */
package com.leyuz.bbs.system.audit;
