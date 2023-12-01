import {_handleNativeCall, bridge} from "@/jssdk/util";


export default class Navigation {
    close(successCB, failCB) {

        // call 方法的声明：  call (handlerName: string, args?: any, responseCallback?: (retValue: any) => void): any;
        bridge().call('close', {}, _handleNativeCall(successCB, failCB));
    }
}