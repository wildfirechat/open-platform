<template>
    <el-container style="height: 100%">
        <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
            <div style="height: 60px; display: flex; justify-content: center;align-items: center" @click="go2home">
                <p>野火开放平台</p>
            </div>
            <el-menu default-active='/dev/app' router>
                <el-menu-item index="/dev/app">应用</el-menu-item>
                <el-menu-item index="/dev/channel">频道</el-menu-item>
                <el-menu-item index="/dev/robot">机器人</el-menu-item>
                <el-menu-item>
                    <template #title>
                        <!-- 没有 index，需要阻止冒泡，避免 el-menu 的 router 模式跳到一个空路由 -->
                        <a href="https://docs.wildfirechat.cn/open" style="color: #303133" target="_blank"
                           @click.stop>开发文档</a>
                    </template>
                </el-menu-item>
            </el-menu>
        </el-aside>
        <el-container :class="{'content-collapse':collapse}">
            <el-header style="text-align: left; font-size: 14px; display: flex; padding-right: 40px">
                <el-button link>
                    <el-icon>
                        <ArrowLeft/>
                    </el-icon>
                </el-button>
                <span style="flex: 1"> </span>
                <el-dropdown style="margin-right: 15px" @command="handleCommand">
                    <el-icon>
                        <Setting/>
                    </el-icon>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="logout">退出</el-dropdown-item>
                            <el-dropdown-item command="updatePwd">修改密码</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
                <span>{{ account.displayName }}</span>
            </el-header>
            <el-main style="padding: 0">
                <router-view v-slot="{ Component }">
                    <transition name="move" mode="out-in">
                        <keep-alive>
                            <component :is="Component"/>
                        </keep-alive>
                    </transition>
                </router-view>
            </el-main>


            <el-dialog title="修改密码" v-model="modifyPwdDialogVisible">
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
                <template #footer>
                    <div class="dialog-footer">
                        <el-button @click="modifyPwdDialogVisible = false">取 消</el-button>
                        <el-button type="primary" @click="updatePwd('updatePwdForm')">修 改</el-button>
                    </div>
                </template>
            </el-dialog>

        </el-container>
    </el-container>
</template>

<script>

import {mapState} from "vuex";
import {ArrowLeft, Setting} from '@element-plus/icons-vue'

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
    components: {ArrowLeft, Setting},
    created() {
        this.$store.dispatch('getAppList');
        this.$store.dispatch('getAccount')
    },
    computed: mapState({
        account: state => state.user.account,
    }),
    methods: {
        handleCommand(command) {
            if (command === 'logout') {
                this.logout();
            } else if (command === 'updatePwd') {
                this.modifyPwdDialogVisible = true;
            }
        },
        go2home() {
            if (this.$route.path !== '/index') {
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
