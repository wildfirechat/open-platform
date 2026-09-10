<template>
    <!--
      与 organization-web 的 logo.svg 同一种构造：品牌色圆角方块 + 白色图形，
      这样两个后台并排放在一起时是同一个产品家族。图形用火焰，对应「野火」。
    -->
    <svg
        class="brand-mark"
        :width="size"
        :height="size"
        viewBox="0 0 48 48"
        fill="none"
        role="img"
        aria-label="野火开放平台"
    >
        <rect width="48" height="48" :rx="radius" fill="var(--wf-brand)"/>
        <path
            d="M25.2 9.6c-.5-.7-1.6-.5-1.9.3-1.3 3.4-3.2 5.6-5.1 7.6-2.4 2.5-4.4 5.3-4.4 9.4 0 5.9 4.6 10.2 10.2 10.2s10.2-4.3 10.2-10.2c0-3.2-1.4-5.8-3.6-8.2-.5-.6-1.5-.2-1.5.6-.1 1.6-.7 2.8-1.6 3.7-.2-5-1.3-9.1-2.3-13.4z"
            fill="#fff"
        />
        <path
            d="M24.4 25.1c-.3-.5-1-.4-1.2.1-.6 1.4-1.5 2.3-2.3 3.1-.9.9-1.5 2-1.5 3.4 0 2.4 2 4.1 4.4 4.1s4.4-1.7 4.4-4.1c0-2.3-2.1-4.4-3.8-6.6z"
            fill="var(--wf-brand)"
        />
    </svg>
</template>

<script>
export default {
    name: 'BrandMark',
    props: {
        size: {
            type: [Number, String],
            default: 26
        }
    },
    computed: {
        // 圆角按 organization-web 的 logo 比例（48 → 11）等比缩放
        radius() {
            return 48 * (11 / 48);
        }
    }
}
</script>

<style scoped>
.brand-mark {
    display: block;
    flex: none;
    border-radius: var(--wf-radius-sm);
}
</style>
