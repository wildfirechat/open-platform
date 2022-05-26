<template>
    <div class="app-card">
        <el-avatar shape="square" :size="60" style="margin: 0 10px" :src="app.portraitUrl">
        </el-avatar>
        <div>
            <p class="title">{{ app.name }}</p>
            <p class="desc">{{ app.description }}</p>
        </div>
    </div>
</template>

<script>
export default {
    name: "AppCard",
    props: {
        app: {
            type: Object,
            required: true,
        }
    },
    methods: {}
}
</script>

<style lang="css" scoped>
.app-card {
    display: flex;
    flex-direction: row;
    align-items: center;
    width: 250px;
    height: 100px;
    margin: 20px 10px;
}

.app-card:hover {
    background: #EEF5FE;
}

.app-card div {
    flex: 1;
    height: 60px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.app-card .title {
    font-size: 18px;
}

.app-card .desc {
    font-size: 14px;
}

>>>.el-avatar > img {
    width: 100%;
}

</style>