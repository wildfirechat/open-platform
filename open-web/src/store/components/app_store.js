import Api from '@/api/api'

export default {
    state: {
        apps: [],
        channels: [],
        robots: [],
        // 列表是否已经拉取过一次。没有这个标记的话，首次加载期间页面会先闪一下
        // "还没有应用"的空状态，然后数据才进来。
        loaded: false,
    },
    mutations: {
        saveAppList(state, payload) {
            state.apps = payload;
        }
    },
    actions: {
        getAppList(context) {
            return Api.getAppList(0).then((data) => {
                context.state.apps = data.filter(app => app.type === 0 || app.type === 3);
                context.state.channels = data.filter(app => app.type === 1);
                context.state.robots = data.filter(app => app.type === 2);
            }).finally(() => {
                context.state.loaded = true;
            })
        },

        async createApp({dispatch}, payload) {
            await Api.createApp(payload);
            return dispatch('getAppList');
        },
        async updateApp({dispatch}, payload) {
            await Api.updateApp(payload);
            return dispatch('getAppList')
        },
        async deleteApp({dispatch}, payload) {
            await Api.deleteApp(payload);
            return dispatch('getAppList')
        }
    }
}
