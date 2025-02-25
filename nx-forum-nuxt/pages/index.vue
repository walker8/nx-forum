<template>
  <forum-content />
</template>
<script setup lang="ts">
import { getWebsiteBaseInfo } from '~/apis/config'

definePageMeta({
  layout: 'forum'
})
const website = ref()
getWebsiteBaseInfo().then((res) => {
  website.value = res.data
})
useSeoMeta({
  title: () => website.value?.seoTitle,
  ogTitle: website.value?.seoTitle,
  description: website.value?.seoContent,
  ogDescription: website.value?.seoContent
})
</script>
