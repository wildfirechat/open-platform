import {createRouter, createWebHashHistory} from 'vue-router';

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        component: () => import('../components/page/Login.vue')
    },
    {
        path: '/',
        component: () => import('../components/common/Home.vue'),
        meta: {title: '系统首页'},
        children: [
            {
                path: '/index',
                component: () => import('../components/page/app/app.vue'),
                meta: {title: '应用开发'}
            },
            {
                path: '/dev/app',
                component: () => import('../components/page/app/app.vue'),
                meta: {title: '应用开发'}
            },
            {
                path: '/dev/channel',
                component: () => import('../components/page/channel/channel.vue'),
                meta: {title: '频道应用开发'}
            },
            {
                path: '/dev/robot',
                component: () => import('../components/page/robot/robot.vue'),
                meta: {title: '机器人应用开发'}

            },
            {
                path: '/updatePwd',
                component: () => import('../components/page/UpdatePwd.vue'),
                meta: {title: '更新密码'}
            },
        ]
    },
    {
        path: '/403',
        component: () => import('../components/page/403.vue')
    },
    {
        path: '/404',
        component: () => import('../components/page/404.vue')
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/404'
    }
]

export default createRouter({
    history: createWebHashHistory(),
    routes,
})
