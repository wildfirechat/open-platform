<template>
    <el-container style="height: 100%">
        <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
            <div style="height: 60px; display: flex; justify-content: center;align-items: center" @click="go2home">
                <p>野火开放平台</p>
            </div>
            <el-menu :default-openeds="['1']" router>
                <el-submenu index="1">
                    <template slot="title"><i class="el-icon-menu"></i>应用开发</template>
                    <el-menu-item index="appDev">自建应用开发</el-menu-item>
                </el-submenu>
                <el-menu-item>
                    <template slot="title"><i class="el-icon-document"></i>
                        <a href="https://docs.wildfirechat.cn/open" style="color: #303133" target="_blank">开发文档</a>
                    </template>
                </el-menu-item>
            </el-menu>
        </el-aside>
        <el-container :class="{'content-collapse':collapse}">
            <el-header style="text-align: left; font-size: 14px; display: flex; padding-right: 40px">
                <el-button type="text"><i class="el-icon-arrow-left"></i></el-button>
                <span style="flex: 1"> </span>
                <el-dropdown>
                    <i class="el-icon-setting" style="margin-right: 15px"></i>
                    <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item @click.native="logout">退出</el-dropdown-item>
                        <el-dropdown-item @click.native="modifyPwdDialogVisible = true">修改密码</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
                <span>{{ account }}</span>
            </el-header>
            <el-main style="padding: 0">
                <transition name="move" mode="out-in">
                    <keep-alive>
                        <router-view></router-view>
                    </keep-alive>
                </transition>
            </el-main>


            <el-dialog title="修改密码" :visible.sync="modifyPwdDialogVisible">
                <el-form :model="updatePwdRequest" ref="updatePwdForm" :rules="rules">
                    <el-form-item label="旧密码" :label-width="formLabelWidth" prop="oldPwd">
                        <el-input v-model="updatePwdRequest.oldPwd" autocomplete="off" placeholder="请输入旧密码"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" :label-width="formLabelWidth" prop="newPwd">
                        <el-input v-model="updatePwdRequest.newPwd" autocomplete="off" placeholder="请输入新密码"></el-input>
                    </el-form-item>
                    <el-form-item label="确认新密码" :label-width="formLabelWidth" prop="confirmNewPwd">
                        <el-input v-model="updatePwdRequest.confirmNewPwd" autocomplete="off" placeholder="请确认新密码"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="modifyPwdDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="updatePwd('updatePwdForm')">修 改</el-button>
                </div>
            </el-dialog>

        </el-container>
    </el-container>
</template>

<script>

import {mapState} from "vuex";

export default {
    data() {
        return {
            tagsList: [],
            collapse: false,
            modifyPwdDialogVisible: false,
            formLabelWidth: '120px',
            updatePwdRequest: {},
            rules: {
                oldPwd: [
                    {required: true, message: '旧密码不能为空', trigger: 'blur'}
                ],
                newPwd: [
                    {required: true, message: '新密码不能为空', trigger: 'blur'}
                ],
                confirmNewPwd: [
                    {required: true, message: '新密码不能为空', trigger: 'blur'}
                ]
            },
        }
    },
    components: {},
    created() {
        this.$store.dispatch('getAppList');
        this.$store.dispatch('getAccount')
    },
    computed: mapState({
        account: state => state.user.account,
    }),
    methods: {
        go2home() {
            if (this.$router.history.current.path !== '/index') {
                this.$router.replace('/index')
            }
        },
        logout() {
            localStorage.clear();
            this.$router.replace('/login')
        },
        updatePwd(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {

                    if (this.updatePwdRequest.newPwd !== this.updatePwdRequest.confirmNewPwd) {
                        this.$message.error('两次输入的密码不一致');
                    } else {
                        this.$store.dispatch('updatePwd', {
                            oldPassword: this.updatePwdRequest.oldPwd,
                            newPassword: this.updatePwdRequest.newPwd
                        })
                        this.modifyPwdDialogVisible = false;
                    }
                }
            });
        }
    }
}
</script>

<style lang="css" scoped>
.el-header {
    color: #333;
    line-height: 60px;
    /*background-color: rgb(238, 241, 246);*/
}

.el-aside {
    color: #333;
}

</style>
