import {createApp} from 'vue';
import {createI18n} from 'vue-i18n';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';

// 样式加载顺序很重要，不要调整：
// 1) Element Plus 自带样式；2) 设计令牌，覆盖 Element Plus 的 CSS 变量；3) 全局基础样式。
import 'element-plus/dist/index.css';
import './assets/css/tokens.css';
import './assets/css/base.css';

import App from './App.vue';
import router from './router';
import store from './store';

const i18n = createI18n({
    // 保持 Options API 的 this.$t 用法
    legacy: true,
    globalInjection: true,
    // 使用localStorage存储语言状态是为了保证页面刷新之后还是保持原来选择的语言状态
    locale: localStorage.getItem('lang') ? localStorage.getItem('lang') : 'zh-CN', // 定义默认语言为中文
    fallbackLocale: 'zh-CN',
    messages: {
        'zh-CN': require('@/assets/languages/zh-CN.json'),
        'zh-TW': require('@/assets/languages/zh-TW.json'),
        'en': require('@/assets/languages/en.json')
    }
})

const app = createApp(App);

app.use(router);
app.use(store);
app.use(i18n);
// 用默认尺寸而不是 small：控件高度 32px，和新的间距节奏匹配，也更好点
app.use(ElementPlus, {locale: zhCn});

app.mount('#app');

export default app;
