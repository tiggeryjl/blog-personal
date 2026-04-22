// 设置主题
export function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme)
  localStorage.setItem('blog-theme', theme)
}

// 读取本地缓存的主题
export function getTheme() {
  return localStorage.getItem('blog-theme') || 'default'
}

// 初始化主题
export function initTheme() {
  const theme = getTheme()
  setTheme(theme)
}