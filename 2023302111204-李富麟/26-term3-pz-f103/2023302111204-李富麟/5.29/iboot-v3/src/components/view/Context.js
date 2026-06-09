import {FormContext} from "@/components/form/basic/FormContext";
import {confirm, msgError, msgLoading, msgSuccess, msgWarn} from "@/utils/message";
import {FuncNameMeta, TypeMethodMaps} from "@/utils/MetaUtils";
import SysUtils from "@/utils/SysUtils";
import CoreConsts from "@/components/CoreConsts";
import {GET, urlConfig} from "@/utils/request";
import dayjs from "dayjs";
import {Modal} from "ant-design-vue";
import {createVNode, defineComponent, reactive} from "vue";
import {ImportUpload} from "@/components/basic";

function Unmount() {
    console.warn("此方法只能在组件挂载时才能使用");
}
function unUFuncTag() {
    console.warn("需使用[UFuncTag]作为表格按钮")
}
const VoidFun = () => {};
const VoidResolveCall = () => Promise.resolve();
let env = import.meta.env;
/**
 * 视图组件提供的操作接口
 * @see UView
 * @see IvzFuncView
 * @see IvzMenuView
 * @param context 视图组件的上下文对象{@link ViewContext}
 */
export function $View(context) {

    this.context = context;

    if(!context) {
        return console.error("未传入视图上下文对象[ViewContext]")
    }

    this.context['__$View'] = this;

    /**
     * 功能的唯一代表字段 - 比如：id
     * @see CoreConsts#DefaultRowKey
     * @return {String|Number}
     */
    this.getRowKey = function () {
        return this.context.getRowKey();
    }

    /**
     * 当前视图(功能)页的上下文对象
     * @see ViewContext
     * @return {ViewContext}
     */
    this.getViewContext = function () {
        return this.context;
    }

    /**
     * 获取功能元上下文, 只能用于以下两种组件
     * @see IvzFuncView
     * @set IvzMenuView
     * @return {void|FuncMetaContext}
     */
    this.getMetaContext = function () {
        if(!this.context.funMetasContext) {
            return console.warn("此方法只能在组件[IvzFuncView or IvzMenuView]中使用")
        }

        return this.context.funMetasContext;
    }

    /**
     * 获取编辑功能元数据
     * @param field
     * @return {void|Object}
     */
    this.getEditMeta = function (field) {
        return this.getMetaContext().getEditMeta(field);
    }

    /**
     * 获取表格功能元数据
     * @param field
     * @return {void|Object}
     */
    this.getTableMeta = function (field) {
        return this.getMetaContext().getTableMeta(field);
    }

    /**
     * 获取搜索功能元数据
     * @param field
     * @return {void|Object}
     */
    this.getSearchMeta = function (field) {
        return this.getMetaContext().getSearchMeta(field);
    }

    /**
     * 获取当前视图页的名称 eg: 用户管理、部门管理等
     * 需要在视图组件设置属性：name
     * @return {String}
     */
    this.getFuncName = function () {
        return this.context.name;
    }

    /**
     * 校验一个model是编辑还是新增
     * 默认根据 rowKey判断
     * @param model
     * @return {boolean}
     */
    this.isEdit = function (model) {
        let rowKey = this.getRowKey();
        return model[rowKey] != null;
    }

    /**
     * 返回编辑框获取数据需要的地址
     * 注：可根据情况自行替换和修改
     * @param data 操作参数
     * @param funcPath {String[]} 功能路径
     * @param params {Function} 获取请求参数回调
     * @return {Promise}
     */
    this.getRequestParams = function (data, params, funcPath) {
        let func = funcPath[0];
        params = params || CoreConsts.DefaultFuncParams[func];
        if(params instanceof Function) {
            let param = params(data, funcPath, this.getRowKey());
            if(param instanceof Promise) {
                return param;
            } else {
                return Promise.reject(`params方法只支持返回Promise对象`);
            }
        } else if(typeof params == 'object') {
            return Promise.resolve(params);
        } else {
            return Promise.resolve(data);
        }
    }

    /**
     * 打开编辑框
     * @param config {Config}
     */
    this.openForAdd = function ({uid, data, params, func, funcPath, context}) {
        let linkContext = context.getLinkContext();
        let editContext = linkContext.getChildrenContext(uid || CoreConsts.DefaultEditUid);
        if(editContext == null) {
            return console.warn(`未找到对应uid的可编辑组件[${uid || CoreConsts.DefaultEditUid}]`)
        }

        try {
            editContext.openType = 'add';
            let formContext = editContext.getFormContext();
            let formModel = formContext.getInitModel();

            this.getRequestParams({data, formModel}, params, funcPath).then((editModel) => {
                editContext.asyncVisible(data, true).then((edit) => {
                    // 先触发v-model事件
                    formContext.setEditModel(editModel)
                    // 再触发编辑事件
                    edit.$emit('edit', editModel);
                })
            })
        } catch (e) {
            console.error(e);
        }
    }

    /**
     * 表格新增一行
     * @param config {Config}
     */
    this.tableForAdd = function ({uid, data, params, func, funcPath, context}) {
        let linkContext = context.getLinkContext();
        let tableContext = linkContext.getChildrenContext(uid || CoreConsts.DefaultTableUid);
        if(!(tableContext instanceof TableContext)) {
            return console.warn(`未找到对应uid的表格组件[${uid || CoreConsts.DefaultTableUid}]`)
        }
        // 新增一行
        tableContext.addEditableRow();
    }

    /**
     * 删除主表格记录
     * @param param {Config}
     */
    this.del = function ({method, url, data, func, uid
                             , config: {confirmTitle, confirmContent}, callback
         , context, params, funcPath, reload}) {

        let linkContext = context.getLinkContext();
        let tableContext = linkContext.getChildrenContext(CoreConsts.DefaultTableUid);
        if(!(tableContext instanceof TableContext)) {
            return console.warn(`未找到对应uid的表格组件[${linkContext.uid}]`)
        }

        // 批量删除, 获取表列表数据
        if(funcPath[1] && funcPath[1] == CoreConsts.ChildFuncNameMeta.BATCH) {
            data = tableContext.getSelectedRows();
        }

        if(!data || data.length == 0) {
            return msgWarn("请选择要删除的记录");
        }

        let title = confirmTitle || CoreConsts.DelConfirmTitle;
        let content = confirmContent || CoreConsts.DelConfirmContent;

        if(tableContext.editable) { // 是可编辑的表格
            // 如果是新增的编辑行直接删除
            // 删除成功直接返回
            if(tableContext.delEditableRow()) {
                return;
            }
        }

        confirm({title, content, onOk: () => {
            this.getRequestParams(data, params, funcPath).then((params => {
                    tableContext.setLoading(true, CoreConsts.DeleteExecTipping)
                    this.getRequestMethod({func, method})(url, params).then(({code, message, data}) => {
                        if (code == CoreConsts.SuccessCode) {
                            msgSuccess(message || CoreConsts.DelSuccessMsg);
                            if(reload) {
                                this.queryByFunc(linkContext); // 删除成功, 重新刷新列表
                            }
                        } else {
                            msgError(message);
                        }

                        callback({code, message, data}, params)
                    }).catch(reason => console.error(reason))
                        .finally(() => tableContext.setLoading(false, ""));
                }));
            }, onCancel: () => null
        })
    }

    /**
     * 打开编辑框
     * @param param {Config}
     */
    this.openForEdit = function ({uid, url, data, params
                             , func, funcPath, method, config: {}, context}) {

        let linkContext = context.getLinkContext();
        let editContext = linkContext.getChildrenContext(uid || CoreConsts.DefaultEditUid);
        if(!(editContext instanceof EditContext)) {
            return console.warn(`未找到对应uid的可编辑组件[${uid || CoreConsts.DefaultEditUid}]`)
        }

        let formModel = null;
        editContext.openType = 'edit';
        this.getRequestParams({data, formModel}, params, funcPath).then((reqParam) => {
            editContext.asyncVisible(data, false).then((edit) => {
                editContext.setLoading(true, CoreConsts.FormSpinLoadingTip);
                    this.getRequestMethod({func, method})(url, reqParam)
                        .then(({code, message, data}) => {
                            if(code == CoreConsts.SuccessCode) {
                                // 先触发v-model事件
                                editContext.getFormContext().setEditModel(data)
                                // 再触发编辑事件
                                edit.$emit('edit', data);
                            } else {
                                msgError(message);
                            }
                        }).finally(() => editContext.setLoading(false, null))
                })
        })
    }

    /**
     * 编辑表格
     * @param param {Config}
     */
    this.tableForEdit = function ({uid, url, data, params
                                     , func, funcPath, method, config: {}, context}) {
        let linkContext = context.getLinkContext();
        let tableContext = linkContext.getChildrenContext(uid || CoreConsts.DefaultTableUid);
        if(!(tableContext instanceof TableContext)) {
            return console.warn(`未找到对应uid的可表格组件[${uid || CoreConsts.DefaultTableUid}]`)
        }

        tableContext.setEditRow(data);
    }

    /**
     * 新增子记录
     * 支持格式：func='add:child'
     * 设置参数：:params="{pid: 'pid'}"
     * @param param {Config}
     */
    this.openForChild = function ({uid, data, params, func, funcPath
        , config: {pid, id}, context}) {
        if(!pid) {
            return console.error(`child子功能必须在[config]指定属性pid`)
        }

        let linkContext = context.getLinkContext();
        let editContext = linkContext.getChildrenContext(CoreConsts.DefaultEditUid);
        if(!(editContext instanceof EditContext)) {
            return console.warn(`未找到对应uid的可编辑组件[${uid}]`)
        }

        // 打开编辑框并且复制pid字段
        let formModel = editContext.getFormContext().getInitModel();
        // 设置pid
        formModel[pid] = data[id || this.getRowKey()];
        this.getRequestParams({data, formModel}, params, funcPath).then(editModel => {
            editContext.asyncVisible(data, true).then(edit => {
                // 先触发v-model事件
                editContext.getFormContext().setEditModel(editModel)
                // 再触发编辑事件
                edit.$emit('edit', editModel);
            })
        })
    }

    /**
     * edit:set
     * 设置其他功能 比如：设置密码
     * @param param {Config}
     */
    this.openForSet = function ({uid, data, func, params
        , funcPath, config: {copy}, context}) {
        if(uid == CoreConsts.DefaultEditUid) {
            return console.error(`不支持set子功能编辑组件的uid[${uid}]和默认uid[${CoreConsts.DefaultEditUid}]一致`)
        }

        if(!copy) {
            copy = [];
        } else if(typeof copy == 'string') {
            copy = [copy];
        }

        let linkContext = context.getLinkContext();
        let editContext = linkContext.getChildrenContext(uid);
        if(!(editContext instanceof EditContext)) {
            return console.warn(`未找到对应uid的可编辑组件[${uid}]`)
        }

        // 批量操作
        if(data == null) {
            let tableContext = linkContext.getChildrenContext(CoreConsts.DefaultTableUid);
            data = tableContext.getSelectedRows();
            if(!data || data.length == 0) {
                return msgWarn(CoreConsts.BatchDataNullTip).then();
            }
        }

        editContext.openType = 'edit';
        let formModel = editContext.getFormContext().getInitModel();
        this.getRequestParams({data, formModel}, params, funcPath).then(editModel => {
            // 打开编辑框后复制对应的字段到新的model
            editContext.asyncVisible(data, true).then(edit => {
                // 复制属性
                copy.forEach(field => {
                    let fieldPath = field.split("=");
                    if(fieldPath.length == 1) {
                        fieldPath[1] = fieldPath[0];
                    }

                    if(data instanceof Array) {
                        editModel[fieldPath[0]] = data.map(item => item[fieldPath[1]]);
                    } else {
                        let firstField = fieldPath[0];

                        // 数组格式
                        if(firstField.endsWith("[]")) {
                            firstField = firstField.substring(0, firstField.length - 2);
                            editModel[firstField] = [data[fieldPath[1]]];
                        } else {
                            editModel[fieldPath[0]] = data[fieldPath[1]];
                        }
                        if(env.DEV && editModel[firstField] === undefined) {
                            console.warn(`要copy的字段[${fieldPath[1]}]在data里面不存在`)
                        }
                    }
                })

                // 先触发v-model事件
                editContext.getFormContext().setEditModel(editModel)
                // 再触发编辑事件
                edit.$emit('edit', editModel);
            })
        })

    }

    /**
     * 打开某个弹框(模态框或者抽屉)
     * 适用组件 UDrawer or UModal
     * @param param {Config}
     */
    this.openForVisible = function ({uid, url, method
            , funcPath, data, params, func, context}) {
        /**
         * @type {LinkContext|*}
         */
        let linkContext = context.getLinkContext()

        if(linkContext) {
            let visibleContext = linkContext.getChildrenContext(uid);
            if(visibleContext instanceof VisibleContext) {
                this.getRequestParams(data, params, funcPath).then(reqParam => {
                    if(url) {
                        visibleContext.asyncVisible(data).then((vue) => {
                            this.getRequestMethod({func, method})(url, reqParam)
                                .then(({code, message, data}) => {
                                    if(code == CoreConsts.SuccessCode) {
                                        // 再触发编辑事件
                                        vue.$emit('update:source', data);
                                    } else {
                                        msgError(message).then();
                                    }
                                }).catch(reason => console.error(reason));
                            visibleContext.childLinkContext.forEach(item => {
                                // 触发查询功能
                                item.getQueryFunc().forEach(item => item.trigger())
                            })
                        })

                    } else {
                        visibleContext.asyncVisible(data).then((vue) => {
                            let visibleContext = vue.getVisibleContext();
                            if(visibleContext instanceof VisibleContext) {
                                visibleContext.childLinkContext.forEach(item => {
                                    // 触发查询功能
                                    item.getQueryFunc().forEach(item => item.trigger())
                                })
                            }
                        })
                    }
                })
            } else {
                console.warn(`不存在[uid=${uid}]的UDrawer或者UModal组件`)
            }
        }

    }

    /**
     * 打开一个编辑框并且从指定url获取数据
     * func = edit:loading:eid:/user/detail
     * @param param {Config}
     */
    this.openForLoading = function ({url, uid, func, funcPath
        , data, context, params, method, config: {}}) {
        if(uid == CoreConsts.DefaultEditUid) {
            return console.error(`不支持loading子功能操作的编辑组件的uid[${uid}]和默认uid[${CoreConsts.DefaultEditUid}]一致`)
        }

        if(typeof url != 'string') {
            return console.error(`loading子功能必须指定url`)
        }

        let linkContext = context.getLinkContext();
        let editContext = linkContext.getChildrenContext(uid);
        if(editContext == null) {
            return console.error(`未找到对应uid的可编辑组件[${linkContext.uid}:${uid}]`)
        }

        editContext.openType = 'edit';
        // 打开编辑框后复制对应的字段到新的model
        editContext.asyncVisible(data, true).then(edit => {
            editContext.setLoading(true, CoreConsts.FormSpinLoadingTip);
            method = method || "GET";
            this.getRequestParams(data, params, funcPath).then(params => {
                this.getRequestMethod({func, method})(url, params)
                    .then(({code, message, data}) => {
                        if(code == CoreConsts.SuccessCode) {
                            edit.$emit('edit', data);
                            editContext.getFormContext().setEditModel(data);
                        } else {
                            msgError(message).then(() => {});
                        }
                    }).finally(() => editContext.setLoading(false, null))
            })
        })
    }

    /**
     * 更具查询功能点查询
     * @param linkContext {LinkContext}
     */
    this.queryByFunc = function (linkContext) {
        if(linkContext != null) {
            let queryFunc = linkContext.getQueryFunc();
            if(queryFunc != null) {
                queryFunc.forEach(item => item.trigger())
            } else {
                console.warn(`没有找到搜索功能在组件[${linkContext.uid}]`)
            }
        }
    }

    /**
     * 查询主表格数据
     * @param config {Config}配置信息
     */
    this.query = function ({uid, url, context, reload, method
                               , func, funcPath, params, callback}) {
        let linkContext = context.getLinkContext();

        let model = {};
        let searchContext = linkContext.getChildrenContext(CoreConsts.DefaultSearchUid);
        if(searchContext instanceof SearchContext) {
            model = searchContext.getModel();
        } else if(context instanceof ViewVoidContext){
            //
        }

        let tableContext = linkContext.getChildrenContext(CoreConsts.DefaultTableUid);
        if(!(tableContext instanceof TableContext)) {
            return console.warn(`未找到对应uid的表组件[${linkContext.uid}]`)
        }

        if(tableContext.pageSize && tableContext.currentPage) {
            // 完全相等重置为第一页
            if(model[CoreConsts.PageSize] == tableContext.pageSize &&
                model[CoreConsts.PageCurrent] == tableContext.currentPage) {
                tableContext.resetPagination(null, null);
            }

            model[CoreConsts.PageSize] = tableContext.pageSize;
            model[CoreConsts.PageCurrent] = tableContext.currentPage;
        }

        function reload(status, tip) {
            if(reload) {
                tableContext.setLoading(status, tip);
            }
        }
        this.getRequestParams(model, params, funcPath).then(params => {
            reload(true, CoreConsts.TableSpinLoadingTip);
            this.getRequestMethod({func, method})(url, params).then(({code, message, data}) => {
                if(code == CoreConsts.SuccessCode) {
                    if(data instanceof Array){
                        tableContext.setDataSource(data)
                    } else if(data instanceof Object) { // 需要分页
                        let {records, total} = data;

                        tableContext.setTotalRows(total);
                        tableContext.setDataSource(records)
                    }
                } else {
                    msgError(message)
                }

                callback({code, message, data});
            }).finally(() =>  reload(false, null))
        });
    }

    /**
     * 获取详情(暂时未实现此功能)
     * @param config {Config}
     */
    this.detail = function (config) {

    }

    /**
     * 隐藏主编辑框
     * @param config {Config}
     */
    this.cancel = function ({uid, context}) {
        /**
         * @type LinkContext
         */
        let linkContext = context.getLinkContext();
        let editContext = context;
        if(uid) {
            editContext = linkContext.getChildrenContext(uid);
        }

        if(!(context instanceof EditContext)) {
            return console.error(`cancel功能只能放在编辑组件下面`)
        }

        // 关闭编辑框
        editContext.setVisible(false);
        editContext.resetStatus();
    }
    /**
     * 隐藏主编辑框
     * @param config {Config}
     */
    this.tableForCancel = function ({uid, context}) {
        /**
         * @type LinkContext
         */
        let linkContext = context.getLinkContext();
        let tableContext = linkContext.getChildrenContext(uid) || context;
        if(!(tableContext instanceof TableContext)) {
            return console.error(`cancel功能只能放在表格组件下面`)
        }

        tableContext.cancelEditableRow();
    }
    /**
     * 展开树形的表格
     * @param config {Config}
     * @param expandedRowKeys 要展开的行的key列表 不指定则展开所有
     */
    this.expanded = function ({uid, context}, expandedRowKeys) {
        let linkContext = context.getLinkContext();
        let tableContext = linkContext.getChildrenContext(CoreConsts.DefaultTableUid);
        if(tableContext == null) {
            return console.warn(`未找到对应uid的表组件[${linkContext.uid}:${CoreConsts.DefaultTableUid}]`)
        }

        tableContext.expanded(expandedRowKeys);
    }
    /**
     * 提交主编辑表单
     * @param config {Config}
     * @return void
     */
    this.submit = function ({uid, url, callback, funcPath
        , func, params, method, config: {}, funcConfig, context}) {
        let linkContext = context.getLinkContext();

        let editContext = context;
        if(!(editContext instanceof EditContext)) {
            return console.error(`submit功能只能放在编辑组件下面`)
        }

        editContext.getFormContext().validate().then(() => {
            let model = editContext.getModel();

            function setLoading(status, tip) {
                funcConfig.setLoading(status);
                editContext.setLoading(status, tip)
            }

            this.getRequestParams(model, params, funcPath).then(params => {
                setLoading(true, CoreConsts.FormSpinSubmitTip);
                this.getRequestMethod({method, func})(url, params).then(({code, message, data}) => {
                    let promise = callback({code, message, data, editContext, query: () => this.queryByFunc(linkContext)});
                    if(promise instanceof Promise) {
                        promise.then(() => {
                            if (code == CoreConsts.SuccessCode) {
                                msgSuccess(CoreConsts.SubmitSuccessMsg);
                                editContext.setVisible(false);
                                this.queryByFunc(linkContext);
                            } else {
                                msgError(message);
                            }
                        }).catch(reason => null);
                    } else {
                        console.error("callback必须返回Promise对象");
                    }
                }).catch(reason => msgError(reason)).finally(() => setLoading(false))
            }).catch(reason => reason ? msgWarn(reason) : null)
        })
    }

    /**
     * 表格编辑的提交
     * @param config {Config}
     */
    this.tableForSubmit = function ({uid, url, callback, funcPath, data
                                        , func, params, method, config: {}, funcConfig, context}) {
        let linkContext = context.getLinkContext();
        context.submitEditableRow().then((editModel) => {
            this.getRequestParams(editModel, params, funcPath).then(params => {
                context.setLoading(true, CoreConsts.FormSpinSubmitTip)
                this.getRequestMethod({method, func})(url, params).then(({code, message, data}) => {
                    let promise = callback({code, message, data, query: () => this.queryByFunc(linkContext)});
                    if(promise instanceof Promise) {
                        promise.then(() => {
                            if (code == CoreConsts.SuccessCode) {
                                context.setEditRow(null);
                                msgSuccess(CoreConsts.SubmitSuccessMsg).then();
                                this.queryByFunc(linkContext);
                            } else {
                                msgError(message).then();
                            }
                        }).catch(reason => null)
                    } else {
                        console.error("callback必须返回Promise对象");
                    }
                }).catch(reason => msgError(reason))
            }).catch(reason => reason ? msgWarn(reason) : null)
                .finally(() => context.setLoading(false, null))
        })
    }

    /**
     * 重置主编辑表单
     * @param config {Config}
     */
    this.resetEditModel = function ({uid, func, funcPath, context, params}) {
        /**
         * @type LinkContext
         */
        let linkContext = context.getLinkContext();

        let editContext = context;
        if(uid) { // 获取指定的操作组件
            editContext = linkContext.getChildrenContext(uid);
        }

        if(!(editContext instanceof EditContext)) {
            return console.error(`reset功能只能放在编辑组件下面`)
        }

        editContext.resetStatus();
        let editModel = editContext.getFormContext().getEditModel();
        // 编辑时需要重新获取详情
        if(this.isEdit(editModel)) {
            let editFunc = linkContext.getFunc(FuncNameMeta.EDIT, context.uid);
            let method = editFunc.getMethod();
            this.getRequestParams(editModel, params, funcPath).then(params => {
                editContext.setLoading(true, CoreConsts.FormSpinResetTip);
                this.getRequestMethod({method, func: FuncNameMeta.EDIT})(editFunc.getUrl(), params)
                    .then(({code, message, data}) => {
                        if(code == CoreConsts.SuccessCode) {
                            editContext.getFormContext().setEditModel(data);
                        } else {
                            msgError(message);
                        }
                    }).finally(() => editContext.setLoading(false, null))
            })

        } else { // 新增的重置只需要重置字段
            editContext.getFormContext().resetFields();
        }
    }

    /**
     * 重置搜索表单
     * @param config {Config}
     */
    this.resetSearchModel = function ({uid, context, reload}) {
        let linkContext = context.getLinkContext();

        let searchContext = context;
        if(uid) {
            searchContext = linkContext.getChildrenContext(uid);
        }

        if(!(searchContext instanceof SearchContext)) {
            return console.error(`reset功能只能放在搜索组件下面`)
        }

        searchContext.getFormContext().resetFields();
        if(reload) { // 如果需要重新刷新列表
            let tableContext = linkContext.getChildrenContext(CoreConsts.DefaultTableUid);
            if(tableContext instanceof TableContext) {
                tableContext.resetPagination(null, null);
            }
            this.queryByFunc(linkContext);
        }
    }

    /**
     * excel导出
     * @param config {Config}
     */
    this.excelExport = function ({uid, url, func, funcPath, params
         , config: {fileName}, method, context}) {
        let linkContext = context.getLinkContext();
        let searchContext = linkContext.getChildrenContext(uid || CoreConsts.DefaultSearchUid);
        if(searchContext == null) {
            return console.warn(`未找到对应uid的可搜索组件[${linkContext.uid}]`)
        }

        let model = searchContext.getModel();
        this.getRequestParams(model, params, funcPath).then(params => {
            let messageType = msgLoading(CoreConsts.ExcelDownloadTip, 0);
            let config = {responseType: 'blob'}; config[CoreConsts.CancelRespResolver] = true;
            this.getRequestMethod({func, method})(url, params, config).then(resp => {
                let {data, headers} = resp;
                if(headers['content-type']) {
                    if(headers['content-type'].indexOf('application/json') > -1 ) {
                        if(data instanceof Object) {
                            SysUtils.readBlobToJson(data, ({message}) => {
                                msgError(message).then();
                            });

                            return messageType();
                        }
                    }
                }

                let header = resp.headers["content-disposition"];
                fileName = fileName || (header != null ? decodeURI(header.split("filename=")[1]) : dayjs().format("YYYY-MM-DD") + ".xlsx");
                SysUtils.downloadFile(resp.data, "application/vnd.ms-excel;charset=utf-8", fileName)
            }).catch(reason => console.error(reason)).finally(messageType)
        })

    }

    /**
     * excel excel导入
     * @param config {Config}
     */
    this.excelImport = function ({$vue, uid, data, url, method, func, funcPath, context, params, config}) {
        let linkContext = context.getLinkContext();
        // let searchContext = linkContext.getChildrenContext(uid || CoreConsts.DefaultSearchUid);
        let fileList;
        let uploadConfig = {data, accept: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel'
            , beforeUpload: (file) => {
                fileList = file;
                return Promise.reject();
            }, onReject: (file) => {
                msgWarn("文件格式错误(支持.xls, .xlsx)").then();
            }, onRemove: (file) => {
                fileList = null;
            }, templateUrl: ''}
        uploadConfig = Object.assign(uploadConfig, config['uploadConfig']);
        let assign = Object.assign({title: '', okText: "导入", icon: null, maskClosable: true, onOk: () => {
                return new Promise((resolve, reject) => {
                    if(fileList) {
                        this.getRequestParams(data, params, funcPath).then(reqParams => {
                            const formData = new FormData();
                            formData.append("file", fileList);
                            if(reqParams) {
                                formData.append("data", new Blob([JSON.stringify(reqParams)], {type: 'application/json'}))
                            }

                            this.getRequestMethod({method, func})(url, formData, {headers: {'content-type': 'multipart/form-data'}}).then(({code, message, data}) => {
                                if(code == CoreConsts.SuccessCode) {
                                    resolve();
                                } else {
                                    msgError(message).then(() => {
                                        reject(message);
                                    });
                                }
                            }).catch(reason => {
                                msgError("请求失败").then(reject);
                            })
                        })
                    } else {
                        reject("请选择文件");
                        msgError("请选择文件").then();
                    }
                })
            }}, config);

        if(!assign.content) {
            uploadConfig['maxCount'] = 1;
            assign.content = createVNode(ImportUpload, uploadConfig);
        }

        $vue['modalRef'] = Modal.info(assign);
    }

    /**
     * 下载
     * @param config {Config}
     */
    this.download = function ({url, funcPath, data, params, func, method
          , context, uid, config: {fileName, contentType}}) {
        if(funcPath[1] == CoreConsts.ChildFuncNameMeta.BATCH) { // 从列表里面获取数据
            let linkContext = context.getLinkContext();
            uid = uid || CoreConsts.DefaultTableUid
            let tableContext = linkContext.getChildrenContext(uid);
            if(tableContext instanceof TableContext) {
                data = tableContext.getSelectedRows();
            }
        }

        this.getRequestParams(data, params, funcPath).then(params => {
            let messageType = msgLoading(CoreConsts.FileDownloadTip, 0);
            let config = {responseType: 'blob'}; config[CoreConsts.CancelRespResolver] = true;
            this.getRequestMethod({method, func})(url, params, config).then(resp => {
                let {data, headers} = resp;
                if(headers['content-type']) {
                    if(headers['content-type'].indexOf('application/json') > -1 ) {
                        if(data instanceof Object) {
                            SysUtils.readBlobToJson(data, ({message}) => {
                                msgError(message).then();
                            })
                            return messageType();
                        }
                    }
                }

                let header = headers["content-disposition"];
                contentType = contentType || headers['content-type'] || 'application/octet-stream';
                fileName = fileName || (header ? decodeURI(header.split("filename=")[1]) : console.warn("未指定文件名"));
                SysUtils.downloadFile(data, contentType, fileName)
            }).finally(messageType)
        });

    }

    /**
     * 其他功能的执行操作
     * 如果需要确认请用：func='confirm'
     * @param config {Config}
     */
    this.otherFuncAjax = function ({func, data, url, reload, params, context
               , uid, funcPath, method, config: {confirmTitle, confirmContent}, callback}) {

        let linkContext = context.getLinkContext();

        function reload(status, tip) {
            if(reload) {
                let tableContext = linkContext.getChildrenContext(CoreConsts.DefaultTableUid);
                if(tableContext instanceof TableContext) {
                    tableContext.setLoading(status, tip)
                }
            }
        }

        function request() {
            this.getRequestParams(data, params, funcPath).then(params => {
                reload(true, CoreConsts.AjaxExecTipping);
                this.getRequestMethod({func, method})(url, params).then(({code, message, data}) => {
                    if (code == CoreConsts.SuccessCode) {
                        msgSuccess(message || CoreConsts.DefaultExecSuccess);
                        if (reload) {
                            this.queryByFunc(linkContext); // 重新刷新列表
                        }
                    } else {
                        msgError(message);
                    }

                    callback({code, message, data}, params)
                }).catch(reason => console.error(reason)).finally(() => reload(false, null));
            });
        }

        if(funcPath[1] && funcPath[1].toUpperCase() == CoreConsts.ChildFuncNameMeta.CONFIRM) {
            let title = confirmTitle || CoreConsts.DefaultConfirmTitle;
            let content = confirmContent || CoreConsts.DefaultConfirmTitle;
            confirm({title, content, onOk: () => request.call(this) , onCancel: () => null})
        } else {
            request.call(this);
        }
    }

    /**
     * 其他功能的执行操作
     * @param config {Config}
     */
    this.linkTo = function ({url, data, router}) {
        router.isReady().then(() => {
            router.push({ path: url, query: data || {}}).catch(reason => console.error(reason))
        });
    }

    /**
     * @param config {{method: (Config.method|String), func: (Config.func|String)}}
     * @return {Promise<AxiosResponse<any>>|*}
     */
    this.getRequestMethod = function ({func, method}) {
        let request = TypeMethodMaps[method];
        return request || (() => Promise.reject(`功能[${func}]查找不到请求方法[${method}]`));
    }

    /**
     * 通过前缀获取搜索上下文
     * @param uid
     * @param prefix ULinkView uid
     * @return {null | SearchContext}
     */
    this.getSearchContextByUid = function (prefix, uid) {
        let context = this.getViewContext().getContextByPrefixAndUid(prefix, uid);
        if(context == null) {
            return context;
        } else if(!(context instanceof SearchContext)) {
            console.error(`此uid[${uid}]不是搜索组件`)
            return null;
        } else {
            return context;
        }
    }

    /**
     * @param uid
     * @return {null|TableContext}
     */
    this.getTableContextByUid = function (uid) {
        let context = this.getViewContext().getContextByUid(uid);
        if(context == null) {
            return context;
        } else if(!(context instanceof TableContext)) {
            console.error(`此uid[${uid}]不是表组件`)
            return null;
        } else {
            return context;
        }
    }

    /**
     * 通过前缀获取编辑上下文
     * @param uid
     * @return {EditContext}
     */
    this.getEditContextByUid = function (uid) {
        return this.getViewContext().getContextByUid(uid);
    }
}

/**
 * 功能元数据对象
 * @constructor
 */
export function FuncMetaContext(editFunMetas, tableFunMetas, searchFunMetas) {

    this.queryFunc = [];
    this.editFunMetas = editFunMetas || [];
    this.tableFunMetas = tableFunMetas || [];
    this.searchFunMetas = searchFunMetas || [];

    this.getEditMeta = function (field) {
        return this.editFunMetas.find(item => item.field == field)
    }

    this.getTableMeta = function (field) {
        return this.tableFunMetas.find(item => item.field == field)
    }

    this.getSearchMeta = function (field) {
        return this.searchFunMetas.find(item => item.field == field)
    }
}

/**
 * 搜索栏
 * @param linkContext {LinkContext}
 * @constructor
 */
export function SearchContext(linkContext) {
    // 用于关联各个组件(表格、编辑、详情)
    this.uid = '';
    // 存储UFuncBtn和UFuncTag组件的信息
    this.funcMetas = {};

    /**
     * @param func
     * @return {FuncConfig | null}
     */
    this.getFunc = function (func) {
        func = func.toUpperCase();
        let funcMeta = this.funcMetas[func];

        // 遗留问题, query和view都代表查询
        let isQuery = func == 'QUERY' || func == 'VIEW';
        if(isQuery) {
            funcMeta = this.funcMetas['QUERY'] || this.funcMetas['VIEW'];
        }

        return funcMeta;
    }

    /**
     * @param config {FuncConfig}
     */
    this.register = function (config) {
        this.funcMetas[func] = config;
    }

    this.getModel = function () {
        return this.getFormContext().getEditModel();
    }

    /**
     * 重置当前表单
     */
    this.reset = function () {
        this.getFormContext().resetFields();
    }
    /**
     * @return {FormContext}
     */
    this.getFormContext = () => new FormContext();
    /**
     * @return {LinkContext}
     */
    this.getLinkContext = () => linkContext;

    this.get$View = function () {
        // viewContext['__$View'] 不可能为空, 为空说明异常
        return this.getLinkContext().getViewContext()['__$View'] || new $View(null);
    }

}


/**
 * 编辑
 * @param linkContext {LinkContext} 属于哪个容器
 * @constructor
 */
export function EditContext(linkContext) {
    // 用于关联各个组件(搜索、表格、详情)
    this.uid = null;
    // 当前打开类型(add or edit)
    this.openType = null;
    // 存储UFuncBtn和UFuncTag组件的信息
    this.funcMetas = {};

    // 获取功能组件配置
    this.getFunc = function (func) {
        return this.funcMetas[func.toUpperCase()];
    }

    /**
     * @param config {FuncConfig}
     */
    this.register = function (config) {
        this.funcMetas[func] = config;
    }

    this.getModel = function () {
        return this.getFormContext().getEditModel();
    }

    this.setModel = function (model) {
        this.getFormContext().setEditModel(model);
    }

    /**
     * 重置当前表单字段
     */
    this.reset = function () {
        this.getFormContext().resetFields();
    }

    /**
     * 重置当前组件各个状态
     */
    this.resetStatus = function () {
        // 重置提交按钮的加载状态
        let submit = this.getLinkContext()
            .getFunc(FuncNameMeta.SUBMIT, this.uid); // 获取当前编辑组件下的提交按钮

        if(submit) {
            submit.setLoading(false);
        }

        // 重置编辑框的加载状态
        this.setLoading(false)
        // 移除表单的所有的校验
        this.getFormContext().clearValidate();
    }

    /**
     * 关闭当前弹框
     */
    this.cancel = function () {
        this.setVisible(false);
    }

    // 修改加载状态
    this.setLoading = (status, tip) => {Unmount()};
    // 修改弹框状态(模态框或者抽屉框)
    this.setVisible = (status) => {Unmount()};
    // 异步打开弹框(模态框或者抽屉框) 等表单初始化完成
    this.asyncVisible = (row, isResetToInit) => Promise.reject("未挂载");

    /**
     * @return {FormContext | null}
     */
    this.getFormContext = Unmount;
    /**
     * @return {LinkContext}
     */
    this.getLinkContext = () => linkContext;
    /**
     * @returns {$View}
     */
    this.get$View = function () {
        // viewContext['__$View'] 不可能为空, 为空说明异常
        return this.getLinkContext().getViewContext()['__$View'] || new $View(null);
    }
}

/**
 * 表格组件{UTable | UEditTable}上下文对象
 * @param linkContext {LinkContext} ULinkView组件上下文对象
 * @constructor
 */
export function TableContext(linkContext) {
    // 用于关联各个组件(搜索、编辑、详情)
    this.uid = null;

    // 在使用UFuncTag#data组件时, 将可以获取该值
    this.CurrentRow = null;

    this.reset = true; // 重置分页
    this.pageSize = null;
    this.currentPage = null;
    // 存储UBtn和UTag组件的信息
    this.funcMetas = {};

    // 是否是可编辑表格
    this.editable = false;

    // 获取在编辑的行
    this.getEditRow = () => null;
    // 设置当前正在编辑的行
    this.setEditRow = (row) => null;

    // 新增编辑行
    this.addEditableRow = () => null;
    // 删除编辑行
    this.delEditableRow = () => false;
    // 取消编辑行
    this.cancelEditableRow = () => null
    // 保存编辑行
    this.submitEditableRow = () => Promise.resolve();

    this.getRowKey = () => null;

    // 获取功能组件配置
    this.getFunc = function (func) {
        return this.funcMetas[func.toUpperCase()];
    }

    /**
     * @return {Array | null}
     */
    this.getColumns = () => null;

    /**
     * @param config {FuncConfig}
     */
    this.register = function (config) {
        this.funcMetas[func] = config;
    }

    this.del = function (url, data) {}

    /**
     * 展开行
     * @param expandedRowKeys 要展开的行key, 不传则展开所有行
     */
    this.expanded = (expandedRowKeys) => {};

    /**
     * 重置当前页
     */
    this.resetPagination = (page, size) => {};

    /**
     * 获取当前页码
     */
    this.getCurrentPage = function () {
        return this.currentPage;
    }

    /**
     * 获取每页条数
     */
    this.getPageSize = function () {
        return this.pageSize;
    }

    /**
     * 获取当前点击的行
     * 使用此方法必须在表格的操作栏里面使用以下组件作为功能按钮
     * @see UTag 必须设置data属性
     */
    this.getCurrentRow = function () {
        return this.CurrentRow || unUFuncTag();
    }

    /**
     * 页码改变
     * @param current 当前页
     * @param pageSize 每页条数
     */
    this.pageChange = function (current, pageSize) {
        this.pageSize = pageSize;
        this.currentPage = current;

        linkContext.getQueryFunc().forEach(item => {
            item.trigger();
        })
    }

    /**
     * 每页条数改变事件
     * @param current 当前页
     * @param pageSize 每页条数
     */
    this.sizeChange = function (current, pageSize) {
        this.pageSize = pageSize;
        this.currentPage = current;

        linkContext.getQueryFunc().forEach(item => {
            item.trigger();
        })
    }

    /**
     * 获取当前选中的行key列表
     * @returns {*[]}
     */
    this.getSelectedRowKeys = () => [];

    /**
     * 获取当前选中的行列表
     * @returns {*[]}
     */
    this.getSelectedRows = () => [];

    /**
     * @return {LinkContext}
     */
    this.getLinkContext = () => linkContext;

    /**
     * 获取当前页面视图
     * @returns {$View}
     */
    this.get$View = function () {
        // viewContext['__$View'] 不可能为空, 为空说明异常
        return this.getLinkContext().getViewContext()['__$View'] || new $View(null);
    }

    /**
     * 粘表头
     * @param status {Boolean}
     */
    this.setSticky = (status) => {}

    /**
     * 设置表格的加载状态
     * @param tip {String}
     * @param status {Boolean}
     */
    this.setLoading = (status, tip) => {};

    /**
     * 设置数据源
     * @param dataSource {Boolean}
     */
    this.setDataSource = (dataSource) => {};
}

/**
 * 详情
 * @param linkContext {LinkContext}
 * @constructor
 */
export function DetailContext(linkContext) {
    // 用于关联各个组件(搜索、编辑、表格)
    this.uid = '';

    // 存储UFuncBtn和UFuncTag组件的信息
    this.funcMetas = {};

    // 获取功能组件配置
    this.getFunc = function (func) {
        return this.funcMetas[func.toUpperCase()];
    }
    this.regFunc = function (func, config) {
        this.funcMetas[func] = config;
        linkContext.registerFunc(config);
    }
    /**
     * @return {LinkContext}
     */
    this.getLinkContext = () => linkContext;

    this.get$View = function () {
        // viewContext['__$View'] 不可能为空, 为空说明异常
        return linkContext.getViewContext()['__$View'] || new $View(null);
    }
}

/**
 * 空对象上下文, 有View组件提供
 * @param linkContext {LinkContext} ULinkView组件上下文对象
 * @constructor
 */
export function ViewVoidContext(linkContext) {

    /**
     * @return {LinkContext}
     */
    this.getLinkContext = () => linkContext;

    /**
     * @returns {$View}
     */
    this.get$View = function () {
        // viewContext['__$View'] 不可能为空, 为空说明异常
        return this.getLinkContext().getViewContext()['__$View'] || new $View(null);
    }
}

/**
 * 视图上下文
 * @constructor
 */
export function ViewContext(props) {

    /**
     * @type {{String: EditContext | DetailContext | SearchContext | TableContext}}
     */
    this.uidContextMaps = {} // 声明fullUid的上下文对象
    /**
     * 组合容器组件上下文对象列表
     * @type {{'prefix:uid': LinkContext}}
     */
    this.linkContextMaps = {}

    this.funMetasContext = new FuncMetaContext();

    /**
     * 获取指定功能
     * @param func {String}
     */
    this.getFunc = function (func) {
        let values = Object.values(this.uidContextMaps);
        for (let item of values) {
            let funcValue = item.getFunc(func);
            if(funcValue != null) {
                return funcValue;
            }
        }

        return null;
    }

    /**
     * 功能点权限校验
     * @return {{default: boolean, type: BooleanConstructor}}
     */
    this.isAuth = function () {
        return props.urlAuth;
    }

    /**
     * 获取视图名称
     * @return {String}
     */
    this.getName = function () {
        return props.name;
    }

    /**
     * 返回功能的唯一key
     * @return {String}
     */
    this.getRowKey = function () {
        return props.rowKey;
    }

    /**
     * 获取元数的id获取对应上下文
     * @param fullUid ${prefix}:${uid}
     * @return {EditContext | TableContext | SearchContext | DetailContext}
     */
    this.getContextByUid = function (fullUid) {
        return this.uidContextMaps[fullUid];
    }

    /**
     *
     * @param prefix ULinkView组件uid
     * @param uid 其他组件uid
     * @return {EditContext|TableContext|SearchContext|DetailContext}
     */
    this.getContextByPrefixAndUid = function (prefix, uid) {
        return this.getContextByUid(prefix + ":" + uid);
    }

    /**
     * 增加上下文
     * @param fullUid = prefix:uid
     * @param context {EditContext|TableContext|SearchContext|DetailContext}
     */
    this.addContextByUid = function (fullUid, context) {
        if(fullUid && context) {
            if(!this.uidContextMaps[fullUid]) {
                this.uidContextMaps[fullUid] = context;
            } else {
                console.warn(`已经存在同名的组件[${fullUid}]`)
            }
        } else {
            console.error(`新增Context失败, 错误的参数[fullUid or context]`)
        }
    }

    /**
     * @param uid {String}
     * @param context {LinkContext}
     */
    this.addLinkContextByUid = function (uid, context) {
        if(!(context instanceof LinkContext)) {
            throw new Error(`参数错误[context]`)
        }

        if(this.linkContextMaps[uid]) {
            throw new Error(`uid为[${uid}]的ULinkView组件已经存在`)
        }

        this.linkContextMaps[uid] = context;
    }

    /**
     * @param uid ULinkView#uid
     * @return LinkContext
     */
    this.getLinkContextByUid = function (uid) {
        return this.linkContextMaps[uid];
    }
}

export function EventContext() {

    /**
     *
     * @type {*[Function]}
     */
    this.listeners = [];

    /**
     * 注册监听器
     * @param callback
     */
    this.register = function (callback) {
        this.listeners.push(callback);
    }

    /**
     * 执行监听
     * @param args
     */
    this.notify = function (args) {
        this.listeners.forEach(item => item(args))
    }
}

/**
 * 联动容器上下文
 * @param uid {String}
 * @param parent {LinkContext}
 * @param viewContext {ViewContext}
 * @constructor
 */
export function LinkContext(uid, viewContext, parent) {
    /**
     * 容器标识前缀, 此uid将作为子组件的前缀
     * @type {String}
     */
    this.uid = uid;

    /**
     * 父上下文
     * @type {LinkContext | ViewContext}
     */
    this.parent = parent;

    /**
     *  子组件的上下文对象
     * @type {{String: EditContext | TableContext | DetailContext | SearchContext}}
     */
    this.childContextMaps = {};

    /**
     * @type {{String: EventContext}}
     */
    this.events = {};

    /**
     * 监听事件
     * @param event
     * @param callback
     */
    this.on = (event, callback) => {
        if(!event || !callback) {
            throw new Error("监听事件失败[event or callback不能为null]");
        }

        /**
         * @type EventContext
         */
        let context = this.events[event];
        if(!(context instanceof EventContext)) {
            this.events[event] = context = new EventContext();
        }

        context.register(callback);
    }

    /**
     * 触发事件
     * @param event
     * @param args
     */
    this.emit = (event, args) => {
        /**
         * @type EventContext
         */
        let context = this.events[event];
        if(context instanceof EventContext) {
            context.notify(args);
        } else {
            console.warn(`没有创建对应的监听器[${event}]`)
        }
    }

    /**
     * @type {Array<FuncConfig>}
     */
    this.queryFuncs = [];

    /**
     * @type {reactive{string: FuncConfig}}
     */
    this.funcs = reactive({});

    /**
     * 注册功能
     * @param config {FuncConfig}
     */
    this.registerFunc = function (config) {
        let func = config.getFunc();
        let context = config.getContext();
        let toUid = config.getProp("uid")
            || CoreConsts.FuncOperationUid[func];

        // 说明要操作的是功能所在的组件
        if(toUid == CoreConsts.SelfComponentConst) {
            toUid = context.uid;
        }

        if(func == FuncNameMeta.QUERY) {
            this.queryFuncs.push(config);
        }

        // 功能按钮所在的组件的uid + func + 要操作组件的uid
        let key = context.uid+":"+func + `:${toUid}`;
        if(!this.funcs[key]) {
            this.funcs[key] = config;
        }
    }

    /**
     * @param func 功能点
     * @param toUid 要操作的uid
     * @return {FuncConfig}
     */
    this.getFunc = function (func, toUid) {
        return Object.values(this.funcs).find(item => {
            return func == item.getFunc() && item.getUid() == toUid;
        });
    }

    /**
     * @param func
     * @return {FuncConfig[]}
     */
    this.getFuncs = function (func) {
        return Object.values(this.funcs).filter(item => {
            return func == item.getFunc();
        });
    }

    /**
     * @return {Array<FuncConfig>}
     */
    this.getQueryFunc = function () {
        return this.queryFuncs;
    }

    /**
     * 获取子组件的上下文对象
     * @param uid 子组件的uid
     * @return {EditContext | SearchContext | DetailContext | TableContext | VisibleContext}
     */
    this.getChildrenContext = function (uid) {
        return this.childContextMaps[this.uid+":"+uid];
    }

    /**
     * 增加子组件上下文对象
     * @param childContext {EditContext | SearchContext | DetailContext | TableContext | VisibleContext}
     */
    this.addChildrenContext = function (childContext) {
        if(!childContext.uid) {
            throw new Error(`组件不存在uid属性`)
        }

        let fullUid = this.uid + ":" + childContext.uid;
        if(!this.childContextMaps[fullUid]) {
            this.childContextMaps[fullUid] = childContext;
            this.getViewContext().addContextByUid(fullUid, childContext);
        } else {
            console.warn(`已经存在同名的组件[${fullUid}]`)
        }
    }

    /**
     * @return {ViewContext}
     */
    this.getViewContext = () => viewContext;

    viewContext.addLinkContextByUid(uid, this);
}
export function FuncConfig(props, context, funcPath) {
    /**
     * @type {Function}
     * @return {String}
     */
    this.getUrl = () => props.url;

    /**
     * 返回功能名称
     * @return {String}
     */
    this.getFunc = () => funcPath[0]

    /**
     * 返回要操作的uid
     * @return String
     */
    this.getUid = () => {
        if(!props['uid']) {
            let uid = CoreConsts.FuncOperationUid[this.getFunc()];
            //
            if(CoreConsts.SelfComponentConst == uid) {
                uid = this.getContext().uid;
            }

            return uid;
        } else {
            return props['uid'];
        }
    }

    /**
     * 返回子功能名称
     * @return {String}
     */
    this.getChildFunc = () => funcPath[1]

    /**
     * @param key
     * @return {Object}
     */
    this.getProp = (key) => props[key]

    /**
     * @return {string}
     */
    this.getMethod = () => props.method ? props.method.toUpperCase() : CoreConsts.FuncMethodMaps[funcPath[0]];
    /**
     * @return {EditContext | DetailContext | SearchContext | TableContext}
     */
    this.getContext = () => context;
    /**
     * 触发此功能
     */
    this.trigger = () => null

    /**
     * @param status {Boolean}
     */
    this.setLoading = status => null;
}
export function ChildConfig() {

    /**
     * @type {String | Array}
     */
    this.copy = null;

    /**
     * @type {String | Number | *}
     */
    this.pid = null;

    /**
     * 确认标题
     * @type {String}
     */
    this.confirmTitle = null;

    /**
     * 确认内容
     * @type {String}
     */
    this.confirmContent = null;
}

/**
 * @param vue vue实例uid
 * @constructor
 */
export function Config(vue) {

    /**
     * @type {Vue}
     */
    this.$vue = vue;

    /**
     * @type {Router}
     */
    this.router = null

    /**
     * @see LinkContext#uid
     * @type {String}
     */
    this.uid = null;
    /**
     * @see LinkContext#uid
     * @type {SearchContext | DetailContext | EditContext | TableContext}
     */
    this.context = null;
    /**
     * 功能地址
     * @type {String}
     */
    this.url = null;

    /**
     * 请求的参数
     * @type {Function | Object}
     * @param data
     * @return {Promise}
     */
    this.params = null;

    /**
     * 功能参数
     * @type {ChildConfig}
     */
    this.config = null;

    /**
     * 要操作的主功能
     * @type {String}
     */
    this.func = null;

    /**
     * 重新加载列表
     * @type {boolean}
     */
    this.reload = false

    /**
     * 功能路径
     * @type {*[]}
     */
    this.funcPath = null;

    /**
     * 要操作的数据
     * @type {Object | Array}
     */
    this.data = null;

    /**
     * ajax请求方法 get、post、...
     * @see CoreConsts#FuncMethodMaps
     * @type {String}
     */
    this.method = null;

    /**
     * 回调
     * @type {Function}
     * @return {Promise}
     */
    this.callback = null;

    /**
     * @type {FuncConfig}
     */
    this.funcConfig = null;

    /**
     *
     * @param uid
     * @param func
     * @param url
     * @param config
     * @param method
     * @param data
     * @param params
     * @param context {DetailContext | EditContext | TableContext | SearchContext | VisibleContext}
     * @param router {Router}
     * @param funcConfig {FuncConfig}
     * @return {Config}
     */
    this.build = function ({uid, func, url, config, method, data, params, reload, callback}, context, router, funcConfig) {
        this.url = url;
        this.uid = uid;
        this.data = data;
        this.router = router;
        this.config = config;
        this.reload = reload;
        this.params = params;
        this.context = context;
        this.funcConfig = funcConfig;
        this.callback = callback || VoidResolveCall
        this.funcPath = func.split(":")
            .map(item => item.toUpperCase());
        this.func = this.funcPath[0];
        this.method = method || CoreConsts.FuncMethodMaps[this.func];
        return this;
    }
}

/**
 * 普通类型的模态框或者抽屉上下文
 * @param linkContext
 * @constructor
 */
export function VisibleContext(linkContext) {
    // 用于关联各个组件(搜索、表格、详情)
    this.uid = null;

    // 存储UFuncBtn和UFuncTag组件的信息
    this.funcMetas = {};

    /**
     * @type {Array<LinkContext>}
     */
    this.childLinkContext = [];

    // 获取功能组件配置
    this.getFunc = function (func) {
        return this.funcMetas[func.toUpperCase()];
    }

    this.regFunc = function (func, config) {
        this.funcMetas[func] = config;
        linkContext.registerFunc(config);
    }

    // 修改加载状态
    this.setLoading = (status, tip) => {Unmount()};
    // 修改弹框状态(模态框或者抽屉框)
    this.setVisible = (status) => {Unmount()};
    // 异步打开弹框(模态框或者抽屉框) 等表单初始化完成
    this.asyncVisible = (row) => Promise.reject("未挂载");

    /**
     * @return {LinkContext}
     */
    this.getLinkContext = () => linkContext;

    /**
     * @returns {$View}
     */
    this.get$View = function () {
        // viewContext['__$View'] 不可能为空, 为空说明异常
        return this.getLinkContext().getViewContext()['__$View'] || new $View(null);
    }
}
