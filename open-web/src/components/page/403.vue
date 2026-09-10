<template>
    <ErrorPage
        code="403"
        title="没有访问权限"
        hint="当前账号无权查看这个页面。如果需要访问，请联系开放平台管理员。"
    />
</template>

<script>
import ErrorPage from '@/components/common/ErrorPage';

export default {
    name: 'Forbidden',
    components: { ErrorPage }
};
</script>
