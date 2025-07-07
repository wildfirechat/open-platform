# 野火开放平台
## 系统组成
野火开放平台系统包括3个部分，分别是IM系统、开放平台和应用。IM系统只是即时通讯的功能，在企业应用中需要对接大量的第三方办公类服务，比如考勤、打卡、文档、会议等，这些服务就是开放平台系统中的应用。需要一个服务来管理他们，方便他们的对接IM服务，这个服务就是开放平台。

开放平台包括3部分，[open-server](./open-server)为后端服务；[open-web](./open-web)为平台的管理平台的管理页面；[open-work](./open-work)为客户端工作平台展示的页面。

在管理后台可以创建和管理应用。每个应用都拥有一个机器人账户和一个频道，使用野火机器人SDK、频道SDK和JS SDK就可以实现应用与IM服务的交互。

客户端的工作平台页面可以展示全局应用，可以收藏普通应用，可以点击应用来打开应用。还有一种是后台应用，在前台不展示应用，但应用还是可以使用频道和机器人接口来跟IM服务交互。

开放平台与应用是一对多的关系，理论上每个应用都要有个独立的服务，我们也提供了一个demo服务野火日报服务，可以直接基于demo开发新的应用，或者参考对接方式来对接现有应用。

## 系统架构图
![架构图](http://static.wildfirechat.cn/open_arch.png)
图上红色部分为开放平台；绿色部分为应用，可能会有很多个应用，图上以应用1来举例；蓝色部分为IM系统。
### 开放平台
开放平台有后端应用和work页面组成，work页面运行在野火内置的浏览器中，内置浏览器支持JS Bridge，可以通过JS SDK来与IM SDK进行交互。
* 接口1 Work页面调用开放平台接口来登录、获取全局应用列表和用户自己的收藏应用列表、及处理收藏/取消收藏等操作。
* 接口2 Work页面通过JS SDK调用IM SDK来获取AuthCode，AuthCode用来登录开放平台使用。
* 接口3 Work页面登录时带上AuthCode，开放平台后端收到后调用野火IM进行验证AuthCode，确认前端用户身份。

### IM系统
应用页面和Work页面都会通过JS SDK来调用IM服务，最基础的调用就是获取AuthCode和config操作。

AuthCode是页面用来登录的令牌，应用页面和Work页面获取到AuthCode后去后台登录，后台再去IM服务验证用户身份，通过之后就确认了用户身份，从而完成了登录。

config是IM验证页面合法性的方法。页面去后台获取config签名，后台计算出config签名返回给页面，页面调用JS SDK的config方法，IM SDK收到签名后去IM服务验证，验证通过后，JS SDK就可以调用其它方法了。

### 应用
每个应用都是独立服务，互不影响。以App1举例，前端页面通过JS SDK与客户端的IM SDK进行交互，后端通过频道API或者机器人API来与IM服务进行交互。

### 流程图
工作页面的流程图

![流程图](http://static.wildfirechat.cn/open_flow.png)

## 编译
项目是前后端分离的，但为了方便部署，默认打包是放到一起的。编译时请先编译[open-work](./open-work)，然后编译[open-web](./open-web)，最后编译[open-server](./open-server)。默认前端页面编译后会拷贝到后端的resource目录，这样就可以前后端放到一起部署，也可以自行修改为前后端分离的方式部署。

## 启动
请参考[open-server](./open-server)说明。

## 截图

应用列表
![应用列表](http://static.wildfirechat.cn/open_admin_application_list.png)

创建应用
![创建应用](http://static.wildfirechat.cn/open_admin_create_application.png)

修改应用
![修改应用](http://static.wildfirechat.cn/open_admin_modify_application.png)

手机截图

<img src="http://static.wildfirechat.cn/open_admin_client.png" width = 50% height = 50% />
