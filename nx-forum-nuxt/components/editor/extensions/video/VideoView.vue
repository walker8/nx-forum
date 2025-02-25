<script lang="ts" setup>
import { NodeViewWrapper, type NodeViewProps } from "@tiptap/vue-3";
import { computed } from "vue";
import InlineBlockBox from "../../components/InlineBlockBox.vue";

const props = defineProps<NodeViewProps>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    props.updateAttributes({ src: src });
  },
});

const controls = computed(() => {
  return props.node.attrs.controls;
});

const autoplay = computed(() => {
  return props.node.attrs.autoplay;
});

const loop = computed(() => {
  return props.node.attrs.loop;
});

const align = computed({
  get: () => {
    return props.node?.attrs.align || 'left';
  },
  set: (align: string) => {
    props.updateAttributes({ align: align });
  },
});

const handleDelete = () => {
  props.deleteNode();
};

const handleAlign = (alignment: string) => {
  align.value = alignment;
};

const handleClick = () => {
  (props as any).selectNode();
};
</script>

<template>
  <NodeViewWrapper class="video-view-wrapper">
    <InlineBlockBox>
      <div class="video-wrapper" :class="`align-${align}`">
        <!-- 视频操作菜单 - 选中时显示在视频上方 -->
        <div v-if="selected" class="video-menu">
          <div class="menu-inner">
            <button class="menu-btn" data-tooltip="左对齐" @click="handleAlign('left')"
              :class="{ active: align === 'left' }">
              <Icon name="tabler:align-left" />
            </button>
            <button class="menu-btn" data-tooltip="居中对齐" @click="handleAlign('center')"
              :class="{ active: align === 'center' }">
              <Icon name="tabler:align-center" />
            </button>
            <button class="menu-btn" data-tooltip="右对齐" @click="handleAlign('right')"
              :class="{ active: align === 'right' }">
              <Icon name="tabler:align-right" />
            </button>
            <div class="menu-divider" />
            <button class="menu-btn menu-btn-danger" data-tooltip="删除视频" @click="handleDelete">
              <Icon name="tabler:trash" />
            </button>
          </div>
        </div>

        <div class="video-container" :class="{
          'ring-2 ring-primary': selected,
        }" :style="{
          width: node.attrs.width || '100%',
        }" @click="handleClick">
          }">
          <video v-if="src" :src="src" :controls="controls" :autoplay="autoplay" :loop="loop" playsinline
            preload="metadata" class="m-0 rounded-md w-full" :style="{
              width: node.attrs.width,
              height: node.attrs.height,
            }"></video>
          <div v-else
            class="flex flex-col items-center justify-center p-8 border-2 border-dashed border-gray-300 rounded-lg text-gray-500">
            <Icon name="tabler:video" class="text-4xl mb-2" />
            <span>视频地址无效</span>
          </div>
        </div>
      </div>
    </InlineBlockBox>
  </NodeViewWrapper>
</template>

<style scoped lang="scss">
.video-view-wrapper {
  display: inline-block;
  max-width: 100%;
  width: 100%;
}

.video-wrapper {
  position: relative;
  display: inline-block;
  max-width: 100%;
  width: 100%;

  &.align-left {
    text-align: left;
  }

  &.align-center {
    text-align: center;
    display: block;

    .video-container {
      margin: 0 auto;
    }
  }

  &.align-right {
    text-align: right;
    display: block;

    .video-container {
      margin-left: auto;
    }
  }
}

.video-menu {
  position: absolute;
  top: -48px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 100;
  animation: fadeIn 0.15s ease;

  .menu-inner {
    display: flex;
    gap: 4px;
    background: #ffffff;
    padding: 6px;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1), 0 2px 4px rgba(0, 0, 0, 0.06);
  }

  .menu-btn {
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

    &.menu-btn-danger {
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

  .menu-divider {
    width: 1px;
    background: #e5e7eb;
    margin: 4px 2px;
  }
}

.video-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  border-radius: 6px;
  text-align: center;
  cursor: default;
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

@keyframes tooltipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-4px);
  }

  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>
