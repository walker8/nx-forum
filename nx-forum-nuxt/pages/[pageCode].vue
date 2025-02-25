<template>
  <div class="page-content">
    <div v-html="pageContent"></div>
  </div>
</template>
<script setup lang="ts">
import { getPageContent } from '~/apis/custom-page'

definePageMeta({
  layout: 'empty'
})

const route = useRoute()
const pageCode = route.params.pageCode
const pageContent = ref('')

if (pageCode) {
  const res = await getPageContent(String(pageCode))
  // 获取自定义页面成功
  const data = res.data
  pageContent.value = data
}
</script>
<style lang="scss" scoped>
.page-content {
  max-width: 1200px;
  margin: 20px auto 0;
  font-size: 16px;
  line-height: 1.75;
  color: #333;
  padding: 0 20px;
  padding-bottom: 20px;

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
    margin: 0 0 13px;
    color: #1d2129;
    padding-top: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #e4e6eb;
  }

  :deep(h3) {
    font-size: 20px;
    font-weight: 600;
    line-height: 1.4;
    margin: 0 0 13px;
    color: #1d2129;
  }

  :deep(h4) {
    font-size: 18px;
    font-weight: 600;
    line-height: 1.4;
    margin: 0 0 13px;
    color: #1d2129;
  }
}
</style>
