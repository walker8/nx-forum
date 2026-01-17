<template>
  <div class="comment-editor" :class="{ 'is-focused': isFocused, 'is-disabled': disabled }">
    <EditorContent :editor="editor" class="comment-editor-content" />
  </div>
</template>

<script setup lang="ts">
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Image from '@tiptap/extension-image'
import { mergeAttributes } from '@tiptap/core'

interface Props {
  modelValue: string
  placeholder?: string
  disabled?: boolean
  maxlength?: number
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '理性发言，友善互动',
  disabled: false,
  maxlength: 1000
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'focus': [event: FocusEvent]
  'blur': [event: FocusEvent]
}>()

const isFocused = ref(false)

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      heading: false,
      codeBlock: false,
      blockquote: false,
      horizontalRule: false,
      listItem: false,
      orderedList: false,
      bulletList: false,
      strike: false,
      code: false,
      hardBreak: false,
    }),
    Placeholder.configure({
      placeholder: props.placeholder,
    }),
    // 自定义 Image 扩展用于表情
    Image.extend({
      addAttributes() {
        return {
          ...this.parent?.(),
          'data-name': {
            default: null,
          },
          class: {
            default: 'emotion-img',
          },
        }
      },
      renderHTML({ HTMLAttributes }) {
        return ['img', mergeAttributes(HTMLAttributes, {
          style: 'display:inline;vertical-align:middle;height:22px;width:22px;margin:0 2px;',
        })]
      },
    }).configure({
      inline: true,
      allowBase64: false,
    }),
  ],
  editable: !props.disabled,
  autofocus: false,
  editorProps: {
    attributes: {
      class: 'comment-prosemirror',
    },
  },
  onUpdate: ({ editor }) => {
    const html = editor.getHTML()
    emit('update:modelValue', html)
  },
  onFocus: () => {
    isFocused.value = true
  },
  onBlur: () => {
    isFocused.value = false
  },
})

// Watch for external modelValue changes
watch(
  () => props.modelValue,
  (value) => {
    if (editor.value && value !== editor.value.getHTML()) {
      editor.value.commands.setContent(value, false)
    }
  }
)

// Watch for disabled changes
watch(
  () => props.disabled,
  (value) => {
    if (editor.value) {
      editor.value.setEditable(!value)
    }
  }
)

// Expose editor methods
const insertEmotion = (emotion: { url: string; name: string }) => {
  if (!editor.value) return
  // 使用 setImage 命令插入内联图片
  editor.value.chain().focus().setImage({
    src: emotion.url,
    alt: emotion.name,
    'data-name': emotion.name,
  }).run()
}

const focus = () => {
  if (editor.value) {
    // 使用 chain().focus() 确保 editor 正确聚焦
    editor.value.chain().focus().run()
  }
}

const blur = () => {
  editor.value?.blur()
}

const getText = () => {
  return editor.value?.getText() || ''
}

const getHTML = () => {
  return editor.value?.getHTML() || ''
}

const isEmpty = () => {
  const text = getText().trim()
  const html = getHTML()
  // Check if empty (no text and no emotion images)
  return text === '' && !html.includes('<img')
}

defineExpose({
  insertEmotion,
  focus,
  blur,
  getText,
  getHTML,
  isEmpty,
  editor: computed(() => editor.value),
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<style scoped lang="scss">
.comment-editor {
  width: 100%;
  background-color: var(--el-bg-color);
  transition: all 0.2s ease;
  overflow: hidden; // 确保内容不会超出圆角

  &.is-focused {
    :deep(.ProseMirror) {
      outline: none;
    }
  }

  &.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.comment-editor-content {
  width: 100%;

  :deep(.ProseMirror) {
    outline: none;
    min-height: 60px;
    max-height: 200px;
    overflow-y: auto;
    font-size: 16px;
    line-height: 1.5;
    padding: 12px;
    color: var(--el-text-color-primary);
    word-wrap: break-word;
    word-break: break-word;
    white-space: pre-wrap;
    overflow-wrap: break-word;

    &:focus {
      outline: none;
    }

    // Placeholder styling
    p.is-editor-empty:first-child::before {
      content: attr(data-placeholder);
      float: left;
      color: var(--el-text-color-placeholder);
      pointer-events: none;
      height: 0;
    }

    // Paragraph styling
    p {
      margin: 0;
      line-height: 1.5;
      display: block;
      white-space: normal; // 允许正常换行，但不是强制
      word-wrap: break-word;

      // 隐藏 TipTap 自动添加的 trailing break
      :deep(.ProseMirror-trailingBreak) {
        display: none;
      }
    }

    // Emotion image styling - inline display
    :deep(img.emotion-img) {
      display: inline !important;
      height: 22px;
      width: 22px;
      vertical-align: middle !important;
      margin: 0 2px;
      object-fit: contain;
      pointer-events: none;
    }

    // 隐藏 separator
    :deep(.ProseMirror-separator) {
      display: none !important;
    }
  }
}

// 移动端优化
@media screen and (max-width: 768px) {
  .comment-editor-content {
    :deep(.ProseMirror) {
      font-size: 16px; // 防止iOS自动放大
      padding: 12px;
      min-height: 80px;
    }
  }
}
</style>
