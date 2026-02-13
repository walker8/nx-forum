<template>
  <div class="page-content">
    <div v-html="pageContent"></div>
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

if (pageCode) {
  const res = await getPage(pageCode)
  // 获取自定义页面成功
  const data = res.data
  pageContent.value = data.content || ''
  pageName.value = data.pageName || ''

  // 设置 SEO 标题
  if (pageName.value) {
    useSeoMeta({
      title: pageName.value
    })
  }
}
</script>
