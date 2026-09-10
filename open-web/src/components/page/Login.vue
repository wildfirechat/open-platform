<template>
    <div class="login">
        <!-- 左侧沿用主界面导航栏的深色，让登录页和进入后的产品是同一个东西 -->
        <section class="intro">
            <div class="intro-brand">
                <BrandMark :size="30"/>
                <span class="intro-name">野火开放平台</span>
            </div>

            <p class="intro-lead">
                把考勤、文档、会议这些第三方服务接入野火 IM，统一在这里注册和管理。
            </p>

            <!-- 直接说明这里管理的三种东西，比一句口号更有用 -->
            <dl class="intro-list">
                <div v-for="entry in managed" :key="entry.term">
                    <dt>{{ entry.term }}</dt>
                    <dd>{{ entry.detail }}</dd>
                </div>
            </dl>
        </section>

        <section class="panel">
            <div class="form-wrap">
                <h1 class="form-title">登录</h1>
                <p class="form-sub">使用开放平台管理员账号登录。</p>

                <el-form
                    ref="loginForm"
                    :model="loginForm"
                    :rules="rules"
                    label-position="top"
                    @submit.prevent
                >
                    <el-form-item :label="$t('common.user_name')" prop="username">
                        <el-input
                            v-model.trim="loginForm.username"
                            size="large"
                            autocomplete="username"
                            :placeholder="$t('login.input_user_name_tip')"
                        />
                    </el-form-item>
                    <el-form-item :label="$t('common.password')" prop="password">
                        <el-input
                            v-model.trim="loginForm.password"
                            type="password"
                            size="large"
                            show-password
                            autocomplete="current-password"
                            :placeholder="$t('login.input_password_tip')"
                            @keyup.enter="submit"
                        />
                    </el-form-item>
                    <el-button
                        class="submit"
                        type="primary"
                        size="large"
                        native-type="submit"
                        :loading="loading"
                        @click="submit"
                    >
                        {{ $t('login.login') }}
                    </el-button>
                </el-form>

                <p class="copyright">wildfirechat.net © All rights reserved</p>
            </div>
        </section>
    </div>
</template>

<script>
import LoginRequest from '@/model/loginRequest';
import BrandMark from '@/components/common/BrandMark';

export default {
    name: 'Login',
    components: { BrandMark },
    data() {
        return {
            loading: false,
            loginForm: {
                username: '',
                password: ''
            },
            managed: [
                {term: '应用', detail: '出现在客户端工作台，用户点开即用'},
                {term: '频道', detail: '类似公众号，向订阅用户推送消息'},
                {term: '机器人', detail: '用机器人账号自动收发消息'}
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
    display: grid;
    grid-template-columns: minmax(0, 5fr) minmax(0, 6fr);
    height: 100%;
    background: var(--wf-surface);
}

/* ---- 左：品牌与说明 --------------------------------------------------- */
.intro {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: var(--wf-space-6);
    padding: var(--wf-space-7);
    background: var(--wf-rail);
    color: var(--wf-ink-300);
}

.intro-brand {
    display: flex;
    align-items: center;
    gap: var(--wf-space-3);
    color: var(--wf-ink-0);
}

.intro-name {
    font-size: var(--wf-text-lg);
    font-weight: 600;
    letter-spacing: -0.01em;
}

.intro-lead {
    max-width: 32ch;
    font-size: var(--wf-text-md);
    line-height: 1.7;
    color: var(--wf-ink-200);
}

.intro-list {
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: var(--wf-space-4);
    max-width: 40ch;
}

.intro-list > div {
    display: flex;
    align-items: baseline;
    gap: var(--wf-space-3);
    padding-top: var(--wf-space-4);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.intro-list dt {
    flex: none;
    width: 4em;
    font-size: var(--wf-text-base);
    font-weight: 600;
    color: var(--wf-ink-0);
}

.intro-list dd {
    margin: 0;
    font-size: var(--wf-text-sm);
    line-height: 1.6;
    color: var(--wf-ink-400);
}

/* ---- 右：表单 --------------------------------------------------------- */
.panel {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: var(--wf-space-6);
    overflow-y: auto;
}

.form-wrap {
    width: 100%;
    max-width: 340px;
}

.form-title {
    font-size: var(--wf-text-xl);
    font-weight: 600;
    letter-spacing: -0.01em;
    color: var(--wf-text);
}

.form-sub {
    margin: var(--wf-space-1) 0 var(--wf-space-6);
    font-size: var(--wf-text-base);
    color: var(--wf-text-muted);
}

.submit {
    width: 100%;
    margin-top: var(--wf-space-2);
}

.copyright {
    margin-top: var(--wf-space-7);
    font-size: var(--wf-text-xs);
    color: var(--wf-text-faint);
}

@media (max-width: 860px) {
    .login {
        grid-template-columns: 1fr;
        grid-template-rows: auto 1fr;
        overflow-y: auto;
    }

    .intro {
        gap: var(--wf-space-4);
        padding: var(--wf-space-5) var(--wf-space-4);
    }

    /* 窄屏上说明列表让位给表单 */
    .intro-list {
        display: none;
    }

    .intro-lead {
        max-width: none;
        font-size: var(--wf-text-base);
    }

    .panel {
        align-items: flex-start;
        padding: var(--wf-space-6) var(--wf-space-4);
    }

    .copyright {
        margin-top: var(--wf-space-6);
    }
}
</style>
