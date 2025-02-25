# NX Forum

NX Forum 是一个基于 Spring Boot + Nuxt 构建的现代化开源论坛系统。

- 前后端分离，方便后期维护
- 前端使用 Nuxt 构建，支持 SSR，对 SEO 有很好的支持
- 模块化设计，可独立部署
- 采用 COLA 架构，实现清晰的领域驱动设计

测试账号：admin/12345678

## 注意事项

- 当前项目为测试版本，仅供学习和测试环境使用，不可用于生产环境
- 因为是测试版本，未经过充分测试，后续升级亦不考虑兼容性
- 目前只实现了核心功能，后续会继续完善剩余功能开发
- 正式版本预计会在 2026 年发布，敬请期待

## 常见问题

1. 更新代码后启动报 FlywayValidateException: Validate failed: Migrations have failed validation
    - 测试版本脚本升级不兼容，请删除数据库中所有表后重新运行程序，正式版本不会有此类问题

## 项目结构

```
nx-forum/
├── nx-forum-backend/          # 后端项目
│   ├── nx-biz-forum/          # 论坛业务模块
│   │   ├── nx-forum-adapter/  # 接口适配层
│   │   ├── nx-forum-app/      # 应用服务层
│   │   ├── nx-forum-domain/   # 领域层
│   │   ├── nx-forum-infrastructure/ # 基础设施层
│   │   └── nx-forum-start/    # 启动模块
│   └── nx-platform/           # 平台基础模块
│       ├── nx-common/         # 公共工具模块
│       ├── nx-module-cache/   # 缓存模块
│       ├── nx-module-config/  # 配置中心模块
│       ├── nx-module-mail/    # 邮件模块
│       ├── nx-module-ratelimit/ # 限流模块
│       ├── nx-module-sms/     # 短信模块
│       └── nx-module-uc/      # 用户中心模块
│           ├── nx-module-uc-adapter/
│           ├── nx-module-uc-app/
│           ├── nx-module-uc-domain/
│           └── nx-module-uc-infrastructure/
└── nx-forum-nuxt/             # 前端项目
    ├── apis/                  # API 接口层
    ├── components/            # 组件目录
    ├── composables/           # 组合式函数
    ├── layouts/               # 布局组件
    ├── pages/                # 页面文件
    ├── types/                # TypeScript 类型定义
    └── utils/                # 工具函数
```

## 技术栈

### 后端技术栈

- Java 17
- Spring Boot 3.5.3
- MyBatis-Plus 3.5.12
- Redis
- MySQL
- COLA 架构
- Knife4j (API 文档)
- MapStruct (对象映射)

### 前端技术栈

- Nuxt
- TypeScript
- Tailwind CSS
- Element-Plus
- Vant UI
- TipTap (富文本编辑器)

## 快速开始

### 环境要求

**后端：**

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+（可选）

**前端：**

- Node.js v20+

### 后端启动

1. 克隆项目

```bash
git clone <repository-url>
cd nx-forum/nx-forum-backend
```

2. 配置数据库
    - 创建 MySQL 数据库
    - 修改配置文件 `nx-forum-start/src/main/resources/application.yml`
    - 配置数据库连接信息

3. 配置 Redis（可选）
    - 修改配置文件中的 Redis 连接信息

4. 配置文件上传路径

```yaml
# 文件上传路径本地测试建议配前端项目的public目录地址，方便快速调试
# 生产环境建议使用 nginx 反向代理
nx.file.upload.path=/path/to/nx-forum-nuxt/public/
```

5. 启动项目

```bash
mvn clean install
cd nx-forum-start
mvn spring-boot:run
```

6. 访问 API 文档
    - 启动后访问：`http://localhost:8083/nx-forum/doc.html`

### 前端启动

1. 进入前端目录

```bash
cd nx-forum-nuxt
```

2. 安装依赖

```bash
yarn install
```

3. 开发模式

```bash
yarn dev
```

4. 生产构建

```bash
yarn run build
yarn run preview
```

## 许可证

MIT

