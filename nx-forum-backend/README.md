# NX Forum

NX Forum 是一个基于 Spring Boot + Nuxt 构建的现代化开源论坛系统。

## 主要功能模块

### 论坛核心功能 (nx-biz-forum)

- 版块管理
- 帖子管理
- 评论系统
- 通知系统
- 内容审核

### 用户中心 (nx-module-uc)

- 用户管理
- 角色权限
- 认证授权
- 安全控制

### 配置中心 (nx-module-config)

- 系统配置
- 网站信息配置
- 版块配置

### 公共模块 (nx-common)

- 工具类
- 通用组件
- MyBatis 增强
- IP 地址解析
- 安全组件

## 项目特点

1. 采用 COLA 架构，实现清晰的领域驱动设计
2. 完整的权限控制体系
3. 支持内容审核机制
4. 灵活的配置管理
5. 完善的 API 文档（集成 Knife4j）
6. 分层清晰，代码结构规范

## 开发环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+（可选）

## 配置说明

主要配置文件位于 `nx-forum-start/src/main/resources/application.yml`：

- 数据库配置
- Redis 配置
- 文件上传地址
- 日志配置

```
# 文件上传路径本地测试建议配前端项目的public目录地址，方便快速调试，生产环境建议使用 nginx 反向代理
nx.file.upload.path=/Users/walker/Code/nx-forum-nuxt/public/
```

## API 文档

启动项目后访问：`http://localhost:8083/nx-forum/doc.html`