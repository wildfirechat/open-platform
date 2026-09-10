<template>
    <div class="error-page">
        <div class="error-body">
            <p class="code">{{ code }}</p>
            <h1 class="title">{{ title }}</h1>
            <p class="hint">{{ hint }}</p>
            <div class="actions">
                <el-button type="primary" @click="goHome">回到应用列表</el-button>
                <el-button @click="goBack">返回上一页</el-button>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'ErrorPage',
    props: {
        code: {
            type: [String, Number],
            required: true
        },
        title: {
            type: String,
            required: true
        },
        // 说明出了什么问题、可以怎么办，而不是只卖个萌
        hint: {
            type: String,
            default: ''
        }
    },
    methods: {
        goHome() {
            this.$router.replace('/index');
        },
        goBack() {
            this.$router.go(-1);
        }
    }
};
</script>

<style scoped>
.error-page {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    padding: var(--wf-space-6);
    background: var(--wf-canvas);
}

.error-body {
    max-width: 44ch;
}

.code {
    font-family: var(--wf-font-mono);
    font-size: var(--wf-text-sm);
    color: var(--wf-text-faint);
    font-variant-numeric: tabular-nums;
}

.title {
    margin-top: var(--wf-space-2);
    font-size: var(--wf-text-xl);
    font-weight: 600;
    letter-spacing: -0.01em;
    color: var(--wf-text);
}

.hint {
    margin-top: var(--wf-space-2);
    font-size: var(--wf-text-base);
    line-height: 1.7;
    color: var(--wf-text-muted);
}

.actions {
    display: flex;
    flex-wrap: wrap;
    gap: var(--wf-space-2);
    margin-top: var(--wf-space-5);
}
</style>
