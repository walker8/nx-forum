<template>
  <div class="editor-container">
    <div class="tinymce-editor">
      <Editor v-model="content" tinymceScriptSrc="/tinymce/tinymce.min.js" :init="initEditor" />
    </div>
    <div class="catalog hidden-sm-and-down" v-if="catalog.length > 0">
      <div class="catalog-title">目录</div>
      <div class="catalog-list">
        <div
          v-for="(item, index) in catalog"
          :key="index"
          :class="['catalog-item', `level-${item.level}`]"
        >
          {{ item.text }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { uploadImage } from '@/apis/image'
import Editor from '@tinymce/tinymce-vue'

const emits = defineEmits(['updateContent'])
const catalog = ref<Array<{ id: string; text: string; level: number }>>([])

// 解析内容中的标题
const parseHeadings = (content: string) => {
  const parser = new DOMParser()
  const doc = parser.parseFromString(content, 'text/html')
  const headings = doc.querySelectorAll('h1, h2, h3')
  const newCatalog: Array<{ id: string; text: string; level: number }> = []

  headings.forEach((heading, index) => {
    const level = parseInt(heading.tagName.substring(1))
    if (level <= 3) {
      // 只处理h1-h3
      newCatalog.push({
        id: `heading-${index}`,
        text: heading.textContent || '',
        level
      })
    }
  })

  catalog.value = newCatalog
  return doc.body.innerHTML
}

const my_images_upload_handler = (blobInfo: any, progress: any) =>
  new Promise((resolve, reject) => {
    if (blobInfo.blob().size / 1024 / 1024 > 10) {
      return reject('上传失败，图片大小请控制在 10M 以内')
    }
    const formData = new FormData()
    formData.append('file', blobInfo.blob(), blobInfo.filename())
    uploadImage(formData)
      .then((res) => {
        resolve(res.data)
      })
      .catch((err) => {
        reject(err)
      })
  })

const initEditor = {
  placeholder: '请输入内容',
  language: 'zh_CN',
  promotion: false,
  branding: false,
  license_key: 'gpl',
  plugins:
    'lists image table wordcount link autolink quickbars fullscreen preview autosave codesample insertdatetime emoticons',
  toolbar:
    'undo redo |  blocks | bold italic forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link unlink lists image axupimgs table | codesample insertdatetime emoticons preview | removeformat fullscreen',
  height: 'calc(100vh - 130px)',
  contextmenu: 'copy paste link image imagetools',
  contextmenu_never_use_native: true,
  mobile: {
    menubar: true,
    toolbar: 'undo bold italic forecolor backcolor styleselect'
  },
  toolbar_items_size: 'small',
  codesample_languages: [
    { text: 'Bash', value: 'bash' },
    { text: 'C', value: 'c' },
    { text: 'Java', value: 'java' },
    { text: 'Python', value: 'python' },
    { text: 'C++', value: 'cpp' },
    { text: 'C#', value: 'csharp' },
    { text: 'JavaScript', value: 'javascript' },
    { text: 'PHP', value: 'php' },
    { text: 'JSON', value: 'json5' },
    { text: 'R', value: 'r' },
    { text: 'SQL', value: 'sql' },
    { text: 'Groovy', value: 'groovy' },
    { text: 'CSS', value: 'css' },
    { text: 'Ruby', value: 'ruby' },
    { text: 'HTML/XML', value: 'markup' },
    { text: 'Other', value: 'other' }
  ],
  //设置富文本的样式初始化
  content_style:
    'img{max-width:100%;width: auto; height: auto;}iframe.video{width:640px;height:480px}',
  paste_data_images: true,
  quickbars_selection_toolbar: 'bold italic forecolor backcolor | removeformat | link h2 h3 h4',
  quickbars_insert_toolbar: '',
  images_upload_handler: my_images_upload_handler,
  // 上传图片文件路径变成相对路径
  convert_urls: false,
  // 添加内容变化事件处理
  setup: (editor: any) => {
    editor.on('init', () => {
      // 初始化时解析标题
      const content = editor.getContent()
      parseHeadings(content)
    })

    editor.on('NodeChange', () => {
      const content = editor.getContent()
      parseHeadings(content)
    })
  }
}

const content = ref('')
const initContent = (newContent: string) => {
  content.value = newContent
  nextTick(() => {
    parseHeadings(newContent)
  })
}

watchEffect(() => {
  emits('updateContent', content.value)
})

defineExpose({
  initContent
})
</script>

<style lang="scss" scoped>
.editor-container {
  display: flex;
  gap: 20px;
  height: 100%;
}

.tinymce-editor {
  flex: 1;
  min-width: 0;
}

.catalog {
  width: 250px;
  background-color: #fff;
  border-left: 1px solid #e4e4e4;
  padding: 16px;
  height: calc(100vh - 130px);
  display: flex;
  flex-direction: column;
}

.catalog-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e4e4;
  flex-shrink: 0;
  height: 40px;
  display: flex;
  align-items: center;
}

.catalog-list {
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
  flex: 1;
  height: calc(100% - 56px); /* 40px标题高度 + 16px margin-bottom */
  min-height: 0;
}

/* 自定义滚动条样式 */
.catalog-list::-webkit-scrollbar {
  width: 6px;
}

.catalog-list::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 3px;
}

.catalog-list::-webkit-scrollbar-track {
  background-color: transparent;
}

.catalog-item {
  color: #666;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  user-select: none;
}

.catalog-item:hover {
  color: #666;
}

.catalog-item.level-1 {
  font-weight: bold;
}

.catalog-item.level-2 {
  padding-left: 16px;
}

.catalog-item.level-3 {
  padding-left: 32px;
  font-size: 14px;
}
</style>
