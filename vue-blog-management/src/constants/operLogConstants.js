/**
 * 操作类型枚举
 */
export const OPER_LOG_TYPE = {
  ADD: 1,
  UPDATE: 2,
  DELETE: 3,
  QUERY: 4,
  EXPORT: 5,
  IMPORT: 6,
};

/**
 * 操作类型文本
 */
export const OPER_LOG_TYPE_TEXT = {
  [OPER_LOG_TYPE.ADD]: '新增',
  [OPER_LOG_TYPE.UPDATE]: '修改',
  [OPER_LOG_TYPE.DELETE]: '删除',
  [OPER_LOG_TYPE.QUERY]: '查询',
  [OPER_LOG_TYPE.EXPORT]: '导出',
  [OPER_LOG_TYPE.IMPORT]: '导入',
};

/**
 * 操作类型标签颜色
 */
export const OPER_LOG_TYPE_TAG = {
  [OPER_LOG_TYPE.ADD]: 'success',
  [OPER_LOG_TYPE.UPDATE]: 'warning',
  [OPER_LOG_TYPE.DELETE]: 'danger',
  [OPER_LOG_TYPE.QUERY]: 'info',
  [OPER_LOG_TYPE.EXPORT]: '',
  [OPER_LOG_TYPE.IMPORT]: '',
};

/**
 * 操作类型下拉选项
 */
export const OPER_LOG_TYPE_OPTIONS = Object.entries(OPER_LOG_TYPE_TEXT).map(([value, label]) => ({
  value: Number(value),
  label,
}));

/**
 * 操作状态文本
 */
export const OPER_LOG_STATUS_TEXT = {
  1: '成功',
  0: '失败',
};

/**
 * 操作状态标签颜色
 */
export const OPER_LOG_STATUS_TAG = {
  1: 'success',
  0: 'danger',
};

export const getOperLogTypeText = (type) => OPER_LOG_TYPE_TEXT[type] ?? '未知';

export const getOperLogTypeTagType = (type) => OPER_LOG_TYPE_TAG[type] ?? 'info';

export const getOperLogStatusText = (status) => OPER_LOG_STATUS_TEXT[status] ?? '未知';

export const getOperLogStatusTagType = (status) => OPER_LOG_STATUS_TAG[status] ?? 'info';
