<template>
  <div class="edit">
    <el-empty :description="errorMsg" v-if="errorMsg" />
    <ClientOnly v-else>
      <EditorTiptap
        v-if="mounted && contentLoaded"
        v-model="threadCmd.content"
        v-model:title="threadCmd.subject"
        v-model:cover="threadCmd.cover"
      />
      <template #fallback>
        <div class="editor-loading">
          <el-skeleton :rows="10" animated />
        </div>
      </template>
    </ClientOnly>
  </div>
</template>
<script setup lang="ts">
import { getThreadForEdit } from '@/apis/thread'

definePageMeta({
  layout: 'editor'
})

// 懒加载编辑器组件
const EditorTiptap = defineAsyncComponent(() => import('~/components/editor/tiptap.vue'))
const mounted = ref(false)
const contentLoaded = ref(false)

onMounted(() => {
  mounted.value = true
})
const route = useRoute()
const threadCmd = useThreadCmd()
const threadId = Number(route.params.id)
const errorMsg = ref()
threadCmd.value.threadId = threadId

// 获取帖子内容
getThreadForEdit(threadId)
  .then((res) => {
    const data = res.data
    threadCmd.value.subject = data.subject
    threadCmd.value.content = data.content
    threadCmd.value.forumId = data.forumId
    threadCmd.value.commentOrder = data.commentOrder
    threadCmd.value.cover = data.cover || null
    contentLoaded.value = true
    errorMsg.value = ''
  })
  .catch((err) => {
    errorMsg.value = err
  })

useSeoMeta({
  title: '编辑文章'
})
</script>
<style lang="scss" scoped>
.edit {
  min-height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  background: #f5f7fb;
}

.editor-loading {
  width: 100%;
  padding: 20px;
}
</style>
