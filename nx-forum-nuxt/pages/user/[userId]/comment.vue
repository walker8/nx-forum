<template>
  <el-card class="comment-list">
    <client-only>
      <el-empty description="暂无内容" v-if="commentPage.records?.length === 0" />
      <div v-else v-for="comment in commentPage.records" class="comment-item">
        <div style="padding-top: 12px">
          <div class="message" v-html="comment.message"></div>
          <div class="bg-gray-100 p-2 mt-1 text-xs">
            <span>主题：</span>
            <a :href="`/t/${comment.threadId}`" target="_blank">{{ comment.threadTitle }}</a>
            <span class="pl-2">回复({{ comment.threadCommentCount }})</span>
          </div>
          <div class="text-xs pt-2 text-gray-500">{{ comment.createTime }}</div>
        </div>
        <el-divider style="margin: 0px; margin-top: 8px" />
      </div>
      <common-load-more
        :has-next="commentPage.hasNext"
        :disable-load-more="disableLoadMore"
        @load-more="loadMoreComments"
        class="pt-1"
      >
        加载更多内容
      </common-load-more>
    </client-only>
  </el-card>
</template>
<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { queryCommentsByUserId } from '~/apis/comment'
import type { CommentVO, CustomPage, Query } from '~/types/global'
definePageMeta({
  layout: 'user'
})
useSeoMeta({
  title: '个人主页-评论'
})
const commentPage = reactive<CustomPage<CommentVO>>({
  total: 0,
  size: 0,
  current: 0,
  hasNext: false,
  errMsg: '',
  records: [],
  loading: false
})
const route = useRoute()
const userId = Number(route.params.userId)
const disableLoadMore = ref(false)
let query: Query = {
  pageNo: 1,
  pageSize: 10
}
if (!isNaN(userId) && userId > 0) {
  // 查询帖子列表
  queryCommentsByUserId(userId, query)
    .then((res) => {
      // 获取列表成功
      let data = res.data
      commentPage.current = data.current
      commentPage.records = data.records
      commentPage.hasNext = data.hasNext
      commentPage.total = data.total
      commentPage.size = data.size
      commentPage.errMsg = ''
    })
    .catch((errMsg) => {
      // 获取列表失败
      commentPage.errMsg = String(errMsg)
    })
}
const loadMoreComments = () => {
  disableLoadMore.value = true
  const pageNo = commentPage.current + 1
  query.pageNo = pageNo
  queryCommentsByUserId(Number(userId), query)
    .then((res) => {
      const data = res.data
      commentPage.records.push(...data.records)
      commentPage.hasNext = data.hasNext
      commentPage.total = data.total
      commentPage.current = data.current
      commentPage.size = data.size
    })
    .catch((err) => {
      ElMessage.error(err)
    })
    .finally(() => {
      disableLoadMore.value = false
    })
}
</script>
<style lang="scss" scoped>
.comment-content {
  margin-top: 3px;
  box-sizing: border-box;
  min-width: 0px;
  overflow: hidden;
  color: rgb(55, 58, 64);
  overflow-wrap: break-word;
  font-size: 15px;
  line-height: 20px;
  :deep(.gallery) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding-top: 5px;
    img {
      width: 32%; /* 图片宽度自适应容器 */
      max-width: 300px; /* 设置最大宽度限制 */
      object-fit: cover; /* 保持宽高比，裁剪图片以填充容器 */
      height: auto; /* 高度自适应 */
    }
    @media screen and (max-width: 768px) {
      gap: 6px;
    }
  }
}
.message {
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 控制显示的行数 */
  -webkit-box-orient: vertical;
  line-clamp: 2; /* 标准属性，确保兼容性 */
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}
.comment-list :deep(a) {
  color: #409eff;
}
.el-card :deep(.el-card__body) {
  padding-top: 0px;
}
</style>
