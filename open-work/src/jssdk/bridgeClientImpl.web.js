/**
 * 开放平台 JS-SDK Client (Web 版本)
 *
 * 运行在 iframe 中，通过 postMessage 与父窗口通信
 * 协议格式与 Electron WebSocket 版本保持一致
 */

let callbackMap = new Map();
let eventListeners = {};
let requestId = 0;
let initialized = false;

const windowId = 'web-tab';

export function initWeb() {
    if (initialized) {
        return;
    }

    window.addEventListener('message', (event) => {
        // 只接受来自父窗口的消息
        if (event.source !== window.parent) {
            return;
        }

        const obj = event.data;
        if (!obj || !obj.type) {
            return;
        }

        // 过滤：检查 windowId 和 appUrl
        if (!obj.windowId.startsWith(windowId) || obj.appUrl !== location.href) {
            console.log('ignore wf-op data', obj);
            return;
        }

        if (obj.type === 'wf-op-event') {
            handleOpEvent(obj.handlerName, obj.args);
        } else if (obj.type === 'wf-op-response') {
            handleOpResponse(obj.requestId, obj.args);
        }
    });

    initialized = true;
    console.log('bridgeClientImpl init');
}

async function call(handlerName, args, callback) {
    if (!initialized) {
        initWeb();
    }

    let reqId = 0;
    if (callback && typeof callback === 'function') {
        reqId = ++requestId;
        callbackMap.set(reqId, callback);
    }

    let appUrl = location.href;
    let obj = { type: 'wf-op-request', requestId: reqId, windowId, appUrl, handlerName, args };
    console.log('wf-op-request', obj);

    window.parent.postMessage(obj, '*');
}

function handleOpResponse(requestId, args) {
    console.log('handle op response', requestId, args);
    let cb = callbackMap.get(requestId);
    if (cb) {
        cb(args);
        callbackMap.delete(requestId);
    }
}

function handleOpEvent(handlerName, args) {
    eventListeners[handlerName] && eventListeners[handlerName](args);
}

function register(handlerName, callback) {
    eventListeners[handlerName] = callback;
}

window.__wf_bridge_ = {
    call: call,
    register: register,
};

console.log('export call and register');
