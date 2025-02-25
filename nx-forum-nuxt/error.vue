<template>
  <div class="error-page">
    <el-empty :description="description" />
  </div>
</template>
<script setup lang="ts">
import type { NuxtError } from '#app'
const props = defineProps({
  error: Object as () => NuxtError
})
const description = ref('')
description.value =
  props.error?.statusCode === 404 ? '页面不存在' : props.error?.message || '页面发生错误'
</script>
<style lang="scss" scoped>
.error-page {
  margin-top: 20px;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
