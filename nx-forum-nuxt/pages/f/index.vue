<template></template>
<script setup lang="ts">
definePageMeta({
  layout: false
})

// 服务端和客户端都重定向到首页，避免 forum layout 在无 forumName 时执行无效的 API 请求
if (import.meta.server) {
  await navigateTo('/', { redirectCode: 301, external: false })
} else {
  onMounted(() => navigateTo('/'))
}
</script>
