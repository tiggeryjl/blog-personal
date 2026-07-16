import LayoutView from '@/views/layout/index.vue'

const modules = import.meta.glob('@/views/**/*.vue')

// 懒加载页面组件
export const loadView = (view) => {
  const path = `/src/views/${view}.vue`
  return modules[path]
}

/**
 * 递归解析后端返回的路由数组
 * 1. 自动生成唯一路由name 解决403权限拦截
 * 2. 把后端menuName、icon映射到route.meta 给侧边栏渲染用
 * 3. 把Layout字符串替换为Layout组件，页面字符串转为懒加载函数
 * @param {Array} routes 后端原始路由数组
 * @returns 前端可直接addRoute的标准路由数组
 */
export function filterAsyncRoutes(routes, parentPath = '') {
  const resultRoutes = []

  routes.forEach((route, index) => {
    const routeItem = { ...route }

    // 构建唯一名称：基于完整路径，防止重复
    const currentPath = route.path || ''
    const fullPath = parentPath ? `${parentPath}/${currentPath}` : currentPath
    // 如果路径为空，使用 'root' 加索引确保唯一
    let nameBase = fullPath || `root_${index}`
    // 替换非字母数字为下划线，并确保不以数字开头（可选）
    routeItem.name = nameBase.replace(/[^a-zA-Z0-9]/g, '_')
    // 如果 name 为空，给个默认
    if (!routeItem.name) routeItem.name = `route_${index}`

    // meta 映射
    routeItem.meta = {
      ...routeItem.meta,
      title: routeItem.menuName,
      icon: routeItem.icon,
      hidden: false
    }

    // 组件处理
    // 组件处理 先判断是不是Layout父容器
    if (routeItem.component === 'Layout') {
      routeItem.component = LayoutView
    } else if (routeItem.component) {
      let compPath = routeItem.component
      // 1. 全局删除所有 ../  ./  不管在开头还是中间
      compPath = compPath.replace(/\.{1,2}\//g, '')
      // 2. 全局删除 views/ 前缀
      compPath = compPath.replace(/^views\//, '')
      // 3. 去除首尾斜杠
      compPath = compPath.trim().replace(/^\/+|\/+$/g, '')
      // 4. 去除.vue后缀
      compPath = compPath.replace(/\.vue$/i, '')

      if (!compPath) compPath = 'index'
      // 保持你原来的导入写法不变
      routeItem.component = loadView(compPath)
    }

    // 递归处理子路由
    if (routeItem.children && routeItem.children.length > 0) {
      const childParent = fullPath || `root_${index}`
      routeItem.children = filterAsyncRoutes(routeItem.children, childParent)
    }

    resultRoutes.push(routeItem)
  })
  return resultRoutes
}