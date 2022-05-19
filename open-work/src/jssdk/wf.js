import bridge from 'dsbridge'
import {Biz} from "./biz";

export class Wf {
    biz = new Biz();

    openUrl(url) {
        bridge.call('openUrl', url);
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