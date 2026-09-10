const HtmlWebpackPlugin = require('html-webpack-plugin')
const HtmlInlineScriptPlugin = require('html-inline-script-webpack-plugin')

module.exports = {
    css: {
        extract: false,
    },
    configureWebpack: {
        optimization: {
            splitChunks: false // makes there only be 1 js file - leftover from earlier attempts but doesn't hurt
        },
        plugins: [
            new HtmlWebpackPlugin({
                filename: 'output.html', // the output file name that will be created
                template: 'src/output-template.html', // this is important - a template file to use for insertion
                // 脚本是内联进 html 的，内联 script 上的 defer 会被浏览器忽略，
                // 所以必须放到 body 末尾，否则会在 <div id="app"> 之前执行，挂载不上。
                inject: 'body',
                scriptLoading: 'blocking',
            }),
            // 把 js 内联进 output.html，css 已经通过 css.extract = false 内联进 js 里了。
            // 只处理 output.html，index.html 仍然按正常方式引用外部 js。
            new HtmlInlineScriptPlugin({
                htmlMatchPattern: [/output\.html$/],
                assetPreservePattern: [/.*/],
            })
        ]
    }
}
