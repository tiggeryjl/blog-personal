import { createApp } from 'vue'
// import { createPinia } from 'pinia'
import router from './router'
import pinia from './stores'
import permDirective from './directives/perm'

import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 引入 Font Awesome
import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons' // 全部实心图标
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

import './assets/main.css'

// 注册所有图标
library.add(fas)

const app = createApp(App)

// app.use(createPinia())
app.use(router)
app.use(pinia)
app.use(permDirective)
app.use(ElementPlus, { locale: zhCn })
// 注册图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.component('FontAwesomeIcon', FontAwesomeIcon)

app.mount('#app')
