const state = {
    errorMsg: ''
}
const getters = {
    errorMsg: state => state.errorMsg
}
const mutations = {
    showErrorMsg(state, msg) {
        state.errorMsg = {msg: msg}
        // App.$message.show(msg);
    }
}
export default {
    state,
    getters,
    mutations
}