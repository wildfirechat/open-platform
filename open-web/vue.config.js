const path = require('path')
const webpack = require('webpack')

module.exports = {
    configureWebpack: {
        resolve: {
            alias: {
                // 语言包是运行时从 json 加载的普通字符串，需要带消息编译器的完整版 vue-i18n
                'vue-i18n': path.resolve(__dirname, 'node_modules/vue-i18n/dist/vue-i18n.esm-bundler.js'),
            }
        },
        plugins: [
            // vue-i18n 的特性开关，不定义的话打包时会有警告
            new webpack.DefinePlugin({
                __VUE_I18N_FULL_INSTALL__: JSON.stringify(true),
                __VUE_I18N_LEGACY_API__: JSON.stringify(true),
                __INTLIFY_PROD_DEVTOOLS__: JSON.stringify(false),
            })
        ]
    }
}
