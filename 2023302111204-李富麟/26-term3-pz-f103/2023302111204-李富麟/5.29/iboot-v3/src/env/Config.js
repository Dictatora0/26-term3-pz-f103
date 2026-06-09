/**
 * 高德地图配置
 * @param {AMap}
 * @constructor
 */
export function AMap({key, center, city, security}) {
    this.key = key; // 高德api key
    this.city = city;
    this.center = center; // 中心点
    this.security = security; // 高德api密钥
}

/**
 * http配置
 * @param {Http}
 * @constructor
 */
export function Http({httpUrl, websocketUrl, apiUrl}) {
    this.httpUrl = httpUrl;
    this.apiUrl = apiUrl || httpUrl;
    this.websocketUrl = websocketUrl;
    this.isDev = import.meta.env.DEV;
    this.isProd = !this.isDev;

    this.getApiUrl = function () {
        return this.apiUrl;
    }

    this.getBaseURI = function () {
        return this.httpUrl;
    }

    this.getFullURL = function (url) {
        return this.getBaseURI() + url || "";
    }

    this.getWsBaseURI = function () {
        return this.websocketUrl;
    }

    this.getFullWsURL = function (url) {
        return this.getWsBaseURI() + url || "";
    }
}

/**
 * @param config {EnvConfig}
 * @constructor
 */
export function EnvConfig(config) {

    /**
     * 是否开发环境
     * @type {boolean}
     */
    this.isDev = import.meta.env.DEV

    /**
     * 高德地图配置信息
     * @type {AMap}
     */
    this.amap = config.amap;

    /**
     * http接口配置
     * @type {Http}
     */
    this.http = config.http;

    /**
     * rtvs流媒体配置
     * @type {{port: number, host: null, type: string}}
     */
    this.rtvs = config.rtvs;
}