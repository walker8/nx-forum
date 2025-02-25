<template>
  <forum-content />
</template>
<script setup lang="ts">
definePageMeta({
  layout: 'forum'
})
const forumInfo = useForumInfo()
useSeoMeta({
  title: () => `${forumInfo.value.seoTitle}`,
  ogTitle: forumInfo.value.seoTitle,
  description: forumInfo.value.seoContent,
  ogDescription: forumInfo.value.seoContent
})
</script>
