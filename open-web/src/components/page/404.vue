<template>
    <ErrorPage
        code="404"
        title="页面不存在"
        hint="地址可能拼错了，或者这个页面已经被移除。"
    />
</template>

<script>
import ErrorPage from '@/components/common/ErrorPage';

export default {
    name: 'NotFound',
    components: { ErrorPage }
};
</script>
