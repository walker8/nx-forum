<script lang="ts" setup>
import { NodeViewWrapper, type NodeViewProps } from "@tiptap/vue-3";
import { computed, onMounted, ref } from "vue";
import InlineBlockBox from "../../components/InlineBlockBox.vue";

const props = defineProps<NodeViewProps>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    props.updateAttributes({
      src: src,
    });
  },
});

const alt = computed({
  get: () => {
    return props.node?.attrs.alt;
  },
  set: (alt: string) => {
    props.updateAttributes({ alt: alt });
  },
});

const width = computed({
  get: () => {
    return props.node?.attrs.width;
  },
  set: (width: string) => {
    props.updateAttributes({ width: width });
  },
});

const height = computed({
  get: () => {
    return props.node?.attrs.height;
  },
  set: (height: string) => {
    props.updateAttributes({ height: height });
  },
});

const align = computed({
  get: () => {
    return props.node?.attrs.align || 'left';
  },
  set: (align: string) => {
    props.updateAttributes({ align: align });
  },
});

const resizeRef = ref<HTMLDivElement>();
const aspectRatio = ref<number>(0);

function onImageLoaded() {
  if (!resizeRef.value) return;
  aspectRatio.value = resizeRef.value.clientWidth / resizeRef.value.clientHeight;
}

onMounted(() => {
  if (!resizeRef.value) return;
  // 移除旧的事件监听逻辑
});

let startX: number, startWidth: number;

const onResizeStart = (e: MouseEvent) => {
  e.preventDefault();
  startX = e.clientX;
  startWidth = resizeRef.value?.clientWidth || 1;
  document.documentElement.addEventListener("mousemove", doDrag, false);
  document.documentElement.addEventListener("mouseup", stopDrag, false);
};

function doDrag(e: MouseEvent) {
  if (!resizeRef.value) return;

  const newWidth = Math.min(
    startWidth + e.clientX - startX,
    resizeRef.value.parentElement?.clientWidth || 0
  );

  const w = newWidth.toFixed(0) + "px";
  const h = aspectRatio.value ? (newWidth / aspectRatio.value).toFixed(0) + "px" : "auto";

  props.updateAttributes({ width: w, height: h });
}

function stopDrag() {
  document.documentElement.removeEventListener("mousemove", doDrag, false);
  document.documentElement.removeEventListener("mouseup", stopDrag, false);
}

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
  <InlineBlockBox>
    <div class="image-wrapper" :class="`align-${align}`">
      <!-- 图片操作菜单 - 选中时显示在图片上方 -->
      <div v-if="selected" class="image-menu">
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
          <button class="menu-btn menu-btn-danger" data-tooltip="删除图片" @click="handleDelete">
            <Icon name="tabler:trash" />
          </button>
        </div>
      </div>

      <div ref="resizeRef" class="image-container" :class="{
        'ring-2 ring-primary': selected,
      }" :style="{
        width: width,
        height: height,
      }" @click="handleClick">
        <img :src="src" :alt="alt" class="h-full w-full object-cover" @load="onImageLoaded" />
        <!-- 调整大小手柄 -->
        <div v-if="selected" class="resize-handle" @mousedown.stop="onResizeStart"></div>
      </div>
    </div>
  </InlineBlockBox>
</template>

<style scoped lang="scss">
.image-wrapper {
  position: relative;
  display: inline-block;
  max-width: 100%;

  &.align-left {
    text-align: left;
  }

  &.align-center {
    text-align: center;
    display: block;

    .image-container {
      margin: 0 auto;
    }
  }

  &.align-right {
    text-align: right;
    display: block;

    .image-container {
      margin-left: auto;
    }
  }
}

.image-menu {
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

.image-container {
  position: relative;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  border-radius: 6px;
  text-align: center;
  cursor: default;

  .resize-handle {
    position: absolute;
    right: 6px;
    bottom: 6px;
    width: 10px;
    height: 10px;
    background-color: #3b82f6;
    border: 2px solid #fff;
    border-radius: 50%;
    cursor: nwse-resize;
    z-index: 10;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: transform 0.1s ease;

    &:hover {
      transform: scale(1.2);
    }
  }
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
