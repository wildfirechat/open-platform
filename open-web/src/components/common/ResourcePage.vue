<template>
    <div class="resource-page">
        <header class="page-header">
            <div class="page-heading">
                <h1>{{ config.title }}</h1>
                <span v-if="loaded" class="count">{{ items.length }}</span>
            </div>
            <p class="page-summary">
                {{ config.summary }}
                <a :href="config.docsUrl" target="_blank" rel="noopener">开发文档 ↗</a>
            </p>
            <el-button type="primary" class="page-action" @click="openCreate">
                {{ config.createLabel }}
            </el-button>
        </header>

        <!-- 加载中先占位，避免空状态一闪而过 -->
        <div v-if="!loaded" class="grid" aria-busy="true">
            <div v-for="n in 4" :key="n" class="skeleton"></div>
        </div>

        <div v-else-if="items.length" class="grid">
            <ResourceCard
                v-for="item in items"
                :key="item.targetId"
                :app="item"
                :accent="config.accent"
                :id-label="config.idLabel"
                :badges="badgesFor(item)"
                @open="openEdit"
            />
        </div>

        <div v-else class="empty">
            <p class="empty-title">{{ config.emptyTitle }}</p>
            <p class="empty-hint">{{ config.emptyHint }}</p>
            <el-button type="primary" @click="openCreate">{{ config.createLabel }}</el-button>
        </div>

        <ResourceFormDialog
            v-model="createVisible"
            mode="create"
            :config="config"
            :source="draft"
            :submitting="submitting"
            @submit="create"
        />

        <ResourceFormDialog
            v-model="editVisible"
            mode="edit"
            :config="config"
            :source="editing"
            :submitting="submitting"
            @submit="update"
            @delete="remove"
        />
    </div>
</template>

<script>
import ResourceCard from '@/components/common/ResourceCard';
import ResourceFormDialog from '@/components/common/ResourceFormDialog';
import AppInfo from '@/model/appInfo';

export default {
    name: 'ResourcePage',
    components: { ResourceCard, ResourceFormDialog },
    props: {
        // 来自 config/resourceTypes.js，决定这个页面展示哪一类资源
        config: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            createVisible: false,
            editVisible: false,
            submitting: false,
            draft: new AppInfo(this.config.type),
            editing: new AppInfo(this.config.type)
        };
    },
    computed: {
        items() {
            return this.$store.state.app[this.config.storeKey] || [];
        },
        loaded() {
            return this.$store.state.app.loaded;
        }
    },
    methods: {
        badgesFor(item) {
            const badges = [];
            if (this.config.key === 'app') {
                if (item.global) {
                    badges.push('全局');
                }
                if (item.type === 3) {
                    badges.push('网页');
                }
            } else if (this.config.key === 'channel' && item.global) {
                badges.push('广播');
            }
            return badges;
        },
        openCreate() {
            this.draft = new AppInfo(this.config.type);
            this.createVisible = true;
        },
        openEdit(item) {
            // 弹窗内部会再复制一份，这里只是把选中的资源交给它作为初始值
            this.editing = item;
            this.editVisible = true;
        },
        create(payload) {
            this.run(
                this.$store.dispatch('createApp', payload),
                () => {
                    this.createVisible = false;
                    this.$message.success(`${this.config.title}已创建`);
                }
            );
        },
        update(payload) {
            this.run(
                this.$store.dispatch('updateApp', payload),
                () => {
                    this.editVisible = false;
                    this.$message.success('修改已保存');
                }
            );
        },
        remove(payload) {
            this.run(
                this.$store.dispatch('deleteApp', payload.targetId),
                () => {
                    this.editVisible = false;
                    this.$message.success(`${this.config.title}已删除`);
                }
            );
        },
        // 三个操作的加载态和异常处理完全一致，收敛到一处
        run(promise, onSuccess) {
            this.submitting = true;
            Promise.resolve(promise)
                .then(onSuccess)
                .catch(() => { /* 具体错误信息由 axios 拦截器统一提示 */ })
                .finally(() => {
                    this.submitting = false;
                });
        }
    }
};
</script>

<style scoped>
.resource-page {
    padding: 20px;
    max-width: 1440px;
}

/* 与 organization-web 的 PageHeader 同一套排版 */
.page-header {
    display: grid;
    grid-template-columns: 1fr auto;
    align-items: start;
    column-gap: var(--wf-space-4);
    margin-bottom: var(--wf-space-4);
}

.page-heading {
    display: flex;
    align-items: baseline;
    gap: var(--wf-space-2);
}

.page-heading h1 {
    margin: 0;
    font-size: var(--wf-text-xl);
    font-weight: 600;
    line-height: 28px;
    color: var(--wf-text-1);
}

.count {
    font-size: var(--wf-text-sm);
    color: var(--wf-text-3);
    font-variant-numeric: tabular-nums;
}

.page-summary {
    grid-column: 1;
    margin: 4px 0 0;
    font-size: var(--wf-text-sm);
    line-height: 20px;
    color: var(--wf-text-3);
    max-width: 68ch;
}

.page-action {
    grid-column: 2;
    grid-row: 1 / span 2;
}

.grid {
    display: grid;
    /* 卡片自适应列数：原来固定 250px 宽的卡片放在 el-col :span="6" 里，
       窗口一窄就会互相重叠。 */
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: var(--wf-space-4);
}

.skeleton {
    height: 128px;
    border-radius: var(--wf-radius-lg);
    background: linear-gradient(
        100deg,
        var(--wf-border-light) 30%,
        var(--wf-fill) 50%,
        var(--wf-border-light) 70%
    );
    background-size: 220% 100%;
    animation: shimmer 1.2s linear infinite;
}

@keyframes shimmer {
    to {
        background-position: -220% 0;
    }
}

@media (prefers-reduced-motion: reduce) {
    .skeleton {
        animation: none;
    }
}

.empty {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: var(--wf-space-2);
    padding: 48px 20px;
    background: var(--wf-surface);
    border-radius: var(--wf-radius-lg);
    box-shadow: var(--wf-shadow-sm);
}

.empty-title {
    font-size: var(--wf-text-md);
    font-weight: 600;
    color: var(--wf-text-1);
}

.empty-hint {
    margin-bottom: var(--wf-space-2);
    font-size: var(--wf-text-sm);
    color: var(--wf-text-3);
    max-width: 46ch;
}

@media (max-width: 720px) {
    .resource-page {
        padding: 16px 12px;
    }

    .page-header {
        grid-template-columns: 1fr;
    }

    .page-action {
        grid-column: 1;
        grid-row: auto;
        margin-top: var(--wf-space-3);
        justify-self: start;
    }
}
</style>
