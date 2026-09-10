<template>
    <svg
        class="brand-mark"
        :width="size"
        :height="size"
        viewBox="0 0 24 24"
        fill="none"
        role="img"
        aria-label="野火"
    >
        <!-- 外焰 -->
        <path
            class="flame-outer"
            d="M12.6 1.6c-.3-.4-.9-.3-1.1.1-1.1 2-2.6 3.6-4.3 5.3C5 9.1 3.6 11.4 3.6 14.4c0 4.6 3.8 8 8.4 8s8.4-3.4 8.4-8c0-2.5-1.1-4.4-2.9-6.3-.3-.3-.8-.1-.8.3-.1 1.3-.6 2.3-1.4 3-.1-3.8-1.3-6.9-2.7-9.8z"
        />
        <!-- 内焰：火焰最热的部分，也是品牌名的由来 -->
        <path
            class="flame-inner"
            d="M12.3 12.3c-.2-.3-.6-.2-.7.1-.5 1.1-1.2 1.8-1.9 2.5-.8.8-1.3 1.7-1.3 2.9 0 2 1.7 3.5 3.7 3.5s3.7-1.5 3.7-3.5c0-1.9-1.8-3.7-3.5-5.5z"
        />
    </svg>
</template>

<script>
export default {
    name: 'BrandMark',
    props: {
        size: {
            type: [Number, String],
            default: 24
        }
    }
}
</script>

<style scoped>
.brand-mark {
    display: block;
    flex: none;
}

.flame-outer {
    fill: var(--wf-brand-500);
}

.flame-inner {
    fill: var(--wf-brand-200);
}
</style>
