import axios from './axios.config'

export default {
    async login(params) {
        return axios.post('/login', params)
    },
    async getAccount() {
        return axios.get('/account')
    },
    async getAppList() {
        return axios.get('/application/list_all')
    },
    async createApp(params) {
        return axios.post('/application/create', params)
    },
    async updateApp(params) {
        return axios.post('/application/update', params)
    },
    async deleteApp(targetId) {
        return axios.delete('/application/del/' + targetId)
    },
    async udpatePwd(params) {
        return axios.post('/update_pwd', params)
    },
    // TODO more
}
