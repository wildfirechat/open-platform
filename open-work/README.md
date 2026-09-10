# 客户端工作页面
此项目为客户端工作页面，运行在手机上的某个Tab页的浏览器中。展示给用户全局应用列表，另外用户可以收藏/取消收藏普通应用。

## 环境
1. node >= 16（推荐 v18，开发验证版本 v18.19.0）
2. npm >= 8（开发验证版本 v10.2.3）

## 主要依赖
| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| vue | ^3.5.13 | Vue 3 |
| dsbridge | ^3.1.4 | 原生移动端 JS Bridge |
| axios | ^0.27.2 | |
| @vue/cli-service | ~5.0.8 | 基于 webpack 5 |
| html-webpack-plugin | ^5.6.3 | |
| html-inline-script-webpack-plugin | ^3.2.1 | 把 js 内联进 output.html，替换原来只支持 webpack 4 的 html-webpack-inline-source-plugin |

## 编译
```
npm install
npm run build
```

## 本地调试
```
npm run serve
```

## 说明
* 页面最终会被打包成单个 `dist/output.html`（js、css 全部内联），再拷贝成 open-server 的 `work.html`。
* 内联的 script 必须放在 body 末尾：内联 script 上的 `defer` 会被浏览器忽略，放在 head 里会在 `<div id="app">` 之前执行导致挂载失败，所以 `vue.config.js` 里指定了 `inject: 'body'`。
* webpack 5 不再自动 mock node 的 `process`，代码里判断 electron 环境时需要先做 `typeof process !== 'undefined'` 判断。
