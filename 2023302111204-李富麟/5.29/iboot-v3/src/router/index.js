import store from "@/store";
import {computed, watch} from 'vue'
import Main from '@msn/main/index.vue'
import Login from '@msn/login/login.vue'
import NotFound from '@msn/error/404.vue'
import Refresh from "@msn/main/Refresh.vue";
import Index from '@msn/core/index/index.vue'
import {createRouter, createWebHashHistory} from 'vue-router'

// 主页路由名称            // 首页路径
const MainName = "Main";

const router = createRouter({
    routes: [
        {path: '/', component: Main, name: MainName,
            children: [
                {path: '', component: Index, name: '工作台', meta: {closable: false, keepAlive: 'Index'}},
                {path: 'refresh', name: 'refresh', component: Refresh}, // 页面刷新功能中做路由中转
                {path: 'profile', component: () => import("@msn/core/admin/UserProfile.vue"), name: '个人资料', meta: {closable: true}},
                {path: 'search/result/:type', component: () => import("@msn/main/search/index.vue"), name: '搜索结果', meta: {closable: true}},
                {path: 'core/dict/data', component: () => import("@msn/core/dict/data/index.vue"), name: '字典数据', meta: {keepAlive: 'DictData', closable: true}},
                {path:'/:chapters+', name: '404', component: NotFound, beforeEnter(to, from, next) {
                        let path = to.path;
                        let init = store.getters["sys/init"];
                        // 用来处理整个页面刷新时, 动态菜单还没有生成路由报404的问题
                        if(!init) { // 判断当前系统是否初始化, 如果没有初始化则监听初始化状态
                            // 先移除任务, 用来把首页放在任务栏第一个位置
                            store.commit('sys/removeTask', path);
                            init = computed(() => store.getters["sys/init"]);
                            watch(init, (val) => {
                                // 系统初始化完成之后, 跳到刷新时所在的页面
                                let urlMenuMaps = store.getters['sys/urlMenuMaps'];
                                let menu = urlMenuMaps[path];
                                if(menu) { // 当前url对应的菜单, 如果存在激活
                                    router.push(path).catch(reason => console.error(`${reason}`))
                                }
                            });

                            // 跳到首页进行初始化
                            next({path: '/'})

                        } else {
                            next();
                        }
                    }
                }
            ]
        },
        {path: '/login', component: Login, name: '登录'},
    ],
    history: createWebHashHistory()
});

/**
 * 1. 切换路由时同步切换任务栏
 */
router.beforeResolve((to, form, next) => {
    // 刷新页面不放到任务栏, 只做页面中转处理
    // 登录页面不放到任务栏
    if(to.name != 'refresh' && to.path != '/login') {
        // 从一个不在任务栏的路由跳转的, 取消激活菜单
        if(form.meta && form.meta['taskBar'] === false) {
            store.commit('sys/switchActiveMenuTo', to.path)
        }

        // 如果不需要加入到任务栏
        if(to.meta && to.meta['taskBar'] === false) {
        //    什么都不做
        } else {
            store.commit('sys/openOrSwitchTask', to)
        }
    }

    next();
});

// 动态菜单生成路由
function importLocale(path, menu, route) {
    return () => {
        let urlPath = path.split('/');
        if(urlPath.length == 2) {
            // 必须使用这种写法才能正确开启动态加载
            // https://github.com/rollup/plugins/tree/master/packages/dynamic-import-vars#limitations
            return import(`../views/${urlPath[0]}/${urlPath[1]}/index.vue`).then(item => {
                // 获取组件名称 用来处理组件缓存(KeepAlive)
                let componentName = item.default.name;
                if(!componentName) {
                    console.warn(`菜单[${menu.url}]对应的组件没有设置[name]属性`);
                }

                // 注意：每个组件的名称都不能重复, 否则会导致KeepAlive缓存清除失败
                store.commit('sys/setRouteKeepAlive', {url: menu.url, componentName})
                return item;
            })
        } else if(urlPath.length == 3) {
            return import(`../views/${urlPath[0]}/${urlPath[1]}/${urlPath[2]}/index.vue`).then(item => {
                // 获取组件名称 用来处理组件缓存(KeepAlive)
                let componentName = item.default.name;
                if(!componentName) {
                    console.warn(`菜单[${menu.url}]对应的组件没有设置[name]属性`);
                }

                // 注意：每个组件的名称都不能重复, 否则会导致KeepAlive缓存清除失败
                store.commit('sys/setRouteKeepAlive', {url: menu.url, componentName})
                return item;
            })
        } else {
            return Promise.reject(`动态菜单不支持大于3级[${path}]`);
        }
    }
}

const resolverMenuToRoutes = (urlMenuMaps) => {
    Object.values(urlMenuMaps).forEach(menu => {
        let {url, name, target} = menu;

        // 如果以'/'开头
        let split = url.split('/').filter(item => item != '');
        let path = split.join('/');

        if(target == '_blank') { // 新标签页
            let resolve = router.resolve(path);
            if(resolve.name == '404') { // 页面不存在
                let route = {path, component: null, name: name, meta: {closable: true, taskBar: true, keepAlive: null, target: target}};
                route.component = importLocale(path, menu, route); // 动态加载组件
                router.addRoute(MainName, route)
            }
        } else {
            if(split.length > 1) {
                // closable 是否显示任务的关闭按钮   taskBar 是否加入任务栏
                let route = {path, component: null, name: menu.name, meta: {closable: true, taskBar: true, keepAlive: null}};
                route.component = importLocale(path, menu, route);
                router.addRoute(MainName, route)
            }
        }
    })
}

export {resolverMenuToRoutes}
export default router
