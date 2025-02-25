/**
 * 论坛业务应用层（Forum Business Application Layer）
 * <p>
 * 本层是论坛业务的应用层，按照业务领域组织代码结构，遵循 COLA 架构规范。
 * </p>
 *
 * <h2>业务领域划分</h2>
 * 
 * <h3>1. 内容管理域（content）</h3>
 * <ul>
 *   <li>{@link com.leyuz.bbs.content.thread} - 帖子管理（DDD 模式）</li>
 *   <li>{@link com.leyuz.bbs.content.comment} - 评论管理（DDD 模式）</li>
 * </ul>
 * 
 * <h3>2. 用户交互域（interaction）</h3>
 * <ul>
 *   <li>{@link com.leyuz.bbs.interaction.like} - 点赞功能（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.interaction.favorite} - 收藏功能（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.interaction.report} - 举报功能（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.interaction.follow} - 关注功能（MVC 模式）</li>
 * </ul>
 * 
 * <h3>3. 论坛组织域（forum）</h3>
 * <ul>
 *   <li>{@link com.leyuz.bbs.forum} - 版块管理（MVC 模式）</li>
 * </ul>
 * 
 * <h3>4. 用户管理域（user）</h3>
 * <ul>
 *   <li>{@link com.leyuz.bbs.user} - 用户信息和禁言管理（MVC 模式）</li>
 * </ul>
 * 
 * <h3>5. 系统管理域（system）</h3>
 * <ul>
 *   <li>{@link com.leyuz.bbs.system.config} - 配置管理（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.system.audit} - 审核管理（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.system.notification} - 通知管理（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.system.image} - 图片管理（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.system.page} - 自定义页面（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.system.rss} - RSS 订阅（MVC 模式）</li>
 *   <li>{@link com.leyuz.bbs.system.attach} - 附件管理（MVC 模式）</li>
 * </ul>
 * 
 * <h3>6. 权限认证域（auth）</h3>
 * <ul>
 *   <li>{@link com.leyuz.bbs.auth} - 权限认证和授权</li>
 * </ul>
 *
 * <h2>架构说明</h2>
 * <ul>
 *   <li><strong>DDD 模式</strong>：用于复杂业务逻辑（Thread、Comment），包含领域实体、领域服务、网关接口</li>
 *   <li><strong>MVC 模式</strong>：用于简单业务逻辑，Application 直接调用 Infrastructure 层的 Mapper</li>
 *   <li><strong>事件驱动</strong>：通过 Spring Event 实现模块间解耦</li>
 *   <li><strong>缓存策略</strong>：合理使用本地缓存和分布式缓存提升性能</li>
 * </ul>
 *
 * @author walker
 * @since 2024-04-03
 */
package com.leyuz.bbs;

