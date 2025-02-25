import type { Editor } from '@tiptap/core'
import { onBeforeUnmount, reactive, watch, type Ref } from 'vue'

export interface EditorStats {
  characters: number
  words: number
}

export const useEditorStats = (editor: Ref<Editor | null>) => {
  const stats = reactive<EditorStats>({
    characters: 0,
    words: 0
  })

  const calculateStats = (instance: Editor) => {
    const storage = instance.storage.characterCount
    if (storage?.characters) {
      stats.characters = storage.characters()
      stats.words = storage.words ? storage.words() : calculateWords(instance.getText())
      return
    }

    const text = instance.getText()
    stats.characters = text.length
    stats.words = calculateWords(text)
  }

  const calculateWords = (text: string) => {
    if (!text.trim()) {
      return 0
    }
    return text
      .trim()
      .split(/\s+/)
      .filter(Boolean).length
  }

  let cleanup: (() => void) | null = null

  const attach = (instance: Editor) => {
    const handler = () => calculateStats(instance)
    instance.on('create', handler)
    instance.on('update', handler)
    handler()

    return () => {
      instance.off('create', handler)
      instance.off('update', handler)
    }
  }

  watch(
    editor,
    (instance) => {
      cleanup?.()
      cleanup = null
      if (instance) {
        cleanup = attach(instance)
      }
    },
    { immediate: true }
  )

  onBeforeUnmount(() => cleanup?.())

  return {
    stats,
    refreshStats: () => (editor.value ? calculateStats(editor.value) : undefined)
  }
}

