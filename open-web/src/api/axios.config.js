import Axios from 'axios'
import App from '../main'
// axios实例
const instance = Axios.create({
    // 针对实际情况进行修改
    // 如果是默认部署方式，即前后端部署在同一个服务器上，保持原样即可
    // 如果前后端分开部署，需要修改为后端服务器地址
    baseURL: './api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json;charset=utf-8',
        'Accept': 'application/json;charset=utf-8',
        'authToken': localStorage.getItem('authToken'),
    }
})
instance.interceptors.request.use(request => {
    request.headers['authToken'] = localStorage.getItem('authToken');
    console.log('request', request);
    return request;
})

instance.interceptors.response.use(response => {
    let {code, message, result} = response.data
    if (code === 0) {
        if (response.config.url === '/login') {
            let authToken = response.headers['authtoken'] ? response.headers['authtoken'] : response.headers['authToken']
            localStorage.setItem('authToken', authToken);
        }
    } else {
        if (response.config.url !== '/login' && code === 13) {
            localStorage.removeItem('authToken');
            App.$message.error('请先登陆')
            App.$router.replace('/login')
        }
        App.$message.error(message);

        return Promise.reject({code});
    }
    return result
});
export default instance