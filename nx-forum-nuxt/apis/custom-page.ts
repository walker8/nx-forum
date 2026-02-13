import Http from '../utils/request'

export type PageCmd = {
    pageId?: number | null
    pageName: string
    pageCode: string
    accessLevel: number
    pageStatus: number
}

export type PageQuery = {
    pageName?: string
    pageCode?: string
    accessLevel?: number
    pageStatus?: number
    pageNo?: number
    pageSize?: number
}

export type PageVO = {
    pageId: number
    pageName: string
    pageCode: string
    accessLevel: number
    pageStatus: number
    createTime: string
    updateTime: string
}

export type PageInfoVO = {
    pageName: string
    content: string
}

// 分页查询自定义页面
export const queryPagesByAdmin = (query: PageQuery) => {
    return Http.get(`/v1/admin/pages`, query)
}

// 创建页面
export const createPage = (page: PageCmd) => {
    return Http.post(`/v1/admin/pages`, page)
}

// 更新页面信息
export const updatePage = (pageId: number, page: PageCmd) => {
    return Http.put(`/v1/admin/pages/${pageId}`, page)
}

// 删除页面
export const deletePage = (pageId: number) => {
    return Http.delete(`/v1/admin/pages/${pageId}`)
}

// 管理员获取页面内容
export const getPageContentByAdmin = (pageId: number) => {
    return Http.get(`/v1/admin/pages/${pageId}/content`)
}

// 获取页面完整信息（包含页面名称和内容）
export const getPage = (pageCode: string) => {
    return Http.get(`/v1/pages/${pageCode}`)
}

// 更新页面内容
export const updatePageContent = (pageId: number, content: string) => {
    return Http.put(`/v1/admin/pages/${pageId}/content`, content)
}
