export default class AppInfo {
    constructor(type = 0) {
        this.type = type;
    }

    targetId = '';
    secret = '';
    name = '';
    description = '';
    portraitUrl = '';
    // 应用时有效
    mobileUrl = '';
    // 应用时有效
    desktopUrl = '';
    serverUrl = '';
    // 应用时，表示是否是全局应用；频道是，表示是否是广播号
    global = false;
    type = 0; // 0， 创建应用；1，创建频道；2，创建机器人
    updateDt = 0;
    createDt = 0;
}