import {computed, defineComponent, inject, mergeProps, provide, ref, watch} from "vue";
import {EditableTableKey, FuncContextKey, RowContextKey} from "@/utils/ProvideKeys";
import {msgError} from "@/utils/message";
import {Config, DetailContext, EditContext, FuncConfig, SearchContext, TableContext} from "@/components/view/Context";
import {mapGetters} from "vuex";
import CoreConsts from "@/components/CoreConsts";
import {useRouter} from "vue-router";
import {InboxOutlined} from "@ant-design/icons-vue";
import {UploadDragger} from "ant-design-vue";
import SysUtils from "@/utils/SysUtils";
import {GET} from "@/utils/request";

export const URow = defineComponent({
    name: 'URow',
    props: ['col', 'span', 'responsive'], // 如果两个都配置已span为主
    setup({col, span, responsive}) {
        let colConfig = col
        if(span) { // 指定
            colConfig = {span: span};
        } else if(typeof col == 'string') { // 响应式(兼容旧版本)
            colConfig = {...CoreConsts.TypeResponsiveConfig[col]};
        } else if(typeof responsive == 'string') { // 响应式
            colConfig = {...CoreConsts.TypeResponsiveConfig[responsive]};
        } else if(typeof responsive == 'number') { // 响应式最大列数
            if(responsive == 1) {
                colConfig = {xs: 24, sm: 24, md: 24, lg: 24, xl: 24, xxl: 24}
            } else if(responsive == 2) {
                colConfig = {xs: 24, sm: 24, md: 12, lg: 12, xl: 12, xxl: 12}
            } else if(responsive == 3) {
                colConfig = {xs: 24, sm: 24, md: 12, lg: 12, xl: 8, xxl: 8}
            }
        }

        provide(RowContextKey, colConfig || {});
    },
    render() {
        return <ARow {...this.$attrs} v-slots={this.$slots} class="u-row" />
    }
})

function funcClickHandle(context, config) {
    if(context != null) {
        let $view = context.get$View();

        let childFunc = config.funcPath[1]; // 子功能
        switch (config.func) {
            case CoreConsts.FuncNameMeta.ADD:
                /**
                 * @type LinkContext
                 */
                let linkContext = context.getLinkContext();
                let tableContext = linkContext.getChildrenContext(config['uid'] || CoreConsts.DefaultTableUid);

                if(tableContext instanceof TableContext && tableContext.editable === true) { //可编辑表格
                    return $view.tableForAdd(config);
                } else {
                    // 新增子记录功能
                    if(childFunc == CoreConsts.ChildFuncNameMeta.CHILD) {
                        return $view.openForChild(config);
                    }
                    return $view.openForAdd(config);
                }
            case CoreConsts.FuncNameMeta.DEL:
                return $view.del(config);
            case CoreConsts.FuncNameMeta.EDIT:
                if(context.editable === true) { //可编辑表格
                    return $view.tableForEdit(config);
                } else { // 使用modal或者drawer编辑
                    if(childFunc == CoreConsts.ChildFuncNameMeta.SET) { // 设置
                        return $view.openForSet(config);
                    } else if(childFunc == CoreConsts.ChildFuncNameMeta.LOADING) { // 加载数据
                        return $view.openForLoading(config);
                    } else {
                        return $view.openForEdit(config);
                    }
                }
            case CoreConsts.FuncNameMeta.DETAIL:
                return $view.detail(config);
            case CoreConsts.FuncNameMeta.QUERY:
                return $view.query(config);
            case CoreConsts.FuncNameMeta.CANCEL:
                if(context.editable === true) { //可编辑表格
                    return $view.tableForCancel(config);
                } else {
                    return $view.cancel(config);
                }
            case CoreConsts.FuncNameMeta.RESET:
                if(context instanceof EditContext) {
                    return $view.resetEditModel(config);
                } else if(context instanceof SearchContext) {
                    return $view.resetSearchModel(config)
                } else {
                    return console.error(`[reset]功能不支持上下文[${context}]只支持[EditContext、SearchContext]`);
                }
            case CoreConsts.FuncNameMeta.SUBMIT:
                if(context.editable === true) { //可编辑表格
                    return $view.tableForSubmit(config);
                } else {
                    return $view.submit(config);
                }
            case CoreConsts.FuncNameMeta.EXPAND:
                return $view.expanded(config); // 展开所有行
            case CoreConsts.FuncNameMeta.IMPORT:
                return $view.excelImport(config);
            case CoreConsts.FuncNameMeta.EXPORT:
                return $view.excelExport(config);
            case CoreConsts.FuncNameMeta.AJAX:
                return $view.otherFuncAjax(config);
            case CoreConsts.FuncNameMeta.DOWNLOAD:
                return $view.download(config);
            case CoreConsts.FuncNameMeta.OPEN:
                return $view.openForVisible(config);
            case CoreConsts.FuncNameMeta.LINK:
                return $view.linkTo(config);
            default: // 其他功能操作
                return console.warn(`不支持的功能[${config.func}]`)
        }
    }
}

export const UFuncTag = defineComponent({
    name: 'UTag', // 别名 UFuncTag
    props: {
        url: String,
        uid: String,
        color: String,
        onClick: Function, // 自定义单击处理
        callback: Function, // 执行完成后的回调
        data: {type: Object}, // 行数据
        method: {type: String},
        params: {default: null},
        reload: {default: true}, // 重新加载列表
        disabled: {default: null}, // 是否禁用
        config: {type: Object, default: () => { return {}}}, // 配置
        func: {type: String, required: true}, // add, del, edit, query, import, export, cancel, detail, reset, expand, ...
    },
    setup(props) {
        let router = useRouter();
        let instance = ref(null);
        let funcPath = ref(props.func.toUpperCase().split(":"));

        /**
         * @type {DetailContext | EditContext | TableContext | SearchContext}
         */
        let context = inject(FuncContextKey);
        let editable = inject(EditableTableKey); // 是否属于可编辑表组件

        let editing = ref(false);
        let disabledComputed = computed(() => {
            if(typeof props.disabled == 'function') {
                return props.disabled(props.data);
            }

            return props.disabled === true || editing.value;
        });

        let typeCompute = computed(() => props.func.split(":")[0].toUpperCase())

        let funcConfig = new FuncConfig(props, context, funcPath.value);
        let clickProxy = funcConfig.trigger = (e) => {
            if(!disabledComputed.value) {
                // 配置对象只能在触发事件内创建
                let config = new Config(instance.value).build(props, context, router, funcConfig);
                if(props.onClick instanceof Function) {
                    props.onClick({data: props.data, event: () => funcClickHandle(context, config)})
                } else {
                    if(context != null) {
                        funcClickHandle(context, config)
                    } else {
                        console.warn("无效的操作(需要自定义事件或者在指定的组件下面)")
                    }
                }
            }
        }

        // 注册功能点
        context.getLinkContext().registerFunc(funcConfig);

        /**
         * @type {ViewContext}
         */
        let viewContext = context.get$View().getViewContext();
        return {clickProxy, context, typeCompute, viewContext, disabledComputed, editing, funcPath, instance, editable};
    },
    computed: {
        ...mapGetters({
            auth: 'sys/authMenuMap'
        }),
        tagColor() {
            let func = this.funcPath[0];
            return this.color || CoreConsts.FuncTagColorMaps[func] || 'blue'
        }
    },
    mounted() {
        this.instance = this;
    },
    render() {
        if(this.editable && this.$props.disabled == null) {
            if(this.data && this.data['__editing']) {
                if (this.typeCompute == CoreConsts.FuncNameMeta.SUBMIT) {
                    this.editing = false;
                }
            } else {
                if (this.typeCompute == CoreConsts.FuncNameMeta.SUBMIT) {
                    this.editing = true;
                }
            }
        }

        let tagColor = this.disabledComputed ? '#d8d8d8' : this.tagColor;
        let disabledClass = this.disabledComputed ? 'iv-func-disabled' : 'iv-func-tag'
        return <ATag closable={false} class={disabledClass} class="u-func" bordered={false}
                     color={tagColor} onClick={this.clickProxy} v-slots={this.$slots} />
    },
    deactivated() {
        if(this['modalRef']) {
            this['modalRef'].destroy();
        }
    },
    methods: {
        trigger() {
            this.clickProxy();
        },
    }
})

/**
 * 功能按钮, 可以指定url, 功能类型
 * 注：只适用于编辑组件包括不限于(搜索组件, 编辑组件)等编辑组件
 * @type {DefineComponent<{func: {default: string, type: StringConstructor}, meta: {default: (function(): {}), type: ObjectConstructor}, url: {type: StringConstructor}}, unknown, unknown, {typeCompute(): *}, {}, ComponentOptionsMixin, ComponentOptionsMixin, Record<string, any>, string>}
 */
export const UFuncBtn = defineComponent({
    name: 'UButton', // 别名 UFuncBtn
    props: {
        uid: String, // 要操作的uid
        onClick: Function,
        callback: Function, // 执行完成后的回调
        url: {type: String}, // 功能地址
        data: {type: Object},
        params: {default: null},
        reload: {default: true}, // 重新加载列表
        disabled: {type: Boolean, default: false},
        method: {type: String, default: null}, // 请求方法
        config: {type: Object, default: () => { return {}}}, // 配置
        func: {type: String, required: true, default: ''},  // add, del, edit, query, import, export, cancel, detail, reset
    },
    setup(props, {attrs}) {
        let router = useRouter();
        let instance = ref(null);
        let funcPath = ref(props.func.toUpperCase().split(":"));

        /**
         * @type {EditContext | SearchContext | DetailContext | SearchContext}
         */
        let context = inject(FuncContextKey);
        let funcConfig = new FuncConfig(props, context, funcPath.value);

        let clickProxy = {onClick: (e) => {
                // 配置对象只能在触发事件内创建
                let config = new Config(instance.value).build(props, context, router, funcConfig);
                if(props.onClick instanceof Function) {
                    props.onClick({data: props.data, event: () => funcClickHandle(context, config)})
                } else if(context != null) {
                    funcClickHandle(context, config)
                }else {
                    console.warn(`无效的操作(需要自定义事件或者在指定的组件下面)`)
                }
            }
        }

        let loading = ref(false);

        funcConfig.trigger = clickProxy.onClick;
        funcConfig.setLoading = status => loading.value = status;
        context.getLinkContext().registerFunc(funcConfig)

        let viewContext = context.get$View().getViewContext();
        return {clickProxy, context, loading, viewContext, instance, funcPath};
    },
    computed: {
        ...mapGetters({
            auth: 'sys/authMenuMap'
        })
    },
    render() {
        this.instance = this;
        return <AButton {...this.handleProps()} disabled={this.disabled} v-slots={this.$slots} style="margin: 0px 3px" loading={this.loading} />
    },
    methods: {
        trigger() {
            this.clickProxy.onClick(null);
        },
        handleProps() {
            let type = CoreConsts.FuncBtnTypeMaps[this.funcPath[0]];
            return mergeProps(type, this.clickProxy, this.$attrs);
        }
    }
})

export const UTree = defineComponent({
    name: 'UTree',
    props: {
        url: {type: String}, // 数据地址
        childUrl: {type: String}, // 获取子数据地址
        field: {type: String, default: null},
        loadData: {type: Function},
        directory: {type: Boolean, default: false}, // 是否是目录
        checkable: {type: Boolean, default: false},
        checkedKeys: {type: Array},
        'onUpdate:checkedKeys': {type: Function},
        selectable: {type: Boolean, default: false},
        selectedKeys: {type: Array},
        'onUpdate:selectedKeys': {type: Function},
        onLoaded: {default: () => { return () => null}}, // 数据加载完成
        defaultExpandAll: {type: Boolean, default: false},
        resolverNode: {type: Function, default: (data, fields, isDync) => {
            if(isDync) { // childUrl存在表示是动态加载获取子节点
                if(data[fields.children] instanceof Array) {
                    data['isLeaf'] = false;
                } else {
                    data['isLeaf'] = true;
                }
            }
        }},
        fieldNames: {type: Object, default: () => {return {key: CoreConsts.DefaultRowKey, title: 'name', children:'children', level: null}}}
    },
    setup(props, {attrs}) {
        let allKeys = ref([]);
        let treeData = ref([]);
        let treeDataBack = ref(null);
        let treeDataMap = ref({});
        let expandedKeys = ref([]);
        let selectedKeys = ref([]);
        let loadDataRef = ref(props.loadData);

        if(import.meta.env.DEV) {
            if(!props.fieldNames.key || !props.fieldNames.title || !props.fieldNames.children) {
                console.warn(`fieldNames配置没有完整[${props.fieldNames}]`)
            }
        }

        function initAllKeys(data) {
            if(data instanceof Array) {
                data.forEach(item => {
                    props.resolverNode(item, props.fieldNames, props.childUrl)
                    let keyValue = item[props.fieldNames.key];
                    allKeys.value.push(keyValue);
                    treeDataMap.value[keyValue] = item;
                    let children = item[props.fieldNames.children];
                    if(children instanceof Array) {
                        initAllKeys(children);
                    }
                })
            }
        }

        // childUrl说明需要动态加载子节点
        if(props.childUrl && !(props.loadData instanceof Function)) {
            loadDataRef.value = (node) => {
                let children = node.dataRef[props.fieldNames.children];
                if(!children || children.length == 0) {
                    let newUrl = SysUtils.resolverPlaceholderUrl(props.childUrl, node.dataRef);
                    return new Promise((resolve, reject) => {
                        GET(newUrl).then(({code, message, data}) => {
                            if(code == CoreConsts.SuccessCode) {
                                node.dataRef[props.fieldNames.children] = data;
                                allKeys.value = []; // 重置所有key
                                treeDataMap.value = {}; //
                                initAllKeys(treeData.value);
                                treeData.value = [...treeData.value]
                                props.onLoaded(treeDataMap.value);
                                resolve();
                            } else {
                                reject(message);
                            }
                        }).catch(reject);
                    })
                } else {
                    return Promise.resolve();
                }

            }
        }

        let checkedKeysRef = ref(props.checkedKeys)
            , onCheckedKeys = props["onUpdate:checkedKeys"]
            , selectedKeysRef = ref(props.selectedKeys)
            , onSelectedKeys = props["onUpdate:selectedKeys"]
            , formContext;
        if(props.field) {
            let field = props.field.split(".");
            formContext = inject('formContext');
            watch(() => formContext.getFieldValue(field), (newValue) => {
                if(props.checkable) {
                    checkedKeysRef.value = newValue;
                }
                if(props.selectable) {
                    selectedKeysRef.value = newValue;
                }
            })
            if(props.checkable) {
                checkedKeysRef.value = formContext.getFieldValue(field);
                onCheckedKeys = (value) => {
                    checkedKeysRef.value = value;
                    formContext.setFieldValue(field, value)
                    if(props['onUpdate:checkedKeys'] instanceof Function) {
                        props['onUpdate:checkedKeys'](value);
                    }
                }
            }
            if(props.selectable) {
                selectedKeysRef.value = formContext.getFieldValue(field);
                onSelectedKeys = (value) => {
                    selectedKeysRef.value = value;
                    formContext.setFieldValue(field, value);
                    if(props['onUpdate:selectedKeys'] instanceof Function) {
                        props['onUpdate:selectedKeys'](value);
                    }
                }
            }
        } else {
            if(props.checkable) {
                onCheckedKeys = (value) => {
                    checkedKeysRef.value = value;
                    if(props['onUpdate:checkedKeys'] instanceof Function) {
                        props['onUpdate:checkedKeys'](value);
                    }
                }
            }
            if(props.selectable) {
                onSelectedKeys = (value) => {
                    selectedKeysRef.value = value;
                    if(props['onUpdate:selectedKeys'] instanceof Function) {
                        props['onUpdate:selectedKeys'](value);
                    }
                }
            }
        }

        return {allKeys, treeData, selectedKeys, expandedKeys, checkedKeysRef, onCheckedKeys
            , formContext, selectedKeysRef, onSelectedKeys, treeDataMap, loadDataRef, initAllKeys, treeDataBack}
    },
    watch: {
        url(newUrl) {
            this.loadingInitData(newUrl)
        },
    },
    created() {
        if(this.url) {
            this.loadingInitData(this.url);
        }
    },
    render() {
        if(!this.directory) {
            return <ATree {...this.$attrs} checkable={this.checkable} selectable={this.selectable}
                          v-models={[[this.expandedKeys, 'expandedKeys', ["modifier"]]]} v-slots={this.$slots}
                          checkedKeys={this.checkedKeysRef} onUpdate:checkedKeys={this.onCheckedKeys}
                          selectedKeys={this.selectedKeysRef} onUpdate:selectedKeys={this.onSelectedKeys}
                          treeData={this.treeData} loadData={this.loadDataRef} fieldNames={this.fieldNames}
                          defaultExpandAll={this.defaultExpandAll}></ATree>
        } else {
            return <ADirectoryTree {...this.$attrs} checkable={this.checkable} selectable={this.selectable}
                          v-models={[[this.expandedKeys, 'expandedKeys', ["modifier"]]]} v-slots={this.$slots}
                          checkedKeys={this.checkedKeysRef} onUpdate:checkedKeys={this.onCheckedKeys}
                          selectedKeys={this.selectedKeysRef} onUpdate:selectedKeys={this.onSelectedKeys}
                          treeData={this.treeData} loadData={this.loadDataRef} fieldNames={this.fieldNames}
                          defaultExpandAll={this.defaultExpandAll}></ADirectoryTree>
        }
    },
    methods: {
        loadingInitData(dataUrl) {
            dataUrl = dataUrl || this.url;
            this.$http.get(dataUrl).then(({code, message, data}) => {
                if(code == CoreConsts.SuccessCode) {
                    this.allKeys = []; // 重置所有key
                    this.treeDataMap = {};

                    this.treeData = data;
                    this.initAllKeys(data);
                    this.$emit("update:source", data);
                    this.$emit("loaded", this.treeDataMap);
                    if(this.defaultExpandAll) {
                        this.setExpandedAllKeys();
                    }
                } else {
                    msgError(message);
                }
            }).catch(reason => console.error(reason));
        },
        /**
         * @returns {Array}
         */
        getSelectedKeys() {
            return this.selectedKeys;
        },
        /**
         * 选中的所有值
         * @returns {*[]}
         */
        getSelectedValues() {
          return this.selectedKeysRef.map(key => this.treeDataMap[key]);
        },
        setSelectedKeys(selectedKeys) {
            this.selectedKeysRef = selectedKeys;
        },
        /**
         * @returns {Array}
         */
        getCheckedKeys() {
            return this.checkedKeysRef;
        },
        /**
         * 选中的所有值
         * @returns {*[]}
         */
        getCheckedValues() {
            return this.checkedKeysRef.map(key => this.treeDataMap[key]);
        },
        setCheckedKeys(checkedKeys) {
            this.checkedKeysRef = checkedKeys;
        },
        /**
         * 返回key对应的值
         * @param key
         * @returns {*}
         */
        getValueByKey(key) {
            return this.treeDataMap[key];
        },
        getAllKeys() {
          return this.allKeys;
        },

        setFieldValue(values) {
            /**
             * @type {FormContext}
             */
            let formContext = this.formContext;
            if(formContext != null) {
                formContext.setFieldValue([this.field], values);
            }
        },
        getExpandedKeys() {
            return this.expandedKeys;
        },

        /**
         * 设置展开的行的key
         * @param expandedKeys
         */
        setExpandedKeys(expandedKeys) {
            this.expandedKeys = expandedKeys;
        },
        setExpandedAllKeys() {
            this.expandedKeys = this.allKeys;
        },
        /**
         * 恢复
         */
        resetNode() {
            this.treeData = this.treeDataBack;
        },
        /**
         * 节点过滤
         * @param callback {Function} 回调 返回 true或者false
         * @param reset {Boolean} 是否重置节点
         */
        filterNode(callback, reset) {
            if(reset === true) {
                this.resetNode(); return;
            }

            if(callback instanceof Function) {
                let newTreeData = []

                // 先备份所有数据
                if(!this.treeDataBack) {
                    this.treeDataBack = this.treeData;
                }

                function filter(data, newData) {
                    data.forEach(item => {
                        if(callback(item)) {
                            newData.push(item);
                        }
                    })
                }

                filter(this.treeData, newTreeData)
                this.treeData = newTreeData;
            }
        }
    }
})
export const ImportUpload = defineComponent({
    components: {InboxOutlined, UploadDragger},
    render() {
        let templateUrl = this.$attrs.templateUrl;
        return (<div>
            <UploadDragger {...this.$attrs}>
                <p class="ant-upload-drag-icon">
                    <InboxOutlined />
                </p>
                <p class="import-upload-text">点击或者拖拽excel文件到此区域</p>
            </UploadDragger>
            <p className="import-upload-template" style="padding: 12px 0px 0px">
                <a href={templateUrl}>点击下载模板</a>
            </p>
        </div>)
    },
    methods: {
        downloadTemplate() {
            let templateUrl = this.$attrs.templateUrl;
            let config = {responseType: 'blob'}; config[CoreConsts.CancelRespResolver] = true;
            GET(templateUrl, null).then(resp => {
                let {data, headers} = resp;

                // let header = headers["content-disposition"];
                // contentType = contentType || headers['content-type'] || 'application/octet-stream';
                // fileName = fileName || (header ? decodeURI(header.split("filename=")[1]) : console.warn("未指定文件名"));
                // SysUtils.downloadFile(data, contentType, fileName)
            })
        },
        handleDrop() {

        },
        handleChange() {

        }
    }
})
/**
 * 只有两列的ARow
 */
export const U2Col = defineComponent({
    name: 'U2Col',
    props: {
        gutter: {type: Number, default: 8}
    },
    render() {
        return (<ARow {...this.$attrs} gutter={this.gutter} class="iv-row iv-u2col-row">
            <ACol class="iv-col iv-col-left" xs={0} sm={0} md={8} lg={6} xl={6}>
                {this.$slots.left ? this.$slots.left() : []}
            </ACol>
            <ACol class="iv-col iv-col-right" xs={24} sm={24} md={16} lg={18} xl={18}>
                {this.$slots.right ? this.$slots.right() : []}
            </ACol>
        </ARow>)
    },
})
export default {
    install(app) {
        app.component(URow.name, URow)
        app.component(UTree.name, UTree)
        app.component(U2Col.name, U2Col)
        app.component(UFuncBtn.name, UFuncBtn)
        app.component(UFuncTag.name, UFuncTag)
        app.component("UFuncBtn", UFuncBtn)
        app.component("UFuncTag", UFuncTag)
    }
}
