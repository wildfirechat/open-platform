/*
 * 应用 / 频道 / 机器人三个页面的差异配置。
 *
 * 这三个页面原本是三份各自独立、彼此有 90% 重复的组件。现在它们共用
 * ResourcePage.vue，页面之间的差异全部收敛到这个文件里：新增一种资源类型
 * 只需要在这里加一项配置，不需要再复制一份 240 行的页面。
 */

const DOCS_URL = 'https://docs.wildfirechat.cn/open';

// 图标字段三种资源完全一致，抽出来避免重复
function portraitField(label) {
    return {
        prop: 'portraitUrl',
        label,
        kind: 'portrait',
        placeholder: 'https://…/icon.png',
        required: true,
        requiredMessage: '请上传或填写图标地址'
    };
}

function nameField(label, placeholder) {
    return {
        prop: 'name',
        label,
        placeholder,
        required: true,
        max: 10,
        requiredMessage: `请输入${label}`
    };
}

function descriptionField(label) {
    return {
        prop: 'description',
        label,
        placeholder: '一句话说明它是做什么的',
        required: true,
        max: 20,
        requiredMessage: `请输入${label}`
    };
}

export const RESOURCE_TYPES = {
    app: {
        key: 'app',
        // 0 = 普通应用；3 = 纯网页应用（不创建频道和机器人）
        type: 0,
        storeKey: 'apps',
        title: '应用',
        // 列表页标题下方的说明，同时也是"这个东西是什么"的解释
        summary: '应用会出现在客户端工作台，用户点开即可使用。',
        createLabel: '创建应用',
        editLabel: '编辑应用',
        emptyTitle: '还没有应用',
        emptyHint: '创建第一个应用，它会出现在客户端工作台的入口列表里。',
        docsUrl: DOCS_URL,
        idLabel: 'targetId',
        accent: 'var(--wf-type-app)',
        fields: [
            portraitField('应用图标'),
            nameField('应用名称', '例如：日报'),
            descriptionField('应用描述'),
            {
                prop: 'mobileUrl',
                label: '移动端地址',
                placeholder: 'https://example.com/mobile',
                required: true,
                hint: 'Host 需要与回调地址的 Host 保持一致。'
            },
            {
                prop: 'desktopUrl',
                label: '桌面端地址',
                placeholder: 'https://example.com/desktop',
                required: true,
                hint: 'Host 需要与回调地址的 Host 保持一致。'
            },
            {
                prop: 'serverUrl',
                label: '回调 / 服务端地址',
                placeholder: 'https://example.com',
                required: true,
                // 纯网页应用没有服务端，这一项就不再必填
                requiredUnlessFlag: 'webOnly',
                hint: '不配置的话，需要认证的工作台应用将无法登录。'
            }
        ],
        flags: [
            {
                key: 'global',
                source: 'model',
                label: '全局应用',
                description: '所有人都能在工作台看到，无需自行添加。'
            },
            {
                key: 'webOnly',
                source: 'local',
                label: '仅网页应用',
                description: '不创建对应的频道和机器人，也不需要回调地址。',
                typeWhenOn: 3,
                typeWhenOff: 0
            }
        ]
    },

    channel: {
        key: 'channel',
        type: 1,
        storeKey: 'channels',
        title: '频道',
        summary: '类似公众号：由服务端主动向订阅用户推送消息。',
        createLabel: '创建频道',
        editLabel: '编辑频道',
        emptyTitle: '还没有频道',
        emptyHint: '创建频道后，就可以通过频道服务向用户推送消息。',
        docsUrl: DOCS_URL,
        idLabel: 'channelId',
        accent: 'var(--wf-type-channel)',
        fields: [
            portraitField('频道图标'),
            nameField('频道名称', '例如：系统公告'),
            descriptionField('频道描述'),
            {
                prop: 'serverUrl',
                label: '频道服务端地址',
                placeholder: 'http://{频道服务地址}',
                required: false,
                hint: '野火官方频道服务的回调地址需要带上 channelId。',
                // 编辑时才知道 channelId，这时给出可直接照抄的完整示例
                editPlaceholder: model => `http://{频道服务地址}/${model.targetId || '{channelId}'}`,
                link: {
                    text: '野火频道服务',
                    url: 'https://github.com/wildfirechat/channel-platform'
                }
            }
        ],
        flags: [
            {
                key: 'global',
                source: 'model',
                label: '广播号',
                description: '发消息时推送给所有人；关闭则只推送给已订阅的用户。'
            }
        ]
    },

    robot: {
        key: 'robot',
        type: 2,
        storeKey: 'robots',
        title: '机器人',
        summary: '通过机器人账号收发消息，适合告警、审批一类的自动化场景。',
        createLabel: '创建机器人',
        editLabel: '编辑机器人',
        emptyTitle: '还没有机器人',
        emptyHint: '创建机器人后，用机器人 SDK 即可收发消息。',
        docsUrl: DOCS_URL,
        idLabel: 'targetId',
        accent: 'var(--wf-type-robot)',
        fields: [
            portraitField('机器人图标'),
            nameField('机器人名称', '例如：构建通知'),
            descriptionField('机器人描述'),
            {
                prop: 'serverUrl',
                label: '回调 / 服务端地址',
                placeholder: 'https://example.com',
                required: false,
                hint: '机器人收到消息后，野火会把消息回调到这个地址。'
            }
        ],
        flags: []
    }
};

export { DOCS_URL };
