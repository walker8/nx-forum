<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { getAiModels, updateAiModels, testAiModelConnectivity } from '~/apis/aiModel'
import type { AiModelConfigsDTO, AiModelProvider, AiModelEntry } from '~/apis/aiModel'

definePageMeta({
  layout: 'admin'
})

const modelsConfig = reactive<AiModelConfigsDTO>({ providers: [] })
const activeProviderIndex = ref(-1)

const currentProvider = computed<AiModelProvider | null>(
  () => modelsConfig.providers[activeProviderIndex.value] || null
)

const genId = () => `${Date.now()}-${Math.random().toString(36).slice(2)}`

const addProvider = () => {
  modelsConfig.providers.push({
    id: genId(),
    name: '',
    apiUrl: '',
    apiKey: '',
    timeoutSeconds: 60,
    enabled: true,
    models: []
  })
  activeProviderIndex.value = modelsConfig.providers.length - 1
}

const removeProvider = (index: number) => {
  modelsConfig.providers.splice(index, 1)
  if (activeProviderIndex.value >= modelsConfig.providers.length) {
    activeProviderIndex.value = modelsConfig.providers.length - 1
  }
}

const addModel = () => {
  currentProvider.value?.models.push({ id: genId(), model: '' })
}

const removeModel = (index: number) => {
  currentProvider.value?.models.splice(index, 1)
}

const loadModels = async () => {
  try {
    const res = await getAiModels()
    Object.assign(modelsConfig, res.data || { providers: [] })
    activeProviderIndex.value = modelsConfig.providers.length > 0 ? 0 : -1
  } catch (error) {
    ElMessage.error(error || '获取模型列表失败')
  }
}

const saveModels = async () => {
  try {
    await updateAiModels(modelsConfig)
    ElMessage.success('模型保存成功')
  } catch (error) {
    ElMessage.error(error || '模型保存失败')
  }
}

const testingModelId = ref('')
const modelTestResult = ref('')

const handleModelTest = async (model: AiModelEntry) => {
  const provider = currentProvider.value
  if (!provider || !provider.apiUrl) {
    ElMessage.warning('请先填写模型地址')
    return
  }
  if (!model.model) {
    ElMessage.warning('请先填写模型 ID')
    return
  }
  testingModelId.value = model.id
  modelTestResult.value = ''
  try {
    const res = await testAiModelConnectivity({
      providerId: provider.id,
      apiUrl: provider.apiUrl,
      // 掩码值为未修改的已存 Key，交由服务端按厂商解析，不在前后端间往返明文
      apiKey: provider.apiKey.includes('****') ? '' : provider.apiKey,
      timeoutSeconds: provider.timeoutSeconds,
      model: model.model
    })
    modelTestResult.value = res.data || '连接成功'
    ElMessage.success('模型连接正常')
  } catch (error) {
    modelTestResult.value = ''
    ElMessage.error(error || '模型测试失败')
  } finally {
    testingModelId.value = ''
  }
}

onMounted(() => {
  loadModels()
})
</script>

<template>
  <el-card shadow="never">
    <el-alert type="info" show-icon :closable="false" class="mb-6">
      共享 AI 模型库：按「厂商」维护连接信息（模型地址 / API Key），同一厂商下可添加多个「模型
      ID」，可被审核等场景复用。API Key 加密存储，已保存的 Key 不回显，留空或保持掩码保存即保留原
      Key。
    </el-alert>

    <div class="flex gap-4 items-start mt-4">
      <!-- 左侧：厂商列表 -->
      <div class="provider-list">
        <div
          v-for="(provider, index) in modelsConfig.providers"
          :key="provider.id"
          class="provider-item"
          :class="{ active: index === activeProviderIndex }"
          @click="activeProviderIndex = index"
        >
          <div class="flex items-center gap-2">
            <Icon name="tabler:robot" class="text-lg text-gray-500 shrink-0" />
            <div class="min-w-0">
              <div class="provider-name truncate">{{ provider.name || '未命名厂商' }}</div>
              <div class="text-xs text-gray-400">{{ provider.models.length }} 个模型</div>
            </div>
          </div>
          <el-tag v-if="!provider.enabled" type="info" size="small">停用</el-tag>
        </div>
        <el-button class="w-full mt-2" plain @click="addProvider">+ 添加厂商</el-button>
      </div>

      <!-- 右侧：选中厂商详情 -->
      <div class="flex-1 min-w-0">
        <template v-if="currentProvider">
          <el-form label-width="auto">
            <el-form-item label="厂商名称">
              <el-input
                v-model="currentProvider.name"
                placeholder="如 Minimax / DeepSeek / OpenAI"
                class="w-72"
              />
            </el-form-item>
            <el-form-item label="启用">
              <el-switch v-model="currentProvider.enabled" />
            </el-form-item>
            <el-form-item label="模型地址">
              <el-input
                v-model="currentProvider.apiUrl"
                placeholder="base URL，如 https://api.minimaxi.com/v1"
              />
            </el-form-item>
            <el-form-item label="API Key">
              <el-input
                v-model="currentProvider.apiKey"
                type="password"
                show-password
                placeholder="留空/保持掩码则保留原 Key，新厂商必填"
              />
            </el-form-item>
            <el-form-item label="超时(秒)">
              <el-input-number v-model="currentProvider.timeoutSeconds" :min="5" :max="300" />
            </el-form-item>
          </el-form>

          <el-divider content-position="left">模型 ID</el-divider>
          <div
            v-for="(model, index) in currentProvider.models"
            :key="model.id"
            class="flex items-center gap-3 mb-2"
          >
            <el-input v-model="model.model" placeholder="模型 ID（如 Minimax-M3）" class="w-72" />
            <el-button
              size="small"
              :loading="testingModelId === model.id"
              @click="handleModelTest(model)"
              >测试</el-button
            >
            <el-button size="small" type="danger" plain @click="removeModel(index)">删除</el-button>
          </div>
          <el-alert
            v-if="modelTestResult"
            :title="modelTestResult"
            type="success"
            :closable="false"
            class="mt-2"
          />
          <el-button size="small" type="primary" plain @click="addModel">+ 添加模型</el-button>

          <div class="mt-5 flex items-center gap-3">
            <el-button type="primary" @click="saveModels">保存模型</el-button>
            <el-button type="danger" plain @click="removeProvider(activeProviderIndex)"
              >删除厂商</el-button
            >
          </div>
        </template>
        <el-empty v-else description="请先在左侧添加厂商" />
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.provider-list {
  width: 240px;
  flex-shrink: 0;
}

.provider-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin-bottom: 6px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.provider-item:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.provider-item.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.provider-name {
  max-width: 140px;
}
</style>
