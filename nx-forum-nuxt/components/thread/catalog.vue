<template>
  <el-card v-show="catalog?.length" :class="{ 'catalog-sticky': isSticky }" ref="catalogCard">
    <template #header>
      <div class="flex items-center justify-between">
        <span>目录</span>
        <span class="text-gray-400 text-sm cursor-pointer" @click="toggleCatalog">
          {{ showCatalog ? '收起' : '展开' }}
        </span>
      </div>
    </template>
    <div v-show="showCatalog" class="catalog-body" ref="catalogRef">
      <div
        v-for="(item, index) in catalog"
        :key="index"
        :ref="
          (el) => {
            if (el) catalogItems[item.id] = el as HTMLElement
          }
        "
        :class="[
          'cursor-pointer hover:text-primary transition-colors duration-200',
          'py-1 truncate hover:text-[#409eff]',
          item.level === 2 ? 'pl-4' : '',
          item.level === 3 ? 'pl-8 text-sm' : '',
          activeId === item.id ? 'text-[#409eff]' : 'text-gray-600'
        ]"
        @click="scrollToHeading(item.id)"
      >
        {{ item.text }}
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    content?: string
  }>(),
  {
    content: ''
  }
)

// 目录数据
const catalog = ref<Array<{ id: string; text: string; level: number }>>([])

// 解析内容中的标题
const parseHeadings = (content: string) => {
  if (!content) return

  if (import.meta.client) {
    const parser = new DOMParser()
    const doc = parser.parseFromString(content, 'text/html')
    const headings = doc.querySelectorAll('h2, h3, h4')
    const newCatalog: Array<{ id: string; text: string; level: number }> = []

    headings.forEach((heading, index) => {
      const level = parseInt(heading.tagName.substring(1)) - 1
      if (level <= 3) {
        const id = `heading-${index}`
        if (!heading.id) {
          heading.id = id
        }
        newCatalog.push({
          id: heading.id,
          text: heading.textContent || '',
          level
        })
      }
    })

    catalog.value = newCatalog
  }
}

// 监听文章内容变化
watchEffect(() => {
  if (import.meta.client && props.content) {
    parseHeadings(props.content)
  }
})

// 滚动到指定标题
const scrollToHeading = (id: string) => {
  if (import.meta.client) {
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' })
    }
  }
}

// 控制目录显示隐藏
const showCatalog = ref(true)
const toggleCatalog = () => {
  showCatalog.value = !showCatalog.value
}

// 当前激活的标题ID
const activeId = ref('')

// 监听滚动更新激活状态
const updateActiveHeading = () => {
  if (!import.meta.client || catalog.value.length === 0) return

  // 获取所有标题元素
  const headings = catalog.value
    .map((item) => ({
      id: item.id,
      element: document.getElementById(item.id)
    }))
    .filter((item) => item.element)

  // 找到当前视窗中最靠近顶部的标题
  let activeHeading = headings[0]
  let minDistance = Infinity

  headings.forEach((heading) => {
    if (!heading.element) return
    const distance = Math.abs(heading.element.getBoundingClientRect().top)
    if (distance < minDistance) {
      minDistance = distance
      activeHeading = heading
    }
  })

  // 更新激活状态
  if (activeHeading) {
    activeId.value = activeHeading.id
  }
}

// 添加滚动监听
onMounted(() => {
  if (import.meta.client) {
    window.addEventListener('scroll', updateActiveHeading, { passive: true })
    // 初始化激活状态
    updateActiveHeading()
  }
})

// 移除滚动监听
onUnmounted(() => {
  if (import.meta.client) {
    window.removeEventListener('scroll', updateActiveHeading)
  }
})

// 监听目录数据变化，更新激活状态
watch(catalog, () => {
  updateActiveHeading()
})

// 目录容器和目录项的引用
const catalogRef = ref<HTMLElement | null>(null)
const catalogItems: Record<string, HTMLElement> = reactive({})

// 滚动目录到激活项
const scrollCatalogToActive = () => {
  if (!activeId.value || !catalogRef.value || !catalogItems[activeId.value]) return

  const container = catalogRef.value
  const activeItem = catalogItems[activeId.value]

  // 获取容器和激活项的位置信息
  const containerRect = container.getBoundingClientRect()
  const activeRect = activeItem.getBoundingClientRect()

  // 判断激活项是否在可视区域内
  const isInView = activeRect.top >= containerRect.top && activeRect.bottom <= containerRect.bottom

  if (!isInView) {
    // 计算滚动位置，使激活项在容器中间
    const scrollTop =
      activeItem.offsetTop - container.clientHeight / 2 + activeItem.clientHeight / 2
    container.scrollTo({
      top: scrollTop,
      behavior: 'smooth'
    })
  }
}

// 更新激活状态时同时滚动目录
watch(activeId, () => {
  nextTick(() => {
    scrollCatalogToActive()
  })
})

// 在目录数据更新后，重置目录项引用
watch(catalog, () => {
  Object.keys(catalogItems).forEach((key) => {
    delete catalogItems[key]
  })
})

const isSticky = ref(false)
const catalogCard = ref<HTMLElement>()

// 监听滚动事件，检查作者信息卡片是否完全隐藏
const checkAuthorCardVisibility = () => {
  const authorCard = document.querySelector('.author-card') as HTMLElement
  if (!authorCard || !catalogCard.value) return

  const authorCardRect = authorCard.getBoundingClientRect()
  // 当作者卡片完全移出视口时，设置目录为吸顶状态
  isSticky.value = authorCardRect.bottom <= 0
}

// 添加滚动监听
onMounted(() => {
  if (import.meta.client) {
    window.addEventListener('scroll', checkAuthorCardVisibility, { passive: true })
    // 初始检查一次
    checkAuthorCardVisibility()
  }
})

// 移除滚动监听
onUnmounted(() => {
  if (import.meta.client) {
    window.removeEventListener('scroll', checkAuthorCardVisibility)
  }
})
</script>

<style lang="scss" scoped>
.catalog-sticky {
  position: fixed;
  top: 20px;
  width: 270px; // 与原始宽度保持一致
  z-index: 10;
}

.catalog-body {
  max-height: 400px;
  overflow-y: auto;
  scroll-behavior: smooth;
  padding: 10px;
  font-size: 14px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: #dcdfe6;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background-color: #c0c4cc;
  }

  &::-webkit-scrollbar-track {
    background-color: #f5f7fa;
  }
}

.el-card :deep(.el-card__body) {
  padding: 0px;
}
</style>
