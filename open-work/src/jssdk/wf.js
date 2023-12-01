import Biz from "./biz";
import Navigation from "@/jssdk/navigation";
import {bridge} from "./util";


export class Wf {
    biz = new Biz();
    navigation = new Navigation();

    openUrl(url, options) {
        if (navigator.userAgent.indexOf('uni-app') >= 0){
            // uniapp
            bridge().call('openUrl', url);
        } else if (window.__wf_bridge_) {
            // pc or uni
            bridge().call('openUrl', {url, ...options});
        } else {
            // mobile
            bridge().call('openUrl', url);
        }
    }

    ready(callback) {
        bridge().register('ready', () => {
            callback && callback();
        })
    }

    error(callback) {
        bridge().register('error', (reason) => {
            callback && callback(reason);
        })
    }

    config(obj) {
        bridge().call('config', obj)
    }

    /**
     * for debug
     * @param text
     */
    toast(text) {
        bridge().call('toast', text)
    }
}

const self = new Wf();
export default self;