# 主题插件机制

## 概述

主题插件机制允许开发者在主题保存和更新前执行自定义逻辑，例如内容过滤、SEO优化等。插件通过实现`ThreadPlugin`接口来定义自定义逻辑，并由
`ThreadPluginManager`统一管理和执行。

## 插件接口

`ThreadPlugin`接口定义了以下方法：

```java
public interface ThreadPlugin {
    // 在保存主题前执行
    void beforeSave(ThreadE threadE);

    // 在更新主题前执行
    void beforeUpdate(ThreadE threadE);

    // 获取插件优先级，数值越小优先级越高
    default int getOrder() {
        return 0;
    }
}
```

## 如何创建插件

1. 创建一个类，实现`ThreadPlugin`接口
2. 实现`beforeSave`和`beforeUpdate`方法
3. 使用`@Component`注解将插件注册到Spring容器中
4. 可选：重写`getOrder`方法设置插件优先级

示例：

```java

@Component
@Slf4j
public class MyCustomPlugin implements ThreadPlugin {

    @Override
    public void beforeSave(ThreadE threadE) {
        // 自定义逻辑
    }

    @Override
    public void beforeUpdate(ThreadE threadE) {
        // 自定义逻辑
    }

    @Override
    public int getOrder() {
        return 300; // 设置优先级
    }
}
```

## 插件执行顺序

插件按照`getOrder`方法返回的值从小到大排序执行。如果多个插件返回相同的优先级，则按照Spring容器加载顺序执行。