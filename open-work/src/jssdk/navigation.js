import {_handleNativeCall} from "@/jssdk/util";

const bridge = window.__wf_bridge_ ? window.__wf_bridge_ : require('dsbridge');

export default class Navigation {
    close(successCB, failCB) {
        // call 方法的声明：  call (handlerName: string, args?: any, responseCallback?: (retValue: any) => void): any;
        bridge.call('close', {}, _handleNativeCall(successCB, failCB));
    }
}