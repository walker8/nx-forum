import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { Plugin, PluginKey, TextSelection, type Transaction } from '@tiptap/pm/state'
import { findParentNode } from '@tiptap/core'
import CodeBlockView from './CodeBlockView.vue'

export interface CodeBlockOptions {
  lowlight: any
  defaultLanguage: string | null | undefined
  HTMLAttributes: Record<string, any>
  languages: Array<{ label: string; value: string }>
}

type IndentType = 'indent' | 'outdent'

const looksLikeMarkdown = (text: string) => {
  const trimmed = text.trim()
  if (!trimmed) return false
  const patterns = [
    /^#{1,6}\s/m,
    /(^|\n)([-*+])\s+\S/m,
    /(^|\n)\d+\.\s+\S/m,
    /`{3}[\s\S]*?`{3}/m,
    />\s+\S/m,
    /\[.+?\]\(.+?\)/
  ]
  return patterns.some((p) => p.test(text))
}

const updateIndent = (tr: Transaction, type: IndentType): Transaction => {
  const { doc, selection } = tr
  if (!doc || !selection) return tr
  if (!(selection instanceof TextSelection)) {
    return tr
  }
  const { from, to } = selection
  doc.nodesBetween(from, to, (_node, pos) => {
    if (from - to === 0 && type === 'indent') {
      tr.insertText('  ', from, to)
      return false
    }

    const precedeContent = doc.textBetween(pos + 1, from, '\n')
    const precedeLineBreakPos = precedeContent.lastIndexOf('\n')
    const startBetWeenIndex =
      precedeLineBreakPos === -1 ? pos + 1 : pos + precedeLineBreakPos + 1
    const text = doc.textBetween(startBetWeenIndex, to, '\n')
    if (type === 'indent') {
      let replacedStr = text.replace(/\n/g, '\n  ')
      if (startBetWeenIndex === pos + 1) {
        replacedStr = '  ' + replacedStr
      }
      tr.insertText(replacedStr, startBetWeenIndex, to)
    } else {
      let replacedStr = text.replace(/\n {2}/g, '\n')
      if (startBetWeenIndex === pos + 1) {
        const firstNewLineIndex = replacedStr.indexOf('  ')
        if (firstNewLineIndex === 0) {
          replacedStr = replacedStr.replace('  ', '')
        }
      }
      tr.insertText(replacedStr, startBetWeenIndex, to)
    }
    return false
  })

  return tr
}

export const CodeBlockExtension = CodeBlockLowlight.extend<CodeBlockOptions>({
  addAttributes() {
    return {
      ...this.parent?.(),
      collapsed: {
        default: false,
        parseHTML: (element) => element.getAttribute('collapsed') === 'true',
        renderHTML: (attributes) => {
          if (attributes.collapsed) {
            return {
              collapsed: 'true',
            }
          }
          return {}
        },
      },
    }
  },

  addNodeView() {
    return VueNodeViewRenderer(CodeBlockView)
  },

  addOptions() {
    return {
      ...this.parent?.(),
      languages: [],
    }
  },

  addCommands() {
    return {
      ...this.parent?.(),
      codeIndent:
        () =>
        ({ tr, state, dispatch }) => {
          const { selection } = state
          tr = tr.setSelection(selection)
          tr = updateIndent(tr, 'indent')
          if (tr.docChanged && dispatch) {
            dispatch(tr)
            return true
          }
          return false
        },
      codeOutdent:
        () =>
        ({ tr, state, dispatch }) => {
          const { selection } = state
          tr = tr.setSelection(selection)
          tr = updateIndent(tr, 'outdent')
          if (tr.docChanged && dispatch) {
            dispatch(tr)
            return true
          }
          return false
        },
    }
  },

  addKeyboardShortcuts() {
    return {
      ...this.parent?.(),
      Tab: () => {
        if (this.editor.isActive('codeBlock')) {
          return this.editor.chain().focus().codeIndent().run()
        }
        return false
      },
      'Shift-Tab': () => {
        if (this.editor.isActive('codeBlock')) {
          return this.editor.chain().focus().codeOutdent().run()
        }
        return false
      },
      'Mod-a': () => {
        if (this.editor.isActive('codeBlock')) {
          const { tr, selection } = this.editor.state
          const codeBlock = findParentNode(
            (node) => node.type.name === this.name
          )(selection)
          if (!codeBlock) {
            return false
          }
          const head = codeBlock.start
          const anchor = codeBlock.start + codeBlock.node.nodeSize - 1
          const $head = tr.doc.resolve(head)
          const $anchor = tr.doc.resolve(anchor)
          this.editor.view.dispatch(
            tr.setSelection(new TextSelection($head, $anchor))
          )
          return true
        }
        return false
      },
    }
  },

  addProseMirrorPlugins() {
    return [
      ...(this.parent?.() || []),
      new Plugin({
        key: new PluginKey('codeBlockVSCodeHandlerFixPaste'),
        props: {
          handlePaste: (view, event) => {
            if (event.defaultPrevented) {
              return false
            }
            if (!event.clipboardData) {
              return false
            }
            if (this.editor.isActive(this.type.name)) {
              return false
            }

            const text = event.clipboardData.getData('text/plain')
            if (!text) {
              return false
            }
            if (looksLikeMarkdown(text)) {
              return false
            }
            const vscode = event.clipboardData.getData('vscode-editor-data')
            const vscodeData = vscode ? JSON.parse(vscode) : undefined
            const language = vscodeData?.mode

            if (!language) {
              return false
            }

            const { tr, schema } = view.state
            const contentTextNode = schema.text(text.replace(/\r\n?/g, '\n'))

            tr.replaceSelectionWith(
              this.type.create({ language }, contentTextNode)
            )

            const { selection } = tr
            let codeBlockPos = Math.max(0, selection.from - 1)
            while (
              codeBlockPos > 0 &&
              tr.doc.resolve(codeBlockPos).parent.type.name !== this.type.name
            ) {
              codeBlockPos--
            }
            tr.setSelection(TextSelection.near(tr.doc.resolve(codeBlockPos)))
            tr.setMeta('paste', true)
            view.dispatch(tr)
            return true
          },
        },
      }),
    ]
  },
})

export default CodeBlockExtension
