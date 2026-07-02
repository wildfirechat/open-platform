import {initUniappBridge} from "./bridgeClientImpl.uni";
import { initWeb } from './bridgeClientImpl.web';

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
    // for pc
    // preload
    console.log('init bridge-');
    console.log('userAgent', navigator.userAgent);
    if((process && process.versions && process.versions.electron) || navigator.userAgent.toLowerCase().indexOf('electron') >= 0){
        console.log('js bridge, electron')
        // electron pc 端，注入了 window.__wf_bridge_
        return window.__wf_bridge_;
    }

    // 最终，原生移动端，都应当走到这儿
    if(navigator.userAgent.indexOf('WF-DSBridge') >= 0) {
        console.log('js bridge, dsbridge')
        return require('dsbridge');
    }

    // uniapp
    if (navigator.userAgent.indexOf('uni-app') >= 0) {
        console.log('js bridge, uni-app')
        if (!window.__wf_bridge_) {
            initUniappBridge();
        }
        return window.__wf_bridge_;
    }

    // for web
    // 这儿的判断不准确，但先保持了
    // 手机、pad 浏览器时，应当判断为 web
    // 手机、pad 应用里面的 webview，应当判断为 dsbridge
    // 最终，非 electron，UA 不包含 WF-DSBridge 都应当判断为 web
    if (navigator.userAgentData) {
        console.log('userAgentData', JSON.stringify(navigator.userAgentData));
        const isMobile = navigator.userAgentData.mobile || navigator.userAgent.indexOf('Phone') > -1;
        if(!isMobile) {
            if (!window.__wf_bridge_) {
                initWeb();
            }
            console.log('js bridge, web')
            return window.__wf_bridge_;
        }
    }

    // uniapp
    if (navigator.userAgent.indexOf('uni-app') >= 0) {
        console.log('js bridge, uni-app')
        if (!window.__wf_bridge_) {
            initUniappBridge();
        }
        return window.__wf_bridge_;
    } else {
        console.log('js bridge, dsbridge')
        return require('dsbridge');
    }
}

