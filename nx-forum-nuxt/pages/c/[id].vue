<template>
  <div class="error-page" v-if="errorMsg">
    <el-empty :description="errorMsg" />
  </div>
  <div class="success-page" v-else>
    <el-card>
      <div class="flex items-center">
        <el-avatar :size="40" :src="comment.avatarUrl" />
        <div class="flex-row ml-2">
          <div class="font-bold cursor-pointer hover:text-[#409eff]">
            {{ comment.authorName }}
          </div>
          <div class="text-gray-500 text-sm">
            {{ comment.createTime }}
          </div>
        </div>
      </div>
      <div
        class="comment-content"
        v-html="comment.message"
        @click="clickComment($event, comment.message)"
      ></div>
      <a :href="`/t/${comment.threadId}`" target="_blank" class="hover:text-[#409eff]">
        <div class="bg-gray-100 p-2 mt-2 text-xs">
          <span> {{ comment.threadTitle }}</span>
          <span class="pl-2">回复({{ comment.threadCommentCount }})</span>
        </div>
      </a>
    </el-card>
    <div class="reply">
      <el-card class="mt-2">
        <div class="text-xs">{{ comment.replyCount }}条回复</div>
        <comment-replies :comment="comment" :order="CommentOrderV.TIME_DESC" :reply-id="replyId" />
      </el-card>
    </div>
    <common-image-viewer ref="imageViewer" />
  </div>
</template>
<script setup lang="ts">
import { getCommentVOById } from '~/apis/comment'
import { CommentOrderV } from '~/constant'
import type { CommentVO } from '~/types/global'
definePageMeta({
  layout: 'default'
})
const imageViewer = ref()
const errorMsg = ref('')
const comment = reactive<CommentVO>({
  authorId: 0,
  authorName: '',
  avatarUrl: '',
  commentId: 0,
  createTime: '',
  images: [],
  likes: 0,
  message: '',
  replies: null,
  replyCount: 0,
  threadCommentCount: 0,
  threadId: 0,
  threadTitle: '',
  updateTime: null
})
useSeoMeta({
  title: '用户评论'
})
const route = useRoute()
const commentId = route.params.id ? Number(route.params.id) : 0
const replyId = computed(() => {
  const id = route.query.replyId
  return id ? Number(id) : undefined
})

getCommentVOById(commentId, replyId.value)
  .then((res) => {
    Object.assign(comment, res.data)
    comment.pageNo = 1
    errorMsg.value = ''
  })
  .catch((errMsg) => {
    errorMsg.value = errMsg
  })
const clickComment = (element: any, message: string) => {
  imageViewer.value.showImage(element, message)
}
</script>
<style lang="scss" scoped>
:deep(img) {
  max-width: 100%;
  height: auto;
  cursor: pointer;
}
.comment-content {
  margin-top: 10px;
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
.comment-other {
  margin-top: 10px;
  font-size: 14px;
  color: rgb(145, 150, 161);
  line-height: 16px;
}
.reply {
  .el-card {
    --el-card-padding: 10px 20px;
  }
  @media screen and (max-width: 768px) {
    .el-card {
      --el-card-padding: 0 10px;
    }
  }
}
</style>
