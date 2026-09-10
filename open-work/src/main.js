import {createApp} from 'vue'

// 样式在组件之前加载：theme.css 定义设计令牌，main.css 依赖这些令牌
import './assets/theme.css'
import './assets/main.css'

import App from './App.vue'

createApp(App).mount('#app')
