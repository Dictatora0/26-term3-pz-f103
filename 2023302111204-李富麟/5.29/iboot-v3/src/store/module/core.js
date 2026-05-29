/*系统模块相关配置*/
import {getDict, getMenus, getSysConfig, getUser, getPermissions} from "@/api";
import {reactive, ref, h} from "vue";
import {GET} from "@/utils/request";
import router, {resolverMenuToRoutes} from "@/router";
import CoreConsts from "@/components/CoreConsts";
import SysUtils from "@/utils/SysUtils";

/**
 * 搜索结果模型
 * @param key {String} 确保所有的model唯一
 * @param label {String} 名称
 * @param record {Object} 原始记录
 * @param filter {SearchFilter} 过滤器
 * @constructor
 */
function SearchResultModel(key, label, record, filter) {
    this.key = key;
    this.label = label;
    this.record = record;
    this.filter = filter;
}
/**
 * @param name {String} 过滤器名称
 * @param type {String} 过滤类型
 * @param filterFn {Function} 过滤函数
 * @param onClick {function(*): Promise<void>} 单击事件
 * @constructor
 */
function SearchFilter(name, type, filterFn, onClick) {
    this.name = name;
    this.type = type;
    /**
     * @type {function(*): Promise<void>}
     */
    this.onClick = onClick;
    /**
     *
     * @type {Function}
     * @return Array<SearchResultModel>
     */
    this.filter = filterFn;
}

// 解析菜单映射
function resolverMenuMaps(menus) {
    let urlMenuMap = {}, idMenuMap = {};
    let doResolverMenuMaps = (menus) => {
        for(let i=0; i < menus.length; i++) {
            let menu = menus[i];
            idMenuMap[menu.id] = menu;
            if (menu['type'] === 'V') { // 视图类型
                urlMenuMap[menu.url] = menu;
            }

            if (menu['children']) {
                doResolverMenuMaps(menu['children'])
            }
        }
    }

    doResolverMenuMaps(menus)
    return {urlMenuMap, idMenuMap};
}

function resolverBreadcrumbRoutes(menu, idMenuMaps, results, index) {
    results[index] = menu;
    let parentMenu = idMenuMaps[menu.pid];
    if(parentMenu) {
        resolverBreadcrumbRoutes(parentMenu, idMenuMaps, results, index + 1)
    }
}

const registerSysModule = function (store) {
    store.registerModule('sys', {
        namespaced: true,
        state: {
            user: {}, // 当前登录的用户
            views: [], // 视图信息
            init: false, // 系统路由是否已经初始化(菜单初始化, 用于动态路由404问题)
            userKey: null,
            collapsed: false,
            menuMode: 'inline', // 菜单模式
            menuDivider: false, // 菜单项之间是否使用分割线
            userVisible: false, // 用户中心

            // 菜单、路由等配置信息
            openKeys: [], // 当前展开的子菜单key
            activeMenu: {}, // 当前激活的菜单
            activeRoute: {}, // 当前激活的路由
            activeView: {}, // 当前激活的视图
            taskBarData: [], // 任务栏打开的菜单
            optionsMaps: {}, // url | dict -> {options, valueLabelMap}
            idMenuMaps: {}, // id和菜单的映射
            selectedKeys: [], // 选中的菜单
            authMenuMap: {}, // perms -> menu 权限
            urlMenuMaps: {}, // url -> menu 对象
            urlRouteMaps: {}, // url -> route taskBarData
            sysConfigMaps: {}, // 系统配置信息
            optionsInfo: {/*dict -> data | url -> data*/}, // 字典和url的数据信息
            breadcrumbRoutes: [{url: '/', name: '首页', index: 0}], // 面包屑路由信息

            /**
             * 后端运行日志信息
             */
            loggerConfig: {loggers: [], websocket: null, refNum: 0, filters: []},

            // 全局搜索配置信息
            /**
             * 搜索过滤器列表
             * @type Array<SearchFilter>
             */
            searchFilter: [], // 搜索过滤器
            /**
             * 搜索结果列表
             * @type Array<SearchResultModel>
             */
            searchResultModel: [],
            /**
             * 搜索结果数据
             * @type Object
             */
            searchResultData: {title: '', data: null},
        },
        getters: {
            // 标识菜单是否已经初始化完成
            init: state => state.init,
            user: state => state.user,
            views: state => state.views,
            theme: state => state.theme,
            userKey: state => state.userKey,
            menuMode: state => state.menuMode,
            collapsed: state => state.collapsed,
            userVisible: state => state.userVisible,
            loggerConfig: state => state.loggerConfig,

            // 系统名称
            sysName: state => {
                // return '厦门由创源智能科技'
                return state.sysConfigMaps['sys_name'] ? state.sysConfigMaps['sys_name']['value'] : '厦门由创源科技';
            },
            // logo
            sysLogo: state => {
                return state.sysConfigMaps['sys_logo'] ? state.sysConfigMaps['sys_logo']['value'] : '/img/logo.png';
            },
            // 菜单展开级别, 默认展开一级
            sysMenuLevel: state => {
                return state.sysConfigMaps['sys_menu_style'] ? state.sysConfigMaps['sys_menu_style']['value'] : 'one';
            },
            // 打开的子菜单
            openKeys: state => state.openKeys,

            // url -> menu 映射
            urlMenuMaps: state => state.urlMenuMaps,
            // id -> menu
            idMenuMaps: state => state.idMenuMaps,
            // perms -> menu 映射
            authMenuMap: state => state.authMenuMap,
            // 当前激活的视图
            activityView: state => state.activeView,
            // 当前激活的菜单
            activityMenu: state => state.activeMenu,
            // 当前激活的路由
            activeRoute: state => state.activeRoute,
            // 任务栏打开的任务列表
            taskBarData: state => state.taskBarData,
            // 选中的菜单
            selectedKeys: state => state.selectedKeys,
            //系统菜单
            menus: state => state.views,
            getSearchResultModel: state => {
                return state.searchResultModel;
            },
            // 解析面包屑路径
            breadcrumbRoutes: state => {
                let activeMenu = state.activeMenu;
                let activeRoute = state.activeRoute;

                if(activeMenu && activeMenu.id) {
                    let results = [];
                    resolverBreadcrumbRoutes(activeMenu, state.idMenuMaps, results, 0)
                    results.push({url: '/', name: '首页', index: 0});
                    state.breadcrumbRoutes = results.reverse();
                } else if(activeRoute.path != state.breadcrumbRoutes[0].url){
                    state.breadcrumbRoutes.splice(2, 1);
                    state.breadcrumbRoutes[1] = {url: activeRoute.path, name: activeRoute.name, index: 1}
                }

                return state.breadcrumbRoutes;
            },

            getOptionsByUrl: state => {
                return (url, labelField, valueField, targetLabelField, targetValueField, onLoaded) => {
                    let urlOptions = state.optionsMaps[url];
                    if(!urlOptions || !urlOptions['options']) {
                        let optionsData = reactive([])
                            , valueLabelMap = reactive({})
                            , valueModelMap = {};

                        // 保证每次拿到的是同一个对象
                        if(!urlOptions) {
                            state.optionsMaps[url] = {options: optionsData, valueLabelMap, valueModelMap, type: 'url'};
                        } else {
                            urlOptions['options'] = optionsData;
                            urlOptions['valueLabelMap'] = valueLabelMap;
                            urlOptions['valueModelMap'] = valueModelMap;
                        }

                        GET(url).then(({data}) => {
                            if(data instanceof Array) {
                                SysUtils.resolverOptions(data, labelField, valueField, targetLabelField
                                    , targetValueField, valueLabelMap, valueModelMap)

                                optionsData.push(...data)
                                onLoaded(valueModelMap);
                            } else {
                                console.error(`地址[${url}]的返回值必须是数组对象`)
                            }
                        })
                    }

                    return state.optionsMaps[url]
                }
            },

            getValueLabelMap: state => {
              return key =>  state.optionsMaps[key].valueLabelMap;
            },

            getOptionsByDictType: (state) => {
                return (dictType, labelField, valueField, onLoaded) => {
                    let dictData = reactive([])
                        , valueLabelMap = reactive({})
                        , valueModelMap = {};
                    let options = state.optionsMaps[dictType];
                    if(!options || options.length == 0) { // 说明字典数据还不存在, 先缓存
                        state.optionsMaps[dictType] = {options: dictData, valueLabelMap, valueModelMap, type: 'dict'};

                        getDict(dictType).then((options) => {
                            if(options instanceof Array) {
                                options.forEach(item => {
                                    item['label'] = item[labelField];
                                    item['value'] = item[valueField];
                                    valueModelMap[item['value']] = item;
                                })

                                dictData.push(...options)
                                options.forEach(item => {
                                    valueLabelMap[item.value] = item.label;
                                })

                                onLoaded(valueModelMap);
                            }
                        })
                    }

                    return state.optionsMaps[dictType]
                }
            }
        },
        mutations: {
            // 任务栏切换
            pushAndSwitchTask: (state, url) => {
                let route = state.urlRouteMaps[url];
                if(route == null) {
                    return console.error(`url[${url}]对应的路由不存在`)
                }

                router.push(route).then(() => {
                    store.commit('sys/switchActiveMenuTo', url)
                }).catch(reason=> {
                    console.error(`切换任务失败(组件异常或组件不存在或未注册路由)[${reason}]`)
                });
            },

            // 移除任务栏的任务
            removeTask: (state, url) => {
                let urlRoute = state.urlRouteMaps[url];
                if(urlRoute) {
                    state.urlRouteMaps[url] = null;
                    let number = state.taskBarData.findIndex(route => route.path == url);
                    if(number > -1) {
                        state.taskBarData.splice(number, 1);
                    }
                }
            },
            openOrSwitchTask: (state, route) => {
                let path = route.path;
                // 首页(工作台)作为第一个任务
                if(state.taskBarData.length == 0 && route.path != '/') {
                    return router.push('/').then(() => {
                        router.push(route).finally();
                    })
                }

                if(!state.urlRouteMaps[path]) {
                    // https://gitee.com/iteaj/ivzone/issues/I7Y9ZF
                    if(!route.meta.keepAlive) {
                        router.getRoutes().forEach(item => {
                            if(item.path == route.path) {
                                route.meta.keepAlive = item.meta['keepAlive'];
                            }
                        })
                    }

                    state.taskBarData.push(route);
                }

                state.activeRoute = route;
                state.urlRouteMaps[path] = route;
                state.activeMenu = state.urlMenuMaps[route.path];
            },
            setRouteKeepAlive: (state, {url, componentName}) => {
                // https://gitee.com/iteaj/ivzone/issues/I7Y9ZF
                router.getRoutes().forEach(item => {
                    if(item.path == url) {
                        item.meta['keepAlive'] = componentName;
                    }
                })
            },
            switchActiveRouteTo: (state, route) => {
                state.activeRoute = route;
            },
            // 切换激活的菜单
            switchActiveMenuTo: (state, url) => {
                let menu = state.urlMenuMaps[url];

                if(menu) {
                    state.activeMenu = menu;

                    state.selectedKeys.length = 0;
                    state.selectedKeys.push(menu.url);
                } else {
                    state.activeMenu = null;
                    state.selectedKeys.length = 0;
                }
            },

            // 在任务栏上面打开一个任务, 并展开此任务菜单的父菜单
            openTaskAndParentMenu: (state, url) => {
                let menu = state.urlMenuMaps[url];
                if(menu) {
                    if(!state.activeMenu || state.activeMenu != menu) {
                        store.commit('sys/pushAndSwitchTask', url);
                    }

                    function getPidMenuList(menu, openKeys) {
                        let parent = state.idMenuMaps[menu.pid];
                        if(parent) {
                            openKeys.push(parent.id);
                            getPidMenuList(parent, openKeys);
                        }

                        return openKeys;
                    }

                    // 打开菜单栏中此url对应的父菜单
                    let openKeys = getPidMenuList(menu, [])
                    store.commit('sys/switchOpenSubMenuTo', openKeys);
                }
            },

            // 往任务栏中增加新的任务
            addNewMenu: (state, menu) => {
                let taskUrl = menu['url'] != null ? menu['url'] : menu['path'];
                if(taskUrl == null) {
                    return console.warn(`菜单[${menu.name}]未指定url`)
                }

                if(!menu['name'] && import.meta.env.DEV) {
                    console.warn(`未指定任务name[${menu}]`)
                }

                let taskMenu = state.urlMenuMaps[taskUrl];
                if(!taskMenu) {
                    // 是否是侧边栏菜单
                    menu['isMenu'] = menu['isMenu'] || false;

                    state.urlMenuMaps[taskUrl] = menu;
                } else {
                    if(taskMenu.name == menu.name) {
                        console.warn(`此任务名[${menu.name}]已经存在, 任务[${taskMenu}]`)
                    }
                }
            },

            putMenuToTaskBars: (state, menu) => {
                state.taskBarData.push(menu)
            },

            switchActiveViewTo: (state, view) => {
                state.activeView = view;
            },
            switchOpenSubMenuTo: (state, openKeys) => {
                state.openKeys = openKeys
            },
            toggleUserVisible: (state, {visible, key}) => {
                state.userKey = key || state.userKey;
                state.userVisible = visible != null
                    ? visible : state.userVisible;
            },
            /**
             * 全局搜索
             * @param key 关键字
             */
            onGlobalSearch: (state, key) => {
                state.searchResultModel.length = 0;
                if(key) {
                    state.searchFilter.forEach(filter => {
                        try {
                            let result = filter.filter(key);
                            state.searchResultModel.push(...result);
                        } catch (e) {
                            console.error(`搜索过滤[${filter.name}]异常[${e}]`)
                        }
                    })
                }
            },
            /**
             * 注册搜索过滤器
             * @param state
             * @param searchFilter {SearchFilter}
             */
            registerSearchFilter: (state, searchFilter) => {
                if(searchFilter) {
                    state.searchFilter.push(searchFilter);
                } else {
                    console.error("注册搜索过滤器失败[searchFilter = null]")
                }
            },
            /**
             * 注销
             * 清除数据
             * @param state
             */
            logout: (state) => {
                state.init = false;
                state.openKeys = [];
                state.urlMenuMaps = {};
                state.taskBarData = [];
                state.urlRouteMaps = {};
                state.selectedKeys = [];
                state.activeRoute = null;
                state.userVisible = false;
            }
        },
        actions: {
            initUser: ({commit, state}) => {
                return getUser().then((resp) => {
                    if(resp) {
                        let {data, code, message} = resp;
                        if(code == CoreConsts.SuccessCode) {
                            state.user = data;
                        } else {
                            console.error(message || "获取用户失败");
                        }
                    }
                    return {};
                }).catch(reason => reason);
            },
            initPermissions: ({commit, state}) => {
                state.authMenuMap = {}; // 重置
                return getPermissions().then(({data, code, message}) => {
                    if(code == CoreConsts.SuccessCode) {
                        if(data instanceof Array) {
                            data.forEach(item => {
                                state.authMenuMap[item] = true;
                            })
                        }

                        commit('registerSearchFilter', new SearchFilter("权限", 'permissions', function (key) {
                            let filter = this;
                            return Object.keys(state.authMenuMap)
                                .filter(item => item.includes(key))
                                .map(key => {
                                    return new SearchResultModel(`permissions:${key}`, key, key, filter);
                                })
                        }, (model) => {
                            let record = model['record'];
                            return router.push({path: '/core/menu', query: {perms: record}})
                        }))

                        return state.authMenuMap;
                    } else {
                        console.error(message); return {};
                    }
                }).catch(reason => reason)
            },
            initConfig: ({state}) => {
                return getSysConfig({type: 'sys'}).then(({data, code, message}) => {
                    if(code == CoreConsts.SuccessCode) {
                        if(data instanceof Array) {
                            data.forEach(item => {
                                state.sysConfigMaps[item.label] = item;
                            })
                        }
                        return state.sysConfigMaps;
                    } else {
                        console.error(message); return {};
                    }
                }).catch(reason => reason)
            },
            initMenus: ({commit, state}) => {
                 return getMenus().then((resp) => {
                     if(resp) {
                         let {data} = resp;
                         state.views = data;
                         // state.activeView = state.views[0];
                         let {urlMenuMap, idMenuMap} = resolverMenuMaps(data);
                         state.idMenuMaps = idMenuMap;

                         // 加入到菜单列表
                         if(urlMenuMap != null) {
                             Object.keys(urlMenuMap).forEach(key => {
                                 state.urlMenuMaps[key] = urlMenuMap[key];
                             })
                         }

                         resolverMenuToRoutes(urlMenuMap);
                         state.init = true; // 声明路由信息已经初始化完成
                         // 注册菜单名称和url过滤器
                         commit('registerSearchFilter', new SearchFilter("菜单", 'menu', function (key) {
                             let filter = this;
                             let nameSearchResultModels = Object.values(urlMenuMap)
                                 .filter(menu => menu.name && menu.name.includes(key))
                                 .map(menu => {
                                     return new SearchResultModel(`menu:${menu.id}`, menu.name, menu, filter);
                                 });

                             let urlSearchResultModels = Object.values(urlMenuMap)
                                 .filter(menu => menu.url && menu.url.includes(key))
                                 .map(menu => {
                                     return new SearchResultModel(`menu:${menu.id}`, menu.url, menu, filter);
                                 });

                            return [...nameSearchResultModels, ...urlSearchResultModels];
                         }, (model) => {
                             let record = model['record'];
                             return router.push(record.url)
                         }))

                         // 注册url和字典过滤器
                         commit('registerSearchFilter', new SearchFilter("字典/url", 'options', function (key) {
                             let filter = this;
                             return Object.keys(state.optionsMaps)
                                 .filter(item => item.includes(key))
                                 .map(key => {
                                     let result = state.optionsMaps[key];
                                     return new SearchResultModel(`options:${key}`, key, result, filter);
                                 })
                         }, (model) => {
                             let record = model['record'];
                             if(record.type == 'dict') {
                                 return router.push({path: '/core/dict/data', query: {type: model.label}})
                             } else {
                                 state.searchResultData = {title: `请求地址：${model.label}`, data: record['options']};
                                 return router.push({path: `/search/result/data`})
                             }
                         }))

                         return urlMenuMap;
                     }

                    return {};
                }).catch(reason => reason);
            },
        }
    })
}

export default registerSysModule;
