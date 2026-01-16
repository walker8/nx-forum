import { nextTick } from 'vue'

interface MermaidRendererOptions {
  container: HTMLElement
}

export const useMermaidRenderer = () => {
  const isMermaidLoaded = ref(false)
  const isMermaidLoading = ref(false)

  /**
   * Check if the container has any mermaid diagrams
   */
  const hasMermaidDiagrams = (container: HTMLElement): boolean => {
    const mermaidBlocks = container.querySelectorAll('div[data-type="mermaid"]')
    return mermaidBlocks.length > 0
  }

  /**
   * Load mermaid library dynamically
   */
  const loadMermaid = async (): Promise<boolean> => {
    if (isMermaidLoaded.value) {
      return true
    }

    if (isMermaidLoading.value) {
      // Wait for existing load to complete
      return new Promise((resolve) => {
        const checkLoaded = setInterval(() => {
          if (isMermaidLoaded.value) {
            clearInterval(checkLoaded)
            resolve(true)
          }
        }, 100)
      })
    }

    isMermaidLoading.value = true

    try {
      const mermaidModule = await import('mermaid')
      const mermaid = mermaidModule.default

      // Initialize mermaid with security settings
      mermaid.initialize({
        startOnLoad: false,
        securityLevel: 'loose',
        theme: 'default',
        logLevel: 'error',
      })

      isMermaidLoaded.value = true
      isMermaidLoading.value = false
      return true
    } catch (error) {
      console.error('Failed to load mermaid:', error)
      isMermaidLoading.value = false
      return false
    }
  }

  /**
   * Render a single mermaid diagram
   */
  const renderMermaidDiagram = async (element: HTMLElement): Promise<void> => {
    const code = element.getAttribute('data-code')
    if (!code) return

    // Create container for the rendered diagram
    const container = element.querySelector('.mermaid-render-container') as HTMLElement
    if (!container) return

    // Clear previous content
    container.innerHTML = ''

    try {
      const mermaidModule = await import('mermaid')
      const mermaid = mermaidModule.default

      // Generate unique ID for this diagram
      const id = `mermaid-${Math.random().toString(36).substring(2, 9)}`

      // Render the diagram
      const { svg } = await mermaid.render(id, code)
      container.innerHTML = svg
    } catch (error) {
      console.error('Mermaid render error:', error)
      container.innerHTML = `
        <div class="mermaid-error">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
          </svg>
          <span>图表渲染失败，请检查语法是否正确</span>
        </div>
      `
    }
  }

  /**
   * Render all mermaid diagrams in the given container
   */
  const renderMermaidDiagrams = async (container: HTMLElement): Promise<void> => {
    if (!container || !hasMermaidDiagrams(container)) {
      return
    }

    // Load mermaid library
    const loaded = await loadMermaid()
    if (!loaded) {
      console.error('Failed to load mermaid library')
      return
    }

    // Find all mermaid blocks
    const mermaidBlocks = container.querySelectorAll('div[data-type="mermaid"]')

    // Render each diagram
    for (const block of Array.from(mermaidBlocks)) {
      const mermaidElement = block as HTMLElement

      // Check if container already exists, if not create it
      let renderContainer = mermaidElement.querySelector('.mermaid-render-container') as HTMLElement
      if (!renderContainer) {
        renderContainer = document.createElement('div')
        renderContainer.className = 'mermaid-render-container'
        mermaidElement.appendChild(renderContainer)
      }

      await renderMermaidDiagram(mermaidElement)
    }
  }

  return {
    isMermaidLoaded: readonly(isMermaidLoaded),
    hasMermaidDiagrams,
    renderMermaidDiagrams
  }
}
