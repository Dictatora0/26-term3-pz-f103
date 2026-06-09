import {AMap, EnvConfig, Http} from "./Config";

const wsOrigin = window.location.protocol === 'https:'
    ? `wss://${window.location.host}`
    : `ws://${window.location.host}`;

export default new EnvConfig({
    amap: new AMap({
        city: '鍏ㄥ浗',
        center: [117.964498, 24.510099],
        key: '08d611ab9267262e3dabcc18897cc80d',
        security: '34dc2035e2dd6f3980f98179919224a5'
    }),

    http: new Http({
        httpUrl: "/api",
        websocketUrl: wsOrigin
    }),

    rtvs: {
        port: 17000,
        type: 'rtvs',
        host: 'iot.iteaj.com'
    }
});
