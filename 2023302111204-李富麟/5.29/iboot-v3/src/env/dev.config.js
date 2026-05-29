import {EnvConfig, AMap, Http} from "./Config";

export default new EnvConfig({
    amap: new AMap({
        city: '全国',
        center: [117.964498, 24.510099],
        key: '08d611ab9267262e3dabcc18897cc80d',
        security: '34dc2035e2dd6f3980f98179919224a5'
    }),

    http: new Http({
        httpUrl: "/api",
        apiUrl: "http://localhost:8085",
        websocketUrl: 'ws://127.0.0.1:8087'
    }),

    rtvs: {
        port: 17000,
        type: 'rtvs',
        host: '124.222.187.121'
    }
});