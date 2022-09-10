import Vue from 'vue';
import Router from 'vue-router';

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/login',
            component: resolve => require(['../components/page/Login.vue'], resolve)
        },
        {
            path: '/',
            component: resolve => require(['../components/common/Home.vue'], resolve),
            meta: {title: '系统首页'},
            children: [
                {
                    path: '/index',
                    component: resolve => require(['../components/page/Index.vue'], resolve),
                    meta: {title: '野火开放平台'}
                },
                {
                    path: '/dev/app',
                    component: resolve => require(['../components/page/app/app.vue'], resolve),
                    meta: {title: '应用开发'}
                },
                {
                    path: '/dev/channel',
                    component: resolve => require(['../components/page/channel/channel.vue'], resolve),
                    meta: {title: '频道应用开发'}
                },
                {
                    path: '/dev/robot',
                    component: resolve => require(['../components/page/robot/robot.vue'], resolve),
                    meta: {title: '机器人应用开发'}

                },
                {
                    path: '/updatePwd',
                    component: resolve => require(['../components/page/UpdatePwd.vue'], resolve),
                    meta: {title: '更新密码'}
                },
            ]
        },
        {
            path: '/403',
            component: resolve => require(['../components/page/403.vue'], resolve)
        },
        {
            path: '/404',
            component: resolve => require(['../components/page/404.vue'], resolve)
        },
        {
            path: '*',
            redirect: '/404'
        }
    ]
})
