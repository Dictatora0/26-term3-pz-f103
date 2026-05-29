let DefaultEditUid = 'UVEdit',
    DefaultTableUid = 'UVTable',
    DefaultDetailUid = 'UVDetail',
    DefaultSearchUid = 'UVSearch',
    SelfComponentConst = "SELF";
/**
 * 系统全局常量配置
 */
export default {
    PageSize: 'size', // 分页条数字段
    PageCurrent: 'current', // 分页页数

    AccessToken: 'access_token', // 后端返回的token
    CancelRespResolver: 'cancelRespResolver', // 是否自动解析响应

    DefaultPID: 'pid',
    DefaultRowKey: 'id', // 整个系统的默认rowKey
    SuccessCode: 200, // 默认成功码
    ConfirmTitle: '操作提示', // 默认确认标题
    ConfirmContent: '确认要执行操作?', // 默认确认内容

    DelSuccessMsg: '删除记录成功', // 默认删除成功提示, 以后台返回的消息为准
    DelConfirmTitle: '删除提示', // 默认删除提示框标题
    DelConfirmContent: '确认删除选中的记录吗？', // 默认删除提示框内容

    DefaultConfirmTitle: '操作确认',
    DefaultConfirmContent: '确认提交此操作吗',

    DefaultExecSuccess: '执行成功',

    BatchDataNullTip: '请选择要操作的数据',
    SubmitSuccessMsg: '数据提交成功', // 默认数据提交成功提示
    OtherOperaSuccessMsg: '操作成功', // 默认其他动作操作成功的提示

    PrimaryUid: 'UView', // 主要视图标识(UViewDrawer、UViewModal、UTable、USearch)

    DefaultEditUid: DefaultEditUid,
    DefaultTableUid: DefaultTableUid,
    DefaultDetailUid: DefaultDetailUid,
    DefaultSearchUid: DefaultSearchUid,
    SelfComponentConst: SelfComponentConst,

    FormSpinResetTip: '数据重置中...',
    FormSpinSubmitTip: '数据提交中...',
    FormSpinLoadingTip: '数据加载中...',
    TableSpinLoadingTip: '数据加载中...',
    FileDownloadTip: '文件正在下载中...',
    ExcelDownloadTip: '文件正在导出中...',
    AjaxExecTipping: '数据处理中...',
    DeleteExecTipping: '数据删除中...',

    Options_LabelField: 'label',
    Options_ValueField: 'value',
    Options_ChildrenField: 'children',

    DefaultFuncParams: {
        ADD: ({data, formModel}, funcPath, rowKey) => {
            return Promise.resolve(formModel);
        },
        /**
         * @param data 操作数据
         * @param funcPath 功能路径
         * @param rowKey 记录唯一标识id
         * @return {Promise<*[]>|Promise<never>}
         * @constructor
         */
        DEL: (data, funcPath, rowKey) => {
            if(data instanceof Array) {
                return Promise.resolve(data.map(item => item[rowKey]));
            } else if(typeof data == 'object'){
                return Promise.resolve([data[rowKey]]);
            } else {
                return Promise.reject(`不支持的参数(${data})`);
            }
        },
        SUBMIT: (data, funcPath, rowKey) => { return Promise.resolve(data) },
        EDIT: ({data, formModel}, funcPath, rowKey) => {
            if(funcPath[1] == 'SET') { // set子功能操作
                return Promise.resolve(formModel);
            } else {
                let params = {};
                params[rowKey] = data[rowKey];
                return Promise.resolve(params)
            }
        },
        RESET: (data, funcPath, rowKey) => {
            let params = {};
            params[rowKey] = data[rowKey];
            return Promise.resolve(params)
        },
        OPEN: (data, funcPath, rowKey) => {let params = {}; params[rowKey] = data[rowKey]; return Promise.resolve(params)},
        DETAIL: (data, funcPath, rowKey) => {let params = {}; params[rowKey] = data[rowKey]; return Promise.resolve(params)}
    },
    //<URow col='modal | drawer | search | {xs: 24, sm: 24, ...}'>
    TypeResponsiveConfig: {
        modal: {xs: 24, sm: 24, md: 12, lg: 12, xl: 12, xxl: 8},
        drawer: {xs: 24, sm: 24, md: 12, lg: 12, xl: 8, xxl: 8},
        search: {xs: 24, sm: 24, md: 12, lg: 8, xl: 8, xxl: 6}
    },
    // 各功能默认请求方法配置
    FuncMethodMaps: {
        DEL: 'POST', QUERY: 'GET', VIEW: 'GET', DETAIL: 'GET', EDIT: 'GET', SUBMIT: 'POST',
        DEFAULT: 'POST', EXPORT: 'GET', IMPORT: 'POST', AJAX: 'POST', DOWNLOAD: 'POST', OPEN: 'GET'
    },
    // 功能标识
    FuncNameMeta: {ADD: 'ADD', DEL: 'DEL', EDIT: 'EDIT', QUERY: 'QUERY', IMPORT: 'IMPORT', EXPORT: 'EXPORT'
        , CANCEL: 'CANCEL', RESET: 'RESET', EXPAND: 'EXPAND', SUBMIT: 'SUBMIT', DETAIL: 'DETAIL'
        , AJAX: 'AJAX', DOWNLOAD: 'DOWNLOAD', OPEN: 'OPEN', LINK: 'LINK'},
    // 功能要操作的组件uid
    // SELF表示要操作的是功能所在的组件下
    FuncOperationUid: {
        ADD: DefaultEditUid, DEL: DefaultTableUid, EDIT: DefaultEditUid, QUERY: DefaultTableUid, IMPORT: DefaultSearchUid, EXPORT: DefaultSearchUid
        , CANCEL: SelfComponentConst, RESET: SelfComponentConst, EXPAND: DefaultTableUid, SUBMIT: SelfComponentConst, DOWNLOAD: DefaultTableUid
        , DETAIL: DefaultDetailUid, AJAX: SelfComponentConst, OPEN: '', LINK: ''
    },
    // 子功能元数据
    ChildFuncNameMeta: {
        SET: 'SET', //
        LOADING: 'LOADING', // 加载数据
        BATCH: 'BATCH', // 批量
        CHILD: 'CHILD', // 子记录
        CONFIRM: 'CONFIRM', // 确认
    },
    FuncBtnTypeMaps: { // 功能按钮的默认配置
        ADD: {type: 'primary'},
        DEL: {type: 'primary', danger: true},
        EDIT: {type: '#3b5999'},
        QUERY: {type: 'primary'}, // 查询
        VIEW: {type: 'primary'}, // 查询 和query选其一
        IMPORT: {type: 'primary', shape: 'round'},
        EXPORT: {type: 'dashed', shape: 'round'},
        EXPAND: {type: 'primary', ghost: true},
        CANCEL: {type: 'default'},
        DETAIL: {type: '#87d068'},
        RESET: {type: 'primary', ghost: true},
        DEFAULT: {type: 'default'},
        SUBMIT: {type: 'primary'},
        LINK: {type: 'primary'},
    },
    FuncTagColorMaps: { // 功能Tag默认颜色配置
        ADD: '#2db7f5',
        DEL: '#f50',
        EDIT: '#3B5999D5',
        QUERY: '#108ee9',
        IMPORT: 'default',
        EXPORT: 'orange',
        CANCEL: 'red',
        DETAIL: '#87d068',
        RESET: 'warning',
        DEF: 'default',
        SUBMIT: 'blue',
        VIEW: '#108ee9',
        AJAX: '#1890ff', // 执行自定义http请求
        LINK: '#2db7f5', // 跳转
    },
}
