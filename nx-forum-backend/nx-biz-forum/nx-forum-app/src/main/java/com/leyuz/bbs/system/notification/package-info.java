/**
 * 通知管理模块（Notification Management）
 * <p>
 * 本模块负责系统通知相关功能，包括评论通知、@提醒、系统消息等。采用 MVC 模式开发。
 * </p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li>评论通知：帖子收到评论时通知作者</li>
 *   <li>回复通知：评论收到回复时通知评论者</li>
 *   <li>@提醒：内容中@用户时发送通知</li>
 *   <li>系统消息：管理员发送的系统通知</li>
 *   <li>通知查询：分页查询用户通知</li>
 *   <li>通知清理：清除已读通知</li>
 * </ul>
 *
 * <h2>核心类</h2>
 * <ul>
 *   <li>{@link com.leyuz.bbs.system.notification.NotificationApplication} - 通知应用服务</li>
 * </ul>
 *
 * @author walker
 * @since 2024-12-22
 */
package com.leyuz.bbs.system.notification;


