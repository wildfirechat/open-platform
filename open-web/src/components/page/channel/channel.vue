<template>
    <div style="height: 100%">
        <el-main>
            <el-card>
                <h2>频道</h2>
                <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center">
                    <p>类似微信公众号，开发文档请看
                        <el-link href="https://docs.wildfirechat.cn/open" style="flex: 1" target="_blank" type="primary">开发文档</el-link>
                    </p>
                    <el-button type="primary" @click="createAppDialogVisible = true">创建频道</el-button>
                </div>
                <el-row :gutter="20" v-if="apps && apps.length > 0">
                    <el-col :span="6" v-for="(app, index) in apps" :key="index" @click.native="showAppInfo(app)">
                        <AppCard :app="app"/>
                    </el-col>
                </el-row>
                <el-empty v-else description="暂无频道" image=""></el-empty>
            </el-card>
            <el-dialog title="创建频道" :visible.sync="createAppDialogVisible">
                <el-form :model="createAppInfo" :rules="rules" ref="createAppForm">
                    <el-form-item label="频道图标地址" :label-width="formLabelWidth" prop="portraitUrl">
                        <el-input v-model.trim="createAppInfo.portraitUrl" autocomplete="off" placeholder="频道图标地址"></el-input>
                        <el-upload
                            class="upload-demo"
                            :action="uploadMediaUrl"
                            :with-credentials="true"
                            :on-success="onPortraitUploaded"
                            :before-upload="beforePortraitUpload"
                            :show-file-list="false">
                            <el-button size="small" type="primary" style="margin-top: 8px">点击上传</el-button>
                            <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
                        </el-upload>
                    </el-form-item>
                    <el-form-item label="频道名称" :label-width="formLabelWidth" prop="name">
                        <el-input v-model.trim="createAppInfo.name" autocomplete="off" placeholder="频道名称"></el-input>
                    </el-form-item>
                    <el-form-item label="频道描述" :label-width="formLabelWidth" prop="description">
                        <el-input v-model.trim="createAppInfo.description" autocomplete="off" placeholder="频道的一句话描述"></el-input>
                    </el-form-item>
                    <el-form-item label="频道服务端地址" :label-width="formLabelWidth" prop="serverUrl">
                        <el-input v-model.trim="createAppInfo.serverUrl" autocomplete="off" placeholder="http://{channel-server/频道服务}"></el-input>
                        <label>点击查看
                            <a href="https://github.com/wildfirechat/channel-platform" target="_blank">野火频道服务</a>
                        </label>
                    </el-form-item>
                    <el-checkbox label="广播号(默认是订阅号，发送消息时，只给已经订阅了的用户发送；广播号，发送消息时，给所有人发送)" v-model.trim="modifyAppInfo.global"></el-checkbox>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="createAppDialogVisible = false">取 消</el-button>
                    <el-button type="primary" @click="submitForm('createAppForm')">确 定</el-button>
                </div>
            </el-dialog>

            <el-dialog title="修改频道" :visible.sync="modifyAppDialogVisible">
                <el-form :model="modifyAppInfo" :rules="rules" ref="modifyAppForm">
                    <el-form-item label="channelId" :label-width="formLabelWidth">
                        <p>{{ modifyAppInfo.targetId }}</p>
                    </el-form-item>
                    <el-form-item label="secret" :label-width="formLabelWidth">
                        <p>{{ modifyAppInfo.secret }}</p>
                    </el-form-item>
                    <el-form-item label="频道图标地址" :label-width="formLabelWidth" prop="portraitUrl">
                        <el-input v-model.trim="modifyAppInfo.portraitUrl" autocomplete="off" placeholder="频道图标地址"></el-input>
                        <el-upload
                            class="upload-demo"
                            :action="uploadMediaUrl"
                            :with-credentials="true"
                            :on-success="onPortraitUpdated"
                            :before-upload="beforePortraitUpload"
                            :show-file-list="false">
                            <el-button size="small" type="primary" style="margin-top: 8px">点击上传</el-button>
                            <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过500kb</div>
                        </el-upload>
                    </el-form-item>
                    <el-form-item label="频道名称" :label-width="formLabelWidth" prop="name">
                        <el-input v-model.trim="modifyAppInfo.name" autocomplete="off" placeholder="测试频道"></el-input>
                    </el-form-item>
                    <el-form-item label="频道描述" :label-width="formLabelWidth" prop="description">
                        <el-input v-model.trim="modifyAppInfo.description" autocomplete="off" placeholder="频道的一句话描述"></el-input>
                    </el-form-item>
                    <el-form-item label="回调地址" :label-width="formLabelWidth" prop="serverUrl">
                        <el-input v-model.trim="modifyAppInfo.serverUrl" autocomplete="off" :placeholder='"http://{channel-server/频道服务}/" + modifyAppInfo.targetId '></el-input>
                        <label style="color: red"> 野火官方频道服务的回调地址是包含 channelId 的 !!! </label>
                    </el-form-item>
                    <el-checkbox label="广播号(默认是订阅号，发送消息时，只能给已经订阅了的用户发送；广播号，发送消息时，给所有人发送)" v-model.trim="modifyAppInfo.global"></el-checkbox>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="modifyAppDialogVisible = false">取 消</el-button>
                    <el-button type="danger" @click="deleteApp">删 除</el-button>
                    <el-button type="primary" @click="updateApp('modifyAppForm')">修 改</el-button>
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
    name: "Channel",
    data() {
        return {
            currentApp: null,
            createAppDialogVisible: false,
            modifyAppDialogVisible: false,
            createAppInfo: new AppInfo(1),
            modifyAppInfo: new AppInfo(1),
            formLabelWidth: '140px',
            uploadMediaUrl: '/api/application/media/upload/',
            // 本地调试
            // uploadMediaUrl: 'http://localhost:8880/api/application/media/upload/',

            rules: {
                portraitUrl: [
                    {required: true, message: '请上传头像', trigger: 'blur'},
                ],
                name: [
                    {required: true, message: '请输入频道名称', trigger: 'blur'},
                    {min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur'}
                ],
                description: [
                    {required: true, message: '请输入频道描述', trigger: 'blur'},
                    {min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur'}
                ],
                serverUrl: [
                    {required: false, message: '请输入回调地址', trigger: 'blur'},
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
                    this.createAppInfo = new AppInfo(1);
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
        updateApp(formName) {
            this.$refs[formName].validate((valid) => {
                if (valid) {
                    this.modifyAppDialogVisible = false;
                    this.$store.dispatch('updateApp', this.modifyAppInfo);
                    this.modifyAppInfo = new AppInfo(1);
                } else {
                    console.log('error submit!!');
                    return false;
                }
            });

        },
        deleteApp() {
            this.modifyAppDialogVisible = false;
            this.$store.dispatch("deleteApp", this.modifyAppInfo.targetId)
        },

        onPortraitUploaded(res, file) {
            if (res.code === 0) {
                this.createAppInfo.portraitUrl = res.result.url;
            } else {
                this.$message.error('头像上传失败 ' + res);
            }
            console.log('res, file', res, file)
        },

        onPortraitUpdated(res, file) {
            if (res.code === 0) {
                this.modifyAppInfo.portraitUrl = res.result.url;
            } else {
                this.$message.error('头像上传失败 ' + res);
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
            return (isJPG || isPNG) && isLt2M;
        }
    },
    computed: mapState({
        apps: state => state.app.channels,
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

>>> .el-empty__image {
    display: none;
}

</style>
