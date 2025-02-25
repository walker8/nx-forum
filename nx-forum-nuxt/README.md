# NX-Forum-Nuxt 项目规范文档

## 项目架构

- 基于 Nuxt + TypeScript + tailwindcss + Element-Plus 和 vant-ui 的前端架构
- 采用模块化设计，清晰的目录结构和职责划分
- 统一的错误处理和状态管理机制

## 快速启动

### 环境要求

- Node.js v18+

### 安装依赖

```bash
yarn install
```

### 开发模式

```bash
yarn dev
```

### 生产构建

```bash
yarn run build
yarn run preview
```

## 目录结构规范

```
├── apis/               # API 接口层，按模块划分
│   ├── uc/             # 用户中心相关接口
│   ├── forum/          # 论坛相关接口
│   └── ...
├── components/         # 组件目录
│   ├── common/         # 通用组件
│   ├── uc/             # 用户中心组件
│   ├── admin/          # 管理后台组件
│   └── ...
├── composables/        # 组合式函数
├── layouts/            # 布局组件
├── pages/              # 页面文件
├── types/              # TypeScript 类型定义
└── utils/              # 工具函数
```

## 代码规范

### 组件开发规范

1. 统一使用 `<script setup lang="ts">` 语法
2. 组件文件采用 PascalCase 命名方式
3. 组件属性必须定义类型
4. 组件事件使用 emit 定义类型
5. 复杂的组件逻辑抽离到 composables 中

### API 层规范

1. 接口定义统一在 apis 目录下，按模块划分
2. 所有接口请求通过统一的 Http 类处理，不要设置返回类型
3. 接口返回类型统一定义在 types 目录
4. 错误处理统一在 request.ts 中处理
5. 接口地址和请求参数在 openapi.json 文件中定义，使用 OpenApi 规范

### 数据请求处理规范

1. 客户端请求使用 $fetch
2. SSR 场景使用 useFetch
3. DELETE 请求不携带请求体
4. 统一的错误处理机制：
   - 401：未授权处理
   - 500：服务器错误处理
   - 业务错误：统一提示

### TypeScript 使用规范

1. 必须定义接口类型，统一在 types/global.d.ts 中管理
2. 优先使用 interface 而非 type
3. 组件 props 必须声明类型
4. API 请求参数和响应必须定义类型

### 状态管理规范

1. 页面级状态使用 ref/reactive
2. 跨组件状态使用 composables
3. 全局状态使用 useState
4. 表单数据统一使用 reactive

### UI 组件使用规范

1. 基于 Element-Plus 和 vant-ui 组件库
2. 表格操作统一布局：
   - 搜索区域使用 el-form inline 模式
   - 工具栏区域包含批量操作和刷新按钮
   - 分页组件统一布局
3. 弹窗表单统一使用 el-dialog
4. 确认框统一使用 ElMessageBox
5. 消息提示统一使用 ElMessage
6. 使用 Tailwind CSS 实现响应式设计；使用移动优先的方法。

### 项目通用处理规范

1. 分页参数统一：

   ```typescript
   const paginationData = reactive({
     currentPage: 1,
     pageSize: 10,
     total: 0
   })
   ```

2. 搜索处理统一：

   - 重置时清空所有搜索条件
   - 搜索时重置到第一页

3. 表格数据加载统一：

   - loading 状态控制
   - 错误处理
   - 数据为空时显示空状态

4. 弹窗处理统一：
   - 打开时重置表单
   - 关闭时清除验证
   - 提交时统一验证

### 错误处理规范

1. API 错误统一处理：
   - 网络错误
   - 业务错误
   - 权限错误
2. 表单验证错误统一处理
3. 异步操作错误统一处理

### 性能优化规范

1. 合理使用 v-show 和 v-if
2. 大数据列表使用虚拟滚动
3. 合理使用 computed 和 watch
4. 避免不必要的组件重渲染

### 安全规范

1. 敏感数据不在前端存储
2. API 请求统一鉴权
3. 防止 XSS 攻击
4. 防止 CSRF 攻击
