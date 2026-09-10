<template>
    <div class="login">
        <!-- 品牌侧 -->
        <section class="login-brand">
            <div class="brand-inner">
                <BrandMark class="brand-logo" :size="48"/>
                <h1 class="brand-title">{{ $t('login.title') }}</h1>
                <p class="brand-sub">把第三方服务接入野火 IM，统一在这里注册和管理</p>
                <ul class="brand-points">
                    <li v-for="point in points" :key="point">
                        <el-icon>
                            <Check/>
                        </el-icon>
                        <span>{{ point }}</span>
                    </li>
                </ul>
            </div>
            <div class="brand-glow"></div>
        </section>

        <!-- 表单侧 -->
        <section class="login-panel">
            <div class="login-card">
                <h2 class="login-title">{{ $t('login.login') }}</h2>
                <p class="login-hint">请使用开放平台管理员账号登录</p>

                <el-form ref="loginForm" :model="loginForm" :rules="rules" size="large" @submit.prevent>
                    <el-form-item prop="username">
                        <el-input
                            v-model.trim="loginForm.username"
                            autocomplete="username"
                            :placeholder="$t('login.input_user_name_tip')"
                        >
                            <template #prefix>
                                <el-icon>
                                    <User/>
                                </el-icon>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input
                            v-model.trim="loginForm.password"
                            type="password"
                            show-password
                            autocomplete="current-password"
                            :placeholder="$t('login.input_password_tip')"
                            @keyup.enter="submit"
                        >
                            <template #prefix>
                                <el-icon>
                                    <Lock/>
                                </el-icon>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-button
                        class="login-btn"
                        type="primary"
                        size="large"
                        :loading="loading"
                        @click="submit"
                    >
                        {{ $t('login.login') }}
                    </el-button>
                </el-form>
            </div>

            <footer class="login-foot">wildfirechat.net &copy; All rights reserved</footer>
        </section>
    </div>
</template>

<script>
import {Check, Lock, User} from '@element-plus/icons-vue';
import LoginRequest from '@/model/loginRequest';
import BrandMark from '@/components/common/BrandMark';

export default {
    name: 'Login',
    components: {BrandMark, Check, Lock, User},
    data() {
        return {
            loading: false,
            loginForm: {
                username: '',
                password: ''
            },
            points: [
                '创建应用，出现在客户端工作台',
                '创建频道，向订阅用户推送消息',
                '创建机器人，自动收发消息'
            ],
            rules: {
                username: [
                    {required: true, message: this.$t('login.input_user_name_tip'), trigger: 'blur'}
                ],
                password: [
                    {required: true, message: this.$t('login.input_password_tip'), trigger: 'blur'}
                ]
            }
        };
    },
    created() {
        if (localStorage.getItem('authToken')) {
            this.$router.replace({path: '/index'});
        }
    },
    methods: {
        submit() {
            this.$refs.loginForm.validate(valid => {
                if (!valid) {
                    return;
                }
                this.loading = true;
                this.$store.dispatch(
                    'login',
                    new LoginRequest(this.loginForm.username, this.loginForm.password)
                ).then(() => {
                    this.$router.push({path: '/index'});
                }).catch(() => {
                    // 具体失败原因由 axios 拦截器统一提示
                    this.loading = false;
                });
            });
        }
    }
};
</script>

<style scoped>
.login {
    display: flex;
    height: 100%;
    background: var(--wf-surface);
}

/* ---------- 品牌侧 ---------- */
.login-brand {
    position: relative;
    display: flex;
    align-items: center;
    flex: 1 1 52%;
    padding: 0 8%;
    overflow: hidden;
    background: linear-gradient(150deg, #12203c 0%, #1a2d54 45%, #0e42d2 130%);
    color: #fff;
}

.brand-inner {
    position: relative;
    z-index: 1;
    max-width: 460px;
}

.brand-logo {
    border-radius: 11px;
}

.brand-title {
    margin: 20px 0 0;
    font-size: 32px;
    font-weight: 600;
    line-height: 44px;
    letter-spacing: 0.5px;
}

.brand-sub {
    margin: 12px 0 0;
    font-size: 15px;
    line-height: 24px;
    color: rgba(255, 255, 255, 0.72);
}

.brand-points {
    margin: 36px 0 0;
    padding: 0;
    list-style: none;
}

.brand-points li {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 14px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.86);
}

.brand-points .el-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.14);
    font-size: 12px;
}

/* 柔光，替代旧版那张 1.7MB 的平铺 + 模糊背景图 */
.brand-glow {
    position: absolute;
    right: -160px;
    bottom: -200px;
    width: 560px;
    height: 560px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(64, 128, 255, 0.55) 0%, transparent 68%);
}

/* ---------- 表单侧 ---------- */
.login-panel {
    position: relative;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    flex: 1 1 48%;
    padding: 40px;
}

.login-card {
    width: 100%;
    max-width: 360px;
}

.login-title {
    margin: 0;
    font-size: 24px;
    font-weight: 600;
    color: var(--wf-text-1);
}

.login-hint {
    margin: 6px 0 28px;
    font-size: var(--wf-text-sm);
    color: var(--wf-text-3);
}

.login-btn {
    width: 100%;
    margin-top: 8px;
    font-weight: 500;
    letter-spacing: 2px;
}

.login-foot {
    position: absolute;
    bottom: 24px;
    font-size: var(--wf-text-xs);
    color: var(--wf-text-4);
}

/* 窄屏隐藏品牌侧 */
@media (max-width: 900px) {
    .login-brand {
        display: none;
    }
}
</style>
