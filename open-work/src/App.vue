<template>
    <div id="app">
        <div class="top">
            <p>欢迎使用野火IM工作台</p>
        </div>
        <div v-if="!showManageFavApp" class="apps-container">
            <div class="title-action-container">
                <p class="title">我的</p>
                <p class="action" @click="showManageFavAppView">管理</p>
            </div>
            <div class="apps">
                <div v-for="(app, index) in favApps" :key="index" class="app" @click="openApp(app)">
                    <img :src="app.portraitUrl">
                    <p>{{ app.name }}</p>
                </div>
            </div>
            <p v-if="favApps.length === 0" class="empty">没有应用，请点击管理按钮进行配置</p>
        </div>
        <div v-if="showManageFavApp" class="apps-container">
            <div class="title-action-container">
                <p class="title">管理</p>
                <div class="action-container">
                    <p class="action" @click="cancelManageFavApp">取消</p>
                    <p class="action" @click="manageFavApp">确定</p>
                </div>
            </div>
            <div class="apps">
                <div v-for="(app, index) in apps" :key="index" class="app" :class="{checked: app._checked}" @click="checkApp(app)">
                    <img :src="app.portraitUrl">
                    <p>{{ app.name }}</p>
                    <input type="checkbox" :value="app.targetId" v-model="checkedAppIds">
                </div>
            </div>
            <p v-if="apps.length === 0" class="empty">没有应用，请到开放平台创建</p>
        </div>
        <div class="apps-container">
            <p class="title">全员</p>
            <div class="apps">
                <div v-for="(app, index) in globalApps" :key="index" class="app" @click="openApp(app)">
                    <img :src="app.portraitUrl">
                    <p>{{ app.name }}</p>
                </div>
            </div>
            <p v-if="globalApps.length === 0" class="empty">没有应用，请到开放平台添加</p>
        </div>
    </div>
</template>

<script>

import wf from "@/jssdk/wf";
import api from "@/api/api";

export default {
    name: 'App',
    data() {
        return {
            hasLogin: false,
            favApps: [],
            apps: [], // 非 globalApps
            globalApps: [],
            checkedAppIds: [],
            showManageFavApp: false,
        }
    },
    components: {},
    created() {
        document.title = '野火IM工作台'
        this.getAppList();
        this.getFavAppList();
    },
    methods: {
        getAppList() {
            api.getAppList().then((apps) => {
                console.log('apps', apps)
                this.apps = apps.filter(app => app.global !== true)
                this.globalApps = apps.filter(app => app.global === true)
            });
        },

        getFavAppList(loginOnFail = true) {
            api.getFavAppList().then(favApps => {
                this.hasLogin = true;
                this.favApps = favApps;
                this.favApps.forEach(app => {
                    this.checkedAppIds.push(app.targetId);
                })
            }).catch(reason => {
                console.log('getFavAppList error', reason)
                if (reason.code === 13) {
                    if (loginOnFail) {
                        wf.toast('未登录，重新登录中...')
                        console.log('getAuthCode and login')
                        this.login();
                    }
                    wf.toast('获取我的应用失败')
                }
            })
        },

        login() {
            // type: 0, robot; 1, channel; 2, admin
            wf.biz.getAuthCode('wfcadmin', 2, (authCode) => {
                console.log('getAuthCode success', authCode)
                api.login({
                    appId: 'wfcadmin',
                    appType: 2,
                    authCode: authCode,
                }).then(() => {
                    this.getFavAppList(false);
                }).catch(reason => {
                    console.log('login failed', reason);
                    wf.toast('登录失败 ' + reason);
                })
            }, err => {
                console.log('getAuthCode error', err)
            })
        },

        showManageFavAppView() {
            if (this.hasLogin) {
                this.showManageFavApp = true;
            } else {
                console.log('not login, to login')
                this.login();
            }
        },

        openApp(app) {
            // TODO 判断是一下是否是 PC
            wf.openUrl(app.mobileUrl);
        },

        checkApp(app) {
            let index = this.checkedAppIds.indexOf(app.targetId);
            if (index >= 0) {
                this.checkedAppIds = this.checkedAppIds.filter(id => id !== app.targetId);
            } else {
                this.checkedAppIds.push(app.targetId);
            }
        },

        cancelManageFavApp() {
            this.showManageFavApp = false;
            this.checkedAppIds = [];
        },

        manageFavApp() {
            let toUnFavApps = [];
            let toFavApps = [];
            this.favApps.forEach(app => {
                if (this.checkedAppIds.indexOf(app.targetId) === -1) {
                    toUnFavApps.push(app.targetId);
                }
            })

            this.checkedAppIds.forEach(targetId => {
                if (this.favApps.length > 0) {
                    if (this.favApps.indexOf(targetId) === -1) {
                        toFavApps.push(targetId);
                    }
                } else {
                    toFavApps.push(targetId);
                }
            })

            console.log('manageFavApp', toFavApps, toUnFavApps);
            let fp = api.favApps(toFavApps);
            let ufp = api.unFavApps(toUnFavApps);
            Promise.all([fp, ufp]).then(value => {
                console.log('manageFavApp result', value);
                this.getFavAppList(false);
            })

            this.showManageFavApp = false;
            this.checkedAppIds = [];
        }
    }
}
</script>

<style lang="css" scoped>
@import "./assets/main.css";

body {
    margin: 0;
    padding: 0;
}

#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    background-color: #EDEDED;
    width: 100vw;
    height: 100vh;

}

.top {
    height: 60px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: flex-start;
    background-color: white;
}

.top p {
    padding-left: 15px;
    font-size: 18px;
    font-weight: bold;
}

.apps-container {
    background-color: white;
    border-radius: 5px;
    margin: 5px;
}

.apps-container .title {
    text-align: left;
    padding: 5px;
}

.title-action-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.title-action-container .action-container{
    display: flex;
    flex-direction: row;
}

.title-action-container .action {
    padding: 5px 10px;
    border-radius: 5px;
}

.title-action-container .action:active {
    background-color: lightgrey;
}

.apps-container .empty {
    padding: 10px 0;
    font-size: 14px;
}

.apps {
    display: grid;
    grid-template-columns: repeat(auto-fill, 80px);
    justify-content: space-between;
}

.app {
    margin: 5px 0;
    width: 80px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 5px 0;
    position: relative;
}

.app:active {
    background-color: lightgrey;
    border-radius: 5px;
}

.app img {
    width: 40px;
    height: 40px;
}

.app p {
    margin: 5px 0 0 0;
    font-size: 12px;
}

.app input {
    position: absolute;
    top: 5px;
    left: 5px;
}
</style>
