<template>
  <thread-list
    :forum-post-page="favoritePage"
    :disable-load-more="disableLoadMore"
    @load-more-threads="loadMoreFavorites"
  />
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { queryFavoritesByUserId } from '~/apis/favorite'
import type { CustomPage, Query, Thread } from '~/types/global'

definePageMeta({
  layout: 'user'
})
useSeoMeta({
  title: '个人主页-收藏'
})

const favoritePage = useForumPostPage()

const route = useRoute()
const userId = route.params.userId
const disableLoadMore = ref(false)

const query = reactive<Query>({
  pageNo: 1,
  pageSize: 15
})

const fetchFavorites = () => {
  queryFavoritesByUserId(Number(userId), query)
    .then((res) => {
      const data = res.data
      favoritePage.value = data
      favoritePage.value.errMsg = ''
    })
    .catch((errMsg) => {
      favoritePage.value.errMsg = String(errMsg)
    })
}

if (userId) {
  fetchFavorites()
}

const loadMoreFavorites = async () => {
  if (
    disableLoadMore.value ||
    !favoritePage.value.hasNext ||
    favoritePage.value.records.length === 0
  )
    return

  disableLoadMore.value = true
  query.pageNo += 1

  try {
    const res = await queryFavoritesByUserId(Number(userId), query)
    favoritePage.value.records.push(...res.data.records)
    favoritePage.value.hasNext = res.data.hasNext
    favoritePage.value.current = res.data.current
  } catch (err) {
    ElMessage.error(String(err))
    query.pageNo -= 1
  } finally {
    disableLoadMore.value = false
  }
}
</script>
