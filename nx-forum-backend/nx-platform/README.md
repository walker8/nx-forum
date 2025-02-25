# nx-platform 集成文档

## 1. 概述

nx-platform 是一个平台通用能力集合，提供多个可复用的模块，包括：

- nx-common：公共组件和工具类
- nx-module-cache：缓存管理模块
- nx-module-config：配置管理模块
- nx-module-mail：邮件服务模块
- nx-module-ratelimit：限流模块
- nx-module-sms：短信服务模块
- nx-module-uc：用户中心模块

## 2. 环境要求

- JDK 17+
- Maven 3.8+
- Spring Boot 3.x

## 3. 集成步骤

### 3.1 添加仓库配置

在项目的 `pom.xml` 中添加仓库配置：

```xml
<repositories>
    <repository>
        <id>nx-platform-repo</id>
        <name>nx-platform Repository</name>
        <url><!-- 替换为实际的仓库地址 --></url>
    </repository>
</repositories>
```

### 3.2 添加依赖

根据需要在项目的 `pom.xml` 中添加相应模块的依赖：

```xml
<!-- 公共组件 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-common</artifactId>
    <version>${nx.platform.version}</version>
</dependency>

<!-- 缓存模块 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-module-cache</artifactId>
    <version>${nx.platform.version}</version>
</dependency>

<!-- 配置模块 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-module-config</artifactId>
    <version>${nx.platform.version}</version>
</dependency>

<!-- 邮件模块 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-module-mail</artifactId>
    <version>${nx.platform.version}</version>
</dependency>

<!-- 限流模块 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-module-ratelimit</artifactId>
    <version>${nx.platform.version}</version>
</dependency>

<!-- 短信模块 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-module-sms</artifactId>
    <version>${nx.platform.version}</version>
</dependency>

<!-- 用户中心模块 -->
<dependency>
    <groupId>com.leyuz.boot</groupId>
    <artifactId>nx-module-uc-adapter</artifactId>
    <version>${nx.platform.version}</version>
</dependency>
```

## 4. 模块配置和使用

### 4.1 nx-common 模块

#### 功能介绍
提供基础工具类、异常处理、上下文管理等通用能力。

#### 使用示例

```java
// IP工具类使用
import com.leyuz.common.ip.IpUtils;

String ip = IpUtils.getIpAddr(request);

// 异常处理
import com.leyuz.common.exception.BusinessException;

throw new BusinessException("业务异常信息");

// DTO工具
import com.leyuz.common.dto.SingleResponse;
import com.leyuz.common.dto.MultiResponse;

return SingleResponse.of(data);
return MultiResponse.of(list, total);
```

### 4.2 nx-module-cache 模块

#### 配置
在 `application.yml` 中添加:

```yaml
spring:
  profiles:
    include: jetcache
```

#### 使用示例

```java
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;

@Service
public class UserService {
    
    @Cached(name="user:", key="#userId", expire=3600, cacheType=CacheType.BOTH)
    public UserDTO getUser(Long userId) {
        // 业务逻辑
    }
}
```

### 4.3 nx-module-ratelimit 模块

#### 使用示例

```java
import com.leyuz.ratelimit.annotation.RateLimit;

@RestController
public class ApiController {
    
    @RateLimit(key = "#user.id", limit = 5, period = 60)
    @GetMapping("/api/data")
    public SingleResponse<Data> getData(User user) {
        // 业务逻辑
    }
}
```

### 4.4 nx-module-mail 模块

#### 配置
在 `application.properties` 中添加:

```properties
spring.mail.host=smtp.example.com
spring.mail.port=465
spring.mail.username=your-email@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.ssl.enable=true
```

#### 使用示例

```java
import com.leyuz.module.mail.adapter.MailService;

@RequiredArgsConstructor
@Service
public class NotificationService {
    
    private final MailService mailService;
    
    public void sendNotification(String to, String subject, String content) {
        mailService.sendSimpleMail(to, subject, content);
    }
}
```

### 4.5 nx-module-sms 模块

#### 配置
在 `application.properties` 中添加:

```properties
sms.provider=aliyun
sms.aliyun.accessKeyId=your-access-key
sms.aliyun.accessKeySecret=your-secret
sms.aliyun.signName=your-sign-name
```

#### 使用示例

```java
import com.leyuz.module.sms.adapter.SmsService;

@RequiredArgsConstructor
@Service
public class VerificationService {
    
    private final SmsService smsService;
    
    public void sendVerificationCode(String phone, String code) {
        Map<String, String> params = Map.of("code", code);
        smsService.send(phone, "SMS_TEMPLATE_ID", params);
    }
}
```

## 5. 最佳实践

1. **按需引入**: 只引入项目需要的模块，避免不必要的依赖
2. **版本管理**: 使用属性统一管理nx-platform版本号
3. **配置隔离**: 使用Spring Profile管理不同环境的配置
4. **遵循分层架构**: 按照DDD分层架构设计应用

## 6. 常见问题

1. **自动配置未生效?**  
   检查是否存在Spring自动配置文件，位置应为`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

2. **缓存未生效?**  
   确认是否引入了`spring-profiles-include: jetcache`配置，以及是否正确添加了`@EnableCreateCacheAnnotation`注解

3. **限流注解不生效?**  
   检查是否开启了AOP支持`@EnableAspectJAutoProxy`
