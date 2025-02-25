-- MySQL dump 10.13  Distrib 8.0.40, for macos15.2 (arm64)
--
-- Host: localhost    Database: nx-forum
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bbs_comment_replies`
--

DROP TABLE IF EXISTS `bbs_comment_replies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_comment_replies` (
  `reply_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '楼中楼帖子id (唯一)',
  `forum_id` int unsigned NOT NULL DEFAULT '0' COMMENT 'BBS论坛ID，属于哪一个论坛的',
  `comment_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '回复id （bbs_comments的主键id）',
  `thread_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '帖子id',
  `reply_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '回复的用户id',
  `likes` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '楼中楼内容，不超过500个字符，纯文本',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '发帖时用户ip地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `audit_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '0 审核通过 1 审核中 2 审核拒绝',
  `audit_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核原因',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`reply_id`) USING BTREE,
  KEY `bbs_comment_replies_comment_id_IDX` (`comment_id`) USING BTREE,
  KEY `bbs_comment_replies_thread_id_IDX` (`thread_id`) USING BTREE,
  KEY `bbs_comment_replies_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_comment_replies_create_by_IDX` (`create_by`) USING BTREE,
  KEY `bbs_comment_replies_user_ip_IDX` (`user_ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='楼中楼评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_comments`
--

DROP TABLE IF EXISTS `bbs_comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_comments` (
  `comment_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '回复id （唯一）',
  `thread_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '主题id（和bbs_threads表一致）',
  `forum_id` int unsigned NOT NULL DEFAULT '0' COMMENT 'BBS论坛ID，属于哪一个论坛的',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '发帖时用户ip地址',
  `doc_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown',
  `likes` int unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `reply_count` int unsigned NOT NULL DEFAULT '0' COMMENT '楼中楼数 0为没有',
  `message` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容，用户提示的原始数据',
  `images` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '正文中的图片，分隔符为,',
  `audit_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '0 审核通过 1 审核中 2 审核拒绝',
  `audit_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核原因',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`comment_id`) USING BTREE,
  KEY `bbs_comments_user_ip_IDX` (`user_ip`) USING BTREE,
  KEY `bbs_comments_thread_id_IDX` (`thread_id`) USING BTREE,
  KEY `bbs_comments_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_comments_create_by_IDX` (`create_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_custom_page_contents`
--

DROP TABLE IF EXISTS `bbs_custom_page_contents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_custom_page_contents` (
  `content_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `page_id` bigint unsigned NOT NULL COMMENT '关联页面ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面内容',
  `version` int unsigned NOT NULL DEFAULT '1' COMMENT '内容版本号',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`content_id`) USING BTREE,
  KEY `bbs_custom_page_contents_page_id_IDX` (`page_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='自定义页面内容版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_custom_pages`
--

DROP TABLE IF EXISTS `bbs_custom_pages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_custom_pages` (
  `page_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '页面ID',
  `page_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面名称',
  `page_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面编码（唯一，用于前端路由）',
  `page_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '页面状态（0启用 1停用）',
  `access_level` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '访问权限（0公开 1登录用户 2指定角色）',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`page_id`) USING BTREE,
  KEY `bbs_custom_pages_page_code_IDX` (`page_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='自定义页面表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_forum_access`
--

DROP TABLE IF EXISTS `bbs_forum_access`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_forum_access` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `forum_id` int unsigned NOT NULL COMMENT '版块ID',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色key',
  `perms` json NOT NULL COMMENT '权限标识列表,例如["thread:create","thread:edit"]',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_forum_access_forum_id_IDX` (`forum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='版块权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_forums`
--

DROP TABLE IF EXISTS `bbs_forums`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_forums` (
  `forum_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '版块ID',
  `name` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块号，只能用英文或数字',
  `nick_name` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块名称',
  `short_brief` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块20字短介绍 纯文本',
  `brief` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块简介 允许HTML',
  `forum_access` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '访问控制 0 所有人能访问 1 所有人禁止访问 2 仅登录用户访问',
  `icon_name` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块图标',
  `seo_title` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO 标题，如果设置会代替版块名称',
  `seo_keywords` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO关键字，不要超过6个，关键字之间用英文的,隔开',
  `seo_content` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO content',
  `seo_ext` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO帖子后缀，保留',
  `background_image` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块背景图片',
  `show_menu` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示（1显示 0不显示）',
  `menu_order` int NOT NULL DEFAULT '0' COMMENT '菜单排序',
  `is_system` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否系统内置（1系统内置 0用户自建）',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`forum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_images`
--

DROP TABLE IF EXISTS `bbs_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_images` (
  `image_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `image_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片存储路径',
  `image_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '图片类型（0帖子图片 1用户头像）',
  `file_size` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文件大小（字节）',
  `file_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件hash值（MD5）',
  `file_ext` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件后缀',
  `storage_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '存储类型（0本地 1阿里云OSS 2腾讯云COS）',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`image_id`) USING BTREE,
  KEY `bbs_images_create_by_IDX` (`create_by`) USING BTREE,
  KEY `bbs_images_file_hash_IDX` (`file_hash`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='图片管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_notifications`
--

DROP TABLE IF EXISTS `bbs_notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_notifications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '接收通知的用户ID',
  `post_id` bigint DEFAULT '0' COMMENT '同主题id，方便索引',
  `notification_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '通知类型（如：回复、点赞、系统消息等）',
  `notification_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '通知的状态（如：未读、已读等）',
  `subject` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '通知的标题',
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知的详细内容',
  `extra` json DEFAULT NULL COMMENT '额外的数据，可以用来存储JSON格式的数据，例如发送者的UID、关联的帖子ID等',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_notifications_user_id_IDX` (`user_id`,`notification_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_thread_contents`
--

DROP TABLE IF EXISTS `bbs_thread_contents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_thread_contents` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `thread_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '主题id',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_thread_contents_thread_id_IDX` (`thread_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_thread_properties`
--

DROP TABLE IF EXISTS `bbs_thread_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_thread_properties` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `thread_id` bigint unsigned NOT NULL COMMENT '主题ID',
  `forum_id` int NOT NULL DEFAULT '0' COMMENT '版块ID',
  `property_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '类型',
  `attribute` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '属性',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_thread_properties_thread_id_IDX` (`thread_id`) USING BTREE,
  KEY `bbs_thread_properties_forum_id_IDX` (`forum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='特殊属性帖子（置顶、推荐、加精）的缓存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_threads`
--

DROP TABLE IF EXISTS `bbs_threads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_threads` (
  `thread_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主题id （唯一）',
  `forum_id` int unsigned NOT NULL DEFAULT '0' COMMENT '版块ID',
  `category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '分类 id 0 是未分类',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '发帖时用户ip地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `subject` char(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主题名称',
  `brief` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主题简介，自动提取自主题内容',
  `doc_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown',
  `views` int unsigned NOT NULL DEFAULT '0' COMMENT '查看次数',
  `comments` int unsigned NOT NULL DEFAULT '0' COMMENT '评论数',
  `likes` int unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `dislikes` int unsigned NOT NULL DEFAULT '0' COMMENT '不喜欢数',
  `collections` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `audit_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '0 审核通过 1 审核中 2 审核拒绝 3 忽略',
  `audit_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核原因',
  `comment_order` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序',
  `last_comment_user_id` bigint DEFAULT NULL COMMENT '最近参与的用户（最后回帖的用户名）',
  `last_comment_time` datetime DEFAULT NULL COMMENT '最后回复时间',
  `images` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '正文中的图片，分隔符为,',
  `image_count` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '正文中的图片数量',
  `property` json DEFAULT NULL COMMENT '主题扩展属性',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`thread_id`) USING BTREE,
  KEY `bbs_threads_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_threads_create_by_IDX` (`create_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_bans`
--

DROP TABLE IF EXISTS `bbs_user_bans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_bans` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '被禁用户ID',
  `forum_id` int unsigned NOT NULL DEFAULT '0' COMMENT '版块ID（0表示全站禁言）',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '禁言原因',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间（null表示永久禁言）',
  `operation_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '操作类型（0禁言 1解禁）',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_bans_user_id_IDX` (`user_id`) USING BTREE,
  KEY `bbs_user_bans_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_user_bans_expire_time_IDX` (`expire_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户禁言记录表（expire_time为null表示永久禁言）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_favorites`
--

DROP TABLE IF EXISTS `bbs_user_favorites`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_favorites` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` bigint unsigned NOT NULL COMMENT '帖子ID',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_favorites_create_by_IDX` (`create_by`,`thread_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户收藏表（仅支持帖子收藏）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_follows`
--

DROP TABLE IF EXISTS `bbs_user_follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_follows` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follow_user_id` bigint unsigned NOT NULL COMMENT '被关注的用户ID',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注（用户可以给关注的人添加备注）',
  `create_by` bigint unsigned NOT NULL COMMENT '关注者用户ID',
  `create_time` datetime NOT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_follows_follow_user_id_IDX` (`follow_user_id`) USING BTREE,
  KEY `bbs_user_follows_create_by_IDX` (`create_by`,`follow_user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户关注表（记录用户之间的关注关系）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_likes`
--

DROP TABLE IF EXISTS `bbs_user_likes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_likes` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '点赞目标类型（0帖子 1评论 2楼中楼回复）',
  `target_id` bigint unsigned NOT NULL COMMENT '目标ID（对应thread_id/comment_id/reply_id）',
  `thread_id` bigint unsigned NOT NULL COMMENT '所属帖子ID（冗余设计方便查询）',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_likes_create_by_IDX` (`create_by`,`thread_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户点赞记录表（thread_id为冗余字段，当target_type=0时等于target_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_properties`
--

DROP TABLE IF EXISTS `bbs_user_properties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_properties` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `threads` int unsigned NOT NULL DEFAULT '0' COMMENT '发帖数',
  `comments` int unsigned NOT NULL DEFAULT '0' COMMENT '评论数',
  `credits` int unsigned NOT NULL DEFAULT '0' COMMENT '积分',
  `fans` int unsigned NOT NULL DEFAULT '0' COMMENT '粉丝数',
  `golds` int unsigned NOT NULL DEFAULT '0' COMMENT '金币',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_properties_user_id_IDX` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='论坛用户属性表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `common_configs`
--

DROP TABLE IF EXISTS `common_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `common_configs` (
  `config_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '配置id',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键值',
  `config_type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '系统内置（0是 1否）',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '说明',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='共用配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_permissions`
--

DROP TABLE IF EXISTS `uc_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_permissions` (
  `perm_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '父节点ID',
  `perm_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限名称',
  `perm_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '权限状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限标识',
  `perm_type` tinyint unsigned NOT NULL DEFAULT '3' COMMENT '权限类型（1目录 2菜单 3按钮）',
  `perm_order` int NOT NULL DEFAULT '0' COMMENT '显示顺序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`perm_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_role_permissions`
--

DROP TABLE IF EXISTS `uc_role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_role_permissions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '角色key',
  `perm_id` bigint unsigned NOT NULL COMMENT '菜单ID',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='角色和权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_roles`
--

DROP TABLE IF EXISTS `uc_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_roles` (
  `role_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '说明',
  `role_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '角色状态0-正常1-停用',
  `priority` int NOT NULL DEFAULT '1' COMMENT '优先级(数字越大优先级越高)',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`role_id`) USING BTREE,
  KEY `uc_roles_role_name_IDX` (`role_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_user_login_tokens`
--

DROP TABLE IF EXISTS `uc_user_login_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_user_login_tokens` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `device_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备id',
  `token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '令牌',
  `token_expires_at` datetime NOT NULL COMMENT '令牌失效时间',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '登录IP',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `uc_user_login_tokens_token_IDX` (`token`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_user_roles`
--

DROP TABLE IF EXISTS `uc_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_user_roles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '角色key',
  `role_scope` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色范围',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_users`
--

DROP TABLE IF EXISTS `uc_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_users` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像地址',
  `account_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用 2已注销）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NOT NULL COMMENT '最后登录时间',
  `intro` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '个人介绍',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `uc_users_email_IDX` (`email`) USING BTREE,
  KEY `uc_users_user_name_IDX` (`user_name`) USING BTREE,
  KEY `uc_users_phone_IDX` (`phone`) USING BTREE,
  KEY `uc_users_login_ip_IDX` (`login_ip`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'nx-forum'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-08  1:12:26

--
-- Table structure for table `bbs_reports`
--

DROP TABLE IF EXISTS `bbs_reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_reports` (
  `report_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '举报ID (主键)',
  `target_id` bigint unsigned NOT NULL COMMENT '被举报内容ID (帖子ID或评论ID)',
  `target_type` tinyint unsigned NOT NULL COMMENT '举报目标类型 (1:主题, 2:评论 3:楼中楼回复)',
  `forum_id` int unsigned NOT NULL DEFAULT '0' COMMENT '所属版块ID (冗余字段，便于查询)',
  `reported_content` varchar(500) CHARACTER utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '被举报内容（冗余字段）' ,
  `report_type` tinyint unsigned NOT NULL COMMENT '举报原因类型',
  `report_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户填写的补充说明',
  `handle_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '处理状态 (0:待处理, 1:已处理-违规, 2:已处理-驳回)',
  `handle_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '处理结果说明',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '举报人IP地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '举报人客户端useragent',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '举报人ID',
  `create_time` datetime NOT NULL COMMENT '举报时间',
  `update_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '处理人ID',
  `update_time` datetime NOT NULL COMMENT '处理时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '删除标志 (0:存在, 1:删除)',
  PRIMARY KEY (`report_id`) USING BTREE,
  KEY `bbs_reports_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_reports_create_by_IDX` (`create_by`) USING BTREE,
  KEY `bbs_reports_update_by_IDX` (`update_by`) USING BTREE,
  KEY `bbs_reports_handle_status_IDX` (`handle_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户举报表';
/*!40101 SET character_set_client = @saved_cs_client */;
