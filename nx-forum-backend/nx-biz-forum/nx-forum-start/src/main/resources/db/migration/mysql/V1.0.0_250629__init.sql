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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_comment_replies` (
  `reply_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '楼中楼帖子id (唯一)',
  `forum_id` int unsigned NOT NULL DEFAULT 0 COMMENT 'BBS论坛ID，属于哪一个论坛的',
  `comment_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '回复id （bbs_comments的主键id）',
  `thread_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '帖子id',
  `reply_user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '回复的用户id',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '楼中楼内容，不超过500个字符，纯文本',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 0 COMMENT '发帖时用户ip地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `audit_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '0 审核通过 1 审核中 2 审核拒绝',
  `audit_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核原因',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`reply_id`) USING BTREE,
  KEY `bbs_comment_replies_comment_id_IDX` (`comment_id`) USING BTREE,
  KEY `bbs_comment_replies_thread_id_IDX` (`thread_id`) USING BTREE,
  KEY `bbs_comment_replies_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_comment_replies_create_by_IDX` (`create_by`) USING BTREE,
  KEY `bbs_comment_replies_user_ip_IDX` (`user_ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='楼中楼评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_comment_replies`
--

LOCK TABLES `bbs_comment_replies` WRITE;
/*!40000 ALTER TABLE `bbs_comment_replies` DISABLE KEYS */;
/*!40000 ALTER TABLE `bbs_comment_replies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_comments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_comments` (
  `comment_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '回复id （唯一）',
  `thread_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '主题id（和bbs_threads表一致）',
  `forum_id` int unsigned NOT NULL DEFAULT 0 COMMENT 'BBS论坛ID，属于哪一个论坛的',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 0 COMMENT '发帖时用户ip地址',
  `doc_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown',
  `likes` int unsigned NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int unsigned NOT NULL DEFAULT 0 COMMENT '楼中楼数 0为没有',
  `message` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '内容，用户提示的原始数据',
  `images` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '正文中的图片，分隔符为,',
  `audit_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '0 审核通过 1 审核中 2 审核拒绝',
  `audit_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核原因',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_custom_page_contents` (
  `content_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `page_id` bigint unsigned NOT NULL COMMENT '关联页面ID',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面内容',
  `version` int unsigned NOT NULL DEFAULT '1' COMMENT '内容版本号',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`content_id`) USING BTREE,
  KEY `bbs_custom_page_contents_page_id_IDX` (`page_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='自定义页面内容版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_custom_page_contents`
--

LOCK TABLES `bbs_custom_page_contents` WRITE;
/*!40000 ALTER TABLE `bbs_custom_page_contents` DISABLE KEYS */;
INSERT INTO `bbs_custom_page_contents` VALUES (1,1,'<h1 style=\"text-align: center;\">关于我们</h1>\n<p data-pm-slice=\"1 1 []\">这是一个自定义页面，你可以在后台的 <code>后台管理</code> -&gt;&nbsp; <code>自定义页面</code> 找到它，你可以用于新建关于页面、联系我们页面等等。</p>',1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_custom_page_contents` VALUES (2,2,'<h1 style=\"text-align: center;\">用户协议</h1>\n<p>更新日期：【2025】年【2】月【22】日<br>生效日期：【2025】年【2】月【22】日</p>\n<h2 id=\"%E4%B8%80%E3%80%81%E5%8D%8F%E8%AE%AE%E7%94%9F%E6%95%88%E4%B8%8E%E8%8C%83%E5%9B%B4\">一、协议生效与范围</h2>\n<p>1.&nbsp;<strong>协议生效</strong></p>\n<p>您在注册并使用本论坛（以下简称&ldquo;平台&rdquo;）时，即视为您已阅读、理解并同意接受本协议的所有条款。如果您不同意本协议，请立即停止注册或使用本平台。</p>\n<p>2. <strong>适用范围</strong></p>\n<p>本协议适用于您在本平台上的所有活动，包括但不限于发布内容、参与讨论、与其他用户互动等。</p>\n<h2 id=\"%E4%BA%8C%E3%80%81%E7%94%A8%E6%88%B7%E6%9D%83%E5%88%A9%E4%B8%8E%E4%B9%89%E5%8A%A1\">二、用户权利与义务</h2>\n<p>1.&nbsp;<strong>用户权利</strong></p>\n<p>- 您有权在遵守本协议的前提下，使用本平台提供的各项服务。</p>\n<p>- 您有权对本平台的服务提出合理建议。</p>\n<p>2.&nbsp;<strong>用户义务</strong></p>\n<p>- 您应确保注册时提供的信息真实、准确、完整，并及时更新相关信息。</p>\n<p>- 您不得利用本平台从事任何违法、违规或违反社会公序良俗的行为。</p>\n<p>- 您不得侵犯他人隐私、知识产权或其他合法权益。</p>\n<p>- 您不得发布包含暴力、色情、低俗、仇恨言论或虚假信息的内容。</p>\n<p>- 您不得通过技术手段干扰本平台的正常运行或破坏其数据安全。</p>\n<h2 id=\"%E4%B8%89%E3%80%81%E5%B9%B3%E5%8F%B0%E7%9A%84%E6%9D%83%E5%88%A9%E4%B8%8E%E4%B9%89%E5%8A%A1\">三、平台的权利与义务</h2>\n<p>1.&nbsp;<strong>平台权利</strong></p>\n<p>- 平台有权根据实际情况调整服务内容或修改本协议，并通过公告等形式通知用户。</p>\n<p>- 平台有权对用户发布的内容进行审核，并删除不符合规定的内容。</p>\n<p>- 平台有权对违反本协议的用户采取警告、限制功能、封禁账号等措施。</p>\n<p>2.&nbsp;<strong>平台义务</strong></p>\n<p>- 平台将尽力保障服务的稳定性和安全性。</p>\n<p>- 平台将依法保护用户的个人信息，不非法收集、使用或泄露用户信息。</p>\n<h2 id=\"%E5%9B%9B%E3%80%81%E5%86%85%E5%AE%B9%E7%AE%A1%E7%90%86%E4%B8%8E%E8%B4%A3%E4%BB%BB\">四、内容管理与责任</h2>\n<p>1.&nbsp;<strong>用户内容</strong></p>\n<p>- 用户在本平台上发布的内容仅代表个人观点，与平台无关。</p>\n<p>- 用户对其发布内容的合法性、真实性、准确性承担全部责任。</p>\n<p>2.&nbsp;<strong>平台责任</strong></p>\n<p>- 平台仅作为信息发布和交流的平台，不对用户行为及内容承担直接责任。</p>\n<p>- 如因用户行为导致第三方权益受损，由用户自行承担责任。</p>\n<h2>五、隐私政策</h2>\n<p>1.&nbsp;<strong>信息收集</strong></p>\n<p>平台可能会收集您的注册信息、登录记录、浏览记录等必要信息，用于提供服务及优化用户体验。</p>\n<p>2.&nbsp;<strong>信息保护</strong></p>\n<p>平台将严格遵守相关法律法规，保护您的个人信息安全，未经您同意不会向第三方披露您的信息，但以下情况除外：</p>\n<p>- 根据法律法规要求；</p>\n<p>- 配合国家机关调查；</p>\n<p>- 维护平台自身合法权益。</p>\n<h2 id=\"%E5%85%AD%E3%80%81%E6%B3%95%E5%BE%8B%E8%B4%A3%E4%BB%BB\">六、法律责任</h2>\n<p>1.&nbsp;<strong>违约处理</strong></p>\n<p>- 如果您违反本协议，平台有权采取相应措施，包括但不限于暂停或终止您的账号使用。</p>\n<p>- 因您违反本协议而给平台或其他用户造成损失的，您需承担相应的赔偿责任。</p>\n<p>2.&nbsp;<strong>争议解决</strong></p>\n<p>- 本协议的解释和执行均适用中华人民共和国法律。</p>\n<p>- 因本协议引起的任何争议，双方应友好协商解决；协商不成的，可提交至平台所在地有管辖权的人民法院诉讼解决。</p>\n<h2 id=\"%E4%B8%83%E3%80%81%E5%85%B6%E4%BB%96\">七、其他</h2>\n<p>1.&nbsp;<strong>协议更新</strong></p>\n<p>平台保留随时修改本协议的权利，修改后的协议将在平台公告后生效。若您继续使用平台服务，则视为您接受修改后的协议。</p>\n<p>2.&nbsp;<strong>联系方式</strong></p>\n<p>如您对本协议有任何疑问或建议，请通过以下方式联系我们：</p>\n<p>客服邮箱：support@platform.com</p>\n<p>官方网站：www.platform.com</p>\n<hr>\n<p>以上为本平台的用户协议，请您仔细阅读并遵守。感谢您对本平台的支持！</p>',1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_custom_page_contents` VALUES (3,3,'<h1 style=\"text-align: center;\"><strong>隐私政策</strong></h1>\n<p>更新日期：【2025】年【2】月【22】日<br>生效日期：【2025】年【2】月【22】日</p>\n<h2 id=\"%E4%B8%80%E3%80%81%E5%BC%95%E8%A8%80\">一、引言</h2>\n<p>本隐私协议旨在说明本论坛（以下简称&ldquo;平台&rdquo;）如何收集、使用、存储和保护您的个人信息。我们深知个人信息对您的重要性，并承诺将按照相关法律法规的要求，妥善处理您的个人信息。</p>\n<h2 id=\"%E4%BA%8C%E3%80%81%E4%BF%A1%E6%81%AF%E6%94%B6%E9%9B%86\">二、信息收集</h2>\n<p>1.&nbsp;<strong>收集的信息类型</strong></p>\n<p>-&nbsp;<strong>注册信息</strong>：您在注册账号时提供的姓名、手机号码、邮箱地址等信息。</p>\n<p>-&nbsp;<strong>登录信息</strong>：包括IP地址、设备标识符、登录时间、操作记录等。</p>\n<p>-&nbsp;<strong>内容信息</strong>：您在平台上发布的内容、评论、互动记录等。</p>\n<p>-&nbsp;<strong>其他信息</strong>：通过Cookie、日志文件等方式收集的浏览记录、偏好设置等。</p>\n<p>2.&nbsp;<strong>收集信息的目的</strong></p>\n<p>- 提供和优化平台服务；</p>\n<p>- 验证用户身份，保障账户安全；</p>\n<p>- 分析用户行为，改善用户体验；</p>\n<p>- 履行法律法规要求。</p>\n<h2 id=\"%E4%B8%89%E3%80%81%E4%BF%A1%E6%81%AF%E4%BD%BF%E7%94%A8\">三、信息使用</h2>\n<p>1.&nbsp;<strong>内部使用</strong></p>\n<p>我们会将您的个人信息用于提供服务、改进功能、分析数据等内部用途。</p>\n<p>2.&nbsp;<strong>第三方共享</strong></p>\n<p>我们不会向任何第三方出售或出租您的个人信息，但在以下情况下可能会共享：</p>\n<p>- 根据法律法规要求或配合司法调查；</p>\n<p>- 为保护平台及其他用户的合法权益；</p>\n<p>- 在获得您明确授权的情况下。</p>\n<p>3.&nbsp;<strong>匿名化处理</strong></p>\n<p>我们可能会对您的个人信息进行匿名化处理后用于统计分析，生成的统计数据不包含任何可识别个人身份的信息。</p>\n<h2 id=\"%E5%9B%9B%E3%80%81%E4%BF%A1%E6%81%AF%E5%AD%98%E5%82%A8\">四、信息存储</h2>\n<p>1.&nbsp;<strong>存储地点</strong></p>\n<p>您的个人信息将存储在中国大陆境内的服务器上。</p>\n<p>2.&nbsp;<strong>存储期限</strong></p>\n<p>我们仅会在实现服务目的所需的最短时间内保留您的个人信息。当超出存储期限或您主动注销账号时，我们将对您的个人信息进行删除或匿名化处理。</p>\n<h2 id=\"%E4%BA%94%E3%80%81%E4%BF%A1%E6%81%AF%E5%AE%89%E5%85%A8\">五、信息安全</h2>\n<p>1.&nbsp;<strong>技术措施</strong></p>\n<p>我们采用行业标准的安全技术和管理措施，防止您的个人信息被未经授权访问、泄露、篡改或销毁。</p>\n<p>2.&nbsp;<strong>应急响应</strong></p>\n<p>如发生信息安全事件，我们将立即启动应急预案，采取合理措施降低损失，并及时通知受影响的用户。</p>\n<h2 id=\"%E5%85%AD%E3%80%81%E6%82%A8%E7%9A%84%E6%9D%83%E5%88%A9\">六、您的权利</h2>\n<p>1.&nbsp;<strong>查询与更正信息</strong></p>\n<p>您有权随时查询、更正或更新您的个人信息。如需操作，请联系我们的客服团队。</p>\n<p>2.&nbsp;<strong>删除信息</strong></p>\n<p>在以下情况下，您可以请求删除您的个人信息：</p>\n<p>- 我们违反法律法规收集或使用您的信息；</p>\n<p>- 您主动注销账号；</p>\n<p>- 您撤回同意。</p>\n<p>3.&nbsp;<strong>撤回同意</strong></p>\n<p>您可以随时撤回对我们处理您个人信息的授权。请注意，撤回同意可能会影响您正常使用部分功能。</p>\n<h2 id=\"%E4%B8%83%E3%80%81%E6%9C%AA%E6%88%90%E5%B9%B4%E4%BA%BA%E4%BF%9D%E6%8A%A4\">七、未成年人保护</h2>\n<p>我们重视未成年人的隐私保护。如果您未满18岁，请在监护人的陪同下使用本平台，并确保已获得监护人的同意。</p>\n<h2 id=\"%E5%85%AB%E3%80%81%E9%9A%90%E7%A7%81%E5%8D%8F%E8%AE%AE%E7%9A%84%E6%9B%B4%E6%96%B0\">八、隐私协议的更新</h2>\n<p>我们保留随时更新本隐私协议的权利。更新后的隐私协议将在平台公告后生效。若您继续使用平台服务，则视为您接受更新后的隐私协议。</p>\n<h2 id=\"%E4%B9%9D%E3%80%81%E8%81%94%E7%B3%BB%E6%88%91%E4%BB%AC\">九、联系我们</h2>\n<p>如您对本隐私协议有任何疑问或建议，请通过以下方式联系我们：</p>\n<p>- 客服邮箱：privacy@platform.com</p>\n<p>- 官方网站：www.platform.com</p>\n<hr>\n<p>感谢您对本平台的信任！我们将始终致力于保护您的个人信息安全。</p>',1,1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `bbs_custom_page_contents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_custom_pages`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_custom_pages` (
  `page_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '页面ID',
  `page_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面名称',
  `page_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面编码（唯一，用于前端路由）',
  `page_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '页面状态（0启用 1停用）',
  `access_level` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '访问权限（0公开 1登录用户 2指定角色）',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`page_id`) USING BTREE,
  KEY `bbs_custom_pages_page_code_IDX` (`page_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='自定义页面表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_custom_pages`
--

LOCK TABLES `bbs_custom_pages` WRITE;
/*!40000 ALTER TABLE `bbs_custom_pages` DISABLE KEYS */;
INSERT INTO `bbs_custom_pages` VALUES (1,'关于我们','about',0,0,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_custom_pages` VALUES (2,'用户协议','agreement',0,0,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_custom_pages` VALUES (3,'隐私政策','privacy',0,0,1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `bbs_custom_pages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_forum_access`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_forum_access` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `forum_id` int unsigned NOT NULL COMMENT '版块ID',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色key',
  `perms` json NOT NULL COMMENT '权限标识列表,例如["thread:create","thread:edit"]',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_forum_access_forum_id_IDX` (`forum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='版块权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_forum_access`
--

LOCK TABLES `bbs_forum_access` WRITE;
/*!40000 ALTER TABLE `bbs_forum_access` DISABLE KEYS */;
/*!40000 ALTER TABLE `bbs_forum_access` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_forums`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_forums` (
  `forum_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '版块ID',
  `name` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块号，只能用英文或数字',
  `nick_name` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块名称',
  `short_brief` char(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块20字短介绍 纯文本',
  `brief` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块简介 允许HTML',
  `forum_access` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '访问控制 0 所有人能访问 1 所有人禁止访问 2 仅登录用户访问',
  `icon_name` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块图标',
  `seo_title` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO 标题，如果设置会代替版块名称',
  `seo_keywords` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO关键字，不要超过6个，关键字之间用英文的,隔开',
  `seo_content` char(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO content',
  `seo_ext` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'SEO帖子后缀，保留',
  `background_image` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '版块背景图片',
  `show_menu` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '是否显示（1显示 0不显示）',
  `menu_order` int NOT NULL DEFAULT 0 COMMENT '菜单排序',
  `is_system` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否系统内置（1系统内置 0用户自建）',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`forum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_forums`
--

LOCK TABLES `bbs_forums` WRITE;
/*!40000 ALTER TABLE `bbs_forums` DISABLE KEYS */;
INSERT INTO `bbs_forums` VALUES (1,'newest','最新','','NX Forum 是一个基于 Spring Boot + Nuxt 构建的现代化开源论坛系统',0,'tabler:star','','','','','',1,1,1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_forums` VALUES (2,'recommend','推荐','','',0,'tabler:thumb-up','','','','','',0,2,1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_forums` VALUES (3,'follow','关注','','',0,'tabler:eye','','','','','',1,3,1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_forums` VALUES (4,'digest','精华','','',0,'tabler:book','','','','','',0,4,1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_forums` VALUES (5,'hot','热门','','',0,'tabler:flame','','','','','',0,5,1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_forums` VALUES (6,'archive','归档','','归档版块，存储已归档的主题',0,'tabler:archive','','','','','',0,6,1,1,NOW(),1,NOW(),0);
INSERT INTO `bbs_forums` VALUES (11,'test','测试','这是一个测试版块的简介，你可以删除或者修改它','这是一个测试版块的详细介绍，你可以删除或者修改它',0,'tabler:brand-speedtest','','','','','',1,8,0,1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `bbs_forums` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bbs_images`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_images` (
  `image_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `image_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片存储路径',
  `image_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '图片类型（0帖子图片 1用户头像）',
  `file_size` bigint unsigned NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件hash值（MD5）',
  `file_ext` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '文件后缀',
  `storage_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '存储类型（0本地 1阿里云OSS 2腾讯云COS）',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`image_id`) USING BTREE,
  KEY `bbs_images_create_by_IDX` (`create_by`) USING BTREE,
  KEY `bbs_images_file_hash_IDX` (`file_hash`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='图片管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_notifications`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_notifications` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '接收通知的用户ID',
  `post_id` bigint DEFAULT 0 COMMENT '同主题id，方便索引',
  `notification_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '通知类型（如：回复、点赞、系统消息等）',
  `notification_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '通知的状态（如：未读、已读等）',
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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_thread_contents` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `thread_id` bigint unsigned NOT NULL DEFAULT 0 COMMENT '主题id',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_thread_contents_thread_id_IDX` (`thread_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_thread_properties`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_thread_properties` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `thread_id` bigint unsigned NOT NULL COMMENT '主题ID',
  `forum_id` int NOT NULL DEFAULT 0 COMMENT '版块ID',
  `property_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '类型',
  `attribute` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '属性',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_thread_properties_thread_id_IDX` (`thread_id`) USING BTREE,
  KEY `bbs_thread_properties_forum_id_IDX` (`forum_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='特殊属性帖子（置顶、推荐、加精）的缓存表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_threads`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_threads` (
  `thread_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主题id （唯一）',
  `forum_id` int unsigned NOT NULL DEFAULT 0 COMMENT '版块ID',
  `category_id` int unsigned NOT NULL DEFAULT 0 COMMENT '分类 id 0 是未分类',
  `user_ip` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 0 COMMENT '发帖时用户ip地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `subject` char(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主题名称',
  `brief` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '主题简介，自动提取自主题内容',
  `doc_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '类型，0: text 1: html 2:markdown 3: ubb 一般就支持html和markdown',
  `views` int unsigned NOT NULL DEFAULT 0 COMMENT '查看次数',
  `comments` int unsigned NOT NULL DEFAULT 0 COMMENT '评论数',
  `likes` int unsigned NOT NULL DEFAULT 0 COMMENT '点赞数',
  `dislikes` int unsigned NOT NULL DEFAULT 0 COMMENT '不喜欢数',
  `collections` int NOT NULL DEFAULT 0 COMMENT '收藏数',
  `audit_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '0 审核通过 1 审核中 2 审核拒绝 3 忽略',
  `audit_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '审核原因',
  `comment_order` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '默认回帖排序方式 0 时间正序 1 时间倒序 2 热门排序',
  `last_comment_user_id` bigint DEFAULT NULL COMMENT '最近参与的用户（最后回帖的用户名）',
  `last_comment_time` datetime DEFAULT NULL COMMENT '最后回复时间',
  `images` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '正文中的图片，分隔符为,',
  `image_count` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '正文中的图片数量',
  `property` json DEFAULT NULL COMMENT '主题扩展属性',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`thread_id`) USING BTREE,
  KEY `bbs_threads_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_threads_create_by_IDX` (`create_by`) USING BTREE,
  KEY `bbs_threads_create_time_IDX` (`create_time`) USING BTREE,
  KEY `bbs_threads_update_time_IDX` (`update_time`) USING BTREE,
  KEY `bbs_threads_last_comment_time_IDX` (`last_comment_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `bbs_user_bans`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_bans` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '被禁用户ID',
  `forum_id` int unsigned NOT NULL DEFAULT 0 COMMENT '版块ID（0表示全站禁言）',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '禁言原因',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间（null表示永久禁言）',
  `operation_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '操作类型（0禁言 1解禁）',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_bans_user_id_IDX` (`user_id`) USING BTREE,
  KEY `bbs_user_bans_forum_id_IDX` (`forum_id`) USING BTREE,
  KEY `bbs_user_bans_expire_time_IDX` (`expire_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户禁言记录表（expire_time为null表示永久禁言）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_favorites`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_favorites` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` bigint unsigned NOT NULL COMMENT '帖子ID',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_favorites_create_by_IDX` (`create_by`,`thread_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户收藏表（仅支持帖子收藏）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_follows`
--

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

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_likes` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `target_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '点赞目标类型（0帖子 1评论 2楼中楼回复）',
  `target_id` bigint unsigned NOT NULL COMMENT '目标ID（对应thread_id/comment_id/reply_id）',
  `thread_id` bigint unsigned NOT NULL COMMENT '所属帖子ID（冗余设计方便查询）',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_likes_create_by_IDX` (`create_by`,`thread_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户点赞记录表（thread_id为冗余字段，当target_type=0时等于target_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bbs_user_properties`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bbs_user_properties` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `threads` int unsigned NOT NULL DEFAULT 0 COMMENT '发帖数',
  `comments` int unsigned NOT NULL DEFAULT 0 COMMENT '评论数',
  `credits` int unsigned NOT NULL DEFAULT 0 COMMENT '积分',
  `fans` int unsigned NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `golds` int unsigned NOT NULL DEFAULT 0 COMMENT '金币',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `bbs_user_properties_user_id_IDX` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='论坛用户属性表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bbs_user_properties`
--

LOCK TABLES `bbs_user_properties` WRITE;
/*!40000 ALTER TABLE `bbs_user_properties` DISABLE KEYS */;
INSERT INTO `bbs_user_properties` VALUES (1,1,0,0,0,0,0,0,NOW(),0,NOW(),0);
/*!40000 ALTER TABLE `bbs_user_properties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `common_configs`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `common_configs` (
  `config_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '配置id',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键名',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键值',
  `config_type` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '系统内置（0是 1否）',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '说明',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='共用配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `common_configs`
--

LOCK TABLES `common_configs` WRITE;
/*!40000 ALTER TABLE `common_configs` DISABLE KEYS */;
INSERT INTO `common_configs` VALUES (1,'default_forum_id','1',0,'默认首页',1,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (2,'website_base_info','{\"seoContent\":\"\",\"seoTitle\":\"nx-forum - 现代化开源论坛系统\",\"websiteIntroduction\":\"NX Forum 是一个基于 Spring Boot + Nuxt 构建的现代化开源论坛系统\",\"websiteName\":\"nx-forum\"}',0,'默认网站设置',1,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (3,'mail_config','{\"auth\":true,\"defaultFrom\":\"string\",\"host\":\"string\",\"password\":\"string\",\"port\":0,\"protocol\":\"string\",\"starttlsEnable\":true,\"username\":\"string\"}',0,'',1,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (4,'mail_list_config','{\"allowedDomains\":[],\"blockedDomains\":[]}',0,'',1,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (5,'mail_smtp_config','{\n  \"auth\": true,\n  \"defaultFrom\": \"\",\n  \"host\": \"smtpdm.aliyun.com\",\n  \"password\": \"\",\n  \"port\": 465,\n  \"protocol\": \"smtps\",\n  \"starttlsEnable\": true,\n  \"username\": \"\"\n}',0,'',1,NOW(),0,NOW(),0);
INSERT INTO `common_configs` VALUES (6,'mail_template_verify_code','{\"content\":\"<div style=\\\"padding: 0px 20px;\\\">\\n    <p style=\\\"margin-bottom: 20px;\\\">尊敬的 ${userName}：</p>\\n    <p>您好！感谢您使用我们的服务。以下是您的验证码：</p>\\n    <div style=\\\"font-size: 28px; font-weight: bold; text-align: center; letter-spacing: 5px; color: #3366cc; margin: 30px 0; padding: 15px; background-color: #f0f5ff; border-radius: 5px;\\\">\\n        ${code}\\n    </div>\\n    <p>此验证码将在 ${expireMinutes} 分钟后失效，请尽快完成验证。</p>\\n    <p>如果这不是您本人的操作，请忽略此邮件。</p>\\n    <p>祝您使用愉快！</p>\\n    <p>此致，<br>客户服务团队</p>\\n</div>\\n\\n<div style=\\\"font-size: 12px; color: #999; text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;\\\">\\n    <p>此邮件由系统自动发送，请勿直接回复。</p>\\n    <p>如需帮助，请联系我们的客户服务：support@your-company.com</p>\\n    <p>&copy; 2025 您的公司名称. 保留所有权利。</p>\\n</div>\",\"description\":\"验证码邮件模板\",\"html\":true,\"subject\":\"测试站点 - 验证码\"}',0,'',0,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (7,'mail_template_reset_password','{\"content\":\"<div style=\\\"padding: 0px 20px;\\\">\\n    <p style=\\\"margin-bottom: 20px;\\\">尊敬的 ${userName}：</p>\\n    <p>我们收到了您重置密码的请求。请使用以下验证码完成密码重置：</p>\\n    <div\\n        style=\\\"font-size: 28px; font-weight: bold; text-align: center; letter-spacing: 5px; color: #3366cc; margin: 30px 0; padding: 15px; background-color: #f0f5ff; border-radius: 5px;\\\">\\n        ${code}\\n    </div>\\n    <p>此验证码将在 <strong>${expireMinutes} 分钟</strong>后失效，请尽快完成密码重置操作。</p>\\n    <p style=\\\"color: #e74c3c; font-weight: bold;\\\">如果这不是您本人的操作，请立即联系我们的客户服务团队，您的账户可能存在安全风险。</p>\\n    <p>为了保障您的账户安全，建议您：</p>\\n    <ul style=\\\"margin-bottom: 20px;\\\">\\n        <li>定期更改密码</li>\\n        <li>使用强密码（包含大小写字母、数字和特殊字符）</li>\\n        <li>不要在多个网站使用相同的密码</li>\\n    </ul>\\n    <p>此致，<br>安全团队</p>\\n</div>\\n\\n<div\\n    style=\\\"font-size: 12px; color: #999; text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;\\\">\\n    <p>此邮件由系统自动发送，请勿直接回复。</p>\\n    <p>如需帮助，请联系我们的客户服务：support@your-company.com</p>\\n    <p>&copy; 2025 您的公司名称. 保留所有权利。</p>\\n</div>\",\"description\":\"重置密码邮件模板\",\"html\":true,\"subject\":\"测试站点 - 重置密码\"}',0,'',0,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (8,'mail_template_register_success','{\"content\":\"<body\\n    style=\\\"margin: 0; padding: 0; font-family: \'Helvetica Neue\', Helvetica, Arial, sans-serif; color: #333333; background-color: #f7f7f7; line-height: 1.6;\\\">\\n    <table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" width=\\\"100%\\\"\\n        style=\\\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-collapse: collapse;\\\">\\n        <!-- 头部 -->\\n        <tr>\\n            <td style=\\\"padding: 30px 0; text-align: center; background-color: #4285f4;\\\">\\n                <h1 style=\\\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 500;\\\">欢迎加入我们</h1>\\n            </td>\\n        </tr>\\n\\n        <!-- 内容区域 -->\\n        <tr>\\n            <td style=\\\"padding: 40px 30px;\\\">\\n                <p style=\\\"margin-top: 0; font-size: 16px;\\\">亲爱的 <span style=\\\"font-weight: bold;\\\">${userName}</span>，</p>\\n                <p style=\\\"font-size: 16px;\\\">感谢您注册我们的网站！您的账号已经成功创建，现在您可以享受我们提供的所有服务了。</p>\\n                <div style=\\\"margin: 30px 0; text-align: center;\\\">\\n                    <a href=\\\"${domain}/uc/login\\\"\\n                        style=\\\"display: inline-block; background-color: #4285f4; color: #ffffff; text-decoration: none; font-weight: bold; padding: 12px 30px; border-radius: 4px; font-size: 16px;\\\">立即登录</a>\\n                </div>\\n                <p style=\\\"font-size: 16px;\\\">如果您有任何问题，请随时联系我们的客服团队。</p>\\n                <p style=\\\"font-size: 16px;\\\">祝您使用愉快！</p>\\n            </td>\\n        </tr>\\n\\n        <!-- 底部 -->\\n        <tr>\\n            <td\\n                style=\\\"padding: 20px 30px; text-align: center; background-color: #f5f5f5; border-top: 1px solid #dddddd; font-size: 14px; color: #777777;\\\">\\n                <p style=\\\"margin: 0 0 10px 0;\\\">© 2025 {公司名称}. 保留所有权利。</p>\\n                <p style=\\\"margin: 0 0 10px 0;\\\">\\n                    <a href=\\\"${domain}/privacy\\\" style=\\\"color: #4285f4; text-decoration: none; margin: 0 10px;\\\">隐私政策</a> |\\n                    <a href=\\\"${domain}/agreement\\\" style=\\\"color: #4285f4; text-decoration: none; margin: 0 10px;\\\">用户协议</a>\\n                </p>\\n                <p style=\\\"margin: 10px 0 0 0; font-size: 12px;\\\">如果您没有注册我们的服务，请忽略此邮件。</p>\\n            </td>\\n        </tr>\\n    </table>\\n</body>\",\"description\":\"注册成功邮件模板\",\"html\":true,\"subject\":\"欢迎加入 测试站点\"}',0,'',0,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (9,'uc_login_config','{\"enableEmailCodeLogin\":true,\"enableEmailResetPassword\":true,\"enableLoginCaptcha\":true,\"enablePasswordLogin\":true,\"enablePhoneCodeLogin\":false,\"enablePhoneResetPassword\":false,\"loginLockMinutes\":30,\"maxLoginFailCount\":5}',0,'',0,NOW(),12,NOW(),0);
INSERT INTO `common_configs` VALUES (10,'uc_register_config','{\"enableEmailRegister\":true,\"enableRegister\":true,\"enableRegisterCaptcha\":true,\"enableSmsRegister\":false,\"forbiddenUsernames\":[\"小编\",\"管理员\",\"版主\",\"admin\"],\"passwordMaxLength\":20,\"passwordMinLength\":6,\"passwordStrength\":1}',0,'',0,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (11,'sms_config','{\n  \"accessKeyId\": \"\",\n  \"accessKeySecret\": \"\",\n  \"blacklist\": [],\n  \"enabled\": false,\n  \"provider\": \"ALIYUN\",\n  \"signName\": \"\",\n  \"verifyCodeTemplateId\": \"\"\n}',0,'',1,NOW(),1,NOW(),0);
INSERT INTO `common_configs` VALUES (12,'audit_config_black_white_users','{\n  \"blackListUsers\": [],\n  \"whiteListUsers\": [\n  ]\n}',0,'',1,NOW(),4,NOW(),0);
INSERT INTO `common_configs` VALUES (13,'audit_config_sensitive_words','{\"enableSensitiveWordsAudit\":true,\"sensitiveWords\":[]}',0,'',1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `common_configs` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `uc_permissions`
--

LOCK TABLES `uc_permissions` WRITE;
/*!40000 ALTER TABLE `uc_permissions` DISABLE KEYS */;
INSERT INTO `uc_permissions` VALUES (3,24,'查看主题',0,'thread:view',3,0,'查看帖子的权限',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (4,24,'编辑帖子',0,'thread:edit',3,2,'编辑帖子的权限',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (7,24,'删除主题',0,'thread:delete',3,3,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (8,24,'新建主题',0,'thread:new',3,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (9,25,'新建评论',0,'comment:new',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (10,25,'删除评论',0,'comment:delete',3,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (11,0,'用户权限',0,'',1,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (12,0,'管理权限',0,'admin:manage',2,2,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (14,12,'系统管理',0,'admin:system',2,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (15,14,'基本设置',0,'admin:system:basic',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (16,14,'版块管理',0,'admin:system:forum',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (17,14,'审核设置',0,'admin:system:audit',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (18,12,'帖子管理',0,'admin:thread',2,2,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (19,11,'访问论坛',0,'forum:visit',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (20,19,'访问版块',0,'forum:visit:section',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (21,18,'主题查询',0,'admin:thread:search',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (22,18,'评论查询',0,'admin:comment:search',2,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (23,21,'删除主题',0,'admin:thread:delete',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (24,11,'主题管理',0,'',1,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (25,11,'评论管理',0,'',1,2,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (26,21,'置顶主题',0,'admin:thread:top',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (27,21,'关闭主题',0,'admin:thread:close',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (28,21,'主题精华',0,'admin:thread:digest',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (29,21,'推荐主题',0,'admin:thread:recommend',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (30,21,'转移主题',0,'admin:thread:transfer',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (31,21,'编辑主题',0,'admin:thread:edit',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (32,21,'还原主题',0,'admin:thread:restore',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (33,21,'通过主题',0,'admin:thread:pass',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (34,21,'拒绝主题',0,'admin:thread:reject',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (35,22,'删除评论',0,'admin:comment:delete',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (36,22,'通过评论',0,'admin:comment:pass',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (37,22,'拒绝评论',0,'admin:comment:reject',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (38,22,'还原评论',0,'admin:comment:restore',3,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (39,19,'访问其他',0,'forum:visit:other',3,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (40,11,'附件上传',0,'',1,3,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (41,40,'上传图片',0,'image:upload',3,2,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (42,40,'上传头像',0,'avatar:upload',3,1,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (43,14,'自定义页面',0,'admin:system:page',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (44,14,'图片管理',0,'admin:system:image',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (45,12,'用户管理',0,'admin:user',2,3,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (46,45,'小黑屋',0,'admin:user:ban',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (47,12,'管理首页',0,'admin:home',2,0,'',1,NOW(),1,NOW(),0);
INSERT INTO `uc_permissions` VALUES (48,18,'用户举报',0,'admin:user:report',2,2,'',1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `uc_permissions` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `uc_role_permissions`
--

LOCK TABLES `uc_role_permissions` WRITE;
/*!40000 ALTER TABLE `uc_role_permissions` DISABLE KEYS */;
INSERT INTO `uc_role_permissions` VALUES (1,'GUEST',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (2,'GUEST',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (3,'GUEST',39,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (4,'GUEST',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (5,'USER',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (6,'USER',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (7,'USER',8,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (8,'USER',4,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (9,'USER',9,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (10,'USER',42,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (11,'USER',41,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (12,'USER',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (13,'MUTED_USER',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (14,'MUTED_USER',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (15,'MUTED_USER',42,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (16,'MUTED_USER',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (17,'CREATOR',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (18,'CREATOR',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (19,'CREATOR',39,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (20,'CREATOR',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (21,'CREATOR',8,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (22,'CREATOR',4,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (23,'CREATOR',7,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (24,'CREATOR',9,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (25,'CREATOR',10,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (26,'CREATOR',42,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (27,'CREATOR',41,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (28,'ADMIN',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (29,'ADMIN',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (30,'ADMIN',39,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (31,'ADMIN',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (32,'ADMIN',8,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (33,'ADMIN',4,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (34,'ADMIN',7,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (35,'ADMIN',9,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (36,'ADMIN',10,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (37,'ADMIN',42,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (38,'ADMIN',41,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (39,'ADMIN',12,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (40,'ADMIN',47,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (41,'ADMIN',14,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (42,'ADMIN',15,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (43,'ADMIN',16,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (44,'ADMIN',17,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (45,'ADMIN',43,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (46,'ADMIN',44,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (47,'ADMIN',18,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (48,'ADMIN',21,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (49,'ADMIN',23,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (50,'ADMIN',26,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (51,'ADMIN',27,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (52,'ADMIN',28,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (53,'ADMIN',29,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (54,'ADMIN',30,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (55,'ADMIN',31,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (56,'ADMIN',32,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (57,'ADMIN',33,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (58,'ADMIN',34,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (59,'ADMIN',22,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (60,'ADMIN',35,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (61,'ADMIN',36,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (62,'ADMIN',37,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (63,'ADMIN',38,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (64,'ADMIN',48,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (65,'ADMIN',45,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (66,'ADMIN',46,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (67,'SUPER_MODERATOR',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (68,'SUPER_MODERATOR',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (69,'SUPER_MODERATOR',39,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (70,'SUPER_MODERATOR',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (71,'SUPER_MODERATOR',8,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (72,'SUPER_MODERATOR',4,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (73,'SUPER_MODERATOR',7,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (74,'SUPER_MODERATOR',9,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (75,'SUPER_MODERATOR',10,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (76,'SUPER_MODERATOR',42,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (77,'SUPER_MODERATOR',41,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (78,'SUPER_MODERATOR',18,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (79,'SUPER_MODERATOR',21,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (80,'SUPER_MODERATOR',23,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (81,'SUPER_MODERATOR',26,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (82,'SUPER_MODERATOR',27,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (83,'SUPER_MODERATOR',28,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (84,'SUPER_MODERATOR',29,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (85,'SUPER_MODERATOR',30,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (86,'SUPER_MODERATOR',31,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (87,'SUPER_MODERATOR',32,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (88,'SUPER_MODERATOR',33,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (89,'SUPER_MODERATOR',34,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (90,'SUPER_MODERATOR',22,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (91,'SUPER_MODERATOR',35,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (92,'SUPER_MODERATOR',36,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (93,'SUPER_MODERATOR',37,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (94,'SUPER_MODERATOR',38,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (95,'SUPER_MODERATOR',48,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (96,'SUPER_MODERATOR',45,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (97,'SUPER_MODERATOR',46,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (98,'SUPER_MODERATOR',12,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (99,'MODERATOR',19,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (100,'MODERATOR',20,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (101,'MODERATOR',39,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (102,'MODERATOR',3,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (103,'MODERATOR',8,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (104,'MODERATOR',4,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (105,'MODERATOR',7,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (106,'MODERATOR',9,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (107,'MODERATOR',10,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (108,'MODERATOR',42,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (109,'MODERATOR',41,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (110,'MODERATOR',18,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (111,'MODERATOR',21,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (112,'MODERATOR',23,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (113,'MODERATOR',26,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (114,'MODERATOR',27,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (115,'MODERATOR',28,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (116,'MODERATOR',29,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (117,'MODERATOR',30,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (118,'MODERATOR',31,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (119,'MODERATOR',32,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (120,'MODERATOR',33,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (121,'MODERATOR',34,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (122,'MODERATOR',22,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (123,'MODERATOR',35,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (124,'MODERATOR',36,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (125,'MODERATOR',37,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (126,'MODERATOR',38,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (127,'MODERATOR',48,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (128,'MODERATOR',45,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (129,'MODERATOR',46,1,NOW(),1,NOW(),0);
INSERT INTO `uc_role_permissions` VALUES (130,'MODERATOR',12,1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `uc_role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `uc_roles`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_roles` (
  `role_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '角色id',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色权限字符串',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '说明',
  `role_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '角色状态0-正常1-停用',
  `priority` int NOT NULL DEFAULT '1' COMMENT '优先级(数字越大优先级越高)',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `uc_roles`
--

LOCK TABLES `uc_roles` WRITE;
/*!40000 ALTER TABLE `uc_roles` DISABLE KEYS */;
INSERT INTO `uc_roles` VALUES (1,'用户中心管理员','UC_ADMIN','用户中心管理员，拥有所有用户管理权限',0,10,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (2,'论坛管理员','ADMIN','论坛管理员，拥有所有论坛权限',0,10,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (3,'超级版主','SUPER_MODERATOR','跨版块的超级版主，可以管理所有版块',0,10,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (4,'版主','MODERATOR','负责管理指定版块的内容和用户',0,10,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (5,'注册用户','USER','已注册的普通用户',0,1,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (6,'禁言用户','MUTED_USER','被禁言的用户，无法发帖和评论',0,2,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (7,'游客','GUEST','未注册用户，只能浏览公开内容',0,1,0,NOW(),1,NOW(),0);
INSERT INTO `uc_roles` VALUES (8,'内容创作者','CREATOR','内容创作者，可以发帖',0,1,0,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `uc_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `uc_user_login_tokens`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_user_login_tokens` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `device_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '设备id',
  `token` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '令牌',
  `token_expires_at` datetime NOT NULL COMMENT '令牌失效时间',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '客户端useragent',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '登录IP',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `uc_user_login_tokens_token_IDX` (`token`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `uc_user_roles`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_user_roles` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 0 COMMENT '角色key',
  `role_scope` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色范围',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `uc_user_roles`
--

LOCK TABLES `uc_user_roles` WRITE;
/*!40000 ALTER TABLE `uc_user_roles` DISABLE KEYS */;
INSERT INTO `uc_user_roles` VALUES (1,1,'UC_ADMIN','ALL',NULL,1,NOW(),1,NOW(),0);
INSERT INTO `uc_user_roles` VALUES (2,1,'ADMIN','ALL',NULL,1,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `uc_user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `uc_users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_users` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '邮箱',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '手机号码',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像地址',
  `account_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '帐号状态（0正常 1停用 2已注销）',
  `last_active_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '最后活跃IP',
  `last_active_date` datetime NOT NULL COMMENT '最后活跃时间',
  `intro` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '个人介绍',
  `create_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint unsigned NOT NULL DEFAULT 0 COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `uc_users_email_IDX` (`email`) USING BTREE,
  KEY `uc_users_user_name_IDX` (`user_name`) USING BTREE,
  KEY `uc_users_phone_IDX` (`phone`) USING BTREE,
  KEY `uc_users_last_active_ip_IDX` (`last_active_ip`) USING BTREE,
  KEY `uc_users_create_time_IDX` (`create_time`) USING BTREE,
  KEY `uc_users_update_time_IDX` (`update_time`) USING BTREE,
  KEY `uc_users_last_active_date_IDX` (`last_active_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `uc_users`
--

LOCK TABLES `uc_users` WRITE;
/*!40000 ALTER TABLE `uc_users` DISABLE KEYS */;
INSERT INTO `uc_users` VALUES (1,'admin','email@domain.com','','$2a$10$L0cG3rXxjpOOumYGkKERZuAoRNOXJlDnII8VFT1QAAKooeK3/N/xK','',0,'127.0.0.1',NOW(),'',0,NOW(),1,NOW(),0);
/*!40000 ALTER TABLE `uc_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `uc_user_logs`
--

DROP TABLE IF EXISTS `uc_user_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uc_user_logs` (
  `log_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `log_type` tinyint unsigned NOT NULL COMMENT '日志类型（1登录 2登出 3注册 4信息修改 5密码修改）',
  `log_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '日志内容',
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '操作IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户代理信息',
  `operation_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '操作状态（0成功 1失败）',
  `create_by` bigint unsigned NOT NULL DEFAULT '0' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `uc_user_logs_create_by_IDX` (`create_by`) USING BTREE,
  KEY `uc_user_logs_create_time_IDX` (`create_time`) USING BTREE,
  KEY `uc_user_logs_log_type_IDX` (`log_type`) USING BTREE,
  KEY `uc_user_logs_ip_address_IDX` (`ip_address`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;


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
   `reported_content` varchar(500) CHARACTER set utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '被举报内容（冗余字段）' ,
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

-- Dump completed on 2025-03-07  0:19:49
