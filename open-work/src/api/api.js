import axios from './axios.config'

export default {
    async login(params) {
        return axios.post('/user_login', params)
    },
    async getAccount(){
        return axios.get('/account')
    },
    async getAppList() {
        return axios.get('/application/list')
    },
    async getFavAppList() {
        return axios.get('/user/fav_list')
    },
    async favApp(targetId) {
        return axios.put('/user/fav/' + targetId)
    },
    async unfavApp(targetId) {
        return axios.delete('/user/fav/' + targetId)
    },

    async favApps(targetIds) {
        let promises = [];
        targetIds.forEach(targetId => {
            promises.push(this.favApp(targetId))
        })
        return Promise.all(promises)
    },

    async unFavApps(targetIds) {
        let promises = [];
        targetIds.forEach(targetId => {
            promises.push(this.unfavApp(targetId))
        })
        return Promise.all(promises)
    }

    // TODO more
}
