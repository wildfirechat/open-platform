<template>
    <div class="shell">
        <nav class="rail" aria-label="主导航">
            <router-link to="/index" class="brand">
                <BrandMark :size="22"/>
                <span class="brand-name">野火开放平台</span>
            </router-link>

            <ul class="nav">
                <li v-for="item in navItems" :key="item.path">
                    <router-link
                        :to="item.path"
                        class="nav-item"
                        :class="{ 'is-active': isActive(item) }"
                        :aria-current="isActive(item) ? 'page' : null"
                    >
                        {{ item.label }}
                    </router-link>
                </li>
                <li>
                    <a class="nav-item nav-external" :href="docsUrl" target="_blank" rel="noopener">
                        开发文档 ↗
                    </a>
                </li>
            </ul>

            <div class="rail-foot">
                <el-dropdown trigger="click" placement="top-start" @command="handleCommand">
                    <button class="account" type="button">
                        <span class="avatar" aria-hidden="true">{{ accountInitial }}</span>
                        <span class="account-name">{{ account.displayName || '未登录' }}</span>
                    </button>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="updatePwd">修改密码</el-dropdown-item>
                            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </nav>

        <main class="content">
            <router-view v-slot="{ Component }">
                <transition name="move" mode="out-in">
                    <keep-alive>
                        <component :is="Component"/>
                    </keep-alive>
                </transition>
            </router-view>
        </main>

        <el-dialog
            v-model="modifyPwdDialogVisible"
            title="修改密码"
            width="min(420px, calc(100vw - 32px))"
            append-to-body
            @closed="resetPwdForm"
        >
            <el-form ref="updatePwdForm" :model="updatePwdRequest" :rules="rules" label-position="top">
                <el-form-item label="当前密码" prop="oldPwd">
                    <el-input v-model="updatePwdRequest.oldPwd" type="password" show-password autocomplete="current-password"/>
                </el-form-item>
                <el-form-item label="新密码" prop="newPwd">
                    <el-input v-model="updatePwdRequest.newPwd" type="password" show-password autocomplete="new-password"/>
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmNewPwd">
                    <el-input v-model="updatePwdRequest.confirmNewPwd" type="password" show-password autocomplete="new-password"/>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="modifyPwdDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="updatingPwd" @click="updatePwd">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script>
import { mapState } from 'vuex';
import BrandMark from '@/components/common/BrandMark';
import { DOCS_URL } from '@/config/resourceTypes';

export default {
    name: 'Home',
    components: { BrandMark },
    data() {
        // 两次输入是否一致，交给校验规则处理，而不是提交时再弹一条错误提示
        const confirmMatches = (rule, value, callback) => {
            if (value !== this.updatePwdRequest.newPwd) {
                callback(new Error('两次输入的新密码不一致'));
            } else {
                callback();
            }
        };
        return {
            docsUrl: DOCS_URL,
            modifyPwdDialogVisible: false,
            updatingPwd: false,
            updatePwdRequest: {oldPwd: '', newPwd: '', confirmNewPwd: ''},
            navItems: [
                // /index 是登录后的落地路由，指向的也是应用页
                {path: '/dev/app', label: '应用', alias: ['/index']},
                {path: '/dev/channel', label: '频道'},
                {path: '/dev/robot', label: '机器人'}
            ],
            rules: {
                oldPwd: [{required: true, message: '请输入当前密码', trigger: 'blur'}],
                newPwd: [{required: true, message: '请输入新密码', trigger: 'blur'}],
                confirmNewPwd: [
                    {required: true, message: '请再次输入新密码', trigger: 'blur'},
                    {validator: confirmMatches, trigger: 'blur'}
                ]
            }
        };
    },
    computed: {
        ...mapState({
            account: state => state.user.account || {}
        }),
        accountInitial() {
            const name = this.account.displayName;
            return name ? name.trim().charAt(0).toUpperCase() : '·';
        }
    },
    created() {
        this.$store.dispatch('getAppList');
        this.$store.dispatch('getAccount');
    },
    methods: {
        isActive(item) {
            const path = this.$route.path;
            return path === item.path || (item.alias || []).includes(path);
        },
        handleCommand(command) {
            if (command === 'logout') {
                this.logout();
            } else if (command === 'updatePwd') {
                this.modifyPwdDialogVisible = true;
            }
        },
        logout() {
            localStorage.clear();
            this.$router.replace('/login');
        },
        resetPwdForm() {
            this.updatePwdRequest = {oldPwd: '', newPwd: '', confirmNewPwd: ''};
            this.$refs.updatePwdForm && this.$refs.updatePwdForm.clearValidate();
        },
        updatePwd() {
            this.$refs.updatePwdForm.validate(valid => {
                if (!valid) {
                    return;
                }
                this.updatingPwd = true;
                this.$store.dispatch('updatePwd', {
                    oldPassword: this.updatePwdRequest.oldPwd,
                    newPassword: this.updatePwdRequest.newPwd
                }).then(() => {
                    // 只有成功才关闭弹窗，失败时保留已填内容让用户改
                    this.modifyPwdDialogVisible = false;
                    this.$message.success('密码已修改');
                }).catch(() => { /* 错误提示由 axios 拦截器统一处理 */ })
                    .finally(() => {
                        this.updatingPwd = false;
                    });
            });
        }
    }
};
</script>

<style scoped>
.shell {
    display: flex;
    height: 100%;
    background: var(--wf-bg);
}

/* ---- 左侧导航栏：整个界面唯一的深色区域，承载品牌和账号 ---------------- */
.rail {
    flex: none;
    display: flex;
    flex-direction: column;
    width: var(--wf-sider-w);
    height: 100%;
    background: var(--wf-nav-bg);
    color: var(--wf-nav-text);
}

.brand {
    display: flex;
    align-items: center;
    flex-shrink: 0;
    gap: 10px;
    height: var(--wf-header-h);
    padding: 0 16px;
    overflow: hidden;
    color: #fff;
    border-bottom: 1px solid var(--wf-nav-border);
}

.brand:hover {
    color: #fff;
}

.brand-name {
    font-size: 15px;
    font-weight: 600;
    white-space: nowrap;
}

.nav {
    list-style: none;
    margin: 0;
    padding: 8px;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.nav-item {
    display: flex;
    align-items: center;
    height: 44px;
    padding: 0 16px;
    border-radius: var(--wf-radius);
    font-size: var(--wf-text-base);
    color: var(--wf-nav-text);
    transition: background 0.15s ease, color 0.15s ease;
}

.nav-item:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #fff;
}

/* 选中项用品牌色实底，与 organization-web 的菜单一致 */
.nav-item.is-active {
    background: var(--wf-brand);
    color: #fff;
    font-weight: 500;
}

.nav-external {
    font-size: var(--wf-text-sm);
    /* 0.45 的白在 #1d2129 上只有 4.36:1，差一点点到 AA */
    color: rgba(255, 255, 255, 0.55);
}

.rail-foot {
    margin-top: auto;
    flex-shrink: 0;
    padding: 8px;
    border-top: 1px solid var(--wf-nav-border);
}

.account {
    display: flex;
    align-items: center;
    gap: 8px;
    width: 100%;
    height: 40px;
    padding: 0 10px;
    font: inherit;
    font-size: var(--wf-text-sm);
    color: var(--wf-nav-text);
    text-align: left;
    background: transparent;
    border: none;
    border-radius: var(--wf-radius);
    cursor: pointer;
    transition: background 0.15s ease, color 0.15s ease;
}

.account:hover {
    background: rgba(255, 255, 255, 0.06);
    color: #fff;
}

.avatar {
    flex: none;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: var(--wf-radius-pill);
    font-size: var(--wf-text-sm);
    font-weight: 600;
    color: #fff;
    background: var(--wf-brand);
}

.account-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* ---- 内容区 ----------------------------------------------------------- */
.content {
    flex: 1;
    min-width: 0;
    overflow-y: auto;
}

/* ---- 窄屏：侧栏变成顶部横向导航 --------------------------------------- */
@media (max-width: 900px) {
    .shell {
        flex-direction: column;
    }

    .rail {
        width: 100%;
        height: auto;
        flex-direction: row;
        align-items: center;
        gap: 8px;
        padding: 0 12px;
        overflow-x: auto;
    }

    .brand {
        padding: 0;
        border-bottom: none;
    }

    .brand-name {
        display: none;
    }

    .nav {
        flex-direction: row;
        padding: 8px 0;
    }

    .nav-item {
        height: 36px;
        padding: 0 12px;
        white-space: nowrap;
    }

    .nav-external {
        display: none;
    }

    .rail-foot {
        margin-top: 0;
        margin-left: auto;
        padding: 0;
        border-top: none;
    }

    .account {
        width: auto;
        height: 36px;
        padding: 0 4px;
    }

    .account-name {
        display: none;
    }
}
</style>
