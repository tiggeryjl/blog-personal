// 游客点赞本地记录
// 未登录用户点赞时后端按 IP 记录，但不会回传 liked 状态，
// 这里按“目标类型_目标ID”在本地记录，保证刷新后仍能显示已点赞。
const GUEST_LIKED_KEY = 'guest_liked_targets';

const todayKey = () => {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
};

const buildKey = (targetType, targetId) => `${targetType}_${targetId}`;

// 读取今日的游客点赞记录(跨天的记录自动失效)
const readTodayMap = () => {
  try {
    const raw = localStorage.getItem(GUEST_LIKED_KEY);
    const map = raw ? JSON.parse(raw) : {};
    if (!map || typeof map !== 'object' || Array.isArray(map)) return {};
    const today = todayKey();
    const result = {};
    Object.entries(map).forEach(([key, date]) => {
      if (date === today) result[key] = date;
    });
    return result;
  } catch (error) {
    return {};
  }
};

// 判断游客今日是否已点赞某目标
export const isGuestLiked = (targetType, targetId) =>
  readTodayMap()[buildKey(targetType, targetId)] === todayKey();

// 记录游客点赞
export const rememberGuestLiked = (targetType, targetId) => {
  try {
    const map = readTodayMap();
    map[buildKey(targetType, targetId)] = todayKey();
    localStorage.setItem(GUEST_LIKED_KEY, JSON.stringify(map));
  } catch (error) {
    // 本地记录失败不影响点赞结果
  }
};

export default { isGuestLiked, rememberGuestLiked };
