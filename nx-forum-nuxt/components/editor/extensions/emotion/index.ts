import { mergeAttributes } from '@tiptap/core'
import { Node } from '@tiptap/core'

export interface EmotionOptions {
  HTMLAttributes: Record<string, any>
}

declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    emotion: {
      setEmotion: (attributes: { src: string; alt: string; name: string }) => ReturnType
    }
  }
}

export default Node.create<EmotionOptions>({
  name: 'emotion',
  group: 'inline',
  inline: true,
  // 不设置 atom: true，避免 TipTap 自动添加 contenteditable="false"

  addOptions() {
    return {
      HTMLAttributes: {},
    }
  },

  addAttributes() {
    return {
      src: {
        default: null,
        parseHTML: (element) => element.getAttribute('src'),
        renderHTML: (attributes) => {
          if (!attributes.src) return {}
          return { src: attributes.src }
        },
      },
      alt: {
        default: null,
        parseHTML: (element) => element.getAttribute('alt'),
        renderHTML: (attributes) => {
          if (!attributes.alt) return {}
          return { alt: attributes.alt }
        },
      },
      name: {
        default: null,
        parseHTML: (element) => element.getAttribute('data-name'),
        renderHTML: (attributes) => {
          if (!attributes.name) return {}
          return { 'data-name': attributes.name }
        },
      },
    }
  },

  parseHTML() {
    return [{
      tag: 'img[data-name]',
    }]
  },

  renderHTML({ HTMLAttributes }) {
    return ['img', mergeAttributes({
      ...HTMLAttributes,
      class: 'emotion-img',
    })]
  },

  addCommands() {
    return {
      setEmotion: (attributes) => ({ commands }) => {
        return commands.insertContent(
          `<img src="${attributes.src}" alt="${attributes.alt}" data-name="${attributes.name}" class="emotion-img" style="display:inline;vertical-align:middle;height:22px;width:22px;margin:0 2px;" />`
        )
      },
    }
  },
})
