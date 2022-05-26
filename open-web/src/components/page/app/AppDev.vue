<template>
    <div style="height: 100%">
        <el-main>
            <el-card>
                <h2>自建应用</h2>
                <p>自建应用是面向组织内部发布并使用的，开发文档请看
                    <el-link href="https://docs.wildfirechat.cn/open" target="_blank" type="primary">开发文档</el-link>
                </p>
                <el-row :gutter="20">
                    <el-col :span="6">
                        <div class="create-button-container" @click="createAppDialogVisible = true">
                            <el-button type="medium" class="button">+创建应用</el-button>
                        </div>
                    </el-col>
                    <el-col :span="6" v-for="(app, index) in apps" :key="index" @click.native="showAppInfo(app)">
                        <AppCard :app="app"/>
                    </el-col>
                </el-row>
            </el-card>
            <el-dialog title="创建应用" :visible.sync="createAppDialogVisible">
                <el-form :model="createAppInfo" :rules="rules" ref="createAppForm">
                    <el-form-item label="应用图标地址" :label-width="formLabelWidth" prop="portraitUrl">
                        <el-input v-model="createAppInfo.portraitUrl" autocomplete="off" disabled placeholder="应用图标地址"></el-input>
                        <el-upload
                            class="upload-demo"
                            action="http://localhost:8880/api/application/media/upload/"
                            :with-credentials="true"
                            :on-success="onPortraitUploaded"
                            :before-upload="beforePortraitUpload"
                            :show-file-list="false">
                            <el-button size="small" type="primary" style="margin-top: 8px">点击上传</el-button>
                            <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
                        </el-upload>
                    </el-form-item>
                    <el-form-item label="应用名称" :label-width="formLabelWidth" prop="name">
                        <el-input v-model="createAppInfo.name" autocomplete="off" placeholder="测试应用"></el-input>
                    </el-form-item>
                    <el-form-item label="应用描述" :label-width="formLabelWidth" prop="description">
                        <el-input v-model="createAppInfo.description" autocomplete="off" placeholder="应用的一句话描述"></el-input>
                    </el-form-item>
                    <el-form-item label="移动端地址" :label-width="formLabelWidth" prop="mobileUrl">
                        <el-input v-model="createAppInfo.mobileUrl" autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                    </el-form-item>
                    <el-form-item label="桌面端地址" :label-width="formLabelWidth" prop="desktopUrl">
                        <el-input v-model="createAppInfo.desktopUrl" autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                    </el-form-item>
                    <el-form-item label="回调/服务端地址" :label-width="formLabelWidth" prop="serverUrl">
                        <el-input v-model="createAppInfo.serverUrl" autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                    </el-form-item>
                    <el-checkbox label="是否是全局应用" v-model="createAppInfo.global"></el-checkbox>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="createAppDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="submitForm('createAppForm')">确 定</el-button>
                </div>
            </el-dialog>

            <el-dialog title="修改应用" :visible.sync="modifyAppDialogVisible">
                <el-form :model="modifyAppInfo">
                    <el-form-item label="targetId" :label-width="formLabelWidth">
                        <p>{{ modifyAppInfo.targetId }}</p>
                    </el-form-item>
                    <el-form-item label="secret" :label-width="formLabelWidth">
                        <p>{{ modifyAppInfo.secret }}</p>
                    </el-form-item>
                    <el-form-item label="应用图标地址" :label-width="formLabelWidth">
                        <el-input v-model="modifyAppInfo.portraitUrl" autocomplete="off" placeholder="应用图标地址"></el-input>
                    </el-form-item>
                    <el-form-item label="应用名称" :label-width="formLabelWidth">
                        <el-input v-model="modifyAppInfo.name" autocomplete="off" placeholder="测试应用"></el-input>
                    </el-form-item>
                    <el-form-item label="应用描述" :label-width="formLabelWidth">
                        <el-input v-model="modifyAppInfo.description" autocomplete="off" placeholder="应用的一句话描述"></el-input>
                    </el-form-item>
                    <el-form-item label="移动端地址" :label-width="formLabelWidth">
                        <el-input v-model="modifyAppInfo.mobileUrl" autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                    </el-form-item>
                    <el-form-item label="桌面端地址" :label-width="formLabelWidth">
                        <el-input v-model="modifyAppInfo.desktopUrl" autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                    </el-form-item>
                    <el-form-item label="回调/服务端地址" :label-width="formLabelWidth">
                        <el-input v-model="modifyAppInfo.serverUrl" autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                    </el-form-item>
                    <el-checkbox label="是否是全局应用" v-model="modifyAppInfo.global"></el-checkbox>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="modifyAppDialogVisible = false">取 消</el-button>
                    <el-button type="danger" @click="deleteApp">删 除</el-button>
                    <el-button type="primary" @click="updateApp">修 改</el-button>
                </div>
            </el-dialog>
        </el-main>
    </div>
</template>

<script>
import {mapState} from "vuex";
import AppCard from "@/components/common/AppCard";
import AppInfo from "@/model/appInfo";

export default {
    name: "AppDev",
    data() {
        return {
            currentApp: null,
            createAppDialogVisible: false,
            modifyAppDialogVisible: false,
            createAppInfo: new AppInfo(),
            modifyAppInfo: new AppInfo(),
            formLabelWidth: '140px',

            rules: {
                portraitUrl: [
                    {required: true, message: '请上传头像', trigger: 'blur'},
                ],
                name: [
                    {required: true, message: '请输入应用名称', trigger: 'blur'},
                    {min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur'}
                ],
                description: [
                    {required: true, message: '请输入应用描述', trigger: 'blur'},
                    {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}
                ],
                mobileUrl: [
                    {required: true, message: '请输入移动端入口地址', trigger: 'blur'},
                ],
                desktopUrl: [
                    {required: true, message: '请输入桌面端入口地址', trigger: 'blur'},
                ],
                serverUrl: [
                    {required: true, message: '请输入回调地址', trigger: 'blur'},
                ],
            }
        }
    },
    methods: {
        submitForm(formName) {
            console.log('submitForm', formName)
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    this.createAppDialogVisible = false;
                    this.$store.dispatch('createApp', this.createAppInfo);
                    this.createAppInfo = new AppInfo();
                } else {
                    console.log('error submit!!');
                    return false;
                }
            });
        },
        showAppInfo(app) {
            this.modifyAppInfo = app;
            this.modifyAppDialogVisible = true;
        },
        updateApp() {
            this.modifyAppDialogVisible = false;
            this.$store.dispatch('updateApp', this.modifyAppInfo);
            this.modifyAppInfo = new AppInfo();

        },
        deleteApp() {
            this.modifyAppDialogVisible = false;
            this.$store.dispatch("deleteApp", this.modifyAppInfo.targetId)
        },

        onPortraitUploaded(res, file) {
            if (res.code === 0) {
                this.createAppInfo.portraitUrl = res.result.url;
            }
            console.log('res, file', res, file)
        },
        beforePortraitUpload(file) {
            const isJPG = file.type === 'image/jpeg';
            const isPNG = file.type === 'image/png';
            const isLt2M = file.size / 1024 / 1024 < 2;

            if (!isJPG && !isPNG) {
                this.$message.error('上传头像图片只能是 JPG/PNG 格式!');
            }
            if (!isLt2M) {
                this.$message.error('上传头像图片大小不能超过 2MB!');
            }
            return isJPG && isLt2M;
        }
    },
    computed: mapState({
        apps: state => state.app.apps,
    }),
    components: {
        AppCard,
    }

}
</script>

<style lang="css" scoped>

.create-button-container {
    display: flex;
    width: 250px;
    height: 100px;
    margin: 20px 10px;
    justify-content: center;
    align-items: center;
}

.create-button-container .button {
    padding: 20px 30px;
}

</style>