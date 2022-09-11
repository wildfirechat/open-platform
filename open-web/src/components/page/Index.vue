<template>
    <el-main class="hello">
        <h2>应用</h2>
        <div>
            <el-row v-if="apps && apps.length > 0" :gutter="20">
                <el-col :span="6" v-for="(app, index) in apps" :key="index">
                    <AppCard :app="app" @click.native="showAppInfo(app)"/>
                </el-col>
            </el-row>
            <el-empty v-else description="暂无应用" image="">
                <el-button @click="createApp(0)" type="primary">创建应用</el-button>
            </el-empty>
        </div>
        <div>
            <h2>频道</h2>
            <el-row v-if="channels && channels.length > 0" :gutter="20">
                <el-col :span="6" v-for="(app, index) in channels" :key="index">
                    <AppCard :app="app" @click.native="showAppInfo(app)"/>
                </el-col>
            </el-row>
            <el-empty v-else description="暂无频道">
                <el-button @click="createApp(1)" type="primary">创建频道</el-button>
            </el-empty>
        </div>
        <div>
            <h2>机器人</h2>
            <el-row v-if="robots && robots.length > 0" :gutter="20">
                <el-col :span="6" v-for="(app, index) in robots" :key="index">
                    <AppCard :app="app" @click.native="showAppInfo(app)"/>
                </el-col>
            </el-row>
            <el-empty v-else description="暂无机器人">
                <el-button @click="createApp(2)" type="primary">创建机器人</el-button>
            </el-empty>
        </div>
        <el-dialog title="应用信息" :visible.sync="appInfoDialogVisible">
            <el-form :model="appInfo">
                <el-form-item label="targetId" :label-width="formLabelWidth">
                    <p>{{ appInfo.targetId }}</p>
                </el-form-item>
                <el-form-item label="secret" :label-width="formLabelWidth">
                    <p>{{ appInfo.secret }}</p>
                </el-form-item>
                <el-form-item label="应用图标地址" :label-width="formLabelWidth">
                    <el-input v-model="appInfo.portraitUrl" disabled autocomplete="off" placeholder="https://"></el-input>
                </el-form-item>
                <el-form-item label="应用名称" :label-width="formLabelWidth">
                    <el-input v-model="appInfo.name" disabled autocomplete="off" placeholder="测试应用"></el-input>
                </el-form-item>
                <el-form-item label="应用描述" :label-width="formLabelWidth">
                    <el-input v-model="appInfo.description" disabled autocomplete="off" placeholder="应用的一句话描述"></el-input>
                </el-form-item>
                <el-form-item label="移动端地址" :label-width="formLabelWidth">
                    <el-input v-model="appInfo.mobileUrl" disabled autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                </el-form-item>
                <el-form-item label="桌面端地址" :label-width="formLabelWidth">
                    <el-input v-model="appInfo.desktopUrl" disabled autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                </el-form-item>
                <el-form-item label="回调/服务端地址" :label-width="formLabelWidth">
                    <el-input v-model="appInfo.serverUrl" disabled autocomplete="off" placeholder="https://wildfirechat.cn"></el-input>
                </el-form-item>
                <el-checkbox label="是否是全局应用" v-model="appInfo.global" disabled></el-checkbox>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="appInfoDialogVisible = false">确 定</el-button>
            </div>
        </el-dialog>
    </el-main>
</template>

<script>
import AppCard from "@/components/common/AppCard";
import {mapState} from "vuex";
import AppInfo from "@/model/appInfo";

export default {
    name: 'Index',
    components: {AppCard},
    data() {
        return {
            appInfoDialogVisible: false,
            appInfo: new AppInfo(),
            formLabelWidth: '120px'
        }
    },
    computed: mapState({
        apps: state => state.app.apps,
        channels: state => state.app.channels,
        robots: state => state.app.robots,
    }),
    methods: {
        showAppInfo(app) {
            this.appInfo = app;
            this.appInfoDialogVisible = true;
        },

        createApp(type = 0) {
            let paths = ['/dev/app', '/dev/channel', '/dev/robot'];
            this.$router.replace(paths[type]);
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h1, h2 {
    font-weight: normal;
}

>>>.el-empty__image{
    display: none;
}
</style>
