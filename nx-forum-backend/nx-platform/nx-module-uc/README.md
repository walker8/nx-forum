# nx-module-uc 用户中心模块

用户中心（User Center）模块是论坛平台的核心业务模块，负责用户管理、身份认证、权限管理等功能。采用 **COLA（分层架构）+ DDD（领域驱动设计）+ MVC** 混合模式开发。

## 📦 模块构成

```
nx-module-uc
├── nx-module-uc-adapter          # 适配层（HTTP 接口）
├── nx-module-uc-app             # 应用层（业务编排）
├── nx-module-uc-domain          # 领域层（核心业务逻辑）
├── nx-module-uc-infrastructure  # 基础设施层（数据持久化）
└── pom.xml                       # 模块配置文件
```

## 🏗️ 业务领域划分

### 1. 用户域 (user) - DDD 模式

**职责**：用户管理、身份认证、验证码服务

**子域划分**：
- `user/` - 用户基本信息管理
- `user/auth/` - 用户认证（登录、登出、Token）
- `user/verify/` - 验证码服务（邮箱/短信验证）

#### 关键 Application 类

| 类名 | 职责 | 所在子域 | 模式 |
|------|------|----------|------|
| `UserApplication` | 用户 CRUD、信息查询、用户缓存 | user/ | DDD |
| `AuthApplication` | 登录、注册、登出、认证 | user/auth/ | DDD |
| `TokenApplication` | Token 生成、验证、缓存 | user/auth/ | DDD |
| `VerifyCodeApplication` | 验证码发送、校验、密码重置、邮箱换绑 | user/verify/ | DDD |

#### 主要功能

- **用户管理**：创建、更新、删除、查询用户
- **身份认证**：密码登录、验证码登录、安全登出
- **用户注册**：密码注册、验证码注册、用户名/密码复杂度校验
- **验证码**：邮箱/短信验证码发送、校验、失效管理
- **密码重置**：通过验证码重置密码
- **邮箱换绑**：安全的邮箱绑定更换流程
- **登录失败锁定**：防暴力破解机制

#### 常用 API

```bash
# 登录
POST /v1/uc/auth/login
{
  "userName": "user123",
  "password": "password"
}

# 登出
POST /v1/uc/auth/logout

# 用户注册
POST /v1/uc/users
{
  "userName": "newuser",
  "email": "user@example.com",
  "password": "Password123",
  "verifyCode": "123456"
}

# 获取当前用户
GET /v1/uc/users/current

# 获取用户信息
GET /v1/uc/users/{userId}/info
```

### 2. 权限域 (auth) - DDD 模式

**职责**：权限管理、角色管理、授权检查

**子域划分**：
- `auth/` - 授权和用户角色管理
- `auth/role/` - 角色管理
- `auth/permission/` - 权限管理
- `auth/token/` - Token 持久化

#### 关键 Application 类

| 类名 | 职责 | 所在子域 | 模式 |
|------|------|----------|------|
| `RoleApplication` | 角色 CRUD、角色查询 | auth/role/ | DDD |
| `PermissionApplication` | 权限 CRUD、权限树查询 | auth/permission/ | DDD |
| `UserRoleApplication` | 用户角色关联、优先级计算 | auth/ | DDD |
| `RolePermissionApplication` | 角色权限关联 | auth/ | MVC |
| `AuthorizationApplication` | 权限检查、角色检查（内部服务） | auth/ | DDD |
| `PermissionResolver` | 权限解析门面（对外服务） | auth/ | 门面 |

#### 主要功能

- **角色管理**：角色 CRUD、角色查询、角色列表
- **权限管理**：权限 CRUD、权限树、权限查询
- **用户角色**：分配/取消角色、角色优先级计算、角色过期处理
- **权限检查**：单权限检查、多权限检查、角色检查
- **权限查询**：获取用户权限集合、获取最高优先级角色

#### 权限检查示例（通过 PermissionResolver）

```java
// 注入门面服务
@Autowired
private PermissionResolver permissionResolver;

// 检查权限
if (permissionResolver.hasPermission("thread:create")) {
    // 用户有权限
}

// 检查角色
if (permissionResolver.hasRole("THREAD_REVIEWER")) {
    // 用户有该角色
}

// 在 Controller 中使用
@PreAuthorize("@permissionResolver.hasPermission('thread:create')")
@PostMapping("/threads")
public SingleResponse<Void> createThread(@RequestBody ThreadCmd cmd) {
    return SingleResponse.buildSuccess();
}
```

### 3. 配置域 (config) - MVC 模式

**职责**：系统配置管理

#### 关键 Application 类

| 类名 | 职责 |
|------|------|
| `LoginConfigApplication` | 登录配置（开关、失败锁定等） |
| `RegisterConfigApplication` | 注册配置（开关、用户名规则、密码复杂度等） |

#### 可配置项

**登录配置**：
- 密码登录开关
- 验证码登录开关（邮箱/短信）
- 密码重置开关（邮箱/短信）
- 登录失败次数限制
- 登录失败锁定时间

**注册配置**：
- 注册开关
- 邮箱注册开关
- 短信注册开关
- 用户名规则
- 密码复杂度要求

### 4. 日志域 (log) - MVC 模式

**职责**：用户操作日志记录和查询

#### 关键 Application 类

| 类名 | 职责 | 模式 |
|------|------|------|
| `UserLogApplication` | 日志记录、日志查询 | MVC |

#### 日志类型

- `LOGIN` (1) - 登录日志
- `LOGOUT` (2) - 登出日志
- `REGISTER` (3) - 注册日志
- `INFO_UPDATE` (4) - 信息修改日志
- `PASSWORD_UPDATE` (5) - 密码修改日志

## 🚀 快速开始

### 使用权限检查

```java
// 在 Controller 中使用 @PreAuthorize 注解
@RestController
public class YourController {
    
    @PreAuthorize("@permissionResolver.hasPermission('thread:create')")
    @PostMapping("/threads")
    public SingleResponse<Void> createThread(@RequestBody ThreadCmd cmd) {
        // 仅有 thread:create 权限的用户才能访问
        return SingleResponse.buildSuccess();
    }
    
    @PreAuthorize("@permissionResolver.hasRole('THREAD_REVIEWER')")
    @PostMapping("/threads/{id}/audit")
    public SingleResponse<Void> auditThread(@PathVariable Long id) {
        // 仅有 THREAD_REVIEWER 角色的用户才能访问
        return SingleResponse.buildSuccess();
    }
}
```

### 记录用户日志

```java
@Autowired
private UserLogApplication userLogApplication;

// 记录操作日志
userLogApplication.recordLog(
    LogTypeV.INFO_UPDATE,
    "用户修改了个人信息",
    OperationStatusV.SUCCESS
);
```

## 📐 设计模式

### 1. COLA 分层架构

- **Adapter 层**：处理 HTTP 请求，参数校验，响应封装
- **Application 层**：业务编排，流程控制，事务管理
- **Domain 层**：核心业务逻辑，领域模型（仅 DDD 模式）
- **Infrastructure 层**：技术实现，数据持久化

### 2. DDD 与 MVC 混合

- **用户域、权限域**：使用 DDD 模式（复杂业务逻辑）
- **日志域、配置域**：使用 MVC 模式（简单 CRUD）

### 3. 领域网关模式

通过 Gateway 接口隔离领域模型与持久化实现，实现依赖倒置原则。

### 4. 门面模式

`PermissionResolver` 作为权限系统的对外门面，其他模块通过此门面进行权限检查。

## 🔒 安全特性

1. **登录失败锁定**：连续登录失败超过指定次数后，账户被锁定 30 分钟
2. **密码加密**：密码使用 BCrypt 加密存储
3. **Token 管理**：JWT Token 有效期为 1 年，支持 Token 失效管理
4. **权限校验**：所有管理接口都需要相应权限
5. **验证码**：支持邮箱和短信验证码，防暴力注册和密码重置
6. **邮箱换绑**：两步验证，先验证当前邮箱，再验证新邮箱

## 📋 项目结构

### Adapter 层（HTTP 接口）

```
nx-module-uc-adapter/src/main/java/com/leyuz/uc/
├── UcAuthController              # 认证接口（登录、登出）
├── UcUserController              # 用户接口（前台）
├── UcVerifyCodeController        # 验证码接口
├── admin/                        # 管理后台接口
│   ├── AdminUserController       # 用户管理
│   ├── AdminRoleController       # 角色管理
│   ├── AdminPermissionController # 权限管理
│   └── ...
└── auth/                         # 认证相关
    ├── SecurityConfig.java       # Spring Security 配置
    └── filter/                   # 过滤器
```

### Application 层（业务编排）

```
nx-module-uc-app/src/main/java/com/leyuz/uc/
├── user/                         # 用户域
│   ├── UserApplication.java      # 用户管理
│   ├── auth/                     # 认证子域
│   │   ├── AuthApplication.java  # 认证服务
│   │   ├── TokenApplication.java # Token 服务
│   │   ├── LoginFailureService.java
│   │   └── dto/                  # 认证相关 DTO
│   ├── verify/                   # 验证码子域
│   │   ├── VerifyCodeApplication.java
│   │   ├── VerifyCodeService.java
│   │   └── dto/                  # 验证码相关 DTO
│   └── dto/                      # 用户基础 DTO
├── auth/                         # 权限域
│   ├── AuthorizationApplication.java
│   ├── UserRoleApplication.java
│   ├── RolePermissionApplication.java
│   ├── PermissionResolver.java   # 权限门面
│   ├── role/                     # 角色子域
│   │   ├── RoleApplication.java
│   │   └── dto/                  # 角色相关 DTO
│   └── permission/               # 权限子域
│       ├── PermissionApplication.java
│       └── dto/                  # 权限相关 DTO
├── config/                       # 配置域 (MVC)
│   ├── LoginConfigApplication.java
│   ├── RegisterConfigApplication.java
│   └── dto/                      # 配置 DTO/VO
├── log/                          # 日志域 (MVC)
│   ├── UserLogApplication.java
│   ├── dto/                      # 日志 DTO/VO
│   └── listener/                 # 事件监听器
└── cache/                        # 缓存配置
    └── CacheConfig.java          # 用户/角色缓存配置
```

### Domain 层（领域模型）

```
nx-module-uc-domain/src/main/java/com/leyuz/uc/
├── user/                         # 用户领域
│   ├── UserE.java               # 用户实体
│   ├── constant/                # 领域常量
│   │   └── RegexConstant.java   # 验证正则
│   ├── gateway/                 # 领域网关
│   │   └── UserGateway.java
│   ├── service/                 # 领域服务
│   │   └── UserDomainService.java
│   ├── dataobject/              # 值对象
│   │   ├── AccountTypeV.java
│   │   └── UserStatusV.java
│   └── event/                   # 领域事件
│       ├── UserLoginEvent.java
│       └── UserRegisteredEvent.java
└── auth/                        # 权限领域
    ├── role/                    # 角色子域
    │   ├── RoleE.java
    │   ├── gateway/RoleGateway.java
    │   └── dataobject/RoleStatusV.java
    ├── permission/              # 权限子域
    │   ├── PermissionE.java
    │   ├── gateway/PermissionGateway.java
    │   └── dataobject/PermissionTypeV.java, PermissionStatusV.java
    └── token/                   # Token 子域
        └── gateway/TokenGateway.java
```

## 📝 规范说明

### 命名规范

- **Controller**：`*Controller` 或 `Admin*Controller`
- **Application**：`*Application`
- **Entity**：`*E`（领域实体）
- **ValueObject**：`*V`（值对象）
- **DTO**：`*DTO`、`*Cmd`、`*Query`、`*VO`
- **PO**：`*PO`（持久化对象）

### 数据对象规范

| 对象 | 说明 | 使用场景 |
|------|------|---------|
| Cmd | 命令对象 | Adapter 层接收写操作参数 |
| Query | 查询对象 | Adapter 层接收查询参数 |
| VO | 视图对象 | Adapter 层返回给前端 |
| DTO | 数据传输对象 | 层间数据传输 |
| E | 领域实体 | Domain 层业务模型 |
| V | 值对象 | Domain 层不可变对象 |
| PO | 持久化对象 | Infrastructure 层数据库映射 |

## 👥 开发建议

1. **新增功能**：
   - 简单 CRUD 使用 MVC 模式
   - 复杂业务逻辑使用 DDD 模式
   - 通过 Application 层进行业务编排

2. **跨模块调用**：
   - 只通过 Application 层调用
   - 使用 PermissionResolver 门面检查权限
   - 禁止直接调用 Domain 或 Infrastructure 层

3. **异常处理**：
   - 使用 `ValidationException` 处理业务校验错误
   - 使用 `BusinessException` 处理业务逻辑错误

4. **缓存策略**：
   - 用户信息使用本地缓存（GenericCache）
   - 角色权限使用分布式缓存

---

**最后更新**：2025-11-08 (结构重构)  
**维护者**：Development Team  
**重构版本**：v2.0 - 优化目录结构和业务域划分
