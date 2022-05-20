import {_handleNativeCall} from "@/jssdk/util";

const bridge =  window.__wf_bridge_ ? window.__wf_bridge_ : require('dsbridge');
export class Biz {

    getAuthCode(appId, type, successCB, failCB) {
        bridge.call('getAuthCode', {
                appId,
                appType: type,
            }, _handleNativeCall(successCB, failCB)
        )
    }

    chooseContacts(options, successCB, failCB) {
        options = options ? options : {};
        options.max = 3;
        bridge.call('chooseContacts', options, _handleNativeCall(successCB, failCB));
    }
}
