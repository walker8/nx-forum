<template>
  <client-only>
    <!-- 移动端遮罩层 -->
    <div v-if="isMobile && isExpanded" class="comment-overlay" @click="handleOverlayClick"></div>

    <!-- 评论输入组件 -->
    <div
      ref="wrapperRef"
      :class="[
        'comment-input-wrapper',
        {
          'is-expanded': isExpanded,
          'is-mobile': isMobile,
          'is-focused': isFocused
        }
      ]"
    >
      <!-- 折叠状态（未点击时） -->
      <div v-if="!isExpanded" class="comment-collapsed" @click="expandInput">
        <el-avatar
          :size="isMobile ? 32 : 40"
          :src="user.avatar || '/img/avatar.png'"
          class="comment-avatar"
        />
        <div class="comment-input-placeholder">
          {{ placeHolder }}
        </div>
        <div class="comment-icons-collapsed">
          <el-icon
            class="cursor-pointer"
            :size="isMobile ? 18 : 20"
            color="rgb(145, 150, 161)"
            @click.stop="expandInput"
          >
            <Icon name="tabler:mood-smile-beam" />
          </el-icon>
          <el-icon
            v-if="commentId <= 0"
            class="cursor-pointer"
            :size="isMobile ? 18 : 20"
            color="rgb(145, 150, 161)"
            @click.stop="expandInput"
          >
            <Icon name="tabler:photo-plus" />
          </el-icon>
        </div>
      </div>

      <!-- 展开状态（点击后） -->
      <div v-else class="comment-expanded" @click.stop>
        <!-- PC端显示用户名和头像 -->
        <div v-if="!isMobile && user.userName" class="comment-user-info">
          <el-avatar
            :size="32"
            :src="user.avatar || '/img/avatar.png'"
            class="comment-avatar-inline"
          />
          <span class="comment-username">{{ user.userName }}</span>
        </div>

        <!-- 输入框容器（包裹 textarea 和工具栏） -->
        <div class="comment-input-container">
          <!-- 输入框 -->
          <el-input
            ref="inputRef"
            v-model="message"
            type="textarea"
            class="comment-textarea"
            :class="{ 'is-mobile-textarea': isMobile }"
            :autosize="isMobile ? { minRows: 3, maxRows: 6 } : { minRows: 2, maxRows: 8 }"
            :maxlength="commentId ? 500 : 1000"
            :show-word-limit="false"
            :placeholder="placeHolder"
            @blur="inputBlur"
            @focus="inputFocus"
          />

          <!-- 工具栏（在输入框下方） -->
          <div class="comment-toolbar">
            <div class="comment-toolbar-left">
              <span class="comment-word-count">
                {{ message.length }} / {{ commentId ? 500 : 1000 }}
              </span>
              <el-popover
                v-model:visible="showEmotions"
                placement="top-start"
                width="auto"
                trigger="click"
                popper-class="comment-emotion-popover"
              >
                <template #reference>
                  <el-icon
                    class="cursor-pointer hover:text-[#409eff]"
                    :size="isMobile ? 20 : 22"
                    color="rgb(51, 51, 51)"
                  >
                    <Icon name="tabler:mood-smile-beam" />
                  </el-icon>
                </template>
                <div class="emotion-selector">
                  <div
                    v-for="emotion in emotions"
                    :key="emotion.name"
                    @click="addEmoji(emotion)"
                    class="emotion-item"
                    :title="emotion.name"
                  >
                    <el-image class="emotion-image" :src="emotion.url" :alt="emotion.name" />
                  </div>
                </div>
              </el-popover>
              <el-icon
                v-if="commentId <= 0"
                :size="isMobile ? 20 : 22"
                class="cursor-pointer hover:text-[#409eff]"
                :color="fileList?.length > 0 ? '#409eff' : 'rgb(51, 51, 51)'"
                @click.stop="toggleImage"
              >
                <Icon name="tabler:photo-plus" />
              </el-icon>
            </div>
            <div class="comment-toolbar-right">
              <el-button
                type="primary"
                @click.stop="submit"
                :disabled="(message === null || message.trim() === '') && fileList?.length <= 0"
              >
                {{ commentId > 0 ? '回复' : '发送' }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 图片上传区域 -->
        <van-uploader
          v-model="fileList"
          multiple
          :max-count="9"
          :max-size="10 * 1024 * 1024"
          @oversize="onOversize"
          result-type="file"
          v-show="showImage"
          class="comment-uploader"
        />
      </div>
    </div>
  </client-only>
</template>

<script setup lang="ts">
import type { UploaderFileListItem } from 'vant'
import { uploadImage } from '@/apis/image'
import { createComment, createCommentReply } from '@/apis/comment'
import { ElMessage, ElInput } from 'element-plus'
import type { Emotion } from '~/composables/useEmotions'

const thread = useThread()
const { user } = useCurrentUser()

const props = defineProps({
  commentId: {
    type: Number,
    default: 0
  },
  threadId: {
    type: Number,
    default: 0
  },
  replyAuthorId: {
    type: Number,
    default: 0
  },
  replyAuthorName: {
    type: String,
    default: ''
  },
  initFocus: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emits = defineEmits(['submit', 'cancel'])

const { commentId, threadId, replyAuthorId, replyAuthorName, initFocus } = props
const placeHolder = computed(() => {
  if (replyAuthorName) {
    return `回复 ${replyAuthorName}:`
  }
  return '写留言'
})

const message = ref('')
const showImage = ref(false)
const inputRef = ref()
const wrapperRef = ref<HTMLElement>()
let inputBlurIndex = 0

const showEmotions = ref(false)
const isExpanded = ref(false)
const isFocused = ref(false)

// 检测是否为移动端
const isMobile = ref(false)

// 监听回复相关 props 变化，如果清空则重置编辑器
watch(
  [() => props.commentId, () => props.replyAuthorName],
  ([newCommentId, newReplyAuthorName], [oldCommentId, oldReplyAuthorName]) => {
    // 如果从有回复状态变为无回复状态，重置编辑器
    if ((oldCommentId > 0 || oldReplyAuthorName) && newCommentId === 0 && !newReplyAuthorName) {
      collapseInput()
      message.value = ''
      fileList.value = []
      showImage.value = false
    }
  }
)

onMounted(() => {
  // 检测移动端
  const checkMobile = () => {
    isMobile.value = window.innerWidth <= 768
  }
  checkMobile()
  window.addEventListener('resize', checkMobile)

  // 添加全局点击监听，用于检测外部点击失焦
  document.addEventListener('mousedown', handleClickOutside)

  onUnmounted(() => {
    window.removeEventListener('resize', checkMobile)
    document.removeEventListener('mousedown', handleClickOutside)
  })

  if (initFocus) {
    nextTick(() => {
      expandInput()
      inputRef.value?.focus()
    })
  }
})

// 展开输入框
const expandInput = () => {
  if (props.disabled) return
  isExpanded.value = true
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// 折叠输入框
const collapseInput = () => {
  isExpanded.value = false
  isFocused.value = false
  showImage.value = false
}

// 处理遮罩层点击
const handleOverlayClick = () => {
  if (isMobile.value && isExpanded.value) {
    collapseInput()
  }
}

// 处理外部点击失焦
const handleClickOutside = (event: MouseEvent) => {
  if (!isExpanded.value || !wrapperRef.value) return

  const target = event.target as HTMLElement

  // 检查点击是否在编辑器容器内（wrapperRef 包含了整个编辑器区域，包括工具栏和图片上传区域）
  const isInsideContainer = wrapperRef.value.contains(target)
  // 检查是否在表情弹框内（弹框是 append-to-body 的，不在容器内，需要单独检查）
  const isInsidePopover = target.closest('.comment-emotion-popover') !== null

  // 如果点击在容器外，且不在表情弹框内，执行失焦逻辑
  if (!isInsideContainer && !isInsidePopover) {
    isFocused.value = false

    // 检查是否有内容
    const hasContent = message.value.trim() !== '' || fileList.value.length > 0
    // 如果没有内容且表情弹框和图片上传未打开，则自动折叠
    if (!hasContent && !showEmotions.value && !showImage.value) {
      if (commentId > 0) {
        // 如果有 commentId，说明是回复模式，调用父组件的 cancel 事件
        emits('cancel')
      } else {
        collapseInput()
      }
    } else {
      // 有内容时，只是失焦但不折叠
      inputRef.value?.blur()
    }
  }
}

const inputBlur = (e: Event) => {
  // 保存光标位置，用于插入表情等操作
  inputBlurIndex = (e.target as HTMLTextAreaElement).selectionStart
  // 不在这里执行失焦逻辑，由 handleClickOutside 统一处理
}

const inputFocus = (e: Event) => {
  isFocused.value = true
  if (!isExpanded.value) {
    expandInput()
  }
  ;(e.target as HTMLTextAreaElement).setSelectionRange(inputBlurIndex, inputBlurIndex)
}

const { emotions } = useEmotions()

function toggleImage() {
  showImage.value = !showImage.value
}

function addEmoji(emotion: Emotion) {
  message.value =
    message.value.slice(0, inputBlurIndex) + emotion.name + message.value.slice(inputBlurIndex)
  inputBlurIndex = inputBlurIndex + emotion.name.length
  showEmotions.value = false // 关闭表情弹框
  inputRef.value.focus()
}

const fileList = ref<Array<UploaderFileListItem>>([])
const onOversize = (file: UploaderFileListItem) => {
  ElMessage.warning('图片大小不能超过 10MB')
}

const submit = async () => {
  let images = []
  try {
    images = await upload()
  } catch (err) {
    ElMessage.error(err || '图片上传失败')
    return
  }

  if (commentId > 0) {
    createCommentReply(commentId, message.value, replyAuthorId)
      .then((res) => {
        message.value = ''
        fileList.value = []
        showImage.value = false
        collapseInput()
        if (res.errCode === '0005') {
          ElMessage.warning(res.data)
        } else {
          ElMessage.success(res.data)
          thread.value.comments++
          emits('submit')
        }
      })
      .catch((err) => {
        ElMessage.error(err)
      })
  } else {
    createComment(threadId, message.value, images)
      .then((res) => {
        message.value = ''
        fileList.value = []
        showImage.value = false
        collapseInput()
        if (res.errCode === '0005') {
          ElMessage.warning(res.data)
        } else {
          ElMessage.success(res.data)
          thread.value.comments++
          emits('submit')
        }
      })
      .catch((err) => {
        ElMessage.error(err)
      })
  }
}

const upload = async () => {
  let images: Array<string> = []
  for (let file of fileList.value) {
    if (file.file instanceof File) {
      file.status = 'uploading'
      const formData = new FormData()
      formData.append('file', file.file)
      try {
        const res = await uploadImage(formData)
        file.status = 'done'
        file.file = undefined
        file.url = res.data
      } catch (error) {
        file.status = 'failed'
        throw error
      }
    }
    if (file?.url) {
      images.push(file.url)
    }
  }
  return images
}
</script>

<style scoped lang="scss">
.comment-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1998;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.comment-input-wrapper {
  position: relative;
  width: 100%;

  // PC端样式
  &.is-focused {
    .comment-input-container {
      border-color: var(--el-color-primary);
    }
  }

  // 移动端样式
  &.is-mobile {
    &.is-expanded {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      z-index: 1999;
      background-color: var(--el-bg-color);
      border-top-left-radius: 16px;
      border-top-right-radius: 16px;
      box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
      padding: 12px;
      padding-bottom: calc(12px + env(safe-area-inset-bottom));
      max-height: 80vh;
      overflow-y: auto;
    }
  }
}

// 折叠状态
.comment-collapsed {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: var(--el-fill-color-light);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: var(--el-fill-color);
  }

  .comment-avatar {
    flex-shrink: 0;
  }

  .comment-input-placeholder {
    flex: 1;
    color: var(--el-text-color-placeholder);
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .comment-icons-collapsed {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;

    // 确保图标保持正方形
    :deep(.el-icon) {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 20px !important;
      height: 20px !important;
      min-width: 20px;
      min-height: 20px;
      flex-shrink: 0;
    }
  }
}

// 展开状态
.comment-expanded {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 14px;
  color: var(--el-text-color-primary);

  .comment-avatar-inline {
    flex-shrink: 0;
  }

  .comment-username {
    font-weight: 500;
  }
}

// 输入框容器（微信风格）
.comment-input-container {
  position: relative;
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  transition: border-color 0.2s ease;
  background-color: var(--el-bg-color);
  cursor: text;

  &:focus-within {
    border-color: var(--el-color-primary);
  }
}

.comment-textarea {
  width: 100%;

  // 移除所有包装元素的边框
  :deep(.el-textarea__wrapper) {
    border: none !important;
    box-shadow: none !important;
  }

  :deep(.el-textarea__inner) {
    font-size: 16px;
    line-height: 1.5;
    padding: 12px;
    border-radius: 0; // 移除圆角
    border: none !important; // 完全移除边框
    border-top: none !important;
    border-right: none !important;
    border-bottom: none !important;
    border-left: none !important;
    outline: none !important; // 移除轮廓
    box-shadow: none !important; // 移除阴影
    transition: none;
    resize: none;
    background-color: transparent;
    box-sizing: border-box;

    &:focus {
      outline: none !important;
      border: none !important;
      border-top: none !important;
      border-right: none !important;
      border-bottom: none !important;
      border-left: none !important;
      box-shadow: none !important;
    }

    &:hover {
      border: none !important;
      box-shadow: none !important;
    }
  }

  // 隐藏默认的字数统计
  :deep(.el-input__count) {
    display: none;
  }

  &.is-mobile-textarea {
    :deep(.el-textarea__inner) {
      font-size: 16px !important; // 防止iOS自动放大
      padding: 12px;
      border-radius: 0;
      border: none !important;
      outline: none !important;
    }
  }
}

.comment-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  gap: 12px;
  min-height: 44px; // 确保有足够的高度
  border-radius: 0 0 4px 4px; // 底部圆角

  .comment-toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;

    // 确保图标保持正方形
    :deep(.el-icon) {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 22px !important;
      height: 22px !important;
      min-width: 22px;
      min-height: 22px;
      flex-shrink: 0;
    }
  }

  .comment-toolbar-right {
    flex-shrink: 0;
  }

  .comment-word-count {
    font-size: 12px;
    color: var(--el-text-color-placeholder);
    margin-right: 4px;
    user-select: none;
  }
}

.comment-uploader {
  margin-top: 8px;
}

// 移动端优化
@media screen and (max-width: 768px) {
  .comment-collapsed {
    padding: 10px 12px;
    border-radius: 24px;

    .comment-icons-collapsed {
      :deep(.el-icon) {
        width: 18px !important;
        height: 18px !important;
        min-width: 18px;
        min-height: 18px;
      }
    }
  }

  .comment-expanded {
    gap: 12px;
  }

  .comment-input-container {
    border-radius: 4px;
  }

  .comment-toolbar {
    padding: 8px 10px;
    gap: 10px;
    min-height: 48px;

    .comment-word-count {
      font-size: 11px;
    }

    .comment-toolbar-left {
      :deep(.el-icon) {
        width: 20px !important;
        height: 20px !important;
        min-width: 20px;
        min-height: 20px;
      }
    }

    .el-button {
      padding: 8px 20px;
      font-size: 14px;
    }
  }

  .comment-uploader {
    margin-top: 12px;
  }
}
</style>

<style lang="scss">
// 全局样式：表情选择器（popover插入到body）
.comment-emotion-popover {
  padding: 6px 12px !important;
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden !important;
  box-sizing: border-box;

  .emotion-selector {
    display: grid;
    grid-template-columns: repeat(6 minmax(0, 1fr));
    gap: 2px !important;
    row-gap: 2px !important;
    column-gap: 2px !important;
    width: 100%;
    box-sizing: border-box;
  }

  .emotion-item {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 6px !important;
    margin: 0 !important;
    min-width: 0;
    min-height: 40px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
    background-color: transparent;
    box-sizing: border-box;
    overflow: hidden;

    &:hover {
      background-color: var(--el-fill-color-light);
      transform: scale(1.1);
    }

    &:active {
      transform: scale(0.95);
    }

    .emotion-image {
      width: 28px !important;
      height: 28px !important;
      min-width: 28px;
      min-height: 28px;
      max-width: 28px !important;
      max-height: 28px !important;
      object-fit: contain;
      user-select: none;
      pointer-events: none;
      flex-shrink: 0;
    }
  }

  // 移动端优化
  @media screen and (max-width: 768px) {
    max-width: calc(100vw - 32px) !important;
    width: auto !important;
    padding: 10px !important;
    max-height: 50vh;

    .emotion-selector {
      grid-template-columns: repeat(6, minmax(0, 1fr));
      gap: 6px !important;
      row-gap: 6px !important;
      column-gap: 6px !important;
    }

    .emotion-item {
      padding: 8px !important;
      margin: 0 !important;
      min-width: 0;
      min-height: 48px;
      border-radius: 8px;

      // 移动端触摸反馈
      &:active {
        background-color: var(--el-fill-color);
        transform: scale(0.9);
      }

      .emotion-image {
        width: 32px !important;
        height: 32px !important;
        min-width: 32px;
        min-height: 32px;
        max-width: 32px !important;
        max-height: 32px !important;
        flex-shrink: 0;
      }
    }
  }

  // PC端优化
  @media screen and (min-width: 769px) {
    width: auto !important;
    min-width: 320px;
    max-width: 500px;

    .emotion-selector {
      grid-template-columns: repeat(10, minmax(0, 1fr));
      gap: 10px !important;
      row-gap: 10px !important;
      column-gap: 10px !important;
    }

    .emotion-item {
      &:hover {
        background-color: var(--el-fill-color-light);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
  }
}
</style>
