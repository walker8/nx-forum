<template>
  <div class="nx-editor">
    <!-- 工具栏在最顶部 -->
    <div class="editor-toolbar" v-if="editor" ref="toolbarRef">
      <div class="toolbar-group">
        <button class="toolbar-btn" data-tooltip="撤销 (Cmd/Ctrl + Z)" @click="editor.chain().focus().undo().run()"
          :disabled="!editor.can().chain().focus().undo().run()">
          <Icon name="tabler:arrow-back-up" />
        </button>
        <button class="toolbar-btn" data-tooltip="重做 (Cmd/Ctrl + Shift + Z)"
          @click="editor.chain().focus().redo().run()" :disabled="!editor.can().chain().focus().redo().run()">
          <Icon name="tabler:arrow-forward-up" />
        </button>
        <button class="toolbar-btn" data-tooltip="清除格式" @click="editor.chain().focus().unsetAllMarks().run()">
          <Icon name="tabler:clear-formatting" />
        </button>
      </div>

      <div class="toolbar-divider" />

      <div class="toolbar-group">
        <el-dropdown trigger="click" @command="handleHeadingSelect">
          <button class="toolbar-btn heading-btn">
            <span class="heading-label">{{ currentHeadingLabel }}</span>
            <Icon name="tabler:chevron-down" class="chevron" />
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="level in [1, 2, 3, 4, 5, 6]" :key="level" :command="level"
                :class="{ active: editor.isActive('heading', { level }) }">
                H{{ level }}
              </el-dropdown-item>
              <el-dropdown-item :class="{ active: editor.isActive('paragraph') }" :command="0">
                段落
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="toolbar-divider" />

      <div class="toolbar-group">
        <button class="toolbar-btn" :class="{ active: editor.isActive('bold') }" data-tooltip="粗体 (Cmd/Ctrl + B)"
          @click="editor.chain().focus().toggleBold().run()">
          <Icon name="tabler:bold" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('italic') }" data-tooltip="斜体 (Cmd/Ctrl + I)"
          @click="editor.chain().focus().toggleItalic().run()">
          <Icon name="tabler:italic" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('underline') }" data-tooltip="下划线 (Cmd/Ctrl + U)"
          @click="editor.chain().focus().toggleUnderline().run()">
          <Icon name="tabler:underline" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('strike') }"
          data-tooltip="删除线 (Cmd/Ctrl + Shift + X)" @click="editor.chain().focus().toggleStrike().run()">
          <Icon name="tabler:strikethrough" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('code') }" data-tooltip="行内代码"
          @click="editor.chain().focus().toggleCode().run()">
          <Icon name="tabler:code" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('subscript') }" data-tooltip="下标"
          @click="editor.chain().focus().toggleSubscript().run()">
          <Icon name="tabler:subscript" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('superscript') }" data-tooltip="上标"
          @click="editor.chain().focus().toggleSuperscript().run()">
          <Icon name="tabler:superscript" />
        </button>
      </div>

      <div class="toolbar-divider" />

      <div class="toolbar-group">
        <button class="toolbar-btn" :class="{ active: editor.isActive('bulletList') }" data-tooltip="无序列表"
          @click="editor.chain().focus().toggleBulletList().run()">
          <Icon name="tabler:list" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('orderedList') }" data-tooltip="有序列表"
          @click="editor.chain().focus().toggleOrderedList().run()">
          <Icon name="tabler:list-numbers" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('taskList') }" data-tooltip="任务列表"
          @click="editor.chain().focus().toggleTaskList().run()">
          <Icon name="tabler:checkbox" />
        </button>
      </div>

      <div class="toolbar-divider" />

      <div class="toolbar-group">
        <button class="toolbar-btn" data-tooltip="左对齐" @click="editor.chain().focus().setTextAlign('left').run()"
          :class="{ active: editor.isActive({ textAlign: 'left' }) }">
          <Icon name="tabler:align-left" />
        </button>
        <button class="toolbar-btn" data-tooltip="居中对齐" @click="editor.chain().focus().setTextAlign('center').run()"
          :class="{ active: editor.isActive({ textAlign: 'center' }) }">
          <Icon name="tabler:align-center" />
        </button>
        <button class="toolbar-btn" data-tooltip="右对齐" @click="editor.chain().focus().setTextAlign('right').run()"
          :class="{ active: editor.isActive({ textAlign: 'right' }) }">
          <Icon name="tabler:align-right" />
        </button>
      </div>

      <div class="toolbar-divider" />

      <div class="toolbar-group">
        <button class="toolbar-btn" :class="{ active: editor.isActive('blockquote') }" data-tooltip="引用"
          @click="editor.chain().focus().toggleBlockquote().run()">
          <Icon name="tabler:blockquote" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('table') }" data-tooltip="插入表格"
          @click="editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()">
          <Icon name="tabler:table" />
        </button>
        <button class="toolbar-btn" :class="{ active: editor.isActive('codeBlock') }" data-tooltip="代码块"
          @click="editor.chain().focus().toggleCodeBlock().run()">
          <Icon name="tabler:code-dots" />
        </button>
        <button class="toolbar-btn" data-tooltip="分隔线" @click="editor.chain().focus().setHorizontalRule().run()">
          <Icon name="tabler:separator" />
        </button>
      </div>

      <div class="toolbar-divider" />

      <div class="toolbar-group">
        <button class="toolbar-btn" data-tooltip="插入图片" @click="handleImageSelect">
          <Icon name="tabler:photo" />
        </button>
        <!-- 暂时隐藏视频、音频、附件功能，后端不支持 -->
        <!-- <button class="toolbar-btn" data-tooltip="插入视频" @click="handleVideoSelect">
          <Icon name="tabler:video" />
        </button>
        <button class="toolbar-btn" data-tooltip="插入音频" @click="handleAudioSelect">
          <Icon name="tabler:music" />
        </button>
        <button class="toolbar-btn" data-tooltip="插入附件" @click="attachmentDialogVisible = true">
          <Icon name="tabler:paperclip" />
        </button> -->
        <button class="toolbar-btn" :class="{ active: editor.isActive('link') }" data-tooltip="插入链接 (Cmd/Ctrl + K)"
          @click="handleLinkInsert">
          <Icon name="tabler:link" />
        </button>
      </div>

      <div class="toolbar-spacer" />

      <div class="toolbar-group">
        <button v-if="outline.length > 0" class="toolbar-btn" data-tooltip="显示/隐藏侧栏" @click="toggleSidebar">
          <Icon :name="sidebarVisible ? 'tabler:layout-sidebar-right-collapse' : 'tabler:layout-sidebar-right'" />
        </button>
      </div>
    </div>
    <!-- 工作区:标题、编辑器内容和侧边栏 -->
    <div class="editor-workspace">
      <div class="editor-main">
        <!-- 标题输入 -->
        <div class="editor-title-wrapper">
          <input ref="titleInputRef" v-model="localTitle" class="title-input" maxlength="80" placeholder="请输入标题"
            @keydown.enter="handleTitleEnter" />
        </div>

        <!-- BubbleMenu -->
        <BubbleMenu v-if="editor" class="bubble-menu" :editor="editor" :tippyOptions="{ duration: 150 }"
          :shouldShow="shouldShowBubbleMenu">
          <div class="bubble-inner">
            <button class="bubble-btn" :class="{ active: editor.isActive('bold') }"
              @click="editor.chain().focus().toggleBold().run()">
              <Icon name="tabler:bold" />
            </button>
            <button class="bubble-btn" :class="{ active: editor.isActive('italic') }"
              @click="editor.chain().focus().toggleItalic().run()">
              <Icon name="tabler:italic" />
            </button>
            <button class="bubble-btn" :class="{ active: editor.isActive('underline') }"
              @click="editor.chain().focus().toggleUnderline().run()">
              <Icon name="tabler:underline" />
            </button>
            <button class="bubble-btn" :class="{ active: editor.isActive('strike') }"
              @click="editor.chain().focus().toggleStrike().run()">
              <Icon name="tabler:strikethrough" />
            </button>
            <div class="bubble-divider" />
            <button class="bubble-btn" :class="{ active: editor.isActive('code') }"
              @click="editor.chain().focus().toggleCode().run()">
              <Icon name="tabler:code" />
            </button>
            <button class="bubble-btn" :class="{ active: editor.isActive('superscript') }"
              @click="editor.chain().focus().toggleSuperscript().run()">
              <Icon name="tabler:superscript" />
            </button>
            <button class="bubble-btn" :class="{ active: editor.isActive('subscript') }"
              @click="editor.chain().focus().toggleSubscript().run()">
              <Icon name="tabler:subscript" />
            </button>
            <div class="bubble-divider" />
            <button class="bubble-btn" data-tooltip="清除格式" @click="editor.chain().focus().unsetAllMarks().run()">
              <Icon name="tabler:clear-formatting" />
            </button>
          </div>
        </BubbleMenu>

        <!-- 表格操作菜单已移至 Teleport 部分，固定在表格上方 -->


        <!-- 编辑器内容 -->
        <div class="editor-content" ref="editorContentRef">
          <EditorContent v-if="editor" :editor="editor" />
          <div v-else class="editor-skeleton">
            <el-skeleton :rows="8" animated />
          </div>
        </div>
      </div>

      <!-- 侧栏 -->
      <div class="editor-sidebar" v-if="sidebarVisible && outline.length > 0">
        <el-tabs v-model="activeSidebarTab" class="sidebar-tabs">
          <el-tab-pane label="目录" name="outline">
            <div class="sidebar-content">
              <div class="outline-list">
                <div v-for="(item, index) in outline" :key="index" class="outline-item"
                  :class="{ active: item.id === activeHeadingId }"
                  :style="{ paddingLeft: `${(item.level - 1) * 12 + 12}px` }" @click="scrollToHeading(item.id)">
                  {{ item.text }}
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>

  <Teleport to="body">
    <div v-if="slashState.open" class="slash-overlay"
      :class="{ 'slash-overlay-top': slashState.placement === 'top' }"
      :style="{ top: `${slashState.coords.y}px`, left: `${slashState.coords.x}px` }" ref="slashPanelRef">
      <input ref="slashInputRef" v-model="slashState.search" type="text" class="slash-search" placeholder="搜索命令" />
      <div class="slash-list">
        <button v-for="(item, index) in filteredSlashCommands" :key="item.id"
          :class="['slash-item', { active: index === slashState.index }]" @click="runSlashCommand(item)">
          <Icon :name="item.icon" class="icon" />
          <div class="info">
            <div class="label">{{ item.label }}</div>
            <div class="desc">{{ item.description }}</div>
          </div>
        </button>
        <div v-if="!filteredSlashCommands.length" class="slash-empty">没有匹配的命令</div>
      </div>
    </div>

    <!-- 表格操作菜单 - 固定在表格上方 -->
    <div v-if="tableMenuState.visible" class="table-menu-overlay"
      :style="{ top: `${tableMenuState.top}px`, left: `${tableMenuState.left}px` }">
      <div class="bubble-inner table-bubble-inner">
        <button class="bubble-btn" data-tooltip="在上方插入行" @click="editor?.chain().focus().addRowBefore().run()">
          <Icon name="tabler:row-insert-top" />
        </button>
        <button class="bubble-btn" data-tooltip="在下方插入行" @click="editor?.chain().focus().addRowAfter().run()">
          <Icon name="tabler:row-insert-bottom" />
        </button>
        <button class="bubble-btn" data-tooltip="删除行" @click="editor?.chain().focus().deleteRow().run()">
          <Icon name="tabler:row-remove" />
        </button>
        <div class="bubble-divider" />
        <button class="bubble-btn" data-tooltip="在左侧插入列" @click="editor?.chain().focus().addColumnBefore().run()">
          <Icon name="tabler:column-insert-left" />
        </button>
        <button class="bubble-btn" data-tooltip="在右侧插入列" @click="editor?.chain().focus().addColumnAfter().run()">
          <Icon name="tabler:column-insert-right" />
        </button>
        <button class="bubble-btn" data-tooltip="删除列" @click="editor?.chain().focus().deleteColumn().run()">
          <Icon name="tabler:column-remove" />
        </button>
        <div class="bubble-divider" />
        <button class="bubble-btn" data-tooltip="合并单元格" @click="editor?.chain().focus().mergeCells().run()">
          <Icon name="tabler:table-options" />
        </button>
        <button class="bubble-btn" data-tooltip="拆分单元格" @click="editor?.chain().focus().splitCell().run()">
          <Icon name="tabler:table-split" />
        </button>
        <div class="bubble-divider" />
        <button class="bubble-btn bubble-btn-danger" data-tooltip="删除表格"
          @click="editor?.chain().focus().deleteTable().run()">
          <Icon name="tabler:trash" />
        </button>
      </div>
    </div>
  </Teleport>

  <!-- 暂时隐藏附件功能，后端不支持 -->
  <!-- <Suspense>
    <component :is="AttachmentModal" v-model="attachmentDialogVisible" :uploading="attachmentUpload.uploading.value"
      :progress="attachmentUpload.progress.value" :error="attachmentUpload.errorMessage.value"
      @select="handleAttachmentSelect" />
  </Suspense> -->

  <!-- 链接插入对话框 -->
  <el-dialog v-model="linkDialogVisible" title="插入链接" width="500px" :close-on-click-modal="false">
    <el-form :model="linkForm" label-width="80px" @submit.prevent="handleLinkConfirm">
      <el-form-item label="链接文字">
        <el-input v-model="linkForm.text" placeholder="请输入链接文字（可选）" />
      </el-form-item>
      <el-form-item label="链接地址" required>
        <el-input v-model="linkForm.url" placeholder="https://example.com" autofocus />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="linkDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleLinkConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { BubbleMenu } from '@tiptap/vue-3/menus'
import { EditorContent, isTextSelection } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Image from './extensions/image'
import Upload from './extensions/upload'
// 暂时隐藏视频、音频功能，后端不支持
// import Video from './extensions/video'
// import Audio from './extensions/audio'
import Link from '@tiptap/extension-link'
import CharacterCount from '@tiptap/extension-character-count'
import { Table } from '@tiptap/extension-table'
import { TableCell } from '@tiptap/extension-table-cell'
import { TableHeader } from '@tiptap/extension-table-header'
import { TableRow } from '@tiptap/extension-table-row'
import CodeBlockExtension from './extensions/code-block'
import Alert from './extensions/alert'
import MermaidExtension from './extensions/mermaid'
import Heading from '@tiptap/extension-heading'
import TaskList from '@tiptap/extension-task-list'
import TaskItem from '@tiptap/extension-task-item'
import Underline from '@tiptap/extension-underline'
import Strike from '@tiptap/extension-strike'
import Subscript from '@tiptap/extension-subscript'
import Superscript from '@tiptap/extension-superscript'
import { TextStyle } from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import TextAlign from '@tiptap/extension-text-align'
import { Extension, type Range } from '@tiptap/core'
import { Editor } from '@tiptap/vue-3'
import Suggestion from '@tiptap/suggestion'
import { PluginKey } from '@tiptap/pm/state'
import { createLowlight, common } from 'lowlight'
import { ElMessage, ElNotification } from 'element-plus'
import { defineAsyncComponent, computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef, watch } from 'vue'
import { useBreakpoints, useLocalStorage } from '@vueuse/core'
import { useEditorOutline, type OutlineItem } from '~/composables/useEditorOutline'
import { useEditorStats } from '~/composables/useEditorStats'
import { useEditorUpload } from '~/composables/useEditorUpload'

// 代码块语法高亮器（用于代码块的语法着色）
const codeHighlighter = createLowlight(common)
const supportedCodeLanguages = [
  { label: 'Auto', value: 'auto' },
  { label: 'Plain Text', value: 'plaintext' },
  { label: 'JavaScript', value: 'javascript' },
  { label: 'TypeScript', value: 'typescript' },
  { label: 'Python', value: 'python' },
  { label: 'Java', value: 'java' },
  { label: 'C++', value: 'cpp' },
  { label: 'C#', value: 'csharp' },
  { label: 'PHP', value: 'php' },
  { label: 'SQL', value: 'sql' },
  { label: 'CSS', value: 'css' },
  { label: 'Bash', value: 'bash' },
  { label: 'JSON', value: 'json' },
  { label: 'XML', value: 'xml' }
]
const supportedLanguageValues = new Set(supportedCodeLanguages.map((item) => item.value))

interface EditorMeta {
  author?: string
  publishTime?: string
  permalink?: string
}

const SidebarPanel = defineAsyncComponent(() => import('./editor-sidebar.vue'))
// 暂时隐藏附件功能，后端不支持
// const AttachmentModal = defineAsyncComponent(() => import('./editor-attachment-modal.vue'))

const props = withDefaults(
  defineProps<{
    modelValue?: string
    title?: string
    placeholder?: string
    initialContent?: string
    cover?: string | null
    meta?: EditorMeta
    stickyHeader?: boolean
  }>(),
  {
    modelValue: '',
    title: '',
    placeholder: '开始创作，输入 / 快速插入模块',
    initialContent: '',
    cover: null,
    stickyHeader: true
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'update:title': [value: string]
  'update:cover': [value: string | null]
  'update:outline': [items: OutlineItem[]]
  'upload:error': [message: string]
}>()

const editor = shallowRef<Editor | null>(null)
const localTitle = ref(props.title ?? '')
const titleInputRef = ref<HTMLInputElement | null>(null)
const editorContentRef = ref<HTMLElement | null>(null)
const toolbarRef = ref<HTMLElement | null>(null)
const currentHeadingLabel = ref('段落')

watch(
  () => props.title,
  (value) => {
    if (value !== undefined && value !== localTitle.value) {
      localTitle.value = value
    }
  }
)

watch(localTitle, (value) => {
  emit('update:title', value)
})



const headingExtension = Heading.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      id: {
        default: null,
        parseHTML: (element) => element.getAttribute('id'),
        renderHTML: (attributes) => (attributes.id ? { id: attributes.id } : {})
      }
    }
  }
})

const slashState = reactive({
  open: false,
  search: '',
  index: 0,

  coords: { x: 0, y: 0 },
  props: null as any,
  placement: 'bottom' as 'top' | 'bottom' // 记录菜单位置
})

const slashPanelRef = ref<HTMLElement | null>(null)
const slashInputRef = ref<HTMLInputElement | null>(null)

// Markdown 粘贴处理状态
const pendingMarkdownText = ref<string | null>(null)
const pendingMarkdownRange = ref<{ from: number; to: number } | null>(null)
let markdownNotify: ReturnType<typeof ElNotification> | null = null

const looksLikeMarkdown = (text: string) => {
  const trimmed = text.trim()
  if (!trimmed) return false
  // 需要至少含有典型 Markdown 语法片段
  const patterns = [
    /^#{1,6}\s/m, // 标题
    /(^|\n)([-*+])\s+\S/m, // 无序列表
    /(^|\n)\d+\.\s+\S/m, // 有序列表
    /`{3}[\s\S]*?`{3}/m, // 代码块
    />\s+\S/m, // 引用
    /\[.+?\]\(.+?\)/ // 链接
  ]
  return patterns.some((p) => p.test(text))
}

const markdownToHtml = async (markdown: string) => {
  if (!markdown) return ''
  try {
    const { marked } = await import('marked')
    // 使用 marked 解析 Markdown，开启 gfm (支持表格) 和 breaks (支持单换行转 <br>)
    const html = await marked.parse(markdown, {
      gfm: true,
      breaks: true
    }) as string

    // 适配 Tiptap 的代码块属性
    const container = document.createElement('div')
    container.innerHTML = html

    const codeNodes = container.querySelectorAll('pre > code')
    const mermaidBlocks: Array<{ pre: HTMLElement; code: string }> = []

    codeNodes.forEach((codeEl) => {
      // 从 class 中提取语言
      const className = codeEl.className || ''
      const match = className.match(/language-(\S+)/)
      const lang = match ? match[1].toLowerCase() : 'plaintext'

      // 检测是否为 mermaid 代码块
      if (lang === 'mermaid') {
        const pre = codeEl.parentElement as HTMLElement
        if (pre) {
          mermaidBlocks.push({ pre, code: codeEl.textContent || '' })
        }
      } else {
        const resolvedLang = supportedLanguageValues.has(lang) ? lang : 'plaintext'
        // 确保类名格式统一并添加 Tiptap 所需的 data-language 属性
        codeEl.className = `language-${resolvedLang}`
        ;(codeEl as HTMLElement).setAttribute('data-language', resolvedLang)
      }
    })

    // 将 mermaid 代码块转换为 mermaid 节点
    mermaidBlocks.forEach(({ pre, code }) => {
      const mermaidDiv = document.createElement('div')
      mermaidDiv.setAttribute('data-type', 'mermaid')
      mermaidDiv.setAttribute('data-code', code)
      mermaidDiv.setAttribute('data-collapsed', 'false')
      pre.replaceWith(mermaidDiv)
    })

    return container.innerHTML
  } catch (error) {
    console.error('Markdown 转 HTML 失败:', error)
    return ''
  }
}

const closeMarkdownNotify = () => {
  if (markdownNotify) {
    markdownNotify.close()
    markdownNotify = null
  }
}

const insertPlainText = (text: string) => {
  if (!editor.value) return
  editor.value.chain().focus().insertContent(text).run()
}

const handleMarkdownConvert = async () => {
  if (!pendingMarkdownText.value || !editor.value) return
  const html = (await markdownToHtml(pendingMarkdownText.value)) || pendingMarkdownText.value

  const range = pendingMarkdownRange.value
  const chain = editor.value.chain().focus()
  if (range && range.from >= 0 && range.to >= range.from) {
    chain.setTextSelection(range)
  }
  chain.insertContent(html).run()

  closeMarkdownNotify()
  pendingMarkdownText.value = null
  pendingMarkdownRange.value = null
}

const handleMarkdownKeep = () => {
  if (!pendingMarkdownText.value) return
  closeMarkdownNotify()
  pendingMarkdownText.value = null
  pendingMarkdownRange.value = null
}

const showMarkdownToast = (text: string) => {
  pendingMarkdownText.value = text
  closeMarkdownNotify()
  markdownNotify = ElNotification({
    title: '是否需要做样式转换?',
    position: 'top-right',
    duration: 0,
    offset: 20,
    customClass: 'markdown-paste-notify',
    message: h('div', { class: 'markdown-paste-message' }, [
      h('p', { class: 'notify-desc' }, '检测到粘贴内容符合 Markdown 语法，是否需要做样式转换？'),
      h('div', { class: 'notify-actions' }, [
        h(
          'button',
          {
            class: 'notify-btn primary',
            onClick: () => handleMarkdownConvert()
          },
          '立即转换'
        ),
        h(
          'button',
          {
            class: 'notify-btn',
            onClick: () => handleMarkdownKeep()
          },
          '保持原文'
        )
      ])
    ]),
    onClose: () => {
      pendingMarkdownText.value = null
      pendingMarkdownRange.value = null
      markdownNotify = null
    }
  })
}

const handleMarkdownPaste = (_view: any, event: ClipboardEvent) => {
  const data = event.clipboardData
  if (!data) return false
  if (data.files && data.files.length > 0) return false

  const html = data.getData('text/html')
  const text = data.getData('text/plain') || ''

  // 已有 HTML 或不符合 Markdown 特征则交由默认处理
  if (html || !looksLikeMarkdown(text)) return false

  event.preventDefault()
  if (editor.value) {
    const { from } = editor.value.state.selection
    editor.value.chain().focus().insertContent(text).run()
    pendingMarkdownRange.value = { from, to: from + text.length }
  }
  showMarkdownToast(text)
  return true
}

const shouldShowBubbleMenu = ({ editor, state, from, to }: any) => {
  // 如果选中了图片，不显示文字菜单
  // 暂时隐藏视频功能，后端不支持
  if (editor.isActive('image')) {
    return false
  }

  // 默认行为：只有选中文字且非空时显示
  const { doc, selection } = state
  const { empty } = selection
  const isEmptyTextBlock = !doc.textBetween(from, to).length && isTextSelection(selection)

  return !empty && !isEmptyTextBlock && editor.isEditable
}

const breakpoints = useBreakpoints({
  desktop: 1024
})
const isDesktop = breakpoints.greaterOrEqual('desktop')
const sidebarStorage = useLocalStorage('nx:editor:show-sidebar', false)
const activeSidebarTab = ref('outline')
const mobileSidebar = ref(false)
const sidebarVisible = computed(() => (isDesktop.value ? sidebarStorage.value : mobileSidebar.value))
const isFullscreen = ref(false)

const toggleSidebar = () => {
  if (isDesktop.value) {
    sidebarStorage.value = !sidebarStorage.value
  } else {
    mobileSidebar.value = !mobileSidebar.value
  }
}

watch(isDesktop, (value) => {
  if (!value) {
    mobileSidebar.value = false
  }
})

// 表格菜单状态 - 固定在表格上方
const tableMenuState = reactive({
  visible: false,
  top: 0,
  left: 0
})

// 链接对话框状态
const linkDialogVisible = ref(false)
const linkForm = reactive({
  text: '',
  url: ''
})


const slashCommands = [
  ...[1, 2, 3].map((level) => ({
    id: `heading-${level}`,
    label: `标题 H${level}`,
    description: '突出章节层级',
    icon: 'tabler:heading',
    action: (instance: Editor) => instance.chain().focus().toggleHeading({ level: level as 1 | 2 | 3 | 4 | 5 | 6 }).run()
  })),
  {
    id: 'blockquote',
    label: '引用',
    description: '引用语句或提示信息',
    icon: 'tabler:blockquote',
    action: (instance: Editor) => instance.chain().focus().toggleBlockquote().run()
  },
  {
    id: 'bullet-list',
    label: '无序列表',
    description: '展示要点列表',
    icon: 'tabler:list',
    action: (instance: Editor) => instance.chain().focus().toggleBulletList().run()
  },
  {
    id: 'ordered-list',
    label: '有序列表',
    description: '步骤说明、排名',
    icon: 'tabler:list-numbers',
    action: (instance: Editor) => instance.chain().focus().toggleOrderedList().run()
  },
  {
    id: 'table',
    label: '表格',
    description: '插入表格',
    icon: 'tabler:table',
    action: (instance: Editor) => instance.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
  },
  {
    id: 'code-block',
    label: '代码块',
    description: '插入代码示例',
    icon: 'tabler:code',
    action: (instance: Editor) => instance.chain().focus().toggleCodeBlock().run()
  },
  {
    id: 'divider',
    label: '分隔线',
    description: '让段落更有节奏',
    icon: 'tabler:separator',
    action: (instance: Editor) => instance.chain().focus().setHorizontalRule().run()
  },
  {
    id: 'image',
    label: '图片',
    description: '插入本地或网络图片',
    icon: 'tabler:photo',
    action: () => handleImageSelect()
  },
  {
    id: 'alert-info',
    label: '提示信息',
    description: '插入灰色提示块',
    icon: 'tabler:info-circle',
    action: (instance: Editor) => instance.chain().focus().setAlert({ type: 'info' }).run()
  },
  {
    id: 'alert-success',
    label: '成功提示',
    description: '插入绿色成功块',
    icon: 'tabler:circle-check',
    action: (instance: Editor) => instance.chain().focus().setAlert({ type: 'success' }).run()
  },
  {
    id: 'alert-warning',
    label: '警告提示',
    description: '插入黄色警告块',
    icon: 'tabler:alert-triangle',
    action: (instance: Editor) => instance.chain().focus().setAlert({ type: 'warning' }).run()
  },
  {
    id: 'alert-error',
    label: '错误提示',
    description: '插入红色错误块',
    icon: 'tabler:alert-circle',
    action: (instance: Editor) => instance.chain().focus().setAlert({ type: 'error' }).run()
  },
  {
    id: 'mermaid',
    label: 'Mermaid图表',
    description: '插入流程图、时序图等图表',
    icon: 'tabler:chart-dots',
    action: () => insertMermaidDiagram()
  }
  // 暂时隐藏附件功能，后端不支持
  // {
  //   id: 'attachment',
  //   label: '附件',
  //   description: '上传 PDF/附件链接',
  //   icon: 'tabler:paperclip',
  //   action: () => {
  //     attachmentDialogVisible.value = true
  //   }
  // }
]

const filteredSlashCommands = computed(() => {
  if (!slashState.search) {
    return slashCommands
  }
  return slashCommands.filter((command) =>
    command.label.toLowerCase().includes(slashState.search.toLowerCase())
  )
})

const openSlashMenu = () => {
  if (!editor.value) return
  // 插入 / 字符来触发 suggestion 插件
  editor.value.chain().focus().insertContent('/').run()
}

const closeSlashMenu = () => {
  slashState.open = false
}

const SlashCommandExtension = Extension.create({
  name: 'slash-command-trigger',
  addProseMirrorPlugins() {
    return [
      Suggestion({
        pluginKey: new PluginKey('slash-command'),
        editor: this.editor,
        char: '/',
        startOfLine: false,
        allowSpaces: false,
        allowedPrefixes: null,
        allow: ({ state, range }) => {
          const $from = state.doc.resolve(range.from)
          const textBefore = $from.parent.textContent.slice(0, $from.parentOffset)
          // 只在行内没有其他文本时触发（去除 / 字符）
          return textBefore.trim() === '' || textBefore.trim() === '/'
        },
        command: ({ editor, range, props }) => {
          // 删除触发字符 /
          editor.chain().focus().deleteRange(range).run()
          // 执行命令
          if (props && typeof props.action === 'function') {
            props.action(editor)
          }
        },
        items: ({ query }) => {
          return filteredSlashCommands.value
        },
        render: () => {
          return {
            onStart: (props) => {
              const rect = props.clientRect?.()
              if (rect) {
                // 估算菜单高度：搜索框(40px) + 列表(280px) + padding(24px) ≈ 344px
                const menuHeight = 344
                const viewportHeight = window.innerHeight
                const spaceBelow = viewportHeight - rect.bottom
                const spaceAbove = rect.top

                // 如果下方空间不足，且上方空间更多，则显示在上方
                const showAbove = spaceBelow < menuHeight && spaceAbove > spaceBelow

                slashState.coords = {
                  x: rect.left,
                  y: showAbove ? rect.top - menuHeight - 8 : rect.bottom + 8
                }
                slashState.placement = showAbove ? 'top' : 'bottom'
              }
              slashState.search = props.query || ''
              slashState.index = 0
              slashState.open = true
              nextTick(() => {
                slashInputRef.value?.focus()
              })
              slashState.props = props
            },
            onUpdate: (props) => {
              const rect = props.clientRect?.()
              if (rect) {
                // 估算菜单高度：搜索框(40px) + 列表(280px) + padding(24px) ≈ 344px
                const menuHeight = 344
                const viewportHeight = window.innerHeight
                const spaceBelow = viewportHeight - rect.bottom
                const spaceAbove = rect.top

                // 如果下方空间不足，且上方空间更多，则显示在上方
                const showAbove = spaceBelow < menuHeight && spaceAbove > spaceBelow

                slashState.coords = {
                  x: rect.left,
                  y: showAbove ? rect.top - menuHeight - 8 : rect.bottom + 8
                }
                slashState.placement = showAbove ? 'top' : 'bottom'
              }
              slashState.search = props.query || ''
            },
            onKeyDown: ({ event }) => {
              if (!slashState.open) return false
              const total = filteredSlashCommands.value.length
              if (!total) {
                if (event.key === 'Escape') {
                  event.preventDefault()
                  closeSlashMenu()
                  return true
                }
                return false
              }
              if (event.key === 'ArrowDown') {
                event.preventDefault()
                slashState.index = (slashState.index + 1) % total
                return true
              } else if (event.key === 'ArrowUp') {
                event.preventDefault()
                slashState.index = (slashState.index - 1 + total) % total
                return true
              } else if (event.key === 'Enter') {
                event.preventDefault()
                const command = filteredSlashCommands.value[slashState.index]
                if (command) {
                  runSlashCommand(command)
                }
                return true
              } else if (event.key === 'Escape') {
                event.preventDefault()
                closeSlashMenu()
                return true
              }
              return false
            },
            onExit: () => {
              closeSlashMenu()
            }
          }
        }
      })
    ]
  },
  addKeyboardShortcuts() {
    return {
      'Mod-/': () => {
        openSlashMenu()
        return true
      }
    }
  }
})

const attachmentUpload = useEditorUpload({
  maxSizeMB: 10,
  onError: (message) => emit('upload:error', message)
})



const handleTitleEnter = (event: KeyboardEvent) => {
  event.preventDefault()
  editor.value?.commands.focus('start')
}

const updateHeadingLabel = () => {
  const instance = editor.value
  if (!instance) {
    currentHeadingLabel.value = '段落'
    return
  }

  for (const level of [1, 2, 3, 4, 5, 6] as const) {
    if (instance.isActive('heading', { level })) {
      currentHeadingLabel.value = `H${level}`
      return
    }
  }

  currentHeadingLabel.value = '段落'
}

const handleHeadingSelect = (level: number) => {
  if (!editor.value) return
  if (level === 0) {
    editor.value.chain().focus().setParagraph().run()
  } else {
    editor.value.chain().focus().toggleHeading({ level: level as 1 | 2 | 3 | 4 | 5 | 6 }).run()
  }
}

const handleLinkInsert = () => {
  if (!editor.value) return

  // 如果已经是链接，则移除链接
  if (editor.value.isActive('link')) {
    editor.value.chain().focus().unsetLink().run()
    return
  }

  // 获取选中的文字作为默认链接文字
  const { from, to } = editor.value.state.selection
  const selectedText = editor.value.state.doc.textBetween(from, to, '')

  // 重置表单并打开对话框
  linkForm.text = selectedText
  linkForm.url = ''
  linkDialogVisible.value = true
}

const handleLinkConfirm = () => {
  if (!editor.value || !linkForm.url.trim()) {
    ElMessage.warning('请输入链接地址')
    return
  }

  // 如果有链接文字且与选中文字不同，则替换文字
  if (linkForm.text.trim() && linkForm.text !== editor.value.state.doc.textBetween(
    editor.value.state.selection.from,
    editor.value.state.selection.to,
    ''
  )) {
    editor.value
      .chain()
      .focus()
      .insertContent(linkForm.text)
      .setLink({ href: linkForm.url.trim(), target: '_blank' })
      .run()
  } else {
    // 否则只添加链接
    editor.value
      .chain()
      .focus()
      .setLink({ href: linkForm.url.trim(), target: '_blank' })
      .run()
  }

  linkDialogVisible.value = false
}

const handleImageSelect = () => {
  attachmentUpload.openFileDialog({
    accept: 'image/*',
    onSuccess: (result) => {
      if (!editor.value) return
      editor.value.chain().focus().setImage({ src: result.url, alt: result.fileName }).run()
      ElMessage.success('图片已插入')
    }
  })
}

const insertMermaidDiagram = () => {
  if (!editor.value) return
  editor.value.chain().focus().insertMermaid().run()
}

// 暂时隐藏视频、音频、附件功能，后端不支持
// const handleVideoSelect = () => {
//   attachmentUpload.openFileDialog({
//     accept: 'video/*',
//     onSuccess: (result) => {
//       if (!editor.value) return
//       editor.value.chain().focus().setVideo({ src: result.url }).run()
//       ElMessage.success('视频已插入')
//     }
//   })
// }

// const handleAudioSelect = () => {
//   attachmentUpload.openFileDialog({
//     accept: 'audio/*',
//     onSuccess: (result) => {
//       if (!editor.value) return
//       editor.value.chain().focus().setAudio({ src: result.url }).run()
//       ElMessage.success('音频已插入')
//     }
//   })
// }

// const handleAttachmentSelect = () => {
//   attachmentUpload.openFileDialog({
//     accept: '*/*',
//     onSuccess: (result) => {
//       if (!editor.value) return
//       if (result.mime?.startsWith('image/')) {
//         editor.value.chain().focus().setImage({ src: result.url, alt: result.fileName }).run()
//       } else {
//         editor.value
//           .chain()
//           .focus()
//           .insertContent(
//             `<a class="editor-attachment" href="${result.url}" target="_blank" rel="noopener noreferrer">${result.fileName}</a>`
//           )
//           .run()
//       }
//       ElMessage.success('附件已插入')
//       attachmentDialogVisible.value = false
//     }
//   })
// }

const runSlashCommand = (command: (typeof slashCommands)[number]) => {
  if (slashState.props) {
    slashState.props.command(command)
  } else {
    if (!editor.value) return
    command.action(editor.value)
  }
  closeSlashMenu()
}



const handleClickOutside = (event: MouseEvent) => {
  if (!slashState.open) return
  if (slashPanelRef.value && !slashPanelRef.value.contains(event.target as Node)) {
    closeSlashMenu()
  }
}

// 处理工具栏按钮 tooltip 位置
const handleTooltipPosition = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const button = target.closest('.toolbar-btn[data-tooltip]') as HTMLElement
  if (!button) return

  const rect = button.getBoundingClientRect()
  const left = rect.left + rect.width / 2
  const top = rect.top

  button.style.setProperty('--tooltip-left', `${left}px`)
  button.style.setProperty('--tooltip-top', `${top}px`)
}

// 更新所有 tooltip 位置（用于滚动和窗口大小变化时）
const updateTooltipPositions = () => {
  if (!toolbarRef.value) return
  const buttons = toolbarRef.value.querySelectorAll('.toolbar-btn[data-tooltip]') as NodeListOf<HTMLElement>
  buttons.forEach((button) => {
    const rect = button.getBoundingClientRect()
    const left = rect.left + rect.width / 2
    const top = rect.top
    button.style.setProperty('--tooltip-left', `${left}px`)
    button.style.setProperty('--tooltip-top', `${top}px`)
  })
}

const initContent = (value: string) => {
  if (!editor.value) return
  editor.value.commands.setContent(value || '', { emitUpdate: false })
}

const getContent = () => editor.value?.getHTML() ?? ''

defineExpose({
  initContent,
  getContent
})

const createEditor = () => {
  editor.value = new Editor({
    content: props.initialContent || props.modelValue || '',
    autofocus: false,
    editorProps: {
      handlePaste: handleMarkdownPaste
    },
    extensions: [
      StarterKit.configure({
        heading: false,
        codeBlock: false,
        strike: false,
        link: false,
        underline: false
      }),
      headingExtension.configure({
        levels: [1, 2, 3, 4, 5, 6]
      }),
      Placeholder.configure({
        placeholder: props.placeholder
      }),
      Image.configure({
        inline: false,
        allowBase64: false,
        HTMLAttributes: {
          loading: 'lazy'
        }
      }),
      // 暂时隐藏视频、音频功能，后端不支持
      // Video,
      // Audio,
      Upload,
      Link.configure({
        autolink: true,
        openOnClick: false,
        HTMLAttributes: {
          rel: 'noopener noreferrer nofollow',
          target: '_blank'
        }
      }),
      CharacterCount.configure(),
      Table.configure({
        resizable: true
      }),
      TableRow,
      TableHeader,
      TableCell,
      TaskList.configure({
        HTMLAttributes: {
          class: 'tiptap-task-list'
        }
      }),
      TaskItem.configure({
        nested: true
      }),
      CodeBlockExtension.configure({
        lowlight: codeHighlighter,
        defaultLanguage: 'plaintext',
        languages: supportedCodeLanguages
      }),
      Alert,
      MermaidExtension,
      Underline,
      Strike,
      Subscript,
      Superscript,
      TextStyle,
      Color,
      TextAlign.configure({
        types: ['heading', 'paragraph'],
      }),
      SlashCommandExtension
    ],
    onUpdate: ({ editor }) => {
      emit('update:modelValue', editor.getHTML())
      updateTableMenuPosition()
      updateHeadingLabel()
    },
    onSelectionUpdate: () => {
      updateTableMenuPosition()
      updateHeadingLabel()
    }
  })
  updateHeadingLabel()
}

// 更新表格菜单位置
const updateTableMenuPosition = () => {
  if (!editor.value) return

  const isTableActive = editor.value.isActive('table')

  if (!isTableActive) {
    tableMenuState.visible = false
    return
  }

  // 通过编辑器的选区找到当前表格元素
  nextTick(() => {
    try {
      const { state } = editor.value!
      const { selection } = state
      const { $anchor } = selection

      // 向上查找表格节点
      let tableDepth = -1
      for (let d = $anchor.depth; d > 0; d--) {
        if ($anchor.node(d).type.name === 'table') {
          tableDepth = d
          break
        }
      }

      if (tableDepth === -1) {
        tableMenuState.visible = false
        return
      }

      // 获取表格节点的 DOM 位置
      const tablePos = $anchor.before(tableDepth)
      const tableDom = editor.value!.view.nodeDOM(tablePos)

      if (!tableDom || !(tableDom instanceof HTMLElement)) {
        tableMenuState.visible = false
        return
      }

      // 查找实际的 table 元素（可能被包裹在 div 中）
      const tableElement = tableDom.tagName === 'TABLE'
        ? tableDom
        : tableDom.querySelector('table')

      if (!tableElement) {
        tableMenuState.visible = false
        return
      }

      const rect = tableElement.getBoundingClientRect()
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop
      const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft

      // 计算菜单位置：表格上方，水平居中；当表头滚出视口/编辑器时，顶部位置保持在编辑器可视区域内
      const desiredTop = rect.top + scrollTop - 48 // 48px 是菜单高度 + 间距
      const editorRect = editorContentRef.value?.getBoundingClientRect()

      const viewportSafeTop = scrollTop + 12 // 视口顶部安全边距
      const editorSafeTop = editorRect ? editorRect.top + scrollTop + 12 : viewportSafeTop
      const editorSafeBottom = editorRect ? editorRect.bottom + scrollTop - 12 : Infinity

      let safeTop = Math.max(desiredTop, viewportSafeTop, editorSafeTop)
      if (safeTop > editorSafeBottom - 48) {
        safeTop = editorSafeBottom - 48
      }

      tableMenuState.top = safeTop
      tableMenuState.left = rect.left + scrollLeft + (rect.width / 2)
      tableMenuState.visible = true
    } catch (error) {
      console.error('Error updating table menu position:', error)
      tableMenuState.visible = false
    }
  })
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
  window.addEventListener('scroll', () => {
    updateTableMenuPosition()
    updateTooltipPositions()
  }, true)
  window.addEventListener('resize', () => {
    updateTableMenuPosition()
    updateTooltipPositions()
  })
  createEditor()
  // 编辑器创建后，自动聚焦到标题输入框
  nextTick(() => {
    titleInputRef.value?.focus()
    // 绑定工具栏 tooltip 位置事件
    if (toolbarRef.value) {
      toolbarRef.value.addEventListener('mouseenter', handleTooltipPosition, true)
    }
  })
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
  window.removeEventListener('scroll', updateTableMenuPosition, true)
  window.removeEventListener('resize', updateTableMenuPosition)
  if (toolbarRef.value) {
    toolbarRef.value.removeEventListener('mouseenter', handleTooltipPosition, true)
  }
  closeMarkdownNotify()
  editor.value?.destroy()
})

watch(
  () => props.modelValue,
  (value) => {
    if (!editor.value) return
    const html = value ?? ''
    if (html === editor.value.getHTML()) return
    editor.value.commands.setContent(html, { emitUpdate: false })
    updateHeadingLabel()
  }
)

const { outline, activeId: activeHeadingId, scrollToHeading } = useEditorOutline(editor, {
  onChange(items) {
    emit('update:outline', items)
  }
})
const { stats } = useEditorStats(editor)

// 暂时隐藏附件功能，后端不支持
// const attachmentDialogVisible = ref(false)

const meta = computed(() => props.meta)
</script>

<style scoped lang="scss">
.nx-editor {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  background-color: #f8f9fa;
  overflow: hidden;
  max-width: 1038px;
}

.editor-toolbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 8px 16px;
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  align-items: center;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;

  /* 美化滚动条 */
  &::-webkit-scrollbar {
    height: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 3px;

    &:hover {
      background: #94a3b8;
    }
  }

  /* Firefox 滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 transparent;
}

.toolbar-group {
  display: flex;
  gap: 2px;
  align-items: center;
  flex-shrink: 0;
}

.toolbar-divider {
  width: 1px;
  background: #e2e8f0;
  height: 20px;
  margin: 0 4px;
  flex-shrink: 0;
}

.toolbar-btn {
  border: none;
  background: transparent;
  border-radius: 6px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
  flex-shrink: 0;

  &.active {
    background: #eff6ff;
    color: #3b82f6;
  }

  &:hover:not(:disabled) {
    background: #f1f5f9;
    color: #1e293b;
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  /* 无延迟 tooltip */
  &[data-tooltip] {
    position: relative;
  }

  &[data-tooltip]:hover::after {
    content: attr(data-tooltip);
    position: fixed;
    top: var(--tooltip-top, 0);
    left: var(--tooltip-left, 0);
    transform: translateX(-50%) translateY(-100%);
    margin-top: -8px;
    background: #1e293b;
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    white-space: nowrap;
    pointer-events: none;
    z-index: 10001;
    animation: tooltipFadeIn 0.1s ease;
  }

  &[data-tooltip]:hover::before {
    content: '';
    position: fixed;
    top: var(--tooltip-top, 0);
    left: var(--tooltip-left, 0);
    transform: translateX(-50%);
    margin-top: -4px;
    border: 4px solid transparent;
    border-top-color: #1e293b;
    pointer-events: none;
    z-index: 10001;
  }
}

.heading-btn {
  width: auto;
  min-width: 68px;
  padding: 0 10px;
  gap: 6px;
  justify-content: center;
}

.heading-label {
  font-size: 14px;
  font-weight: 600;
  color: inherit;
}

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(4px);
  }

  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.chevron {
  font-size: 12px;
  margin-left: 2px;
}

.toolbar-spacer {
  flex: 1;
  min-width: 0;
}

.editor-workspace {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 0;
  overflow: hidden;
}

.editor-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow-y: auto;
  overflow-x: hidden;
}

.editor-title-wrapper {
  padding: 24px 0 16px;
  margin: 0 12px;
  border-bottom: 1px solid #f1f5f9;
}

.title-input {
  font-size: 32px;
  font-weight: 600;
  border: none;
  outline: none;
  width: 100%;
  background: transparent;
  color: #0f172a;
  line-height: 1.2;

  &::placeholder {
    color: #cbd5e1;
  }
}

.editor-content {
  flex: 1;
  padding: 12px;
  overflow-y: visible;

  :deep(.ProseMirror) {
    min-height: calc(100vh - 400px);
    font-size: 16px;
    line-height: 1.75;
    color: #333;
    margin: 0 auto;
    outline: none;
    /* 允许在单词内换行 */
    word-wrap: break-word;
    /* 单词可以在任意字符间断开 */
    word-break: break-word;
    /* 超出显示省略号 */
    white-space: pre-wrap;
    /* 保持所有文字在一个方块内 */
    overflow-wrap: break-word;

    &.ProseMirror-focused {
      outline: none;
    }

    /* Placeholder 样式 */
    p.is-editor-empty:first-child::before {
      content: attr(data-placeholder);
      float: left;
      color: #adb5bd;
      pointer-events: none;
      height: 0;
    }

    h1 {
      margin: 0 0 13px;
      font-size: 26px;
      font-weight: 600;
      line-height: 1.4;
      color: #1d2129;
      padding-bottom: 12px;
      border-bottom: 1px solid #e4e6eb;
    }

    h2 {
      font-size: 24px;
      font-weight: 600;
      line-height: 1.4;
      margin: 13px 0;
      color: #1d2129;
    }

    h3 {
      font-size: 20px;
      font-weight: 600;
      line-height: 1.4;
      margin: 10px 0;
      color: #1d2129;
    }

    h4 {
      font-size: 18px;
      font-weight: 600;
      line-height: 1.4;
      margin: 8px 0;
      color: #1d2129;
    }

    h5 {
      font-size: 16px;
      font-weight: 600;
      line-height: 1.4;
      margin: 8px 0;
      color: #1d2129;
    }

    h6 {
      font-size: 14px;
      font-weight: 600;
      line-height: 1.4;
      margin: 6px 0;
      color: #1d2129;
    }

    p {
      margin: 0.75em 0;
    }

    a {
      color: #3b82f6;
      text-decoration: underline;
      cursor: pointer;

      &:hover {
        color: #2563eb;
      }
    }

    blockquote {
      margin: 16px 0;
      padding: 12px 16px;
      background-color: #f8f9fa;
      border-left: 4px solid #409eff;
      color: #5a6270;
      font-size: 15px;
      line-height: 1.7;
      border-radius: 0 4px 4px 0;
      position: relative;
      overflow: hidden;

      p {
        margin: 0;
        position: relative;
      }

      p:not(:last-child) {
        margin-bottom: 8px;
      }

      blockquote {
        margin-left: 16px;
        border-left-color: #79bbff;
      }
    }

    code {
      background-color: #dfe0e1;
      padding: 2px 4px;
      margin: 0 3px;
      border-radius: 4px;
      font-size: 14px;
      color: #333;
      font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
    }

    pre {
      padding: 16px;
      border-radius: 8px;
      overflow-x: auto;
      font-size: 14px;
      line-height: 1.6;
      margin: 0 0;

      code {
        background: transparent;
        color: inherit;
        padding: 0;
        margin: 0;
        border-radius: 0;
        font-size: inherit;
      }
    }

    img {
      border-radius: 4px;
      margin: 1.5em 0;
      max-width: 100%;
      height: auto;
    }

    ul,
    ol {
      padding-left: 20px;
      margin: 12px 0;
    }

    ul {
      list-style-type: disc;
    }

    ol {
      list-style-type: decimal;
    }

    li {
      margin-bottom: 8px;
      line-height: 1.6;
      position: relative;

      p {
        margin: 0;
      }
    }

    ul ul,
    ol ul {
      list-style-type: circle;
      margin: 8px 0 8px 16px;
    }

    ul ol,
    ol ol {
      list-style-type: lower-alpha;
      margin: 8px 0 8px 16px;
    }

    ul ul ul,
    ol ul ul,
    ul ol ul,
    ol ol ul {
      list-style-type: square;
    }

    ul ol ol,
    ol ol ol,
    ul ul ol,
    ol ul ol {
      list-style-type: lower-roman;
    }

    /* 任务列表样式 */
    ul[data-type='taskList'] {
      list-style: none;
      padding-left: 0;

      li {
        display: flex;
        align-items: center;
        gap: 0.25em;
        margin: 0.25em 0;

        >label {
          display: flex;
          align-items: center;
          gap: 0.25em;
          cursor: pointer;
          user-select: none;
        }

        >label>div {
          flex: 1;
          text-align: left;
        }

        input[type='checkbox'] {
          width: 16px;
          height: 16px;
          cursor: pointer;
          flex-shrink: 0;
        }

        &[data-checked='true']>label>div {
          text-decoration: line-through;
          color: #94a3b8;
        }
      }
    }

    hr {
      border: none;
      border-top: 2px solid #e5e7eb;
      margin: 2em 0;
    }

    table {
      width: 100%;
      margin: 16px 0;
      border-collapse: collapse;
      border-spacing: 0;
      overflow: hidden;
      border-radius: 8px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

      th,
      td {
        position: relative;
        border: 1px solid #e4e6eb;
        padding: 8px 12px;
        text-align: left;
        min-width: 120px;
        vertical-align: top;

        >* {
          margin: 0;
        }
      }

      th {
        background-color: #f6f8fa;
        font-weight: 600;
        color: #1d2129;
      }

      tr:nth-child(even) {
        background-color: #f9f9fa;
      }

      tr:hover {
        background-color: #f2f3f5;
      }

      td {
        background: #fff;
      }

      /* 选中的单元格 */
      .selectedCell {
        background: #dbeafe !important;
        border-color: #3b82f6;

        &::after {
          content: '';
          position: absolute;
          left: 0;
          right: 0;
          top: 0;
          bottom: 0;
          border: 2px solid #3b82f6;
          pointer-events: none;
          z-index: 2;
        }
      }

      /* 列调整大小手柄 */
      .column-resize-handle {
        position: absolute;
        right: -2px;
        top: 0;
        bottom: -2px;
        width: 4px;
        background-color: #3b82f6;
        cursor: col-resize;
        z-index: 10;
      }
    }
  }
}

.editor-skeleton {
  padding: 32px;
  max-width: 780px;
  margin: 0 auto;
}

.editor-sidebar {
  width: 280px;
  height: 100%;
  border-left: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
}

.sidebar-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 16px;
    border-bottom: 1px solid #e5e7eb;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    overflow-y: auto;
    padding: 0;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }
}

.sidebar-content {
  padding: 12px;
}

.outline-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.outline-item {
  font-size: 14px;
  color: #4b5563;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.15s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &:hover {
    background: #f3f4f6;
    color: #111827;
  }

  &.active {
    background: #eff6ff;
    color: #3b82f6;
    font-weight: 500;
  }
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

.stat-item {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;

  &:hover {
    background: #f3f4f6;
  }
}

.stat-label {
  color: #6b7280;
  font-size: 14px;
}

.stat-value {
  color: #111827;
  font-weight: 600;
  font-size: 16px;
}

.bubble-menu {
  .bubble-inner {
    display: flex;
    gap: 4px;
    background: #ffffff;
    padding: 6px;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1), 0 2px 4px rgba(0, 0, 0, 0.06);
  }

  .table-bubble-inner {
    gap: 2px;
    padding: 6px;
  }

  .bubble-btn {
    border: none;
    background: transparent;
    color: #475569;
    width: 32px;
    height: 32px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.15s ease;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover:not(.active) {
      background: #f1f5f9;
      color: #1e293b;
    }

    &.active {
      background: #dbeafe;
      color: #2563eb;

      &:hover {
        background: #bfdbfe;
        color: #1d4ed8;
      }
    }

    &.bubble-btn-danger {
      color: #ef4444;

      &:hover {
        background: #fee2e2;
        color: #dc2626;
      }
    }
  }

  .bubble-divider {
    width: 1px;
    background: #e5e7eb;
    margin: 4px 2px;
  }
}

.slash-overlay {
  position: fixed;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  width: 320px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.15);
  padding: 12px;
  z-index: 9999;
}

.slash-search {
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5f5;
  margin-bottom: 12px;
}

.slash-list {
  max-height: 280px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.slash-item {
  border: none;
  background: transparent;
  display: flex;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  text-align: left;
  align-items: flex-start;

  .icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    color: #4b5563;
    margin-top: 2px;
  }

  .info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    flex: 1;
    min-width: 0;
  }

  .label {
    font-weight: 600;
    font-size: 14px;
    color: #1f2937;
    line-height: 1.4;
  }

  .desc {
    font-size: 12px;
    color: #9ca3af;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &.active,
  &:hover {
    background: #eef2ff;

    .icon {
      color: #4f46e5;
    }

    .label {
      color: #4f46e5;
    }
  }
}

.slash-empty {
  text-align: center;
  color: #94a3b8;
  padding: 16px 0;
}

.table-menu-overlay {
  position: fixed;
  z-index: 9999;
  transform: translateX(-50%);
  animation: fadeIn 0.15s ease;

  .bubble-inner {
    display: flex;
    gap: 4px;
    background: #ffffff;
    padding: 6px;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1), 0 2px 4px rgba(0, 0, 0, 0.06);
  }

  .table-bubble-inner {
    gap: 2px;
  }

  .bubble-btn {
    border: none;
    background: transparent;
    color: #475569;
    width: 32px;
    height: 32px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.15s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;

    &:hover:not(.active) {
      background: #f1f5f9;
      color: #1e293b;
    }

    &.active {
      background: #dbeafe;
      color: #2563eb;

      &:hover {
        background: #bfdbfe;
        color: #1d4ed8;
      }
    }

    &.bubble-btn-danger {
      color: #ef4444;

      &:hover {
        background: #fee2e2;
        color: #dc2626;
      }
    }

    /* 无延迟 tooltip */
    &[data-tooltip]:hover::after {
      content: attr(data-tooltip);
      position: absolute;
      bottom: -36px;
      left: 50%;
      transform: translateX(-50%);
      background: #1e293b;
      color: #fff;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      white-space: nowrap;
      pointer-events: none;
      z-index: 10000;
      animation: tooltipFadeIn 0.1s ease;
    }

    &[data-tooltip]:hover::before {
      content: '';
      position: absolute;
      bottom: -12px;
      left: 50%;
      transform: translateX(-50%);
      border: 4px solid transparent;
      border-bottom-color: #1e293b;
      pointer-events: none;
      z-index: 10000;
    }
  }

  .bubble-divider {
    width: 1px;
    background: #e5e7eb;
    margin: 4px 2px;
  }
}

:global(.markdown-paste-notify) {
  width: 320px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.18);
  border-radius: 12px;
  padding: 12px 14px;
}

:global(.markdown-paste-notify .el-notification__content) {
  margin-top: 8px;
}

:global(.markdown-paste-notify .el-notification__title) {
  font-weight: 700;
  color: #0f172a;
}

:global(.markdown-paste-notify .notify-desc) {
  margin: 0 0 12px;
  color: #475569;
  font-size: 14px;
}

:global(.markdown-paste-notify .notify-actions) {
  display: flex;
  gap: 8px;
}

:global(.markdown-paste-notify .notify-btn) {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #1e293b;
  cursor: pointer;
  transition: all 0.15s ease;
}

:global(.markdown-paste-notify .notify-btn:hover) {
  background: #e2e8f0;
}

:global(.markdown-paste-notify .notify-btn.primary) {
  background: #3b82f6;
  border-color: #3b82f6;
  color: #fff;
}

:global(.markdown-paste-notify .notify-btn.primary:hover) {
  background: #2563eb;
  border-color: #2563eb;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-4px);
  }

  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

@media (max-width: 1023px) {
  .editor-workspace {
    flex-direction: column;
  }

  .editor-sidebar {
    width: 100%;
    border-left: none;
    border-top: 1px solid #e5e7eb;
  }
}
</style>
