import request from '../utils/request'

// 厂商下的单个模型（模型 ID）
export interface AiModelEntry {
  id: string
  model: string
}

// AI 厂商（连接信息）
export interface AiModelProvider {
  id: string
  name: string
  apiUrl: string
  apiKey: string
  timeoutSeconds: number
  enabled: boolean
  models: AiModelEntry[]
}

export interface AiModelConfigsDTO {
  providers: AiModelProvider[]
}

// 获取共享模型库
export function getAiModels() {
  return request.get('/v1/admin/ai-models')
}

// 更新共享模型库
export function updateAiModels(data: AiModelConfigsDTO) {
  return request.put('/v1/admin/ai-models', data)
}

// 测试模型连通性
export function testAiModelConnectivity(data: {
  providerId?: string
  apiUrl: string
  apiKey: string
  timeoutSeconds: number
  model: string
}) {
  return request.post('/v1/admin/ai-models/test', data)
}
