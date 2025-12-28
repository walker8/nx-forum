<template>
  <div class="comment">
    <a id="comment-all" aria-label="所有评论"></a>
    <ClientOnly>
      <el-card class="comment-card">
        <template #header>
          <div class="card-header">
            <div class="comment-num">评论 {{ thread.comments }}</div>
            <div class="comment-sort">
              <el-radio-group v-model="commentOrder" size="small">
                <el-radio-button :value="0">正序</el-radio-button>
                <el-radio-button :value="1">最新</el-radio-button>
                <el-radio-button :value="2">热门</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </template>
        <div class="comment-editor" v-if="!disabled">
          <client-only>
            <div class="login-tip" v-if="!user.userId">
              <span class="login" @click="navigateToLogin">登录</span>后才能评论
            </div>
            <div class="login-tip" v-else-if="!isLoading && !hasPermission('comment:new', forumId)">
              当前用户组无评论权限
            </div>
            <editor-comment :thread-id="threadId" @submit="initComments" :disabled="disabled" v-else />
          </client-only>
        </div>
        <div class="comment-list">
          <div class="comment-item" v-for="(comment, index) in comments" :key="index"
            :id="`comment-${comment.commentId}`"
            :class="{ 'comment-highlight': commentId && commentId === comment.commentId }">
            <client-only>
              <div style="padding-right: 10px">
                <el-avatar :size="40" :src="comment.avatarUrl || '/img/avatar.png'" />
              </div>
              <div style="width: 100%">
                <div class="comment-user">
                  <span class="author-name" @click="open(`/user/${comment.authorId}`)">
                    {{ comment.authorName }}
                  </span>
                  <van-tag plain type="primary" v-if="comment.authorId == authorId" class="ml-1">
                    作者
                  </van-tag>
                </div>
                <div class="comment-content" v-html="replaceEmotions(comment.message)"
                  @click="clickComment($event, comment.message)"></div>
                <div class="comment-other">
                  <div class="flex text-sm mt-1 gap-2" style="color: rgb(145, 150, 161)">
                    <div class="commnet-time">{{ comment.createTime }}</div>
                    <div :class="{ 'comment-thumb': true, 'text-[#409eff]': comment.liked }"
                      @click="handleLike(comment)">
                      <el-icon size="16px" style="position: relative; top: 0px">
                        <Icon name="tabler:thumb-up" />
                      </el-icon>
                      <span style="margin-left: 3px">{{ comment.likes }}</span>
                    </div>
                    <div class="reply" v-if="hasPermission('comment:new', forumId)">
                      <div class="comment-reply-cancel" @click="cancelReply(comment)" v-if="comment.showEditor">
                        取消回复
                      </div>
                      <div v-else @click="!disabled && toComment(comment)" :class="{
                        'cursor-not-allowed': disabled,
                        'cursor-pointer hover:text-[#409eff]': !disabled
                      }">
                        回复
                      </div>
                    </div>
                    <div v-if="user.userId" class="ml-auto cursor-pointer hover:text-[#409eff]"
                      @click="onReportComment(comment)">
                      举报
                    </div>
                  </div>
                </div>
                <div style="width: 100%; margin-top: 7px" v-if="comment.showEditor">
                  <editor-comment 
                    :comment-id="comment.commentId" 
                    :reply-author-name="comment.newReplyAuthorName"
                    :init-focus="true" 
                    @submit="initCommentReplies(comment)"
                    @cancel="cancelReply(comment)"
                  />
                </div>
                <div class="comment-reply-list" v-if="comment.replies?.length">
                  <comment-replies :comment="comment" :author-id="authorId" :disabled="disabled" :forum-id="forumId" />
                </div>
              </div>
            </client-only>
          </div>
        </div>
        <common-load-more :has-next="hasNext" :disable-load-more="disableLoadMore" @load-more="loadMoreComments">
          查看全部 {{ thread.comments }} 条评论
        </common-load-more>
      </el-card>
      <common-image-viewer ref="imageViewer" />
    </ClientOnly>
  </div>
</template>

<script setup lang="ts">
import { type CommentVO } from '~/types/global'
import { queryComments, queryCommentReplies } from '@/apis/comment'
import { ElMessage } from 'element-plus'
import { CommentOrderV } from '~/constant'
import { toggleLike } from '~/apis/like'
import { useEmotions } from '~/composables/useEmotions'
import { useReport } from '~/composables/useReport'

const props = defineProps({
  threadId: {
    type: Number,
    required: true
  },
  forumId: {
    type: Number,
    required: true
  },
  authorId: {
    type: Number,
    required: true,
    default: 0
  },
  order: {
    type: Number,
    default: CommentOrderV.TIME_ASC
  },
  disabled: {
    type: Boolean,
    default: false
  }
})
const { threadId, authorId, order, disabled, forumId } = props
const commentOrder = ref(order)
const thread = useThread()
const imageViewer = ref()
const comments = ref()
const hasNext = ref(false)
const disableLoadMore = ref(false)
let pageNo = 1
const pageSize = 10
const { user } = useCurrentUser()
const { replaceEmotions } = useEmotions()
const route = useRoute()
const { openReportDialog } = useReport()
const commentId = computed(() => {
  const id = route.query.commentId
  return id ? Number(id) : undefined
})

// 滚动到指定评论
const scrollToComment = () => {
  if (commentId.value && comments.value?.length) {
    nextTick(() => {
      const commentElement = document.getElementById(`comment-${commentId.value}`)
      if (commentElement) {
        commentElement.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    })
  }
}

// 添加类型定义
interface Comment extends CommentVO {
  showEditor?: boolean
  newReplyAuthorName?: string
}

const { hasPermission, authPromise, isLoading } = useUserAuth(props.forumId)
// await authPromise
// 监听 commentOrder 变化
watch(commentOrder, () => {
  initComments()
})

const initComments = () => {
  if (thread.value.comments > 0) {
    pageNo = 1
    queryComments(threadId, commentOrder.value, pageNo, pageSize, commentId.value)
      .then((res) => {
        const data = res.data
        comments.value = data.records
        hasNext.value = data.hasNext
        pageNo++
        // 滚动到指定评论
        scrollToComment()
      })
      .catch((err) => {
        ElMessage.error(err)
      })
  }
}
initComments()
const loadMoreComments = () => {
  disableLoadMore.value = true
  queryComments(threadId, commentOrder.value, pageNo, pageSize, commentId.value)
    .then((res) => {
      const data = res.data
      comments.value.push(...data.records)
      hasNext.value = data.hasNext
      pageNo++
    })
    .catch((err) => {
      ElMessage.error(err)
    })
    .finally(() => {
      disableLoadMore.value = false
    })
}
const initCommentReplies = (comment: CommentVO) => {
  comment.pageNo = 1
  const pageSize = 20
  queryCommentReplies(comment.commentId, CommentOrderV.TIME_ASC, comment.pageNo, pageSize)
    .then((res) => {
      const data = res.data
      comment.replies = data.records
      comment.replyCount = data.total
      // 回复成功后隐藏编辑器
      comment.showEditor = false
      comment.newReplyAuthorName = undefined
    })
    .catch((err) => {
      ElMessage.error(err)
    })
}
const toComment = async (comment: Comment) => {
  comment.newReplyAuthorName = comment.authorName
  comment.showEditor = true
}

const cancelReply = (comment: Comment) => {
  comment.showEditor = false
  comment.newReplyAuthorName = undefined
}
const clickComment = (element: any, message: string) => {
  imageViewer.value.showImage(element, message)
}
const navigateToLogin = () => {
  navigateTo('/uc/login')
}
const open = (url: string) => {
  window.open(url, '_blank')
}

const handleLike = async (comment: CommentVO) => {
  const user = useUser()
  if (user.value.userId === 0) {
    ElMessage.warning('请先登录再点赞')
    return
  }
  if (disabled) return

  try {
    const targetId = comment.commentId
    const res = await toggleLike({
      targetType: 1,
      targetId
    })

    if (res.data > 0) {
      comment.liked = true
    } else {
      comment.liked = false
    }
    comment.likes = comment.likes + res.data
  } catch (err) {
    ElMessage.error(err as string)
  }
}

function onReportComment(comment: CommentVO) {
  if (!user.value.userId) {
    ElMessage.error('请登录后操作')
    return
  }
  openReportDialog(comment.commentId, 'COMMENT', props.forumId)
}
</script>

<style lang="scss" scoped>
.comment-item {
  display: flex;
}

.comment-item {
  margin-top: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-num {
  color: black;
  font-size: 18px;
  font-weight: 600;
  line-height: 30px;
}

.comment {
  margin-top: 20px;
  margin-bottom: 20px;
}

.comment-user {
  box-sizing: border-box;
  margin: 0px;

  .author-name {
    min-width: 0px;
    font-size: 15px;
    font-weight: bold;
    text-decoration: none;
    color: rgb(55, 58, 64);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    cursor: pointer;
    line-height: 20px;

    &:hover {
      color: #409eff;
    }
  }
}

.comment-content {
  margin-top: 3px;
  box-sizing: border-box;
  min-width: 0px;
  overflow: hidden;
  color: rgb(55, 58, 64);
  overflow-wrap: break-word;
  font-size: 15px;
  line-height: 20px;
  /* 允许在单词内换行 */
  word-wrap: break-word;
  /* 单词可以在任意字符间断开 */
  word-break: break-word;
  /* 超出显示省略号 */
  white-space: normal;
  /* 保持所有文字在一个方块内 */
  overflow-wrap: break-word;

  :deep(.gallery) {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding-top: 5px;

    img {
      width: 32%;
      /* 图片宽度自适应容器 */
      max-width: 300px;
      /* 设置最大宽度限制 */
      object-fit: cover;
      /* 保持宽高比，裁剪图片以填充容器 */
      height: auto;
      /* 高度自适应 */
    }

    @media screen and (max-width: 768px) {
      gap: 6px;
    }
  }
}

.comment-editor {
  margin-bottom: 20px;
}
.comment-other {
  margin-top: 10px;
  font-size: 14px;
  color: rgb(145, 150, 161);
  line-height: 16px;
}

.comment-reply-other {
  margin-top: 5px;
  font-size: 14px;
  color: rgb(145, 150, 161);
  line-height: 16px;
}

.comment-thumb {
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-reply-cancel,
.comment-thumb:hover {
  cursor: pointer;
  color: #409eff;
}

.comment-reply-list {
  margin-top: 10px;
  margin-bottom: 5px;
  padding-bottom: 5px;
  padding-left: 5px;
  padding-right: 5px;
  background-color: #fafafa;
}

.comment-reply-item {
  margin-top: 10px;
}

.more-relies {
  margin-top: 8px;
}

.el-card {
  --el-card-padding: 15px;
}

.login-tip {
  @apply flex items-center justify-center;
  min-height: 80px;
  background-color: #f5f5f5;
  border-radius: 4px;
  color: #666;
  font-size: 16px;
  transition: all 0.3s ease;

  .login {
    color: #409eff;
    cursor: pointer;
  }
}

.comment-highlight {
  background-color: rgba(64, 158, 255, 0.1);
  transition: background-color 0.5s ease;
  padding: 5px;
}

/* 移动端底部导航空间 */
@media screen and (max-width: 767px) {
  .comment {
    margin-bottom: 50px;
  }
}
</style>
