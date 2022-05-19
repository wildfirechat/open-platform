// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import App from './App';
import router from './router';
import ElementUI from 'element-ui';
import store from './store';
import 'element-ui/lib/theme-chalk/index.css'; // 默认主题
import VueI18n from 'vue-i18n'


Vue.config.productionTip = false

Vue.use(ElementUI, {size: 'small'});

Vue.use(VueI18n)

const i18n = new VueI18n({
    // 使用localStorage存储语言状态是为了保证页面刷新之后还是保持原来选择的语言状态
    locale: localStorage.getItem('lang') ? localStorage.getItem('lang') : 'zh-CN', // 定义默认语言为中文
    messages: {
        'zh-CN': require('@/assets/languages/zh-CN.json'),
        'zh-TW': require('@/assets/languages/zh-TW.json'),
        'en': require('@/assets/languages/en.json')
    }
})

/* eslint-disable no-new */
export default new Vue({
    el: '#app',
    router,
    store,
    i18n,
    components: {App},
    render: h => h(App)
})
