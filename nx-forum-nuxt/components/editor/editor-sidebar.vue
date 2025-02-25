<template>
  <div class="editor-sidebar">
    <div class="sidebar-header">
      <div class="tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab', { active: activeTab === tab.value }]"
          @click="activeTab = tab.value"
        >
          <Icon :name="tab.icon" />
          <span>{{ tab.label }}</span>
        </button>
      </div>
      <button class="close" @click="$emit('close')">
        <Icon name="tabler:x" />
      </button>
    </div>

    <div class="sidebar-body" v-if="activeTab === 'outline'">
      <div v-if="outline.length" class="outline">
        <button
          v-for="item in outline"
          :key="item.id"
          :class="['outline-item', `level-${item.level}`, { active: item.id === activeId }]"
          @click="$emit('select-heading', item.id)"
        >
          <span class="text">{{ item.text || `标题 ${item.level}` }}</span>
        </button>
      </div>
      <div v-else class="empty">暂无标题，输入 / 添加更多内容</div>
    </div>

    <div class="sidebar-body" v-else>
      <div class="stats">
        <div class="stat-card">
          <div class="label">字符</div>
          <div class="value">{{ stats.characters }}</div>
        </div>
        <div class="stat-card">
          <div class="label">词数</div>
          <div class="value">{{ stats.words }}</div>
        </div>
      </div>
      <div v-if="meta?.author || meta?.publishTime || meta?.permalink" class="meta">
        <div v-if="meta.author" class="meta-row">
          <Icon name="tabler:user" />
          <div>
            <div class="meta-label">作者</div>
            <div class="meta-value">{{ meta.author }}</div>
          </div>
        </div>
        <div v-if="meta.publishTime" class="meta-row">
          <Icon name="tabler:clock" />
          <div>
            <div class="meta-label">发布时间</div>
            <div class="meta-value">{{ meta.publishTime }}</div>
          </div>
        </div>
        <div v-if="meta.permalink" class="meta-row">
          <Icon name="tabler:link" />
          <div>
            <div class="meta-label">固定链接</div>
            <a class="meta-value link" :href="meta.permalink" target="_blank" rel="noopener noreferrer">
              {{ meta.permalink }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useLocalStorage } from '@vueuse/core'
import { watch } from 'vue'
import type { EditorStats } from '~/composables/useEditorStats'
import type { OutlineItem } from '~/composables/useEditorOutline'

interface EditorMeta {
  author?: string
  publishTime?: string
  permalink?: string
}

const props = defineProps<{
  outline: OutlineItem[]
  activeId: string
  stats: EditorStats
  meta?: EditorMeta
}>()

defineEmits<{
  'select-heading': [id: string]
  close: []
}>()

const tabs = [
  { label: '目录', value: 'outline', icon: 'tabler:list-details' },
  { label: '统计', value: 'stats', icon: 'tabler:chart-bar' }
]

const activeTab = useLocalStorage<'outline' | 'stats'>('halo:editor:sidebar-tab', 'outline')

watch(
  () => props.outline.length,
  (len) => {
    if (len === 0 && activeTab.value === 'outline') {
      activeTab.value = 'stats'
    }
  }
)
</script>

<style scoped lang="scss">
.editor-sidebar {
  width: 280px;
  height: 100%;
  border-left: 1px solid #edf0f5;
  background-color: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #edf0f5;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: none;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
  color: #607087;
  cursor: pointer;
  transition: all 0.15s ease;

  &.active {
    background: #eef2ff;
    color: #3949ab;
  }

  &:hover {
    background: rgba(57, 73, 171, 0.1);
  }
}

.close {
  border: none;
  background: transparent;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #607087;
  cursor: pointer;

  &:hover {
    background: rgba(96, 112, 135, 0.1);
  }
}

.sidebar-body {
  flex: 1;
  overflow: auto;
  padding: 12px 16px;
}

.outline {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.outline-item {
  text-align: left;
  border: none;
  background: transparent;
  border-radius: 6px;
  padding: 6px 8px;
  color: #334155;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;

  .text {
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &.level-2 {
    padding-left: 20px;
    color: #475569;
  }

  &.level-3 {
    padding-left: 32px;
    color: #64748b;
  }

  &.level-4,
  &.level-5,
  &.level-6 {
    padding-left: 40px;
    color: #94a3b8;
  }

  &.active {
    background: #eef2ff;
    color: #3949ab;
  }

  &:hover {
    background: rgba(57, 73, 171, 0.08);
  }
}

.empty {
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
  padding-top: 40px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border: 1px solid #edf0f5;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  background: #fafcff;

  .label {
    font-size: 12px;
    color: #94a3b8;
  }

  .value {
    margin-top: 6px;
    font-size: 20px;
    font-weight: 600;
    color: #0f172a;
  }
}

.meta {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-row {
  display: flex;
  gap: 8px;
  color: #475569;
  font-size: 13px;

  .meta-label {
    font-size: 12px;
    color: #94a3b8;
  }

  .meta-value {
    color: #1e293b;
    word-break: break-all;
  }

  .link {
    color: #3949ab;
  }
}
</style>

