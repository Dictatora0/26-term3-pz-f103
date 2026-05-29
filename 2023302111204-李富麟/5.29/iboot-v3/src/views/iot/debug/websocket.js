import {msgWarn} from "@/utils/message";
import {urlConfig} from "@/utils/request";

/**
 * @param type {String} 调试类型
 * @param onmessage {Function} 调试消息回调函数
 * @constructor
 */
export function DebugWebsocket(type, onmessage) {

    let clientSn = "SN:" + new Date().getTime().toString(36).toUpperCase();
    let websocket = new WebSocket(urlConfig.getFullWsURL(`/ws/iot/debug?clientSn=${clientSn}&type=${type}`));

    websocket.onmessage =  (ev) => {
        let data = JSON.parse(ev.data);
        if(onmessage instanceof Function) {
            if(data.code == 500) {
                msgWarn(data.message).then();
            } else {
                onmessage(data);
            }
        } else {
            console.log(`接收到消息：${ev.data}`)
        }
    }

    /**
     * dtu调试
     * @param msg
     */
    this.debug = function (model) {
        let message = {clientSn, model}
        websocket.send(JSON.stringify(message));
    }

    this.getWebSocket = function () {
        return websocket;
    }
}

/**
 * @param type 可选值 jvm | server | system
 * @param onmessage
 * @constructor
 */
export function HealthWebsocket(type, onmessage) {

    this.isSuspend = false;
    this.intervalValue = null;
    this.websocket = new WebSocket(urlConfig.getFullWsURL(`/ws/endpoint/health`));

    this.websocket.onopen = () => {
        // 先发送一次
        this.websocket.send(JSON.stringify({type: type}))
    }

    this.websocket.onmessage = function (ev) {
        let resp = JSON.parse(ev.data);
        if(resp['code'] == 200) {
            onmessage(resp['data']);
        } else {
            console.error(resp['msg']);
        }
    }

    /**
     * 暂停
     */
    this.suspend = function () {
        this.isSuspend = true;
    }

    /**
     * 启用
     */
    this.start = function () {
        this.isSuspend = false;
    }

    /**
     * 停止定时任务
     */
    this.stop = function () {
        clearInterval(this.intervalValue);
    }

    /**
     * 关闭websocket和定时任务
     */
    this.close = function () {
        this.stop();
        this.websocket.close(); // 正常关闭socket
    }

    /**
     * 定时获取
     * @param rate 频率(毫秒)
     */
    this.interval = function (rate) {
        this.intervalValue = setInterval(() => {
            if(!this.isSuspend) {
                this.websocket.send(JSON.stringify({type: type}))
            }
        }, rate);
    }
}
