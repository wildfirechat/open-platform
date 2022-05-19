<template>
    <div id="app">
        <router-view></router-view>
    </div>
</template>

<script>
import {mapGetters} from 'vuex'

export default {
    name: 'App',
    computed: mapGetters({
        'errorMsg': 'errorMsg'
    }),
    watch: {
        errorMsg() {
            if (this.errorMsg) {
                this.$message(this.errorMsg.msg)
            }
        }
    },
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
