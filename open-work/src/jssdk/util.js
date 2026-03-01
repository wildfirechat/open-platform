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
    if(process && process.versions && process.versions.electron){
        console.log('js bridge, electron')
        return  window.__wf_bridge_;
    }

    // for web
    if (navigator.userAgentData) {
        const isMobile = navigator.userAgentData.mobile;
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

