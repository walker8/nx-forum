<template>
  <div class="h-screen flex flex-col">
    <div class="bg-white border-t border-gray-200 py-2.5 px-5 text-right">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </div>
    <div class="flex-1 bg-white flex flex-col">
      <!-- 模式切换控件 -->
      <div class="editor-mode-switch">
        <el-segmented v-model="editorMode" :options="editorModeOptions" />
      </div>

      <!-- 编辑器区域 -->
      <div class="editor-content">
        <!-- 源码编辑模式提示 -->
        <el-alert
          v-if="editorMode === 'source'"
          title="提示：使用源码编辑保存后，后续必须继续使用源码编辑，否则样式可能丢失"
          type="warning"
          show-icon
          :closable="false"
          class="mb-3"
        />

        <ClientOnly v-if="editorMode === 'rich'">
          <EditorTiptap
            v-if="mounted && contentLoaded"
            v-model="richContent"
            :sticky-header="false"
            :show-title="false"
            full-width
          />
          <template #fallback>
            <div class="editor-loading">
              <el-skeleton :rows="10" animated />
            </div>
          </template>
        </ClientOnly>

        <div v-else class="source-editor">
          <el-input
            v-model="sourceContent"
            type="textarea"
            :rows="20"
            placeholder="请输入 HTML 源码"
            font-family="monospace"
          />
          <div class="source-tip mt-2 text-gray-500 text-sm">
            提示：可直接粘贴 AI 生成的 HTML 代码
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getPageContentByAdmin, updatePageContent } from '~/apis/custom-page'

definePageMeta({
  layout: 'admin'
})

// 懒加载编辑器组件
const EditorTiptap = defineAsyncComponent(() => import('~/components/editor/tiptap.vue'))

// 编辑器模式类型
type EditorMode = 'rich' | 'source'

const editorMode = ref<EditorMode>('rich')
const editorModeOptions = [
  { label: '富文本编辑', value: 'rich' },
  { label: '源码编辑', value: 'source' }
]

const mounted = ref(false)
const contentLoaded = ref(false)

// 分离富文本和源码内容
const richContent = ref('')
const sourceContent = ref('')

onMounted(() => {
  mounted.value = true
})

const route = useRoute()
const router = useRouter()
const pageId = computed(() => Number(route.params.id))

const saving = ref(false)

const fetchContent = async () => {
  try {
    const res = await getPageContentByAdmin(pageId.value)
    const content = res.data || ''
    // 默认加载到富文本模式
    richContent.value = content
    sourceContent.value = content
    contentLoaded.value = true
  } catch (error) {
    console.error('获取内容失败', error)
  }
}

const handleSave = async () => {
  try {
    saving.value = true
    // 根据当前模式使用对应的内容保存
    const content = editorMode.value === 'rich' ? richContent.value : sourceContent.value
    await updatePageContent(pageId.value, content)
    ElMessage.success('内容保存成功')
    router.back()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.back()
}

onMounted(fetchContent)
</script>

<style scoped>
.editor-mode-switch {
  padding: 10px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.editor-content {
  flex: 1;
  overflow: auto;
}

.editor-loading {
  width: 100%;
  padding: 20px;
}

.source-editor {
  padding: 10px;
}

.source-editor :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.5;
}
</style>
