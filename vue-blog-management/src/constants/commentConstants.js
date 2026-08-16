/**
 * 评论类型枚举
 */
export const COMMENT_TYPE = {
  ARTICLE: 0,
  DAILY: 1,
  MESSAGE: 2
}

/**
 * 评论类型文本映射
 */
export const COMMENT_TYPE_TEXT = {
  [COMMENT_TYPE.ARTICLE]: '文章评论',
  [COMMENT_TYPE.DAILY]: '日常评论',
  [COMMENT_TYPE.MESSAGE]: '留言评论'
}

/**
 * 评论状态枚举
 */
export const COMMENT_STATUS = {
  HIDDEN: 0,
  NORMAL: 1
}

/**
 * 评论状态文本映射
 */
export const COMMENT_STATUS_TEXT = {
  [COMMENT_STATUS.HIDDEN]: '已隐藏',
  [COMMENT_STATUS.NORMAL]: '正常'
}

/**
 * 评论状态对应的标签类型
 */
export const COMMENT_STATUS_TYPE = {
  [COMMENT_STATUS.HIDDEN]: 'danger',
  [COMMENT_STATUS.NORMAL]: 'success'
}

/**
 * 留言类型枚举(仅留言评论使用)
 */
export const MESSAGE_TYPE = {
  COMMENT: 0,
  FEEDBACK: 1,
  FRIEND_LINK: 2
}

/**
 * 留言类型文本映射
 */
export const MESSAGE_TYPE_TEXT = {
  [MESSAGE_TYPE.COMMENT]: '评论留言',
  [MESSAGE_TYPE.FEEDBACK]: '反馈建议',
  [MESSAGE_TYPE.FRIEND_LINK]: '申请友链'
}

/**
 * 根据评论状态获取文本
 */
export const getCommentStatusText = (status) =>
  COMMENT_STATUS_TEXT[status] || '未知'

/**
 * 根据评论状态获取标签类型
 */
export const getCommentStatusType = (status) =>
  COMMENT_STATUS_TYPE[status] || 'info'

/**
 * 根据留言类型获取文本
 */
export const getMessageTypeText = (msgType) =>
  MESSAGE_TYPE_TEXT[msgType] || '—'
