<template>
    <el-dialog
        :model-value="modelValue"
        :title="isEdit ? config.editLabel : config.createLabel"
        :width="dialogWidth"
        :close-on-click-modal="false"
        append-to-body
        @update:model-value="$emit('update:modelValue', $event)"
        @closed="onClosed"
    >
        <!-- 凭证：开发者打开这个弹窗最常见的目的就是取它，所以放在最上面 -->
        <section v-if="isEdit" class="credentials">
            <div class="credential">
                <span class="credential-label">{{ config.idLabel }}</span>
                <code class="wf-mono credential-value">{{ form.targetId }}</code>
                <button class="ghost" type="button" @click="copy(form.targetId, 'id')">
                    {{ copiedField === 'id' ? '已复制' : '复制' }}
                </button>
            </div>
            <div class="credential">
                <span class="credential-label">secret</span>
                <code class="wf-mono credential-value">{{ secretDisplay }}</code>
                <button class="ghost" type="button" @click="secretRevealed = !secretRevealed">
                    {{ secretRevealed ? '隐藏' : '显示' }}
                </button>
                <button class="ghost" type="button" @click="copy(form.secret, 'secret')">
                    {{ copiedField === 'secret' ? '已复制' : '复制' }}
                </button>
            </div>
        </section>

        <el-form
            ref="form"
            :model="form"
            :rules="rules"
            label-position="top"
            @submit.prevent
        >
            <el-form-item
                v-for="field in config.fields"
                :key="field.prop"
                :label="field.label"
                :prop="field.prop"
            >
                <!-- 图标字段：地址输入 + 上传 + 预览 -->
                <div v-if="field.kind === 'portrait'" class="portrait-field">
                    <img
                        v-if="form.portraitUrl"
                        class="portrait-preview"
                        :src="form.portraitUrl"
                        alt="图标预览"
                    />
                    <span v-else class="portrait-preview portrait-empty" aria-hidden="true">图标</span>
                    <div class="portrait-controls">
                        <el-input
                            v-model.trim="form.portraitUrl"
                            autocomplete="off"
                            :placeholder="field.placeholder"
                        />
                        <el-upload
                            :action="uploadUrl"
                            :with-credentials="true"
                            :on-success="onUploaded"
                            :before-upload="beforeUpload"
                            :show-file-list="false"
                        >
                            <el-button>上传图片</el-button>
                        </el-upload>
                    </div>
                </div>

                <el-input
                    v-else
                    v-model.trim="form[field.prop]"
                    autocomplete="off"
                    :placeholder="placeholderFor(field)"
                />

                <p v-if="field.kind === 'portrait'" class="field-hint">
                    支持 JPG / PNG，不超过 2 MB。上传需要服务端已配置 OSS。
                </p>
                <p v-else-if="field.hint" class="field-hint">
                    {{ field.hint }}
                    <a v-if="field.link" :href="field.link.url" target="_blank" rel="noopener">
                        {{ field.link.text }}
                    </a>
                </p>
            </el-form-item>

            <div v-if="config.flags.length" class="flags">
                <label v-for="flag in config.flags" :key="flag.key" class="flag">
                    <el-checkbox
                        :model-value="flagValue(flag)"
                        @update:model-value="setFlag(flag, $event)"
                    />
                    <span class="flag-text">
                        <span class="flag-label">{{ flag.label }}</span>
                        <span class="flag-description">{{ flag.description }}</span>
                    </span>
                </label>
            </div>
        </el-form>

        <template #footer>
            <div class="footer">
                <el-button v-if="isEdit" type="danger" plain @click="confirmDelete">删除</el-button>
                <span class="footer-gap"></span>
                <el-button @click="$emit('update:modelValue', false)">取消</el-button>
                <el-button type="primary" :loading="submitting" @click="submit">
                    {{ isEdit ? '保存' : '创建' }}
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script>
import { ElMessageBox } from 'element-plus';
import AppInfo from '@/model/appInfo';
import { UPLOAD_MEDIA_URL, validatePortrait, readUploadedUrl } from '@/utils/media';
import { copyText } from '@/utils/clipboard';

export default {
    name: 'ResourceFormDialog',
    props: {
        modelValue: {
            type: Boolean,
            default: false
        },
        // 来自 config/resourceTypes.js
        config: {
            type: Object,
            required: true
        },
        // 初始值。弹窗内部编辑的是它的副本，不会直接改到列表里的数据，
        // 所以用户中途点"取消"时列表不会残留改了一半的内容。
        source: {
            type: Object,
            required: true
        },
        mode: {
            type: String,
            default: 'create',
            validator: value => ['create', 'edit'].includes(value)
        },
        submitting: {
            type: Boolean,
            default: false
        }
    },
    emits: ['update:modelValue', 'submit', 'delete'],
    data() {
        return {
            uploadUrl: UPLOAD_MEDIA_URL,
            form: new AppInfo(this.config.type),
            // source 为 local 的开关（例如"仅网页应用"）不是资源的字段，
            // 它只影响最终提交的 type
            localFlags: {},
            secretRevealed: false,
            copiedField: '',
            copiedTimer: null
        };
    },
    computed: {
        isEdit() {
            return this.mode === 'edit';
        },
        dialogWidth() {
            return 'min(560px, calc(100vw - 32px))';
        },
        secretDisplay() {
            if (!this.form.secret) {
                return '—';
            }
            return this.secretRevealed
                ? this.form.secret
                : '•'.repeat(Math.min(this.form.secret.length, 32));
        },
        rules() {
            const rules = {};
            this.config.fields.forEach(field => {
                const list = [];
                // requiredUnlessFlag：例如纯网页应用不需要回调地址
                const waived = field.requiredUnlessFlag && this.flagValueByKey(field.requiredUnlessFlag);
                if (field.required && !waived) {
                    list.push({
                        required: true,
                        message: field.requiredMessage || `请输入${field.label}`,
                        trigger: 'blur'
                    });
                }
                if (field.max) {
                    list.push({
                        min: field.min || 1,
                        max: field.max,
                        message: `长度需要在 ${field.min || 1} 到 ${field.max} 个字符之间`,
                        trigger: 'blur'
                    });
                }
                rules[field.prop] = list;
            });
            return rules;
        }
    },
    watch: {
        modelValue(open) {
            if (open) {
                this.reset();
            }
        }
    },
    methods: {
        reset() {
            this.form = Object.assign(new AppInfo(this.config.type), this.source);
            this.secretRevealed = false;
            const flags = {};
            this.config.flags.forEach(flag => {
                if (flag.source === 'local') {
                    // 从 type 反推本地开关：例如 type === 3 表示"仅网页应用"
                    flags[flag.key] = this.form.type === flag.typeWhenOn;
                }
            });
            this.localFlags = flags;
            // 等表单渲染完再清校验状态，避免复用弹窗时残留上一次的红字
            this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate());
        },
        flagValue(flag) {
            return flag.source === 'local'
                ? !!this.localFlags[flag.key]
                : !!this.form[flag.key];
        },
        flagValueByKey(key) {
            const flag = this.config.flags.find(item => item.key === key);
            return flag ? this.flagValue(flag) : false;
        },
        setFlag(flag, value) {
            if (flag.source === 'local') {
                this.localFlags = { ...this.localFlags, [flag.key]: value };
                // 本地开关会改变资源的 type（例如普通应用 0 <-> 纯网页应用 3）
                this.form.type = value ? flag.typeWhenOn : flag.typeWhenOff;
            } else {
                this.form[flag.key] = value;
            }
        },
        placeholderFor(field) {
            if (this.isEdit && typeof field.editPlaceholder === 'function') {
                return field.editPlaceholder(this.form);
            }
            return field.placeholder;
        },
        beforeUpload(file) {
            return validatePortrait(file, message => this.$message.error(message));
        },
        onUploaded(response) {
            const url = readUploadedUrl(response);
            if (!url) {
                this.$message.error('图标上传失败，请检查服务端 OSS 配置');
                return;
            }
            this.form.portraitUrl = url;
            // 地址填上了，把这一项的校验红字清掉
            this.$refs.form && this.$refs.form.clearValidate('portraitUrl');
        },
        submit() {
            this.$refs.form.validate(valid => {
                if (!valid) {
                    return;
                }
                // 没有本地开关的资源（频道、机器人）用配置里的固定 type
                if (!this.config.flags.some(flag => flag.source === 'local')) {
                    this.form.type = this.config.type;
                }
                this.$emit('submit', this.form);
            });
        },
        confirmDelete() {
            ElMessageBox.confirm(
                `删除后「${this.form.name}」的 ${this.config.idLabel} 和 secret 将立即失效，且无法恢复。`,
                `删除${this.config.title}`,
                {
                    type: 'warning',
                    confirmButtonText: '删除',
                    cancelButtonText: '取消',
                    confirmButtonClass: 'el-button--danger'
                }
            )
                .then(() => this.$emit('delete', this.form))
                .catch(() => { /* 用户取消，不做任何事 */ });
        },
        async copy(value, field) {
            const ok = await copyText(value);
            if (!ok) {
                this.$message.error('复制失败，请手动选中复制');
                return;
            }
            this.copiedField = field;
            clearTimeout(this.copiedTimer);
            this.copiedTimer = setTimeout(() => {
                this.copiedField = '';
            }, 1600);
        },
        onClosed() {
            this.copiedField = '';
            this.secretRevealed = false;
        }
    },
    beforeUnmount() {
        clearTimeout(this.copiedTimer);
    }
};
</script>

<style scoped>
.credentials {
    display: flex;
    flex-direction: column;
    gap: var(--wf-space-2);
    margin-bottom: var(--wf-space-5);
    padding: var(--wf-space-3) var(--wf-space-4);
    background: var(--wf-fill);
    border: 1px solid var(--wf-border-light);
    border-radius: var(--wf-radius-lg);
}

.credential {
    display: flex;
    align-items: center;
    gap: var(--wf-space-2);
    min-width: 0;
}

.credential-label {
    flex: none;
    width: 72px;
    font-size: var(--wf-text-xs);
    color: var(--wf-text-4);
}

.credential-value {
    flex: 1;
    min-width: 0;
    color: var(--wf-text-1);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    user-select: all;
}

.ghost {
    flex: none;
    padding: 2px 8px;
    font: inherit;
    font-size: var(--wf-text-xs);
    color: var(--wf-brand);
    background: transparent;
    border: 1px solid var(--wf-border);
    border-radius: var(--wf-radius);
    cursor: pointer;
}

.ghost:hover {
    background: var(--wf-brand-bg);
}

.portrait-field {
    display: flex;
    align-items: flex-start;
    gap: var(--wf-space-3);
    width: 100%;
}

.portrait-preview {
    flex: none;
    width: 56px;
    height: 56px;
    border-radius: var(--wf-radius);
    object-fit: cover;
    background: var(--wf-fill);
    box-shadow: inset 0 0 0 1px rgba(29, 33, 41, 0.08);
}

.portrait-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: var(--wf-text-xs);
    color: var(--wf-text-4);
}

.portrait-controls {
    flex: 1;
    display: flex;
    gap: var(--wf-space-2);
    min-width: 0;
}

.field-hint {
    margin-top: var(--wf-space-1);
    font-size: var(--wf-text-xs);
    line-height: 1.5;
    color: var(--wf-text-3);
}

.flags {
    display: flex;
    flex-direction: column;
    gap: var(--wf-space-3);
    padding-top: var(--wf-space-2);
}

.flag {
    display: flex;
    align-items: flex-start;
    gap: var(--wf-space-2);
    cursor: pointer;
}

.flag-text {
    display: flex;
    flex-direction: column;
    line-height: 1.45;
}

.flag-label {
    font-size: var(--wf-text-base);
    color: var(--wf-text-1);
}

.flag-description {
    font-size: var(--wf-text-xs);
    color: var(--wf-text-3);
}

.footer {
    display: flex;
    align-items: center;
    gap: var(--wf-space-2);
}

.footer-gap {
    flex: 1;
}

/* el-checkbox 默认自带 label 的行高，这里我们自己排版，去掉它的内边距 */
:deep(.el-checkbox) {
    height: auto;
    margin-top: 2px;
}

@media (max-width: 560px) {
    .portrait-controls {
        flex-direction: column;
    }
}
</style>
