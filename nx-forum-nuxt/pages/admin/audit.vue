<script setup lang="ts">
import type { InputInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import {
  getAuditConfigSensitiveWords,
  updateAuditConfigSensitiveWords,
  getAuditConfigBlackWhiteUsersVO,
  updateAuditConfigBlackWhiteUsers,
  getAuditConfigRules,
  updateAuditConfigRules,
  testRule,
  getAuditConfigAi,
  updateAuditConfigAi,
  testAiAudit
} from '~/apis/audit'
import type {
  AuditRule,
  AuditRuleAction,
  AuditConfigRulesDTO,
  AuditConfigAiDTO
} from '~/apis/audit'
import { getAiModels } from '~/apis/aiModel'
import { queryUsername } from '~/apis/uc/user'
import type { UserVO } from '~/types/global'

definePageMeta({
  layout: 'admin'
})

const activeTab = ref('sensitive')

// ============ 敏感词 ============
const sensitiveConfig = reactive({
  enableSensitiveWordsAudit: false,
  sensitiveWords: [] as string[]
})

const sensitiveInputVisible = ref(false)
const sensitiveInputValue = ref('')
const sensitiveInputRef = ref<InputInstance>()

const handleSensitiveInputConfirm = () => {
  if (
    sensitiveInputValue.value &&
    !sensitiveConfig.sensitiveWords.includes(sensitiveInputValue.value)
  ) {
    sensitiveConfig.sensitiveWords.push(sensitiveInputValue.value)
  }
  sensitiveInputVisible.value = false
  sensitiveInputValue.value = ''
}
const handleSensitiveClose = (word: string) => {
  sensitiveConfig.sensitiveWords = sensitiveConfig.sensitiveWords.filter((w) => w !== word)
}
const showSensitiveInput = () => {
  sensitiveInputVisible.value = true
  nextTick(() => {
    sensitiveInputRef.value?.input?.focus()
  })
}

const saveSensitiveConfig = async () => {
  try {
    await updateAuditConfigSensitiveWords(sensitiveConfig)
    ElMessage.success('敏感词设置保存成功')
  } catch (error) {
    ElMessage.error(error || '敏感词设置保存失败')
  }
}

// ============ 黑白名单 ============
const blackWhiteConfig = reactive({
  whiteListUsers: [] as UserVO[],
  blackListUsers: [] as UserVO[]
})

const whiteSearch = reactive({ query: '', results: [] as UserVO[], visible: false })
const blackSearch = reactive({ query: '', results: [] as UserVO[], visible: false })

const handleUserSearch = async (type: 'white' | 'black') => {
  const query = type === 'white' ? whiteSearch.query : blackSearch.query
  if (!query.trim()) return
  try {
    const res = await queryUsername(query.trim())
    if (type === 'white') {
      whiteSearch.results = res.data
    } else {
      blackSearch.results = res.data
    }
  } catch (error) {
    ElMessage.error(error || '用户搜索失败')
  }
}

const addUserToList = (user: UserVO, list: 'white' | 'black', type: 'white' | 'black') => {
  const arr = list === 'white' ? blackWhiteConfig.whiteListUsers : blackWhiteConfig.blackListUsers
  if (!arr.some((u) => u.userId === user.userId)) {
    arr.push(user)
  }
  if (type === 'white') {
    whiteSearch.visible = false
    whiteSearch.query = ''
  } else {
    blackSearch.visible = false
    blackSearch.query = ''
  }
}

const saveBlackWhiteConfig = async () => {
  try {
    await updateAuditConfigBlackWhiteUsers({
      whiteListUsers: blackWhiteConfig.whiteListUsers.map((u) => u.userId),
      blackListUsers: blackWhiteConfig.blackListUsers.map((u) => u.userId)
    })
    ElMessage.success('黑白名单设置保存成功')
  } catch (error) {
    ElMessage.error(error || '黑白名单设置保存失败')
  }
}

// ============ 审核规则（QLExpress）============
const rulesConfig = reactive<AuditConfigRulesDTO>({ enabled: true, rules: [] })

const ruleActionOptions: { value: AuditRuleAction; label: string }[] = [
  { value: 'PASS', label: '放行' },
  { value: 'REVIEW', label: '转人工' },
  { value: 'REJECT', label: '拒绝' }
]

const genId = () => `${Date.now()}-${Math.random().toString(36).slice(2)}`

const addRule = () => {
  rulesConfig.rules.push({
    id: genId(),
    name: '',
    expression: '',
    action: 'REVIEW',
    enabled: true,
    priority: rulesConfig.rules.length + 1
  })
}

const removeRule = (index: number) => {
  rulesConfig.rules.splice(index, 1)
}

const moveRule = (index: number, delta: number) => {
  const target = index + delta
  if (target < 0 || target >= rulesConfig.rules.length) return
  const tmp = rulesConfig.rules[index]
  rulesConfig.rules[index] = rulesConfig.rules[target]
  rulesConfig.rules[target] = tmp
}

const validateRuleExpression = async (rule: AuditRule) => {
  try {
    const res = await testRule({ expression: rule.expression })
    const error = res.data as string
    if (error) {
      ElMessage.error(`规则【${rule.name || '未命名'}】校验失败：${error}`)
      return false
    }
    ElMessage.success('表达式校验通过')
    return true
  } catch (e) {
    ElMessage.error(e || '表达式校验失败')
    return false
  }
}

const saveRules = async () => {
  try {
    await updateAuditConfigRules(rulesConfig)
    ElMessage.success('审核规则保存成功')
  } catch (error) {
    ElMessage.error(error || '审核规则保存失败')
  }
}

// ============ AI 审核（场景配置，模型引用共享模型库）============
const DEFAULT_PROMPT =
  '你是论坛内容安全审核助手。请根据给定内容及其上下文（发帖人信息、发帖信息、历史发帖/回帖）判断内容是否违规。判断维度：违法违规、色情低俗、暴力恐怖、人身攻击辱骂、广告营销垃圾信息、政治敏感等。请只输出一个 JSON 对象（不要输出任何其他文字），格式：{"verdict":"PASS|REVIEW|REJECT","reason":"简短理由","confidence":0.0~1.0}。verdict 含义：PASS=正常放行，REVIEW=存疑转人工复审，REJECT=明显违规。'

const aiConfig = reactive<AuditConfigAiDTO>({
  enabled: false,
  activeModelId: '',
  historyCount: 5,
  prompt: DEFAULT_PROMPT
})

// 共享模型库（在「模型管理」页维护），这里拍平成「厂商 / 模型id」列表供选择
const sharedModels = ref<{ id: string; label: string }[]>([])

const saveAiConfig = async () => {
  try {
    await updateAuditConfigAi(aiConfig)
    ElMessage.success('AI 审核设置保存成功')
  } catch (error) {
    ElMessage.error(error || 'AI 审核设置保存失败')
  }
}

const aiTesting = ref(false)
const aiTestContent = ref('加我微信低价代购奢侈品，全网最低价')
const aiTestResult = ref('')

const verdictLabel = (v: string) => {
  const map: Record<string, string> = { PASS: '正常放行', REVIEW: '存疑转人工', REJECT: '违规' }
  return map[v] || v
}

const handleAiTest = async () => {
  if (!aiConfig.activeModelId) {
    ElMessage.warning('请先选择生效模型')
    return
  }
  aiTesting.value = true
  aiTestResult.value = ''
  try {
    const res = await testAiAudit({
      modelId: aiConfig.activeModelId,
      prompt: aiConfig.prompt,
      content: aiTestContent.value
    })
    const data = res.data
    aiTestResult.value = `结论：${verdictLabel(data.verdict)}；理由：${data.reason || '无'}；置信度：${data.confidence ?? 0}`
  } catch (error) {
    ElMessage.error(error || 'AI 测试失败')
  } finally {
    aiTesting.value = false
  }
}

// ============ 加载 ============
const getAuditSettings = async () => {
  try {
    const [sensitiveRes, bwRes, rulesRes, aiRes, modelsRes] = await Promise.all([
      getAuditConfigSensitiveWords(),
      getAuditConfigBlackWhiteUsersVO(),
      getAuditConfigRules(),
      getAuditConfigAi(),
      getAiModels()
    ])
    Object.assign(sensitiveConfig, sensitiveRes.data)
    Object.assign(blackWhiteConfig, bwRes.data)
    Object.assign(rulesConfig, rulesRes.data || { enabled: true, rules: [] })
    const aiData = aiRes.data || {}
    Object.assign(aiConfig, {
      enabled: aiData.enabled ?? false,
      activeModelId: aiData.activeModelId ?? '',
      historyCount: aiData.historyCount ?? 5,
      prompt: aiData.prompt || DEFAULT_PROMPT
    })
    const providers = modelsRes.data?.providers || []
    sharedModels.value = providers.flatMap((p: any) =>
      (p.models || []).map((m: any) => ({ id: m.id, label: `${p.name || '未命名厂商'} / ${m.model}` }))
    )
  } catch (error) {
    ElMessage.error(error || '获取审核设置失败')
  }
}

onMounted(() => {
  getAuditSettings()
})
</script>

<template>
  <el-card shadow="never">
    <el-tabs v-model="activeTab">
      <!-- 敏感词设置 -->
      <el-tab-pane label="敏感词设置" name="sensitive">
        <el-form label-width="auto">
          <el-form-item label="敏感词审核">
            <el-checkbox v-model="sensitiveConfig.enableSensitiveWordsAudit">启用</el-checkbox>
          </el-form-item>
          <el-form-item label="敏感词列表">
            <div class="flex flex-wrap items-center gap-2">
              <el-tag
                v-for="word in sensitiveConfig.sensitiveWords"
                :key="word"
                closable
                @close="handleSensitiveClose(word)"
              >
                {{ word }}
              </el-tag>
              <el-input
                v-if="sensitiveInputVisible"
                ref="sensitiveInputRef"
                v-model="sensitiveInputValue"
                class="input-new-tag"
                size="small"
                @keyup.enter="handleSensitiveInputConfirm"
                @blur="handleSensitiveInputConfirm"
              />
              <el-button v-else class="button-new-tag" size="small" type="primary" @click="showSensitiveInput">
                + 添加敏感词
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveSensitiveConfig">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 黑白名单设置 -->
      <el-tab-pane label="黑白名单设置" name="blackwhite">
        <el-form label-width="auto">
          <el-alert type="info" show-icon class="mb-4">
            <p>白名单用户发帖免审核，黑名单用户发帖自动拦截</p>
          </el-alert>
          <el-form-item label="白名单用户">
            <div class="user-list flex flex-wrap items-center gap-2">
              <el-tag
                v-for="user in blackWhiteConfig.whiteListUsers"
                :key="user.userId"
                closable
                type="success"
                @close="
                  blackWhiteConfig.whiteListUsers = blackWhiteConfig.whiteListUsers.filter(
                    (u) => u.userId !== user.userId
                  )
                "
              >
                {{ user.userName }} (ID: {{ user.userId }})
              </el-tag>
              <el-button class="h-8" type="primary" size="small" @click="whiteSearch.visible = true">
                + 添加用户
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="黑名单用户">
            <div class="user-list flex flex-wrap items-center gap-2">
              <el-tag
                v-for="user in blackWhiteConfig.blackListUsers"
                :key="user.userId"
                closable
                type="danger"
                @close="
                  blackWhiteConfig.blackListUsers = blackWhiteConfig.blackListUsers.filter(
                    (u) => u.userId !== user.userId
                  )
                "
              >
                {{ user.userName }} (ID: {{ user.userId }})
              </el-tag>
              <el-button class="h-8" type="danger" size="small" @click="blackSearch.visible = true">
                + 添加用户
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveBlackWhiteConfig">保存设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 审核规则 -->
      <el-tab-pane label="审核规则" name="rules">
        <el-form label-width="auto">
          <el-alert type="info" show-icon class="mb-4">
            <p>规则为 QLExpress 布尔表达式，按优先级从上到下评估，命中首个规则即按其动作处理；未命中则进入 AI 审核（若开启）或直接放行。</p>
          </el-alert>

          <el-collapse class="mb-4">
            <el-collapse-item title="表达式字段与函数速查" name="ref">
              <pre class="text-xs leading-6 text-gray-600">poster.userId / poster.userName / poster.registerTime / poster.registerDays / poster.location / poster.lastActiveDate / poster.postCount / poster.commentCount
post.isNew（新建 true / 编辑 false）/ post.contentType（THREAD/COMMENT/REPLY）/ post.forumId / post.forumName / post.subject / post.ip / post.ipLocation / post.createTime / post.hour
content.text / content.length / content.linkCount / content.externalLinkCount / content.hasLink / content.imageCount
函数：contains(text, keyword) 包含判断；matchRegex(text, pattern) 正则匹配

示例：
poster.registerDays &lt; 7 &amp;&amp; content.linkCount &gt; 0
post.isNew == false
matchRegex(content.text, "\\d{11}")</pre>
            </el-collapse-item>
          </el-collapse>

          <el-form-item label="规则引擎">
            <el-switch v-model="rulesConfig.enabled" />
          </el-form-item>

          <div v-for="(rule, index) in rulesConfig.rules" :key="rule.id" class="rule-item">
            <div class="flex flex-wrap items-center gap-2">
              <el-switch v-model="rule.enabled" />
              <el-input v-model="rule.name" placeholder="规则名称" class="rule-name" />
              <el-select v-model="rule.action" class="rule-action">
                <el-option v-for="o in ruleActionOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
              <span class="text-xs text-gray-500">优先级</span>
              <el-input-number v-model="rule.priority" :min="0" :max="999" size="small" />
              <el-button-group>
                <el-button size="small" :disabled="index === 0" @click="moveRule(index, -1)">上移</el-button>
                <el-button
                  size="small"
                  :disabled="index === rulesConfig.rules.length - 1"
                  @click="moveRule(index, 1)"
                >
                  下移
                </el-button>
              </el-button-group>
              <el-button size="small" @click="validateRuleExpression(rule)">校验</el-button>
              <el-button size="small" type="danger" @click="removeRule(index)">删除</el-button>
            </div>
            <el-input
              v-model="rule.expression"
              type="textarea"
              :rows="2"
              placeholder="例如：poster.registerDays < 7 && content.linkCount > 0"
              class="mt-2"
            />
          </div>

          <el-form-item class="mt-4">
            <el-button type="primary" plain @click="addRule">+ 添加规则</el-button>
            <el-button type="primary" @click="saveRules">保存规则</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- AI 审核 -->
      <el-tab-pane label="AI 审核" name="ai">
        <el-form label-width="auto">
          <el-alert type="info" show-icon class="mb-4">
            <p>
              开启后，规则与敏感词未命中的内容先进入「审核中」，由「生效模型」异步判定：正常则自动发布，违规或存疑则转人工复审。AI 会附带发帖人信息、发帖信息与历史发帖/回帖上下文。
            </p>
          </el-alert>

          <el-form-item label="启用 AI 审核">
            <el-switch v-model="aiConfig.enabled" />
          </el-form-item>

          <el-form-item label="生效模型">
            <el-select v-model="aiConfig.activeModelId" placeholder="选择生效模型" class="w-60">
              <el-option v-for="m in sharedModels" :key="m.id" :label="m.label" :value="m.id" />
            </el-select>
            <el-link type="primary" class="ml-2" @click="navigateTo('/admin/ai-models')">去模型管理</el-link>
          </el-form-item>

          <el-form-item label="历史上下文条数">
            <el-input-number v-model="aiConfig.historyCount" :min="0" :max="20" />
            <span class="text-xs text-gray-500 ml-2">发帖/回帖各取 N 条作为 AI 上下文</span>
          </el-form-item>

          <el-form-item label="审核提示词">
            <el-input v-model="aiConfig.prompt" type="textarea" :rows="6" />
          </el-form-item>

          <el-divider content-position="left">测试</el-divider>

          <el-form-item label="测试内容">
            <el-input v-model="aiTestContent" />
          </el-form-item>
          <el-form-item>
            <el-button :loading="aiTesting" @click="handleAiTest">测试审核</el-button>
            <el-button type="primary" @click="saveAiConfig">保存设置</el-button>
          </el-form-item>
          <el-alert v-if="aiTestResult" :title="aiTestResult" type="info" :closable="false" />
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <!-- 白名单搜索弹窗 -->
  <el-dialog v-model="whiteSearch.visible" title="添加白名单用户" width="30%">
    <el-input v-model="whiteSearch.query" placeholder="输入用户名搜索" @input="handleUserSearch('white')" />
    <div class="search-results mt-4">
      <div
        v-for="user in whiteSearch.results"
        :key="user.userId"
        class="search-item p-2 hover:bg-gray-100 cursor-pointer"
        @click="addUserToList(user, 'white', 'white')"
      >
        {{ user.userName }} (ID: {{ user.userId }})
      </div>
    </div>
  </el-dialog>

  <!-- 黑名单搜索弹窗 -->
  <el-dialog v-model="blackSearch.visible" title="添加黑名单用户" width="30%">
    <el-input v-model="blackSearch.query" placeholder="输入用户名搜索" @input="handleUserSearch('black')" />
    <div class="search-results mt-4">
      <div
        v-for="user in blackSearch.results"
        :key="user.userId"
        class="search-item p-2 hover:bg-gray-100 cursor-pointer"
        @click="addUserToList(user, 'black', 'black')"
      >
        {{ user.userName }} (ID: {{ user.userId }})
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.el-tag + .el-tag {
  margin-left: 10px;
}

.button-new-tag {
  padding-top: 0;
  padding-bottom: 0;
}

.input-new-tag {
  width: 120px;
  margin-left: 10px;
  vertical-align: bottom;
}

.mb-4 {
  margin-bottom: 16px;
}

:deep(.el-dialog__body) {
  padding: 10px 20px;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
}

.user-list {
  gap: 8px;
}

.el-tag {
  margin: 0 !important;
}

.rule-item {
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
}

.rule-name {
  width: 180px;
}

.rule-action {
  width: 110px;
}
</style>
