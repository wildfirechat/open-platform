# 开放平台管理页面
此项目为开放平台管理页面，系统管理员可以登陆管理应用。

## 环境
1. node >= 16（推荐 v18，开发验证版本 v18.19.0）
2. npm >= 8（开发验证版本 v10.2.3）

## 主要依赖
| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| vue | ^3.5.13 | Vue 3 |
| vue-router | ^4.5.0 | 配合 Vue 3，hash 模式 |
| vuex | ^4.1.0 | 配合 Vue 3 |
| vue-i18n | ^9.14.2 | 配合 Vue 3，使用 legacy 模式，保留 `this.$t` 用法 |
| element-plus | ^2.9.1 | 替换原来的 element-ui（element-ui 只支持 Vue 2） |
| @element-plus/icons-vue | ^2.3.1 | Element Plus 的图标组件，替换原来的 `el-icon-xxx` 字体图标 |
| axios | ^0.27.2 | |
| @vue/cli-service | ~5.0.8 | 基于 webpack 5 |

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
* 语言包是运行时从 json 加载的普通字符串，需要带消息编译器的完整版 vue-i18n，已在 `vue.config.js` 里通过 alias 指定。
