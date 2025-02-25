<template>
  <client-only>
    <div class="emotion">
      <el-input
        ref="inputRef"
        v-model="message"
        type="textarea"
        class="w-full emotion-textarea"
        :autosize="{ minRows: 2, maxRows: 8 }"
        :maxlength="commentId ? 500 : 1000"
        show-word-limit
        :placeholder="placeHolder"
        @blur="inputBlur"
        @focus="inputFocus"
      />
      <div class="border-b border-l border-r border-gray-300 p-2">
        <div class="flex items-center gap-2">
          <el-popover 
            placement="bottom-start" 
            :width="isMobile ? 320 : 400" 
            trigger="click"
            popper-class="emotion-popover"
          >
            <template #reference>
              <el-icon
                class="cursor-pointer hover:text-[#409eff]"
                :size="20"
                color="rgb(51, 51, 51)"
                @click="showEmotions = true"
              >
                <Icon name="tabler:mood-smile-beam" />
              </el-icon>
            </template>
            <div>
              <el-space wrap v-if="showEmotions">
                <div
                  v-for="emotion in emotions"
                  :key="emotion.name"
                  @click="addEmoji(emotion)"
                  class="cursor-pointer"
                >
                  <el-image class="w-7 h-7" :src="emotion.url" />
                </div>
              </el-space>
            </div>
          </el-popover>
          <el-icon
            :size="20"
            class="cursor-pointer hover:text-[#409eff]"
            :color="fileList?.length > 0 ? '#409eff' : 'rgb(51, 51, 51)'"
            @click="toggleImage"
            v-show="commentId <= 0"
          >
            <Icon name="tabler:photo-plus" />
          </el-icon>
          <div class="flex justify-end w-full">
            <el-button
              type="primary"
              @click="submit"
              :disabled="(message === null || message.trim() === '') && fileList?.length <= 0"
            >
              回复
            </el-button>
          </div>
        </div>
        <van-uploader
          v-model="fileList"
          multiple
          :max-count="9"
          :max-size="10 * 1024 * 1024"
          @oversize="onOversize"
          result-type="file"
          v-show="showImage"
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
const thread = useThread()
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
  }
})

const emits = defineEmits(['submit'])

const { commentId, threadId, replyAuthorId, replyAuthorName, initFocus } = props
const placeHolder = ref('理性发言，友善互动')
if (replyAuthorName) {
  placeHolder.value = `回复 ${replyAuthorName}`
}
const message = ref('')
const showImage = ref(false)
const inputRef = ref()
let inputBlurIndex = 0

const showEmotions = ref(false)

// 检测是否为移动端
const isMobile = ref(false)
onMounted(() => {
  // 检测移动端
  const checkMobile = () => {
    isMobile.value = window.innerWidth <= 768
  }
  checkMobile()
  window.addEventListener('resize', checkMobile)
  onUnmounted(() => {
    window.removeEventListener('resize', checkMobile)
  })
  
  if (initFocus) {
    // nextTick保证内容已经更新到HTML的DOM结构中了。
    nextTick(() => {
      inputRef.value?.focus()
    })
  }
})

const inputBlur = (e: Event) => {
  inputBlurIndex = (e.target as HTMLTextAreaElement).selectionStart
}
const inputFocus = (e: Event) => {
  (e.target as HTMLTextAreaElement).setSelectionRange(inputBlurIndex, inputBlurIndex)
}

const { emotions } = useEmotions()

function toggleImage() {
  showImage.value = !showImage.value
}

function addEmoji(emotion: Emotion) {
  message.value =
    message.value.slice(0, inputBlurIndex) + emotion.name + message.value.slice(inputBlurIndex)
  inputBlurIndex = inputBlurIndex + emotion.name.length
  inputRef.value.focus()
}

const fileList = ref<Array<UploaderFileListItem>>([])
const onOversize = (file: UploaderFileListItem) => {
  showToast('图片大小不能超过 10mb')
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
.emotion {
  // 移动端优化：防止点击输入框时页面自动放大
  // iOS Safari 会在输入框字体小于16px时自动放大页面
  @media screen and (max-width: 768px) {
    .emotion-textarea {
      :deep(.el-textarea__inner) {
        font-size: 16px !important; // 确保字体大小至少16px，防止iOS自动放大
        line-height: 1.5;
        padding: 12px;
      }
    }

    // 优化移动端按钮和图标大小
    .border-b {
      padding: 8px 12px;
      
      .el-icon {
        flex-shrink: 0;
      }
      
      .el-button {
        padding: 8px 16px;
        font-size: 14px;
      }
    }

    // 优化上传组件在移动端的显示
    .van-uploader {
      margin-top: 8px;
    }
  }
}
</style>

<style lang="scss">
// 移动端表情选择器样式（需要全局样式，因为popover插入到body）
.emotion-popover {
  @media screen and (max-width: 768px) {
    max-width: calc(100vw - 32px) !important;
    
    .el-space {
      gap: 8px !important;
      
      .cursor-pointer {
        padding: 4px;
        // 确保触摸目标足够大（至少44x44px）
        min-width: 44px;
        min-height: 44px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}
</style>
