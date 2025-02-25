import Http from '../utils/request'
export interface ImageVO {
  imageId: number
  imagePath: string
  imageType: number
  fileSize: number
  fileHash: string
  storageType: number
  authorId: number
  authorName: string
  createTime: string
}

export interface CustomPageImageVO {
  records: ImageVO[]
  total: number
  size: number
  current: number
  hasNext: boolean
}

export const uploadImage = (formData: any) => {
  return Http.post(`/v1/images/upload`, formData)
}

export const uploadAvatar = (formData: any) => {
  return Http.post(`/v1/images/avatar/upload`, formData)
}

interface ImageQueryParams {
  pageNo?: number
  pageSize?: number
  imageType?: number
  storageType?: number
  createBy?: number
}

/**
 * 分页查询图片
 */
export const queryPage = (params: ImageQueryParams) => {
  return Http.get('/v1/admin/images', params)
}

/**
 * 删除图片(批量)
 */
export const deleteImage = (imageIds: number[]) => {
  return Http.delete('/v1/admin/images', imageIds)
}
