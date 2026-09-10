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
            </ul>

            <div class="rail-foot">
                <a class="nav-item nav-external" :href="docsUrl" target="_blank" rel="noopener">
                    开发文档 ↗
                </a>

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
    background: var(--wf-canvas);
}

/* ---- 左侧导航栏：整个界面唯一的深色区域，承载品牌和账号 ---------------- */
.rail {
    flex: none;
    width: var(--wf-rail-width);
    display: flex;
    flex-direction: column;
    padding: var(--wf-space-5) var(--wf-space-3) var(--wf-space-4);
    background: var(--wf-rail);
    color: var(--wf-ink-300);
}

.brand {
    display: flex;
    align-items: center;
    gap: var(--wf-space-2);
    padding: 0 var(--wf-space-2);
    margin-bottom: var(--wf-space-6);
    color: var(--wf-ink-0);
}

.brand:hover {
    color: var(--wf-ink-0);
}

.brand-name {
    font-size: var(--wf-text-md);
    font-weight: 600;
    letter-spacing: -0.01em;
}

.nav {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.nav-item {
    position: relative;
    display: block;
    padding: var(--wf-space-2) var(--wf-space-3);
    border-radius: var(--wf-radius-control);
    font-size: var(--wf-text-base);
    color: var(--wf-ink-300);
    transition: background-color 0.15s ease, color 0.15s ease;
}

.nav-item:hover {
    background: var(--wf-rail-hover);
    color: var(--wf-ink-0);
}

.nav-item.is-active {
    color: var(--wf-ink-0);
    background: rgba(71, 99, 220, 0.22);
}

/* 当前位置用品牌蓝的一道竖线标记 */
.nav-item.is-active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 16px;
    border-radius: var(--wf-radius-pill);
    background: var(--wf-brand-300);
}

.rail-foot {
    margin-top: auto;
    display: flex;
    flex-direction: column;
    gap: var(--wf-space-2);
}

.nav-external {
    font-size: var(--wf-text-sm);
    color: var(--wf-ink-400);
}

.account {
    display: flex;
    align-items: center;
    gap: var(--wf-space-2);
    width: 100%;
    padding: var(--wf-space-2);
    font: inherit;
    font-size: var(--wf-text-sm);
    color: var(--wf-ink-200);
    text-align: left;
    background: transparent;
    border: none;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 0;
    cursor: pointer;
}

.account:hover {
    color: var(--wf-ink-0);
}

.avatar {
    flex: none;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border-radius: var(--wf-radius-pill);
    font-size: var(--wf-text-xs);
    font-weight: 600;
    color: var(--wf-ink-0);
    background: var(--wf-brand-600);
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
        flex-direction: row;
        align-items: center;
        gap: var(--wf-space-3);
        padding: var(--wf-space-2) var(--wf-space-3);
        overflow-x: auto;
    }

    .brand {
        margin-bottom: 0;
    }

    .brand-name {
        display: none;
    }

    .nav {
        flex-direction: row;
    }

    .nav-item {
        white-space: nowrap;
    }

    .nav-item.is-active::before {
        display: none;
    }

    .rail-foot {
        margin-top: 0;
        margin-left: auto;
        flex-direction: row;
        align-items: center;
    }

    .nav-external {
        display: none;
    }

    .account {
        width: auto;
        border-top: none;
    }

    .account-name {
        display: none;
    }
}
</style>
