/**
 * 文章状态枚举
 */
export const ARTICLE_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  OFFLINE: 2,
  ARCHIVED: 3,
  SCHEDULED: 4,
  PRIVATE: 5,
};

/**
 * 状态对应的文本映射
 */
export const ARTICLE_STATUS_TEXT = {
  [ARTICLE_STATUS.DRAFT]: '草稿',
  [ARTICLE_STATUS.PUBLISHED]: '已发布',
  [ARTICLE_STATUS.OFFLINE]: '已下架',
  [ARTICLE_STATUS.ARCHIVED]: '已归档',
  [ARTICLE_STATUS.SCHEDULED]: '定时发布',
  [ARTICLE_STATUS.PRIVATE]: '私密',
};

/**
 * 状态对应的颜色/标签类型（可用于 Element Plus 的 el-tag）
 */
export const ARTICLE_STATUS_TYPE = {
  [ARTICLE_STATUS.DRAFT]: 'info',
  [ARTICLE_STATUS.PUBLISHED]: 'success',
  [ARTICLE_STATUS.OFFLINE]: 'danger',
  [ARTICLE_STATUS.ARCHIVED]: 'warning',
  [ARTICLE_STATUS.SCHEDULED]: 'primary',
  [ARTICLE_STATUS.PRIVATE]: 'default',
};

/**
 * 状态对应的图标（可选，如果使用 Element Plus 图标）
 */
export const ARTICLE_STATUS_ICON = {
  [ARTICLE_STATUS.DRAFT]: 'EditPen',
  [ARTICLE_STATUS.PUBLISHED]: 'Select',
  [ARTICLE_STATUS.OFFLINE]: 'Remove',
  [ARTICLE_STATUS.ARCHIVED]: 'Folder',
  [ARTICLE_STATUS.SCHEDULED]: 'Clock',
  [ARTICLE_STATUS.PRIVATE]: 'Lock',
};

/**
 * 生成用于下拉选择框的选项列表（适配 el-option）
 */
export const getStatusOptions = () => {
  return Object.entries(ARTICLE_STATUS).map(([key, value]) => ({
    label: ARTICLE_STATUS_TEXT[value],
    value: value,
    // 可选：扩展其他属性
    type: ARTICLE_STATUS_TYPE[value],
  }));
};

/**
 * 根据状态码获取对应文本
 */
export const getStatusText = (status) => {
  return ARTICLE_STATUS_TEXT[status] || '未知状态';
};

/**
 * 根据状态码获取对应标签类型
 */
export const getStatusType = (status) => {
  return ARTICLE_STATUS_TYPE[status] || 'default';
};
