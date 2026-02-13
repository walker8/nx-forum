<template>
  <div class="custom-page-wrapper">
    <!-- 默认布局：带头部信息，内容居中，内置部分 css 样式-->
    <template v-if="currentLayout === 'default'">
      <el-container class="default-container">
        <div class="default-content">
          <el-header style="padding: 0" class="rn-hide">
            <common-header />
          </el-header>
          <div class="default-layout">
            <el-container>
              <el-main class="default-main">
                <slot />
              </el-main>
            </el-container>
          </div>
        </div>
      </el-container>
    </template>

    <!-- 简洁布局：无头信息，内容居中，内置部分 css 样式 -->
    <template v-else-if="currentLayout === 'simple'">
      <div class="simple-layout">
        <el-container class="default-container">
          <div class="default-content">
            <div class="default-layout">
              <el-container>
                <el-main class="default-main">
                  <slot />
                </el-main>
              </el-container>
            </div>
          </div>
        </el-container>
      </div>
    </template>

    <!-- 无布局：所有 css 样式由用户自定义-->
    <template v-else>
      <slot />
    </template>
  </div>
</template>

<script setup lang="ts">
import { getPage } from '~/apis/custom-page'

const route = useRoute()
const pageCode = route.params.pageCode as string
const currentLayout = ref('default')

// 直接在布局中获取页面数据（顶层 await），避免 hydration mismatch
if (pageCode) {
  try {
    const res = await getPage(pageCode)
    const data = res.data
    currentLayout.value = data.layout || 'default'
  } catch (error) {
    console.error('获取页面布局失败', error)
  }
}
</script>

<style lang="scss" scoped>
.custom-page-wrapper {
  min-height: 100vh;
}

// 默认布局样式
.default-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.default-content {
  flex-grow: 1;
}

.default-layout {
  height: 100%;
  width: fit-content;
  margin: 10px auto 0;
}

.default-main {
  padding-top: 0;
  max-width: 1200px;
}

@media screen and (max-width: 768px) {
  .el-main {
    padding: 0;
  }
  .default-main {
    width: calc(100vw - 10px);
  }
}

@media screen and (max-width: 960px) {
  .el-main {
    padding: 0;
  }
  .default-main {
    width: calc(100vw - 40px);
  }
}

// 简洁布局样式
.simple-layout {
  min-height: 100vh;
  background-color: #f5f5f5;
}

// 富文本样式 - 用于 v-html 渲染的内容
.default-main {
  max-width: 100%;
  font-size: 16px;
  line-height: 1.75;
  color: #333;
  word-wrap: break-word;
  word-break: break-word;
  white-space: normal;
  overflow-wrap: break-word;

  :deep(code:not(pre code)) {
    background-color: #dfe0e1;
    padding: 2px 4px;
    margin: 0 3px;
    border-radius: 4px;
    font-size: 14px;
    color: #333;
  }

  :deep(pre:not(:has(code))) {
    white-space: pre-wrap;
    word-wrap: break-word;
    overflow-wrap: break-word;
    max-width: 100%;
    background-color: #f6f8fa;
    padding: 12px;
    border-radius: 4px;
    margin: 16px 0;
  }

  :deep(h1) {
    font-size: 28px;
    font-weight: 600;
    line-height: 1.4;
    margin: 0 0 13px;
    color: #1d2129;
    padding-top: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e4e6eb;
  }

  :deep(h2) {
    font-size: 24px;
    font-weight: 600;
    line-height: 1.4;
    margin: 13px 0;
    color: #1d2129;
    padding-bottom: 12px;
    border-bottom: 1px solid #e4e6eb;
  }

  :deep(h3) {
    font-size: 20px;
    font-weight: 600;
    line-height: 1.4;
    margin: 10px 0;
    color: #1d2129;
  }

  :deep(h4) {
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    margin: 8px 0;
    color: #1d2129;
  }

  :deep(h5) {
    font-size: 16px;
    font-weight: 600;
    line-height: 1.4;
    margin: 8px 0;
    color: #1d2129;
  }

  :deep(h6) {
    font-size: 14px;
    font-weight: 600;
    line-height: 1.4;
    margin: 6px 0;
    color: #1d2129;
  }

  :deep(ul),
  :deep(ol) {
    padding-left: 20px;
    margin: 12px 0;
  }

  :deep(ul) {
    list-style-type: disc;
  }

  :deep(ol) {
    list-style-type: decimal;
  }

  :deep(li) {
    margin-bottom: 8px;
    line-height: 1.6;
    position: relative;
  }

  :deep(ul ul),
  :deep(ol ul) {
    list-style-type: circle;
    margin: 8px 0 8px 16px;
  }

  :deep(ul ol),
  :deep(ol ol) {
    list-style-type: lower-alpha;
    margin: 8px 0 8px 16px;
  }

  :deep(ul ul ul),
  :deep(ol ul ul),
  :deep(ul ol ul),
  :deep(ol ol ul) {
    list-style-type: square;
  }

  :deep(ul ol ol),
  :deep(ol ol ol),
  :deep(ul ul ol),
  :deep(ol ul ol) {
    list-style-type: lower-roman;
  }

  :deep(blockquote) {
    margin: 16px 0;
    padding: 12px 16px;
    background-color: #f8f9fa;
    border-left: 4px solid #1e80ff;
    color: #5a6270;
    font-size: 15px;
    line-height: 1.7;
    border-radius: 0 4px 4px 0;
    position: relative;
    overflow: hidden;

    p {
      margin: 0;
      position: relative;
    }

    p:not(:last-child) {
      margin-bottom: 8px;
    }

    blockquote {
      margin-left: 16px;
      border-left-color: #409eff;
    }
  }

  :deep(ul[data-type='taskList']) {
    list-style: none;
    padding-left: 0;

    li {
      display: flex;
      align-items: center;
      gap: 0.25em;
      margin: 0.25em 0;

      >label {
        display: flex;
        align-items: center;
        gap: 0.25em;
        cursor: pointer;
        user-select: none;
      }

      >label>div {
        flex: 1;
        text-align: left;
      }

      input[type='checkbox'] {
        width: 16px;
        height: 16px;
        cursor: pointer;
        flex-shrink: 0;
      }

      &[data-checked='true']>label>div {
        text-decoration: line-through;
        color: #94a3b8;
      }
    }
  }

  :deep(.alert-block) {
    display: flex;
    align-items: flex-start;
    padding: 8px 16px;
    margin: 1rem 0;
    border-radius: 4px;
    transition: all 0.2s ease;
    position: relative;
    font-size: 14px;
    line-height: 1.5;

    &::before {
      content: '';
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      margin-right: 8px;
      margin-top: 3px;
      background-repeat: no-repeat;
      background-position: center;
      background-size: contain;
    }

    p {
      margin: 0.5em 0;

      &:first-child {
        margin-top: 0;
      }

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  :deep(.alert-block[data-type="info"]) {
    background-color: #f4f4f5;
    color: #909399;

    &::before {
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23909399'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z'/%3E%3C/svg%3E");
    }
  }

  :deep(.alert-block[data-type="success"]) {
    background-color: #f0f9eb;
    color: #67c23a;

    &::before {
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%2367c23a'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z'/%3E%3C/svg%3E");
    }
  }

  :deep(.alert-block[data-type="warning"]) {
    background-color: #fdf6ec;
    color: #e6a23c;

    &::before {
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23e6a23c'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z'/%3E%3C/svg%3E");
    }
  }

  :deep(.alert-block[data-type="error"]) {
    background-color: #fef0f0;
    color: #f56c6c;

    &::before {
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23f56c6c'%3E%3Cpath d='M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z'/%3E%3C/svg%3E");
    }
  }

  :deep(table) {
    width: 100%;
    margin: 16px 0;
    border-collapse: collapse;
    border-spacing: 0;
    display: block;
    overflow-x: auto;

    th,
    td {
      border: 1px solid #e4e6eb;
      padding: 8px 12px;
      text-align: left;
      min-width: 120px;
    }

    th {
      background-color: #f6f8fa;
      font-weight: 600;
      color: #1d2129;
    }

    tr:nth-child(even) {
      background-color: #f9f9fa;
    }

    tr:hover {
      background-color: #f2f3f5;
    }

    @media (max-width: 768px) {
      font-size: 14px;

      th,
      td {
        padding: 6px 8px;
        min-width: 100px;
      }
    }
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    cursor: pointer;
    border-radius: 4px;
  }

  :deep(div[data-type="mermaid"]) {
    margin: 16px 0;
    border: 1px solid #e4e6eb;
    border-radius: 8px;
    overflow: hidden;
    background: #fff;

    .mermaid-render-container {
      padding: 16px;
      text-align: center;
      overflow-x: auto;

      svg {
        max-width: 100%;
        height: auto;
      }
    }

    .mermaid-error {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
      padding: 12px;
      background: #fef0f0;
      border: 1px solid #f56c6c;
      border-radius: 4px;
      color: #f56c6c;
      font-size: 14px;

      svg {
        width: 1rem;
        height: 1rem;
        flex-shrink: 0;
      }
    }

    @media (max-width: 768px) {
      margin: 12px 0;
      border-radius: 6px;

      .mermaid-render-container {
        padding: 12px;
      }
    }
  }
}
</style>
