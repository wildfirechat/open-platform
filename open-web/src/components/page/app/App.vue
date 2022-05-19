<template>
    <el-container style="height: 100%">
        <el-header class="header">
            <p class="title"><i class="el-icon-back" @click="goBack"></i> 自建应用开发 </p>
            <p class="content">{{ app.title }}</p>
        </el-header>
        <el-main style="display: flex; flex-direction: column">
            <el-card style="flex: 1" class="el-card-define">
                <el-row :gutter="20" style="flex: 1">
                    <el-col :span="6" style="height: 100%">
                        <el-menu style="height: 100%; width: 250px; border-right: 1px solid lightgrey">
                            <el-menu-item>基本信息</el-menu-item>
                            <el-menu-item>入口管理</el-menu-item>
                            <el-menu-item>可信域名</el-menu-item>
                            <el-menu-item>回调事件</el-menu-item>
                            <el-menu-item>接口授权</el-menu-item>
                            <el-menu-item>发布应用</el-menu-item>
                        </el-menu>
                    </el-col>
                    <el-col :span="18" style="height: 100%">
                        <div style="height: 100%">
                            基本信息等
                        </div>
                    </el-col>
                </el-row>
            </el-card>
        </el-main>
    </el-container>
</template>

<script>
export default {
    name: "App",
    props: {
        app: {
            type: Object,
            required: false,
            default: null,
        }
    },
    methods: {
        goBack() {
            console.log('go back')
            this.$parent.currentApp = null;
        }
    }
}
</script>

<style scoped>
.el-card-define {
    flex: 1;
}

.el-card-define >>> .el-card__body {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.header {
    padding-top: 5px;
    padding-bottom: 5px;
    background-color: rgb(238, 241, 246)
}

.header .title {
    font-size: 16px;
}

.header .content {
    font-size: 20px;
    line-height: 1.7;
}

</style>