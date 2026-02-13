<template>
  <div class="h-screen flex flex-col">
    <div class="bg-white border-t border-gray-200 py-2.5 px-5 text-right">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </div>
    <div class="flex-1 bg-white">
      <ClientOnly>
        <EditorTiptap
          v-if="mounted && contentLoaded"
          v-model="content"
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
const mounted = ref(false)
const contentLoaded = ref(false)

onMounted(() => {
  mounted.value = true
})

const route = useRoute()
const router = useRouter()
const pageId = computed(() => Number(route.params.id))

const content = ref('')
const saving = ref(false)

const fetchContent = async () => {
  try {
    const res = await getPageContentByAdmin(pageId.value)
    content.value = res.data
    contentLoaded.value = true
  } catch (error) {
    console.error('获取内容失败', error)
  }
}

const handleSave = async () => {
  try {
    saving.value = true
    await updatePageContent(pageId.value, content.value)
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
.editor-loading {
  width: 100%;
  padding: 20px;
}
</style> 