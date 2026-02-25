export interface ApiResponse<T = any> {
  success: boolean
  errCode?: string
  errMessage?: string
  data: T
}
export interface CustomPage<T> {
  records: T[]
  total: number
  size: number
  current: number
  hasNext: boolean
  loading: boolean
  errMsg: undefined | string
}

export interface Author {
  authorId: null | number
  authorName: null | string
  avatarUrl: null | string
  intro: null | string
  threads: null | number
  comments: null | number
  fans: null | number
  followed: boolean
}

export interface Thread {
  threadId: number
  forumId: number
  forumName: string
  forumNickName: string
  categoryId: number
  content: string
  comments: number
  subject: string
  top: boolean
  blocked: boolean
  closed: boolean
  digest: boolean
  author: null | Author
  createTime: string
  updateAuthor: null | Author
  updateTime: string
  docType: number
  views: number
  likes: number
  liked: boolean
  dislikes: number
  collections: number
  collected: boolean
  auditStatus: number
  commentOrder: number
  imageCount: number
  seoTitle: string
  seoContent: string
  brief?: string
  displayTime?: string
  errMsg: undefined | string
}

export interface CommentVO {
  replies: null | []
  commentId: number
  replyCount: number
  images: string[]
  threadId: number
  threadTitle: string
  threadCommentCount: number
  likes: number
  message: string
  createTime: string
  updateTime: string | null
  authorId: number
  authorName: string
  avatarUrl: string
  loading?: boolean
  pageNo?: number | null | undefined
  liked?: boolean
  replies?: CommentReplyVO[]
}

export interface CommentReplyVO {
  /** 主评论ID */
  commentId: number
  /** 回复ID */
  replyId: number
  /** 主题帖ID */
  threadId: number
  /** 点赞数 */
  likes: number
  /** 是否已点赞 */
  liked: boolean
  /** 回复内容 */
  message: string
  /** 创建时间 */
  createTime: string
  /** 回复作者信息 */
  authorId: number
  authorName: string
  avatarUrl: string
  /** 被回复用户信息 */
  replyAuthorId: number | null
  replyAuthorName: string | null
  replyAvatarUrl: string | null
}

export interface ThreadCmd {
  threadId?: number
  forumId: number
  categoryId?: number
  commentOrder?: number
  content: string
  subject: string
}

export interface ForumInfo {
  forumId: number
  name: string
  nickName: string
  threadCount: number
  followCount: number
  shortBrief: string
  brief: string
  iconName: string
  seoTitle: string
  seoKeywords: string
  seoContent: string
  backgroundImage: string
  isCurrentUserAdmin?: boolean
  followed?: boolean
}

export interface ForumMenuItemVO {
  forumId: number
  name: string
  nickName: string
  iconName: string
}

export interface UserVO {
  userId: number
  userName: string
  avatar?: string
  intro?: string
  createTime: string
  followed: boolean
  lastActiveDate?: string
}

export interface NotificationVO {
  postId: number
  postTitle: string
  forumId: number
  forumName: string
  forumNickName: string
  notificationType: number | null
  notificationStatus: number | null
  subject: string | null
  message: string
  url: string | null
  authorId: number
  authorName: string
  createTime: string
}

export interface Query {
  pageNo: number
  pageSize: number
  orderBy?: string
}

// 角色相关类型
export interface RoleCreateCmd {
  roleName: string
  roleKey: string
  remark: string
  perms: string[]
  priority: number
}

export interface RoleUpdateCmd {
  roleId: number
  roleName: string
  remark: string
  roleStatus: number
  perms: string[]
  priority: number
}

// 权限相关类型
export interface PermissionCreateCmd {
  parentId: number
  permName: string
  perms: string
  remark: string
  permType: number
  permOrder: number
}

export interface PermissionUpdateCmd {
  permId: number
  parentId: number
  permName: string
  permStatus: number
  perms: string
  permType: number
  permOrder: number
  remark: string
}

// 用户角色相关类型
export interface UserRoleVO {
  userName: string
  roleKey: string
  roleScope: string
  createUserName: string
  expireTime: string
  createTime: string
}

export interface BanVO {
  id: number
  userId: number
  userName: string
  forumId?: number
  forumName?: string
  reason: string
  expireTime?: string
  createTime: string
  expired: boolean
}

export interface BanCreateCmd {
  userId: number
  forumId?: number
  reason: string
  expireTime?: string
}

// 补充LikeCmd类型
declare interface LikeCmd {
  targetType: number
  targetId: number
}

interface UserInfo {
  userId?: number
  userName?: string
  avatar?: string
  [key: string]: any
}

interface CreateReportCommand {
  targetId: number
  targetType: 'THREAD' | 'COMMENT' | 'REPLY'
  forumId: number
  reportType: 'SPAM' | 'PORN' | 'HATE_SPEECH' | 'COPYRIGHT' | 'DISORDERLY' | 'VIOLATION' | 'OTHER'
  reportReason?: string
}

interface PagingQuery {
  pageNo?: number
  pageSize?: number
  orderByColumn?: string
  isAsc?: boolean
}

interface ReportInfo {
  reportId: number
  targetId: number
  targetType: 'THREAD' | 'COMMENT' | 'REPLY'
  forumId: number
  reportType: 'SPAM' | 'PORN' | 'HATE_SPEECH' | 'COPYRIGHT' | 'DISORDERLY' | 'VIOLATION' | 'OTHER'
  reportReason?: string
  handleStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  handleReason?: string
  createBy: number
  createTime: string
  updateBy?: number
  updateTime?: string
  reporterName?: string
  handlerName?: string
  forumName?: string
  targetContent?: any
}

interface HandleReportCommand {
  reportId: number
  handleStatus: 'APPROVED' | 'REJECTED'
  handleReason?: string
  notifyUser?: boolean
}

interface ReportQuery extends PagingQuery {
  targetType?: 'THREAD' | 'COMMENT' | 'REPLY'
  forumId?: number
  handleStatus?: 'PENDING' | 'APPROVED' | 'REJECTED'
  reporterId?: number
  handlerId?: number
}

// ========== 设备相关类型 ==========

/**
 * 设备信息接口
 * 用于存储设备 ID 和生成信息
 */
export interface DeviceInfo {
  /** 设备唯一标识符 */
  deviceId: string
  /** 生成时间戳 */
  generatedAt: number
  /** 指纹数据摘要 (用于调试,不存储完整特征) */
  fingerprintData: string
}

/**
 * 设备 ID 类型别名
 * 用于类型安全和代码可读性
 */
export type DeviceId = string
