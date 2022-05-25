import Biz from "./biz";
import Navigation from "@/jssdk/navigation";

const bridge = window.__wf_bridge_ ? window.__wf_bridge_ : require('dsbridge');

export class Wf {
    biz = new Biz();
    navigation = new Navigation();

    openUrl(url, options) {
        if (window.__wf_bridge_) {
            // pc
            bridge.call('openUrl', {url, ...options});
        } else {
            // mobile
            bridge.call('openUrl', url);
        }
    }

    ready(callback) {
        bridge.register('ready', () => {
            callback && callback();
        })
    }

    error(callback) {
        bridge.register('error', (reason) => {
            callback && callback(reason);
        })
    }

    config(obj) {
        bridge.call('config', obj)
    }

    /**
     * for debug
     * @param text
     */
    toast(text) {
        bridge.call('toast', text)
    }
}

const self = new Wf();
export default self;