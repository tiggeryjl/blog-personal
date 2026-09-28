export const ADMIN_NOTICE_RECONNECT_DELAY = 3000;

export const ADMIN_NOTICE_HEARTBEAT_INTERVAL = 25000;

export const ONLINE_COUNT_MESSAGE_TYPE = 'onlineCount';

export const ONLINE_COUNT_SUBSCRIBE_MESSAGE = 'subscribeOnlineCount';

export const NOTICE_DEDUP_CACHE_SIZE = 100;

export const NOTICE_TARGET_TYPE = Object.freeze({
  ARTICLE: 'article',
  DAILY: 'daily',
  LINK: 'link',
});

const NOTICE_TARGET_LABEL = Object.freeze({
  [NOTICE_TARGET_TYPE.ARTICLE]: '文章',
  [NOTICE_TARGET_TYPE.DAILY]: '日常',
  [NOTICE_TARGET_TYPE.LINK]: '友链',
});

const normalizeTargetType = (value) => {
  if (typeof value !== 'string') return null;
  const normalized = value.trim().toLowerCase();
  return Object.values(NOTICE_TARGET_TYPE).includes(normalized) ? normalized : null;
};

const hasExplicitTargetType = (notice) =>
  [notice?.targetType].some((value) => value != null && String(value).trim() !== '');

export const getNoticeTargetType = (notice) => {
  const targetType = normalizeTargetType(notice?.targetType);
  if (targetType) return targetType;
  if (hasExplicitTargetType(notice)) return null;
  if (notice?.type === 'link') return NOTICE_TARGET_TYPE.LINK;
  if (String(notice?.actionText || '').includes('日常')) return NOTICE_TARGET_TYPE.DAILY;
  return NOTICE_TARGET_TYPE.ARTICLE;
};

export const getNoticeTargetId = (notice) => {
  return [notice?.targetId].find((value) => value !== null && value !== undefined && value !== '');
};

export const getNoticeTargetTitle = (notice) => {
  const title = [notice?.targetTitle].find((value) => value != null && String(value).trim() !== '');
  return title == null ? '' : String(title).trim();
};

export const getNoticeTargetLabel = (notice) => {
  return NOTICE_TARGET_LABEL[getNoticeTargetType(notice)] || '内容';
};

export const getNoticeActionDescription = (notice) => {
  const actionText = String(notice?.actionText || '').trim() || '操作';
  const targetLabel = getNoticeTargetLabel(notice);
  if (actionText === '回复评论') return ` 回复了${targetLabel}评论：`;
  if (actionText === '点赞评论') return ` 点赞了${targetLabel}评论：`;
  return actionText.includes(targetLabel) ? ` ${actionText}：` : ` ${actionText}了${targetLabel}：`;
};

export const getNoticeActionButtonText = (notice) => {
  const targetType = getNoticeTargetType(notice);
  if (targetType === NOTICE_TARGET_TYPE.LINK) {
    return String(notice?.actionText || '').includes('催促') ? '去审核' : '查看友链';
  }
  if (targetType === NOTICE_TARGET_TYPE.DAILY) return '查看日常';
  if (targetType === NOTICE_TARGET_TYPE.ARTICLE) return '查看文章';
  return '查看详情';
};

export const getNoticeTargetRoute = (notice) => {
  const targetType = getNoticeTargetType(notice);
  const targetId = getNoticeTargetId(notice);

  if (targetType === NOTICE_TARGET_TYPE.LINK) {
    return { path: '/linkInfo' };
  }
  if (targetId == null) return null;
  if (targetType === NOTICE_TARGET_TYPE.DAILY) {
    const query = { previewId: String(targetId) };
    if (notice?.id != null) query.noticeId = String(notice.id);
    return { path: '/dailyWork', query };
  }
  if (targetType === NOTICE_TARGET_TYPE.ARTICLE) {
    return { path: '/articleDetail', query: { id: String(targetId) } };
  }
  return null;
};
