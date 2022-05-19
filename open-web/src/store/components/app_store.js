import Api from '@/api/api'

export default {
    state: {
        apps: [],
    },
    mutations: {
        saveAppList(state, payload) {
            state.apps = payload;
        }
    },
    actions: {
        getAppList(context) {
            Api.getAppList().then((data) => {
                context.state.apps = data;
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