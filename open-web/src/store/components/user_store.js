import Api from '@/api/api'

export default {
    state: {
        account: ''
    },
    mutations: {
        setAccount(state, account) {
            state.account = account;
        }
    },
    actions: {
        login({commit}, payload) {
            console.log('login', commit)
            return Api.login(payload);
        },

        getAccount({state}) {
            Api.getAccount().then((account) => {
                state.account = account;
            })
        },

        updatePwd({commit}, payload) {
            console.log('updatePwd', commit)
            return Api.udpatePwd(payload)
        },
    }
}