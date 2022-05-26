<template>
    <div id="app">
        <router-view></router-view>
    </div>
</template>

<script>

export default {
    name: 'App',
    data() {
        return {}
    },

    created() {
        document.title = this.$t('login.title').toString();
    }
}
</script>

<style>
@import "./assets/css/main.css";
@import "./assets/css/color-dark.css";
@import "./assets/css/iconfont.css";
</style>
