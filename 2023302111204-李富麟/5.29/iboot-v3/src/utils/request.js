import Qs from 'qs';
import axios from 'axios'
import router from '@/router'
import CoreConsts from "@/components/CoreConsts";
import env from "@/env";

let baseConfig = {
    // 成功响应码
    success: 200,
    //消息框消失时间
    duration: 3000,
    //超时时间
    timeout: 20000,
}

/**
 * @type Http
 */
let urlConfig = env.http;
let baseURL = urlConfig.getBaseURI();

/**
 * @description 处理code异常
 * @param {*} data
 * @param {*} config
 */
const handleResponse = (data) => {
    let {code, message} = data
    switch (code) {
        case 401: // 未授权
            router.push({ path: '/login' }).finally(() => {});
            return Promise.reject('');
        case 404:
            return Promise.reject(data);
        default: return data;
    }
}


const instance = axios.create({
    baseURL: baseURL,
    timeout: baseConfig.timeout,
    headers: {
        'x-requested-with': 'XMLHttpRequest', // 声明是ajax请求
        'Content-Type': 'application/json; charset=UTF-8',
    },
    paramsSerializer: (params) => {
        return Qs.stringify(params, {arrayFormat: 'indices', allowDots: true})
    }
})

instance.interceptors.request.use(
    (config) => {
        // 如果Storage存在token值, 则将token放到请求头里面
        let item = localStorage.getItem(CoreConsts.AccessToken);
        if(item) {
            config.headers[CoreConsts.AccessToken] = item
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

instance.interceptors.response.use(
    (response) => {
        const {data, config, headers} = response
        if(config[CoreConsts.CancelRespResolver]) {
            return response;
        }

        return handleResponse(data);
    },
    (error) => {
        const { response, message } = error

        if (response && response.data) {
            const { status, data, config } = response
            return handleResponse({code: status, message})
        } else {
            let { message } = error
            if(message === 'Network Error') {
                message = '网络连接异常'
            } else if(message.includes('timeout')) {
                message = '接口请求超时'
            } else if(message.includes('Request failed with status code')) {
                const code = message.substr(message.length - 3)
                message = `后端接口[${code}]异常`
            }

            console.error(message || `后端接口异常`)
            return Promise.reject(error)
        }
    }
)

let GET = (url, data, config) => {
    if(config) {
        config['params'] = data;
    } else {
        config = {params: data}
    }

    return instance.get(url, config);
}
let PUT = instance.put;
let POST = instance.post;
let PATCH = instance.patch;
let DELETE = instance.delete;
export {GET, PUT, POST, PATCH, DELETE, baseURL, instance as http, urlConfig, env}
