/**
 * 帖子管理模块（Thread Management）
 * <p>
 * 本模块负责论坛帖子的核心业务逻辑，包括帖子的创建、编辑、查询、审核等功能。
 * 采用 DDD（领域驱动设计）模式开发。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li>帖子发布：支持多种格式（HTML、Markdown）的帖子发布</li>
 *   <li>帖子编辑：支持帖子内容的编辑和更新</li>
 *   <li>帖子查询：支持多种查询策略（热门、最新、精华、关注等）</li>
 *   <li>帖子审核：自动审核和人工审核结合</li>
 *   <li>帖子属性：置顶、精华、推荐、关闭等属性管理</li>
 *   <li>帖子转移：支持跨版块转移</li>
 * </ul>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.content.thread.ThreadApplication} - 帖子应用服务</li>
 *   <li>{@link com.leyuz.bbs.content.thread.ThreadE} - 帖子领域实体</li>
 *   <li>{@link com.leyuz.bbs.content.thread.service.ThreadDomainService} - 帖子领域服务</li>
 *   <li>{@link com.leyuz.bbs.content.thread.gateway.ThreadGateway} - 帖子网关接口</li>
 * </ul>
 *
 * <h2>设计模式</h2>
 * <ul>
 *   <li>策略模式：ThreadQueryStrategy - 支持多种帖子查询策略</li>
 *   <li>插件模式：ThreadPlugin - 支持自定义帖子处理插件</li>
 *   <li>事件驱动：Thread Events - 帖子状态变更事件</li>
 * </ul>
 *
 * @author walker
 * @since 2024-07-07
 */
package com.leyuz.bbs.content.thread;


