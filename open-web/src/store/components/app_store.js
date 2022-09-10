import Api from '@/api/api'

export default {
    state: {
        apps: [],
        channels: [],
        robots: [],
    },
    mutations: {
        saveAppList(state, payload) {
            state.apps = payload;
        }
    },
    actions: {
        getAppList(context) {
            Api.getAppList(0).then((data) => {
                context.state.apps = data.filter(app => app.type === 0);
                context.state.channels = data.filter(app => app.type === 1);
                context.state.robots = data.filter(app => app.type === 2);
            })
        },

        async createApp({dispatch}, payload) {
            await Api.createApp(payload);
            dispatch('getAppList');
        },
        async updateApp({dispatch}, payload) {
            await Api.updateApp(payload);
            dispatch('getAppList')
        },
        async deleteApp({dispatch}, payload) {
            await Api.deleteApp(payload);
            dispatch('getAppList')
        }
    }
}