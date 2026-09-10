<template>
    <div class="page">
        <header class="page-header">
            <h1>{{ $t('home.update_password') }}</h1>
            <p class="page-summary">修改后需要重新登录。</p>
        </header>

        <div class="card">
            <el-form ref="form" :model="user" :rules="rules" label-position="top" @submit.prevent>
                <el-form-item :label="$t('setting.original_password')" prop="oldPwd">
                    <el-input v-model="user.oldPwd" type="password" show-password autocomplete="current-password"/>
                </el-form-item>
                <el-form-item :label="$t('setting.new_password')" prop="newPwd">
                    <el-input v-model="user.newPwd" type="password" show-password autocomplete="new-password"/>
                </el-form-item>
                <el-form-item :label="$t('setting.confirm_new_password')" prop="newPwdRe">
                    <el-input v-model="user.newPwdRe" type="password" show-password autocomplete="new-password"/>
                </el-form-item>
                <el-button type="primary" :loading="loading" @click="save">{{ $t('common.save') }}</el-button>
            </el-form>
        </div>
    </div>
</template>

<script>
export default {
    name: 'UpdatePwd',
    data() {
        // 一致性校验放进规则里，输入时就能看到提示
        const confirmMatches = (rule, value, callback) => {
            if (value !== this.user.newPwd) {
                callback(new Error(this.$t('setting.new_password_error')));
            } else {
                callback();
            }
        };
        return {
            loading: false,
            user: {oldPwd: '', newPwd: '', newPwdRe: ''},
            rules: {
                oldPwd: [{required: true, message: this.$t('setting.original_password'), trigger: 'blur'}],
                newPwd: [{required: true, message: this.$t('setting.new_password'), trigger: 'blur'}],
                newPwdRe: [
                    {required: true, message: this.$t('setting.confirm_new_password'), trigger: 'blur'},
                    {validator: confirmMatches, trigger: 'blur'}
                ]
            }
        };
    },
    methods: {
        save() {
            this.$refs.form.validate(valid => {
                if (!valid) {
                    return;
                }
                this.loading = true;
                this.$store.dispatch('updatePwd', this.user).then(() => {
                    this.$message.success(this.$t('common.action_success'));
                    this.user = {oldPwd: '', newPwd: '', newPwdRe: ''};
                    this.$refs.form.clearValidate();
                }).catch(() => { /* 错误提示由 axios 拦截器统一处理 */ })
                    .finally(() => {
                        this.loading = false;
                    });
            });
        }
    }
};
</script>

<style scoped>
.page {
    padding: var(--wf-space-6) var(--wf-space-7) var(--wf-space-7);
    max-width: 1440px;
}

.page-header {
    margin-bottom: var(--wf-space-5);
}

.page-header h1 {
    font-size: var(--wf-text-xl);
    font-weight: 600;
    letter-spacing: -0.01em;
    color: var(--wf-text);
}

.page-summary {
    margin-top: var(--wf-space-1);
    font-size: var(--wf-text-base);
    color: var(--wf-text-muted);
}

.card {
    max-width: 420px;
    padding: var(--wf-space-5);
    background: var(--wf-surface);
    border: 1px solid var(--wf-border-subtle);
    border-radius: var(--wf-radius-card);
    box-shadow: var(--wf-shadow-raised);
}

@media (max-width: 720px) {
    .page {
        padding: var(--wf-space-5) var(--wf-space-4) var(--wf-space-6);
    }
}
</style>
