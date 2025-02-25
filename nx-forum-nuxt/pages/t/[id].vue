<template>
  <el-card>
    <div v-if="thread.subject">
      <el-tag type="success" v-if="thread.digest" class="mr-1.5 mb-2.5"> 精华 </el-tag>
      <h1 class="article-title">{{ thread.subject }}</h1>
    </div>
    <div class="article-info">
      <el-space wrap>
        <div class="author" @click="open(`/user/${thread.author?.authorId}`)">
          <el-link underline="never">{{ thread.author?.authorName ?? 'unkown' }}</el-link>
        </div>
        <div>发布于 {{ thread.createTime }}</div>
        <div>阅读 {{ thread.views }}</div>
        <ClientOnly>
          <el-link underline="never" :icon="Edit" @click="editThread" v-if="canEdit" />
        </ClientOnly>
        <ClientOnly>
          <el-button v-if="user.userId" link type="danger" @click="onReport">
            举报
          </el-button>
        </ClientOnly>
      </el-space>
    </div>
    <div v-if="thread.updateAuthor" class="edit-info">
      本帖最后由 {{ thread.updateAuthor.authorName }} 于 {{ thread.updateTime }} 编辑
    </div>
    <el-alert v-if="thread.auditStatus === 1" title="当前帖子正在审核中，仅管理可见" type="warning" :closable="false" />
    <el-alert v-if="thread.auditStatus === 2" title="当前帖子审核不通过，仅管理可见" type="error" :closable="false" />
    <div class="article-content" v-html="processEmotions" @click="clickArticle($event)"></div>

    <!-- 关闭提示 -->
    <div v-if="thread.closed" class="closed-alert">
      <el-icon class="warning-icon">
        <Warning />
      </el-icon>
      <span>本帖已关闭，不再接受新回复</span>
    </div>
  </el-card>
  <thread-comment :threadId="thread.threadId" :forumId="thread.forumId" :authorId="thread.author?.authorId || 0"
    :order="thread.commentOrder" :disabled="thread.closed" />
  <common-image-viewer ref="imageViewer" />
  <el-backtop :bottom="50" />

  <!-- 移动端目录按钮和抽屉 - 仅客户端渲染 -->
  <ClientOnly>
    <div class="mobile-catalog-btn" v-if="hasCatalog" @click="showCatalogDrawer = true">
      <el-icon :size="18">
        <List />
      </el-icon>
    </div>

    <!-- 移动端目录抽屉 -->
    <el-drawer
      v-model="showCatalogDrawer"
      title="目录"
      direction="rtl"
      size="280px"
      class="mobile-catalog-drawer"
    >
      <div class="mobile-catalog-content">
        <div
          v-for="(item, index) in catalogItems"
          :key="index"
          :class="[
            'catalog-item cursor-pointer py-2 transition-colors duration-200 truncate',
            item.level === 2 ? 'pl-4' : '',
            item.level === 3 ? 'pl-8 text-sm' : '',
            activeCatalogId === item.id ? 'text-[#409eff] font-medium' : 'text-gray-600'
          ]"
          @click="scrollToHeading(item.id)"
        >
          {{ item.text }}
        </div>
        <div v-if="catalogItems.length === 0" class="text-gray-400 text-center py-8">
          暂无目录
        </div>
      </div>
    </el-drawer>
  </ClientOnly>
</template>

<script setup lang="ts">
import { Edit, Warning, List } from '@element-plus/icons-vue'
import { useReport } from '~/composables/useReport'

definePageMeta({
  layout: 'thread'
})

const imageViewer = ref()
const thread = useThread()
const { user } = useCurrentUser()
const { replaceEmotions } = useEmotions()
const { openReportDialog } = useReport()

const { hasPermission, authPromise } = useUserAuth(thread.value.forumId)
await authPromise

const canEdit = computed(() => {
  if (thread.value.forumId == 6) {
    // 归档版块禁止所有人编辑
    return false;
  }
  return hasPermission('admin:thread:edit', thread.value.forumId) ||
    (hasPermission('thread:edit') && user.value.userId === thread.value.author?.authorId)
})

useSeoMeta({
  title: thread.value.seoTitle,
  ogTitle: thread.value.seoTitle,
  description: thread.value.seoContent,
  ogDescription: thread.value.seoContent
})

const hasPreTag = computed(() => {
  return /<pre\b[^>]*>[\s\S]*<\/pre>/i.test(thread.value.content)
})
if (hasPreTag.value) {
  useHead({
    link: [
      {
        rel: 'stylesheet',
        href: '/prismjs/themes/prism-tomorrow.css'
      },
      {
        rel: 'stylesheet',
        href: '/prismjs/plugins/toolbar/prism-toolbar.min.css'
      }
    ],
    script: [
      {
        src: '/prismjs/components/prism-core.min.js',
        tagPosition: 'bodyClose'
      },
      {
        src: '/prismjs/plugins/autoloader/prism-autoloader.min.js',
        tagPosition: 'bodyClose'
      },
      {
        src: '/prismjs/plugins/toolbar/prism-toolbar.min.js',
        tagPosition: 'bodyClose'
      },
      {
        src: '/prismjs/plugins/copy-to-clipboard/prism-copy-to-clipboard.min.js',
        tagPosition: 'bodyClose'
      }
    ]
  })
}

const editThread = () => {
  navigateTo({
    path: '/editor/t/' + thread.value.threadId
  })
}

const open = (path: string) => {
  window.open(path, '_blank')
}

function onReport() {
  if (!thread.value)
    return
  openReportDialog(thread.value.threadId, 'THREAD', thread.value.forumId)
}

const clickArticle = (element: any) => {
  imageViewer.value.showImage(element, thread.value.content)
}

// 修改表情处理函数
const processEmotions = computed(() => {
  return replaceEmotions(thread.value.content)
})

// 移动端目录相关
const showCatalogDrawer = ref(false)
const catalogItems = ref<Array<{ id: string; text: string; level: number }>>([])
const activeCatalogId = ref('')

// 解析内容中的标题
const parseHeadings = (content: string) => {
  if (!content || !import.meta.client) return

  const parser = new DOMParser()
  const doc = parser.parseFromString(content, 'text/html')
  const headings = doc.querySelectorAll('h2, h3, h4')
  const newCatalog: Array<{ id: string; text: string; level: number }> = []

  headings.forEach((heading, index) => {
    const level = parseInt(heading.tagName.substring(1)) - 1
    if (level <= 3) {
      const id = `heading-${index}`
      if (!heading.id) {
        heading.id = id
      }
      newCatalog.push({
        id: heading.id,
        text: heading.textContent || '',
        level
      })
    }
  })

  catalogItems.value = newCatalog

  // 在 DOM 渲染后设置标题的 id
  nextTick(() => {
    if (import.meta.client) {
      const articleContent = document.querySelector('.article-content')
      if (articleContent) {
        const domHeadings = articleContent.querySelectorAll('h2, h3, h4')
        domHeadings.forEach((heading, index) => {
          if (!heading.id && newCatalog[index]) {
            heading.id = newCatalog[index].id
          }
        })
      }
    }
  })
}

// 检查是否有目录
const hasCatalog = computed(() => {
  return catalogItems.value.length > 0
})

// 滚动到指定标题
const scrollToHeading = (id: string) => {
  if (import.meta.client) {
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' })
      // 滚动后关闭抽屉
      setTimeout(() => {
        showCatalogDrawer.value = false
      }, 300)
    }
  }
}

// 监听文章内容变化，解析标题
watch(() => thread.value.content, (content) => {
  if (import.meta.client && content) {
    parseHeadings(content)
  }
}, { immediate: true })

// 监听滚动更新激活状态
const updateActiveHeading = () => {
  if (!import.meta.client || catalogItems.value.length === 0) return

  const headings = catalogItems.value
    .map((item) => ({
      id: item.id,
      element: document.getElementById(item.id)
    }))
    .filter((item) => item.element)

  let activeHeading = headings[0]
  let minDistance = Infinity

  headings.forEach((heading) => {
    if (!heading.element) return
    const distance = Math.abs(heading.element.getBoundingClientRect().top)
    if (distance < minDistance) {
      minDistance = distance
      activeHeading = heading
    }
  })

  if (activeHeading) {
    activeCatalogId.value = activeHeading.id
  }
}

// 添加滚动监听
onMounted(() => {
  if (import.meta.client) {
    window.addEventListener('scroll', updateActiveHeading, { passive: true })
    updateActiveHeading()
  }
})

// 移除滚动监听
onUnmounted(() => {
  if (import.meta.client) {
    window.removeEventListener('scroll', updateActiveHeading)
  }
})

// 监听目录数据变化，更新激活状态
watch(catalogItems, () => {
  if (import.meta.client) {
    nextTick(() => {
      updateActiveHeading()
    })
  }
})
</script>

<style lang="scss" scoped>
.article-title {
  margin: 0 0 13px;
  font-size: 26px;
  font-weight: 600;
  line-height: 1.4;
  color: #1d2129;
  overflow-wrap: break-word;
  display: inline;
}

.article-info {
  font-size: 14px;
  color: #8a919f;
  line-height: 24px;
  height: 24px;

  .author {
    color: #515767;

    :deep(.el-link) {
      font-weight: 500;

      &:hover {
        color: #1e80ff;
      }
    }
  }

  .el-icon {
    font-size: 16px;
  }
}

.article-content {
  max-width: 100%;
  margin-top: 10px;
  font-size: 16px;
  line-height: 1.75;
  color: #333;
  /* 允许在单词内换行 */
  word-wrap: break-word;
  /* 单词可以在任意字符间断开 */
  word-break: break-word;
  /* 超出显示省略号 */
  white-space: normal;
  /* 保持所有文字在一个方块内 */
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

  /* 优化ol ul li的样式 */
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

  /* 优化 blockquote 样式 */
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

  /* 任务列表样式 */
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

  /* 提示块样式 - Element Plus 风格 */
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
      /* Align with first line of text */
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

  /* 优化表格样式 */
  :deep(table) {
    width: 100%;
    margin: 16px 0;
    border-collapse: collapse;
    border-spacing: 0;
    display: block;
    overflow-x: auto;
    /* 在小屏幕上允许水平滚动 */

    th,
    td {
      border: 1px solid #e4e6eb;
      padding: 8px 12px;
      text-align: left;
      min-width: 120px;
      /* 设置最小宽度防止内容挤压 */
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

    /* 移动端优化 */
    @media (max-width: 768px) {
      font-size: 14px;

      th,
      td {
        padding: 6px 8px;
        min-width: 100px;
      }
    }
  }
}

:deep(img:not(.emotion-img)) {
  max-width: 100%;
  height: auto;
  cursor: pointer;
  border-radius: 4px;
}

.el-alert {
  margin-top: 10px;

  &--warning {
    background-color: #fdf6ec;
    border: 1px solid #e6a23c;
  }

  &--error {
    background-color: #fef0f0;
    border: 1px solid #f56c6c;
  }
}

.edit-info {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin: 8px 0;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
}

.closed-alert {
  font-size: 12px;
  color: #f56c6c;
  justify-content: center;
  margin-top: 10px;
  padding-top: 6px;
  display: flex;
  align-items: center;
  gap: 3px;

  .warning-icon {
    font-size: 16px;
  }
}

/* 移动端目录按钮 */
.mobile-catalog-btn {
  position: fixed;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  z-index: 40;
  display: none;
  align-items: center;
  justify-content: center;
  padding: 6px 2px 6px 6px;
  background-color: #409eff;
  color: white;
  border-radius: 8px 0 0 8px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background-color: #66b1ff;
    box-shadow: 0 4px 16px rgba(64, 158, 255, 0.5);
  }

  &:active {
    transform: translateY(-50%) scale(0.95);
  }

  @media screen and (max-width: 1199px) {
    display: flex;
  }
}

/* 移动端目录内容 */
.mobile-catalog-content {
  padding: 0px;
  max-height: calc(100vh - 60px);
  overflow-y: auto;

  .catalog-item {
    border-bottom: 1px solid #f0f0f0;
    transition: all 0.2s ease;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      color: #409eff;
      background-color: #f5f7fa;
    }

    &:active {
      background-color: #e6f4ff;
    }
  }
}

/* 移动端抽屉样式优化 */
.mobile-catalog-drawer {
  @media screen and (min-width: 1200px) {
    display: none !important;
  }
}

:deep(.mobile-catalog-drawer) {
  .el-drawer__header {
    margin-bottom: 0;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  .el-drawer__body {
    padding: 0;
  }
}
</style>

<style lang="scss">
/* 移动端目录抽屉全局样式 */
.mobile-catalog-drawer {
  .el-drawer__header {
    margin-bottom: 0 !important;
    padding: 20px !important;
    border-bottom: 1px solid #f0f0f0 !important;
  }
}
</style>
