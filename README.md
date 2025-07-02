# NX Forum

NX Forum 是一个基于 Spring Boot + Nuxt 构建的现代化开源论坛系统。

- 前后端分离，方便后期维护
- 前端使用 Nuxt3 构建，支持 SSR，对 SEO 有很好的支持
- 模块化设计，可独立部署

前端仓库：[nx-forum-nuxt](https://github.com/walker8/nx-forum-nuxt) 测试账号admin/12345678

## 注意事项

- 当前项目为测试版本，仅供学习和测试环境使用，不可用于生产环境
- 因为是测试版本，未经过充分测试，后续升级亦不考虑兼容性
- 目前只实现了核心功能，后续会继续完善剩余功能开发
- 正式版本预计在 2025 年年底发布，敬请期待

## 常见问题

1. 更新代码后启动报FlywayValidateException: Validate failed: Migrations have failed validation
   测试版本脚本升级不兼容，请删除数据库中所有表后重新运行程序，正式版本不会有此类问题

## 项目结构

```
nx-parent/
├── nx-biz-forum/ # 论坛业务模块
│ ├── nx-forum-adapter/ # 接口适配层
│ ├── nx-forum-app/ # 应用服务层
│ ├── nx-forum-domain/ # 领域层
│ ├── nx-forum-infrastructure/ # 基础设施层
│ └── nx-forum-start/ # 启动模块
└── nx-platform/ # 平台基础模块
├── nx-common/ # 公共工具模块
├── nx-module-config/ # 配置中心模块
└── nx-module-uc/ # 用户中心模块
│ ├── nx-module-uc-adapter/
│ ├── nx-module-uc-app/
│ ├── nx-module-uc-domain/
│ └── nx-module-uc-infrastructure/
└── README.md
```

## 技术栈

- Java 17
- Spring Boot 3.5.3
- MyBatis-Plus 3.5.12
- Redis
- MySQL
- COLA 架构
- Knife4j (API 文档)
- MapStruct (对象映射)

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
nx.file.upload.path=/Users/walker/Code/nx-forum-nuxt3/public/
```

## API 文档

启动项目后访问：`http://localhost:8083/nx-forum/doc.html`

## 开发团队

- Walker

## 许可证

MIT
