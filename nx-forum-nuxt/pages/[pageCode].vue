<template>
  <!-- 有布局模式：直接渲染 -->
  <div v-if="currentLayout !== 'empty'" class="page-content">
    <div v-html="pageContent"></div>
  </div>

  <!-- 无布局模式：使用 iframe 隔离 -->
  <div v-else class="page-content-iframe">
    <iframe
      title="custom"
      class="custom-page-iframe"
      :srcdoc="iframeSrcDoc"
      sandbox="allow-scripts allow-same-origin allow-downloads"
    />
  </div>
</template>
<script setup lang="ts">
import { getPage } from '~/apis/custom-page'

// 使用动态布局
definePageMeta({
  layout: 'custom-page'
})

const route = useRoute()
const pageCode = route.params.pageCode as string
const pageContent = ref('')
const pageName = ref('')
const currentLayout = ref('default')

// 无布局模式：构建带完整 HTML 结构的 iframe 内容
const iframeSrcDoc = computed(() => {
  if (currentLayout.value !== 'empty') return ''

  return pageContent.value
})

if (pageCode) {
  const res = await getPage(pageCode)
  // 获取自定义页面成功
  const data = res.data
  pageContent.value = data.content || ''
  pageName.value = data.pageName || ''
  currentLayout.value = data.layout || 'default'

  // 设置 SEO 标题
  if (pageName.value) {
    useSeoMeta({
      title: pageName.value
    })
  }
}
</script>

<style scoped>
.page-content-iframe {
  width: 100%;
  min-height: 100vh;
}

.custom-page-iframe {
  width: 100%;
  height: 100vh;
  border: none;
  display: block;
}
</style>
