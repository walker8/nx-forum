<template>
  <thread-list
    :forum-post-page="likePage"
    :disable-load-more="disableLoadMore"
    @load-more-threads="loadMoreLikedThreads"
  />
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { queryLikedThreadsByUserId } from '~/apis/like'
import type { CustomPage, Query, Thread } from '~/types/global'
definePageMeta({
  layout: 'user'
})
useSeoMeta({
  title: '个人主页-点赞'
})
const likePage = useForumPostPage()

const route = useRoute()
const userId = route.params.userId
const disableLoadMore = ref(false)

const query = reactive<Query>({
  pageNo: 1,
  pageSize: 15
})

const fetchLikedThreads = () => {
  queryLikedThreadsByUserId(Number(userId), query)
    .then((res) => {
      let data = res.data
      likePage.value = data
      likePage.value.errMsg = ''
    })
    .catch((errMsg) => {
      likePage.value.errMsg = String(errMsg) || '加载失败'
    })
}

if (userId) {
  fetchLikedThreads()
}

const loadMoreLikedThreads = async () => {
  if (disableLoadMore.value || !likePage.value.hasNext) return
  if (isNaN(Number(userId)) || Number(userId) == 0) return

  disableLoadMore.value = true
  const pageNo = likePage.value.current + 1
  query.pageNo = pageNo

  try {
    const res = await queryLikedThreadsByUserId(Number(userId), query)
    likePage.value.records.push(...res.data.records)
    likePage.value.hasNext = res.data.hasNext
    likePage.value.current = res.data.current
  } catch (err) {
    ElMessage.error(String(err))
    query.pageNo -= 1 // 回滚页码
  } finally {
    disableLoadMore.value = false
  }
}
</script>
