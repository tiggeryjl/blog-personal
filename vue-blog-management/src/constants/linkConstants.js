/**
 * 友链审核状态枚举
 */
export const LINK_AUDIT_STATUS = {
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2
}

/**
 * 友链审核状态文本映射
 */
export const LINK_AUDIT_STATUS_TEXT = {
  [LINK_AUDIT_STATUS.PENDING]: '待审核',
  [LINK_AUDIT_STATUS.APPROVED]: '已通过',
  [LINK_AUDIT_STATUS.REJECTED]: '已拒绝'
}

/**
 * 友链审核状态对应的标签类型
 */
export const LINK_AUDIT_STATUS_TYPE = {
  [LINK_AUDIT_STATUS.PENDING]: 'warning',
  [LINK_AUDIT_STATUS.APPROVED]: 'success',
  [LINK_AUDIT_STATUS.REJECTED]: 'danger'
}

/**
 * 友链展示状态枚举
 */
export const LINK_STATUS = {
  DISABLE: 0,
  ENABLE: 1
}

/**
 * 友链展示状态文本映射
 */
export const LINK_STATUS_TEXT = {
  [LINK_STATUS.DISABLE]: '禁用',
  [LINK_STATUS.ENABLE]: '启用'
}

/**
 * 友链展示状态对应的标签类型
 */
export const LINK_STATUS_TYPE = {
  [LINK_STATUS.DISABLE]: 'info',
  [LINK_STATUS.ENABLE]: 'success'
}

/**
 * 根据审核状态获取文本
 */
export const getLinkAuditStatusText = (status) =>
  LINK_AUDIT_STATUS_TEXT[status] || '未知'

/**
 * 根据审核状态获取标签类型
 */
export const getLinkAuditStatusType = (status) =>
  LINK_AUDIT_STATUS_TYPE[status] || 'info'

/**
 * 根据展示状态获取文本
 */
export const getLinkStatusText = (status) =>
  LINK_STATUS_TEXT[status] || '未知'

/**
 * 根据展示状态获取标签类型
 */
export const getLinkStatusType = (status) =>
  LINK_STATUS_TYPE[status] || 'info'
