# nx-biz-forum 模块架构文档

## 模块概述

nx-biz-forum 是论坛业务模块，基于 COLA（Clean Object-Oriented and Layered Architecture）框架开发，同时支持 DDD（领域驱动设计）和 MVC（模型-视图-控制器）两种开发模式。

## 架构分层

```
┌─────────────────────────────────────────┐
│       Adapter Layer (适配层)            │  ← 按客户端类型分层：web/admin/mobile/wap
├─────────────────────────────────────────┤
│     Application Layer (应用层)          │  ← 按业务领域分层
├─────────────────────────────────────────┤
│       Domain Layer (领域层)             │  ← 【仅 Thread、Comment】核心业务逻辑
├─────────────────────────────────────────┤
│   Infrastructure Layer (基础设施层)     │  ← 按业务领域分层
└─────────────────────────────────────────┘
```

## 业务领域划分

### 1. 内容管理域（content）

#### 1.1 帖子管理（thread）- DDD 模式 ✨
**包路径**：`com.leyuz.bbs.content.thread`

**层次结构**：
- **Adapter**：`com.leyuz.bbs.web.ThreadController`、`com.leyuz.bbs.admin.AdminThreadController`
- **Application**：`com.leyuz.bbs.content.thread.ThreadApplication`
- **Domain**：`com.leyuz.bbs.content.thread.ThreadE`、`ThreadDomainService`、`ThreadGateway`
- **Infrastructure**：`com.leyuz.bbs.content.thread.ThreadGatewayImpl`

**核心功能**：
- 帖子发布、编辑、删除
- 帖子查询（多种策略：热门、最新、精华、关注等）
- 帖子审核（自动+人工）
- 帖子属性管理（置顶、精华、推荐、关闭）
- 帖子转移

**设计模式**：
- 策略模式：`ThreadQueryStrategy`
- 插件模式：`ThreadPlugin`
- 事件驱动：`ThreadNewEvent`、`ThreadPassedEvent` 等

#### 1.2 评论管理（comment）- DDD 模式 ✨
**包路径**：`com.leyuz.bbs.content.comment`

**层次结构**：
- **Adapter**：`com.leyuz.bbs.web.CommentController`、`com.leyuz.bbs.admin.AdminCommentController`
- **Application**：`com.leyuz.bbs.content.comment.CommentApplication`
- **Domain**：`com.leyuz.bbs.content.comment.CommentE`、`CommentReplyE`、`CommentDomainService`
- **Infrastructure**：`com.leyuz.bbs.content.comment.CommentGatewayImpl`

**核心功能**：
- 评论发布、编辑、删除
- 回复发布、编辑、删除
- 评论查询和分页
- 评论审核
- 评论排序（时间正序/倒序/热门）

### 2. 用户交互域（interaction）

#### 2.1 点赞功能（like）- MVC 模式
**包路径**：`com.leyuz.bbs.interaction.like`

**核心功能**：
- 点赞/取消点赞（帖子、评论、回复）
- 查询用户点赞列表
- 点赞状态判断

#### 2.2 收藏功能（favorite）- MVC 模式
**包路径**：`com.leyuz.bbs.interaction.favorite`

**核心功能**：
- 收藏/取消收藏帖子
- 查询用户收藏列表
- 收藏状态判断

#### 2.3 举报功能（report）- MVC 模式
**包路径**：`com.leyuz.bbs.interaction.report`

**核心功能**：
- 举报提交（帖子、评论、回复）
- 举报处理（通过/驳回）
- 举报查询和统计

#### 2.4 关注功能（follow）- MVC 模式
**包路径**：`com.leyuz.bbs.interaction.follow`

**核心功能**：
- 关注/取消关注用户
- 查询关注列表和粉丝列表
- 关注状态判断

### 3. 论坛组织域（forum）

#### 3.1 版块管理（forum）- MVC 模式
**包路径**：`com.leyuz.bbs.forum`

**核心功能**：
- 版块创建、编辑、删除
- 版块菜单和列表查询
- 版块权限配置
- 版块用户角色管理

### 4. 用户管理域（user）

#### 4.1 用户信息和禁言（user）- MVC 模式
**包路径**：`com.leyuz.bbs.user`

**核心功能**：
- 论坛用户信息查询
- 用户属性统计（发帖数、评论数、粉丝数）
- 用户禁言管理

### 5. 系统管理域（system）

#### 5.1 配置管理（config）- MVC 模式
**包路径**：`com.leyuz.bbs.system.config`

**核心功能**：
- 网站基本信息配置
- 审核配置（敏感词、黑白名单）
- 默认版块配置

#### 5.2 审核管理（audit）- MVC 模式
**包路径**：`com.leyuz.bbs.system.audit`

**核心功能**：
- 内容审核（帖子、评论）
- 敏感词检测
- 用户黑白名单判断

#### 5.3 通知管理（notification）- MVC 模式
**包路径**：`com.leyuz.bbs.system.notification`

**核心功能**：
- 评论通知
- 回复通知
- @提醒通知
- 系统消息
- 通知查询和清理

#### 5.4 图片管理（image）- MVC 模式
**包路径**：`com.leyuz.bbs.system.image`

**核心功能**：
- 图片查询
- 图片删除
- 图片去重

#### 5.5 自定义页面（page）- MVC 模式
**包路径**：`com.leyuz.bbs.system.page`

**核心功能**：
- 自定义页面管理
- 页面内容管理

#### 5.6 RSS 订阅（rss）- MVC 模式
**包路径**：`com.leyuz.bbs.system.rss`

**核心功能**：
- RSS Feed 生成
- 内容聚合

#### 5.7 附件管理（attach）- MVC 模式
**包路径**：`com.leyuz.bbs.system.attach`

**核心功能**：
- 图片上传
- 图片下载
- 存储管理

### 6. 权限认证域（auth）

**包路径**：`com.leyuz.bbs.auth`

**核心功能**：
- 权限校验
- 角色管理
- 版块权限控制

## 分层设计原则

### Adapter 层（适配层）
- **职责**：处理 HTTP 请求，参数校验，响应封装
- **分层方式**：按客户端类型分层（web/admin/mobile/wap）
- **命名规范**：`*Controller`
- **技术要点**：
  - 使用 `@RestController`
  - 使用 `@RequiredArgsConstructor` 进行依赖注入
  - 使用 `SingleResponse`/`MultiResponse` 包装响应
  - 使用 `@Operation` 文档化 API

### Application 层（应用层）
- **职责**：业务编排、流程控制、权限校验、事务管理
- **分层方式**：按业务领域分层
- **命名规范**：`*Application`
- **DDD 模式**：
  - 调用 Domain 层的 DomainService
  - 通过 Gateway 接口访问数据
  - 处理 DTO 与领域对象的转换
- **MVC 模式**：
  - 直接调用 Infrastructure 层的 Mapper
  - 处理业务逻辑和数据转换

### Domain 层（领域层）【仅 DDD 模式】
- **职责**：封装核心业务逻辑和规则
- **包含**：
  - 领域实体（`*E`）
  - 值对象（`*V`）
  - 领域服务（`*DomainService`）
  - 网关接口（`*Gateway`）
  - 领域事件（`*Event`）
- **原则**：
  - 不依赖基础设施层
  - 包含业务规则验证
  - 状态管理和转换

### Infrastructure 层（基础设施层）
- **职责**：技术实现，数据持久化
- **分层方式**：按业务领域分层
- **DDD 模式**：
  - 实现 Gateway 接口（`*GatewayImpl`）
  - PO 与领域对象转换
- **MVC 模式**：
  - 定义 Mapper 接口（`I*Service`）
  - 实现 Mapper（`*ServiceImpl`）
  - 继承 MyBatis-Plus `IService`

## 模式选择指南

### 使用 DDD 模式的场景
- ✅ 复杂的业务逻辑
- ✅ 需要领域模型
- ✅ 多层次的业务编排
- ✅ 重要的业务流程

**示例**：帖子管理（发布、审核、属性管理）、评论管理（评论、回复、审核）

### 使用 MVC 模式的场景
- ✅ 简单的 CRUD 操作
- ✅ 查询场景
- ✅ 配置管理
- ✅ 工具类服务

**示例**：点赞、收藏、举报、配置查询、图片管理

## 技术栈

- **框架**：Spring Boot 3.x + COLA
- **ORM**：MyBatis-Plus
- **缓存**：本地缓存（Caffeine）+ Redis
- **事件**：Spring Event
- **文档**：Swagger/OpenAPI 3.0
- **工具**：Lombok、Hutool

## 代码规范

1. **依赖注入**：统一使用 `@RequiredArgsConstructor`
2. **命名规范**：
   - Controller：`*Controller`
   - Application：`*Application`
   - DomainService：`*DomainService`
   - Gateway：`*Gateway`
   - GatewayImpl：`*GatewayImpl`
3. **数据对象**：
   - Cmd：命令对象
   - Query：查询对象
   - VO：视图对象
   - E：领域实体
   - V：值对象
   - PO：持久化对象
4. **事务管理**：在 Application 层使用 `@Transactional`
5. **异常处理**：使用 `ValidationException`、`BusinessException`

## 目录结构

```
nx-biz-forum/
├── nx-forum-adapter/           # 适配层（按客户端类型）
│   └── src/main/java/com/leyuz/bbs/
│       ├── web/               # Web 控制器
│       ├── admin/             # 管理后台控制器
│       ├── mobile/            # 移动端控制器
│       └── wap/               # WAP 端控制器
├── nx-forum-app/              # 应用层（按业务领域）
│   └── src/main/java/com/leyuz/bbs/
│       ├── content/           # 内容管理域
│       │   ├── thread/       # 帖子（DDD）
│       │   └── comment/      # 评论（DDD）
│       ├── interaction/       # 用户交互域
│       │   ├── like/         # 点赞（MVC）
│       │   ├── favorite/     # 收藏（MVC）
│       │   ├── report/       # 举报（MVC）
│       │   └── follow/       # 关注（MVC）
│       ├── forum/            # 论坛组织域（MVC）
│       ├── user/             # 用户管理域（MVC）
│       │   └── ban/          # 禁言管理
│       ├── system/           # 系统管理域
│       │   ├── config/      # 配置（MVC）
│       │   ├── audit/       # 审核（MVC）
│       │   ├── notification/ # 通知（MVC）
│       │   ├── image/       # 图片（MVC）
│       │   ├── page/        # 自定义页面（MVC）
│       │   ├── rss/         # RSS（MVC）
│       │   └── attach/      # 附件（MVC）
│       └── auth/            # 权限认证域
├── nx-forum-domain/           # 领域层（仅 DDD）
│   └── src/main/java/com/leyuz/bbs/
│       ├── content/
│       │   ├── thread/       # 帖子领域模型
│       │   └── comment/      # 评论领域模型
│       └── common/           # 公共值对象和事件
└── nx-forum-infrastructure/   # 基础设施层（按业务领域，扁平化结构）
    └── src/main/java/com/leyuz/bbs/
        ├── content/
        │   ├── thread/       # ThreadPO.java, ThreadMapper.java, ThreadGatewayImpl.java
        │   └── comment/      # CommentPO.java, CommentMapper.java, CommentGatewayImpl.java
        ├── interaction/      # 交互功能数据访问（扁平化）
        │   ├── favorite/     # FavoritePO.java, FavoriteMapper.java
        │   ├── follow/       # UserFollowPO.java, UserFollowMapper.java
        │   ├── like/         # LikePO.java, LikeMapper.java
        │   └── report/       # ReportPO.java, ReportMapper.java
        ├── forum/           # ForumPO.java, ForumMapper.java
        ├── user/            # 用户数据访问
        │   ├── ban/         # BanPO.java, BanMapper.java
        │   └── property/    # ForumUserPropertyPO.java, ForumUserPropertyMapper.java
        └── system/          # 系统功能数据访问（扁平化）
            ├── image/       # ImagePO.java, ImageMapper.java
            ├── notification/ # NotificationPO.java, NotificationMapper.java
            └── page/        # CustomPagePO.java, CustomPageMapper.java
```

## 关键设计

### 1. 事件驱动架构
- Thread 和 Comment 通过事件实现与其他模块解耦
- 事件监听器处理通知、统计等副作用
- 支持异步事件处理

### 2. 策略模式
- 帖子查询支持多种策略：热门、最新、精华、关注、推荐等
- 通过 `ThreadQueryStrategyFactory` 动态选择策略

### 3. 插件机制
- 帖子保存和更新支持插件扩展
- 通过 `ThreadPluginManager` 管理插件生命周期

### 4. 缓存策略
- 本地缓存：版块信息、热门帖子等
- 分布式缓存：用户信息、权限信息等
- 多级缓存提升性能

### 5. 权限控制
- 基于版块的权限控制
- 支持角色优先级
- 灵活的权限分配机制

## 最佳实践

### 1. 模块间调用
- ✅ 优先调用 Application 层
- ✅ 避免调用 Domain 层
- ❌ 禁止调用 Infrastructure 层

### 2. 代码复用
- 公共工具类放在 `common` 包
- 通用业务逻辑抽取到独立的 Application
- 值对象可在模块间共享

### 3. 异常处理
- 使用 `ValidationException` 处理业务校验错误
- 使用 `BusinessException` 处理业务逻辑错误
- 统一的异常处理和错误信息

### 4. 事务管理
- 在 Application 层使用 `@Transactional`
- 注意事务传播级别
- 避免长事务

## 升级和维护

### 何时重构 MVC 为 DDD？
当 MVC 模式的代码出现以下情况时，应考虑重构为 DDD：
- Application 层方法超过 100 行
- 出现大量的 `if-else` 业务判断
- 业务规则散落在多处
- 需要频繁修改业务逻辑

### 何时保持 MVC？
- 简单的 CRUD 操作
- 查询场景
- 配置管理
- 工具类服务

---
**版本**：1.0.0  
**最后更新**：2025-10-27  
**维护者**：walker


