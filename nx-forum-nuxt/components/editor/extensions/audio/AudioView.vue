<script lang="ts" setup>
import { NodeViewWrapper, type NodeViewProps } from "@tiptap/vue-3";
import { computed } from "vue";
import InlineBlockBox from "../../components/InlineBlockBox.vue";
import { useEditorUpload } from "~/composables/useEditorUpload";
import { ElMessage } from "element-plus";

const props = defineProps<NodeViewProps>();

const src = computed({
  get: () => {
    return props.node?.attrs.src;
  },
  set: (src: string) => {
    props.updateAttributes({ src: src });
  },
});

const autoplay = computed(() => {
  return props.node.attrs.autoplay;
});

const loop = computed(() => {
  return props.node.attrs.loop;
});

const { openFileDialog } = useEditorUpload({
  maxSizeMB: 20,
  onError: (message) => ElMessage.error(message),
});

const handleReplace = () => {
  openFileDialog({
    accept: "audio/*",
    onSuccess: (result) => {
      props.updateAttributes({ src: result.url });
    },
  });
};
</script>

<template>
  <NodeViewWrapper class="audio-view-wrapper">
    <InlineBlockBox>
      <div
        class="relative inline-block h-full max-w-full overflow-hidden rounded-md text-center transition-all"
        :class="{
          'rounded ring-2 ring-primary': selected,
        }"
        :style="{
          width: node.attrs.width || '100%',
        }"
      >
        <div v-if="src" class="group relative flex items-center justify-center p-2 bg-gray-50 rounded-md">
          <audio
            class="m-0 rounded-md w-full"
            controls
            :src="src"
            :autoplay="autoplay"
            :loop="loop"
            preload="metadata"
            :style="{
              width: node.attrs.width,
              height: node.attrs.height,
            }"
          ></audio>
          <div
            class="absolute right-2 top-2 hidden cursor-pointer justify-end gap-2 group-hover:flex"
          >
            <el-button size="small" circle @click="handleReplace">
              <Icon name="tabler:refresh" />
            </el-button>
          </div>
        </div>
        <div v-else class="relative p-8 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary cursor-pointer" @click="handleReplace">
          <div class="flex flex-col items-center justify-center text-gray-500">
            <Icon name="tabler:music-plus" class="text-4xl mb-2" />
            <span>点击上传音频</span>
          </div>
        </div>
      </div>
    </InlineBlockBox>
  </NodeViewWrapper>
</template>

<style scoped>
.audio-view-wrapper {
  display: inline-block;
  width: 100%;
}
</style>
