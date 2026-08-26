/**
 * 日常状态枚举
 */
export const DAILY_STATUS = {
  DRAFT: 0,
  PUBLISHED: 1,
  OFFLINE: 2,
  SCHEDULED: 3,
  PRIVATE: 4,
};

/**
 * 状态对应的文本映射
 */
export const DAILY_STATUS_TEXT = {
  [DAILY_STATUS.DRAFT]: '草稿',
  [DAILY_STATUS.PUBLISHED]: '已发布',
  [DAILY_STATUS.OFFLINE]: '已下架',
  [DAILY_STATUS.SCHEDULED]: '定时发布',
  [DAILY_STATUS.PRIVATE]: '私密',
};

/**
 * 状态对应的标签类型（Element Plus el-tag）
 */
export const DAILY_STATUS_TYPE = {
  [DAILY_STATUS.DRAFT]: 'info',
  [DAILY_STATUS.PUBLISHED]: 'success',
  [DAILY_STATUS.OFFLINE]: 'danger',
  [DAILY_STATUS.SCHEDULED]: 'warning',
  [DAILY_STATUS.PRIVATE]: 'primary',
};

/**
 * 日常类型 0纯文字 1图片 2文件 3图文混合
 */
export const DAILY_TYPE = {
  TEXT: 0,
  IMAGE: 1,
  FILE: 2,
  MIXED: 3,
};

/**
 * 类型对应的文本映射
 */
export const DAILY_TYPE_TEXT = {
  [DAILY_TYPE.TEXT]: '纯文字',
  [DAILY_TYPE.IMAGE]: '图片',
  [DAILY_TYPE.FILE]: '文件',
  [DAILY_TYPE.MIXED]: '图文混合',
};

/**
 * 类型对应的标签类型
 */
export const DAILY_TYPE_TAG = {
  [DAILY_TYPE.TEXT]: 'info',
  [DAILY_TYPE.IMAGE]: 'success',
  [DAILY_TYPE.FILE]: 'warning',
  [DAILY_TYPE.MIXED]: 'primary',
};

/**
 * 生成用于下拉选择框的状态选项（适配 el-option）
 */
export const getDailyStatusOptions = () => {
  return Object.entries(DAILY_STATUS).map(([key, value]) => ({
    label: DAILY_STATUS_TEXT[value],
    value: value,
    type: DAILY_STATUS_TYPE[value],
  }));
};

/**
 * 根据状态码获取对应文本
 */
export const getDailyStatusText = (status) => {
  return DAILY_STATUS_TEXT[status] || '未知状态';
};

/**
 * 根据状态码获取对应标签类型
 */
export const getDailyStatusType = (status) => {
  return DAILY_STATUS_TYPE[status] || 'default';
};

/**
 * 根据类型码获取对应文本
 */
export const getDailyTypeText = (type) => {
  return DAILY_TYPE_TEXT[type] || '未知类型';
};

/**
 * 根据类型码获取对应标签类型
 */
export const getDailyTypeTag = (type) => {
  return DAILY_TYPE_TAG[type] || 'default';
};

/**
 * 根据图片/文件数量自动计算日常类型
 */
export const computeDailyType = (images = [], files = []) => {
  const hasImages = Array.isArray(images) && images.length > 0;
  const hasFiles = Array.isArray(files) && files.length > 0;
  if (hasImages && hasFiles) return DAILY_TYPE.MIXED;
  if (hasImages) return DAILY_TYPE.IMAGE;
  if (hasFiles) return DAILY_TYPE.FILE;
  return DAILY_TYPE.TEXT;
};
