import {initUniappBridge} from "./bridgeClientImpl.uni";

export function _handleNativeCall(successCB, failCB) {
    return (result) => {
        console.log('native callback result', result);
        if (result.code === 0) {
            if (typeof result.data === 'string') {
                try {
                    let obj = JSON.parse(result.data);
                    successCB && successCB(obj);
                } catch (e) {
                    successCB && successCB(result.data);
                }
            } else {
                successCB && successCB(result.data);
            }
        } else {
            failCB && failCB(result.code);
        }
    }
}

export function bridge() {
    if (navigator.userAgent.indexOf('uni-app') >= 0) {
        if (!window.__wf_bridge_) {
            initUniappBridge();
        }
        return window.__wf_bridge_;
    } else {
        return window.__wf_bridge_ ? window.__wf_bridge_ : require('dsbridge');
    }
}

