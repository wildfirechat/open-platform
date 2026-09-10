<template>
    <div class="workbench">
        <header class="head">
            <p class="greeting">{{ account ? `欢迎，${account.displayName}` : '欢迎使用野火 IM 工作台' }}</p>
        </header>

        <!-- 我的应用（可管理） -->
        <section class="panel">
            <div class="panel-head">
                <h2>我的</h2>
                <div v-if="managing" class="actions">
                    <button class="action" type="button" @click="cancelManage">取消</button>
                    <button class="action action-primary" type="button" @click="saveManage">完成</button>
                </div>
                <button v-else class="action" type="button" @click="startManage">管理</button>
            </div>

            <!-- 管理态下展示全部可选应用，平时只展示已收藏的 -->
            <ul v-if="visibleMyApps.length" class="apps">
                <li v-for="app in visibleMyApps" :key="app.targetId">
                    <button
                        class="app"
                        type="button"
                        :class="{ selected: managing && isChecked(app) }"
                        :aria-pressed="managing ? isChecked(app) : null"
                        @click="managing ? toggle(app) : openApp(app)"
                    >
                        <span class="tile">
                            <img v-if="app.portraitUrl" :src="app.portraitUrl" :alt="''" @error="onIconError(app)"/>
                            <span v-else class="tile-fallback" aria-hidden="true">{{ initial(app) }}</span>

                            <!-- 管理态下的选中标记，取代原来那个原生复选框 -->
                            <span v-if="managing" class="check" :class="{ on: isChecked(app) }" aria-hidden="true">
                                <svg viewBox="0 0 16 16" width="11" height="11">
                                    <path d="M3 8.4l3.2 3.2L13 4.8" fill="none" stroke="currentColor"
                                          stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </span>
                        </span>
                        <span class="app-name">{{ app.name }}</span>
                    </button>
                </li>
            </ul>

            <p v-else class="empty">
                {{ managing ? '还没有可添加的应用，请先到开放平台创建。' : '还没有应用，点右上角「管理」添加。' }}
            </p>
        </section>

        <!-- 全员应用，所有人可见，不可取消 -->
        <section class="panel">
            <div class="panel-head">
                <h2>全员</h2>
            </div>

            <ul v-if="globalApps.length" class="apps">
                <li v-for="app in globalApps" :key="app.targetId">
                    <button class="app" type="button" @click="openApp(app)">
                        <span class="tile">
                            <img v-if="app.portraitUrl" :src="app.portraitUrl" :alt="''" @error="onIconError(app)"/>
                            <span v-else class="tile-fallback" aria-hidden="true">{{ initial(app) }}</span>
                        </span>
                        <span class="app-name">{{ app.name }}</span>
                    </button>
                </li>
            </ul>

            <p v-else class="empty">还没有全员应用，请到开放平台添加。</p>
        </section>
    </div>
</template>

<script>

import './jssdk/bridgeClientImpl.uni'
import wf from "@/jssdk/wf";
import api from "@/api/api";

export default {
    name: 'App',
    data() {
        return {
            account: null,
            favApps: [],
            apps: [], // 非 globalApps
            globalApps: [],
            checkedAppIds: [],
            managing: false,
        }
    },
    components: {},
    computed: {
        // 管理态列出全部可收藏的应用，平时只列已收藏的
        visibleMyApps() {
            return this.managing ? this.apps : this.favApps;
        }
    },
    created() {
        document.title = '野火IM工作台'
        this.detectTheme();
        this.getAppList();
        this.getAccount();
    },
    methods: {
        detectTheme() {
            const urlParams = new URLSearchParams(window.location.search);
            const theme = urlParams.get('theme');
            if (theme === 'dark') {
                document.documentElement.setAttribute('data-theme', 'dark');
            } else if (theme === 'light') {
                document.documentElement.setAttribute('data-theme', 'light');
            }
        },
        getAccount(failToLogin = true) {
            api.getAccount().then(account => {
                this.account = account;
                this.getFavAppList();
            }).catch(reason => {
                wf.toast('开放平台登录中... ，若不需要开放平台功能，请将 Config.java 里面的 WORKSPACE_URL 置为 null')
                if (reason.code === 13 && failToLogin) {
                    console.log('getAuthCode and login')
                    this.login();
                }
            })
        },
        getAppList() {
            api.getAppList().then((apps) => {
                console.log('apps', apps)
                this.apps = apps.filter(app => app.global !== true)
                this.globalApps = apps.filter(app => app.global === true)
            });
        },

        getFavAppList() {
            api.getFavAppList().then(favApps => {
                this.favApps = favApps;
            }).catch(reason => {
                console.log('getFavAppList error', reason)
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
                    this.getFavAppList();
                    this.getAccount(false);
                }).catch(reason => {
                    console.log('login failed', reason);
                    if (location.host.indexOf('wildfirechat') >= 0) {
                        wf.toast('请部署开开放平台服务，或将 Config.java 里面将工作台地址置为 null');
                    } else {
                        wf.toast('开放平台登录失败 ' + reason.message);
                    }
                })
            }, err => {
                console.log('getAuthCode error', err)
            })
        },

        startManage() {
            if (!this.account) {
                console.log('not login, to login')
                this.login();
                return;
            }
            // 每次进入管理态都按当前收藏重新初始化勾选状态。
            // 原来只在拉取收藏列表时 push 过一次，取消一次之后就再也不会勾上了。
            this.checkedAppIds = this.favApps.map(app => app.targetId);
            this.managing = true;
        },

        cancelManage() {
            this.managing = false;
            this.checkedAppIds = [];
        },

        isChecked(app) {
            return this.checkedAppIds.indexOf(app.targetId) >= 0;
        },

        toggle(app) {
            if (this.isChecked(app)) {
                this.checkedAppIds = this.checkedAppIds.filter(id => id !== app.targetId);
            } else {
                this.checkedAppIds = this.checkedAppIds.concat(app.targetId);
            }
        },

        saveManage() {
            const favIds = this.favApps.map(app => app.targetId);
            // 原来这里拿 favApps（对象数组）去 indexOf 一个 targetId 字符串，
            // 结果恒为 -1，每次都会把已收藏的应用重新收藏一遍。
            const toFavApps = this.checkedAppIds.filter(id => favIds.indexOf(id) === -1);
            const toUnFavApps = favIds.filter(id => this.checkedAppIds.indexOf(id) === -1);

            console.log('manageFavApp', toFavApps, toUnFavApps);
            Promise.all([api.favApps(toFavApps), api.unFavApps(toUnFavApps)]).then(value => {
                console.log('manageFavApp result', value);
                this.getFavAppList();
            })

            this.managing = false;
            this.checkedAppIds = [];
        },

        openApp(app) {
            // 原来是 `process ? app.desktopUrl : app.mobileUrl`，webpack4 把 process mock 成了 `{}`，
            // 判断恒为真，即一直取的 desktopUrl。webpack5 不再 mock，裸 process 会 ReferenceError，
            // 这里保持原有行为。如果需要区分移动端/桌面端，应当另外判断平台。
            let url = app.desktopUrl;
            wf.openUrl(url, {name: app.name});
        },

        initial(app) {
            return app.name ? app.name.trim().charAt(0) : '?';
        },

        // 图标地址失效时回退到首字占位，避免出现破图
        onIconError(app) {
            app.portraitUrl = '';
        }
    }
}
</script>

<style scoped>
.workbench {
    min-height: 100vh;
    padding: var(--wf-space-3);
    /* 刘海屏 / 底部安全区：这个页面直接铺满客户端的 webview */
    padding-top: calc(var(--wf-space-3) + env(safe-area-inset-top));
    padding-bottom: calc(var(--wf-space-5) + env(safe-area-inset-bottom));
    background: var(--wf-canvas);
}

.head {
    padding: var(--wf-space-1) var(--wf-space-2) var(--wf-space-3);
}

/* 问候语是这个页面唯一的标题行 */
.greeting {
    font-size: var(--wf-text-md);
    font-weight: 600;
    letter-spacing: -0.01em;
    color: var(--wf-text);
}

/* ---- 分区 -------------------------------------------------------------- */
.panel {
    margin-bottom: var(--wf-space-3);
    padding: var(--wf-space-2) var(--wf-space-3) var(--wf-space-3);
    background: var(--wf-surface);
    border-radius: var(--wf-radius-card);
}

.panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 36px;
}

.panel-head h2 {
    font-size: var(--wf-text-sm);
    font-weight: 600;
    color: var(--wf-text-muted);
}

.actions {
    display: flex;
    gap: var(--wf-space-1);
}

.action {
    padding: 6px 10px;
    font: inherit;
    font-size: var(--wf-text-sm);
    color: var(--wf-accent);
    background: transparent;
    border: none;
    border-radius: var(--wf-radius-pill);
    cursor: pointer;
}

.action:active {
    background: var(--wf-pressed);
}

.action-primary {
    font-weight: 600;
}

/* ---- 应用宫格 ---------------------------------------------------------- */
.apps {
    list-style: none;
    margin: 0;
    padding: 0;
    display: grid;
    /* 手机上正好 4 列；桌面端内置浏览器变宽时自动加列 */
    grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
    gap: var(--wf-space-2) 0;
}

.app {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    width: 100%;
    padding: var(--wf-space-2) 2px;
    font: inherit;
    color: inherit;
    background: transparent;
    border: none;
    border-radius: 10px;
    cursor: pointer;
}

.app:active {
    background: var(--wf-pressed);
}

.tile {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: var(--wf-radius-tile);
    background: var(--wf-tile);
    /* 浅色图标在浅色底上会糊掉，用一道内描边把边界固定住 */
    box-shadow: inset 0 0 0 1px var(--wf-tile-border);
    overflow: hidden;
}

.tile img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.tile-fallback {
    font-size: var(--wf-text-base);
    font-weight: 600;
    color: var(--wf-accent);
}

.app-name {
    max-width: 100%;
    font-size: var(--wf-text-xs);
    line-height: 1.4;
    color: var(--wf-text);
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* ---- 管理态的选中标记 --------------------------------------------------- */
.check {
    position: absolute;
    top: 2px;
    right: 2px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 17px;
    height: 17px;
    border-radius: var(--wf-radius-pill);
    color: transparent;
    background: var(--wf-surface);
    box-shadow: inset 0 0 0 1.5px var(--wf-check-ring);
}

.check.on {
    color: #FFFFFF;
    background: var(--wf-accent);
    box-shadow: none;
}

.app.selected .tile {
    box-shadow: inset 0 0 0 2px var(--wf-accent);
}

.empty {
    padding: var(--wf-space-4) var(--wf-space-1) var(--wf-space-3);
    font-size: var(--wf-text-sm);
    line-height: 1.6;
    color: var(--wf-text-faint);
}
</style>
