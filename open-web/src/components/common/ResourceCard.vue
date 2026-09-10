<template>
    <div
        class="resource-card"
        role="button"
        tabindex="0"
        :aria-label="`编辑 ${app.name}`"
        :style="{ '--card-accent': accent }"
        @click="$emit('open', app)"
        @keydown.enter.prevent="$emit('open', app)"
        @keydown.space.prevent="$emit('open', app)"
    >
        <div class="card-head">
            <img
                v-if="app.portraitUrl && !imageFailed"
                class="portrait"
                :src="app.portraitUrl"
                :alt="app.name"
                @error="imageFailed = true"
            />
            <!-- 图标缺失或加载失败时用首字母占位，避免出现空白方块 -->
            <span v-else class="portrait portrait-fallback" aria-hidden="true">
                {{ initial }}
            </span>

            <div class="identity">
                <p class="name" :title="app.name">{{ app.name }}</p>
                <p class="description" :title="app.description">{{ app.description || '未填写描述' }}</p>
            </div>

            <span v-if="badges.length" class="badges">
                <span v-for="tag in badges" :key="tag" class="badge">{{ tag }}</span>
            </span>
        </div>

        <!-- 对开发者来说 targetId 和名称一样是这个资源的身份，值得直接展示 -->
        <div class="card-id">
            <span class="id-label">{{ idLabel }}</span>
            <code class="wf-mono id-value" :title="app.targetId">{{ app.targetId || '—' }}</code>
            <button
                v-if="app.targetId"
                class="copy"
                type="button"
                :aria-label="`复制 ${idLabel}`"
                @click.stop="copy"
            >
                {{ copied ? '已复制' : '复制' }}
            </button>
        </div>
    </div>
</template>

<script>
import { copyText } from '@/utils/clipboard';

export default {
    name: 'ResourceCard',
    props: {
        app: {
            type: Object,
            required: true
        },
        // 顶部色条颜色，用来区分应用 / 频道 / 机器人
        accent: {
            type: String,
            default: 'var(--wf-brand)'
        },
        idLabel: {
            type: String,
            default: 'targetId'
        },
        // 例如「全局」「广播」「网页」，可以同时有多个
        badges: {
            type: Array,
            default: () => []
        }
    },
    emits: ['open'],
    data() {
        return {
            copied: false,
            imageFailed: false,
            copiedTimer: null
        };
    },
    computed: {
        initial() {
            return this.app.name ? this.app.name.trim().charAt(0) : '?';
        }
    },
    watch: {
        // 同一个组件实例可能被复用到另一个资源上，需要重置图片失败状态
        'app.portraitUrl'() {
            this.imageFailed = false;
        }
    },
    methods: {
        async copy() {
            const ok = await copyText(this.app.targetId);
            if (!ok) {
                this.$message.error('复制失败，请手动选中复制');
                return;
            }
            this.copied = true;
            clearTimeout(this.copiedTimer);
            this.copiedTimer = setTimeout(() => {
                this.copied = false;
            }, 1600);
        }
    },
    beforeUnmount() {
        clearTimeout(this.copiedTimer);
    }
};
</script>

<style scoped>
/* 无边框 + 轻阴影，与 organization-web 的 .el-card / .wf-panel 一致 */
.resource-card {
    display: flex;
    flex-direction: column;
    gap: var(--wf-space-3);
    padding: var(--wf-space-4);
    background: var(--wf-surface);
    border: none;
    border-radius: var(--wf-radius-lg);
    box-shadow: var(--wf-shadow-sm);
    cursor: pointer;
    transition: box-shadow 0.15s ease;
}

.resource-card:hover {
    box-shadow: var(--wf-shadow);
}

.card-head {
    display: flex;
    align-items: flex-start;
    gap: var(--wf-space-3);
    min-width: 0;
}

.portrait {
    flex: none;
    width: 44px;
    height: 44px;
    border-radius: var(--wf-radius);
    object-fit: cover;
    background: var(--wf-fill);
    /* 浅色图标在白底上会"糊"掉，加一道内描边把它固定住 */
    box-shadow: inset 0 0 0 1px rgba(29, 33, 41, 0.08);
}

.portrait-fallback {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: var(--wf-text-md);
    font-weight: 600;
    color: var(--card-accent);
    background: var(--wf-fill);
}

.identity {
    flex: 1;
    min-width: 0;
}

.name {
    font-size: var(--wf-text-base);
    font-weight: 600;
    color: var(--wf-text-1);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.description {
    margin-top: 2px;
    font-size: var(--wf-text-sm);
    color: var(--wf-text-3);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.badges {
    flex: none;
    display: flex;
    gap: var(--wf-space-1);
}

.badge {
    padding: 1px 8px;
    font-size: var(--wf-text-xs);
    line-height: 18px;
    color: var(--card-accent);
    background: var(--wf-fill);
    border-radius: var(--wf-radius-pill);
}

.card-id {
    display: flex;
    align-items: center;
    gap: var(--wf-space-2);
    padding-top: var(--wf-space-3);
    border-top: 1px solid var(--wf-border-light);
    min-width: 0;
}

.id-label {
    flex: none;
    font-size: var(--wf-text-xs);
    color: var(--wf-text-4);
}

.id-value {
    flex: 1;
    min-width: 0;
    color: var(--wf-text-2);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.copy {
    flex: none;
    padding: 2px 8px;
    font: inherit;
    font-size: var(--wf-text-xs);
    color: var(--wf-brand);
    background: transparent;
    border: 1px solid var(--wf-border);
    border-radius: var(--wf-radius);
    cursor: pointer;
    /* 平时不抢视线，鼠标移到卡片上或键盘聚焦时才显现 */
    opacity: 0;
    transition: opacity 0.15s ease, background-color 0.15s ease;
}

.resource-card:hover .copy,
.copy:focus-visible {
    opacity: 1;
}

.copy:hover {
    background: var(--wf-brand-bg);
}

/* 触摸设备上没有 hover，复制按钮必须常驻 */
@media (hover: none) {
    .copy {
        opacity: 1;
    }
}
</style>
