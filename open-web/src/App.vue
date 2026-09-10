<template>
    <router-view/>
</template>

<script>
export default {
    name: 'App',
    created() {
        document.title = this.$t('login.title').toString();
    }
};
</script>
