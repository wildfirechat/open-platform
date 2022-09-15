<template>
    <div id="update-pwd">
        <div class="crumbs">
            <el-breadcrumb separator="/">
                <el-breadcrumb-item>{{ $t('common.setting') }}</el-breadcrumb-item>
                <el-breadcrumb-item>{{ $t('home.update_password') }}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="container" style="padding: 20px">
            <el-form :model="user">
                <el-form-item v-bind:label="$t('setting.original_password')">
                    <el-input v-model.trim="user.oldPwd" type="password"></el-input>
                </el-form-item>
                <el-form-item v-bind:label="$t('setting.new_password')">
                    <el-input v-model.trim="user.newPwd" type="password"></el-input>
                </el-form-item>
                <el-form-item v-bind:label="$t('setting.confirm_new_password')">
                    <el-input v-model.trim="user.newPwdRe" type="password"></el-input>
                </el-form-item>
                <el-button @click="save" :loading="loading">{{ $t('common.save') }}</el-button>
            </el-form>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            loading: false,
            user: {}
        }
    },
    methods: {
        save() {
            let self = this
            if (this.user.newPwd != this.user.newPwdRe) {
                self.$message(this.$t('setting.new_password_error'))
                return
            }
            this.loading = true
            this.$store.dispatch('updatePwd', this.user).then(() => {
                self.$message(this.$t('common.action_success'))
            }).finally(() => {
                self.loading = false
            })
        }
    }
}
</script>

<style>

</style>
