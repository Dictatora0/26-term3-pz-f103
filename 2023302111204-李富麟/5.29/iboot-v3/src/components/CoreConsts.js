let DefaultEditUid = "UVEdit",
  DefaultTableUid = "UVTable",
  DefaultDetailUid = "UVDetail",
  DefaultSearchUid = "UVSearch",
  SelfComponentConst = "SELF";

export default {
  PageSize: "size",
  PageCurrent: "current",

  AccessToken: "access_token",
  RefreshToken: "refresh_token",
  OAuth2ClientId: "oauth2_client_id",
  OAuth2Scopes: "oauth2_scopes",
  OAuth2Roles: "oauth2_roles",
  OAuth2PkceVerifier: "oauth2_pkce_verifier",
  OAuth2State: "oauth2_state",
  CancelRespResolver: "cancelRespResolver",

  DefaultPID: "pid",
  DefaultRowKey: "id",
  SuccessCode: 200,
  ConfirmTitle: "操作提示",
  ConfirmContent: "确认要执行此操作吗",

  DelSuccessMsg: "删除记录成功",
  DelConfirmTitle: "删除提示",
  DelConfirmContent: "确认删除选中的记录吗",

  DefaultConfirmTitle: "操作确认",
  DefaultConfirmContent: "确认提交此操作吗",
  DefaultExecSuccess: "执行成功",

  BatchDataNullTip: "请选择要操作的数据",
  SubmitSuccessMsg: "数据提交成功",
  OtherOperaSuccessMsg: "操作成功",

  PrimaryUid: "UView",
  DefaultEditUid,
  DefaultTableUid,
  DefaultDetailUid,
  DefaultSearchUid,
  SelfComponentConst,

  FormSpinResetTip: "数据重置中...",
  FormSpinSubmitTip: "数据提交中...",
  FormSpinLoadingTip: "数据加载中...",
  TableSpinLoadingTip: "数据加载中...",
  FileDownloadTip: "文件正在下载中...",
  ExcelDownloadTip: "文件正在导出中...",
  AjaxExecTipping: "数据处理中...",
  DeleteExecTipping: "数据删除中...",

  Options_LabelField: "label",
  Options_ValueField: "value",
  Options_ChildrenField: "children",

  DefaultFuncParams: {
    ADD: ({ formModel }) => Promise.resolve(formModel),
    DEL: (data, funcPath, rowKey) => {
      if (data instanceof Array) {
        return Promise.resolve(data.map(item => item[rowKey]));
      } else if (typeof data === "object") {
        return Promise.resolve([data[rowKey]]);
      }
      return Promise.reject(`unsupported params: ${data}`);
    },
    SUBMIT: data => Promise.resolve(data),
    EDIT: ({ data, formModel }, funcPath, rowKey) => {
      if (funcPath[1] === "SET") {
        return Promise.resolve(formModel);
      }
      let params = {};
      params[rowKey] = data[rowKey];
      return Promise.resolve(params);
    },
    RESET: (data, funcPath, rowKey) => {
      let params = {};
      params[rowKey] = data[rowKey];
      return Promise.resolve(params);
    },
    OPEN: (data, funcPath, rowKey) => {
      let params = {};
      params[rowKey] = data[rowKey];
      return Promise.resolve(params);
    },
    DETAIL: (data, funcPath, rowKey) => {
      let params = {};
      params[rowKey] = data[rowKey];
      return Promise.resolve(params);
    }
  },

  TypeResponsiveConfig: {
    modal: { xs: 24, sm: 24, md: 12, lg: 12, xl: 12, xxl: 8 },
    drawer: { xs: 24, sm: 24, md: 12, lg: 12, xl: 8, xxl: 8 },
    search: { xs: 24, sm: 24, md: 12, lg: 8, xl: 8, xxl: 6 }
  },

  FuncMethodMaps: {
    DEL: "POST",
    QUERY: "GET",
    VIEW: "GET",
    DETAIL: "GET",
    EDIT: "GET",
    SUBMIT: "POST",
    DEFAULT: "POST",
    EXPORT: "GET",
    IMPORT: "POST",
    AJAX: "POST",
    DOWNLOAD: "POST",
    OPEN: "GET"
  },

  FuncNameMeta: {
    ADD: "ADD",
    DEL: "DEL",
    EDIT: "EDIT",
    QUERY: "QUERY",
    IMPORT: "IMPORT",
    EXPORT: "EXPORT",
    CANCEL: "CANCEL",
    RESET: "RESET",
    EXPAND: "EXPAND",
    SUBMIT: "SUBMIT",
    DETAIL: "DETAIL",
    AJAX: "AJAX",
    DOWNLOAD: "DOWNLOAD",
    OPEN: "OPEN",
    LINK: "LINK"
  },

  FuncOperationUid: {
    ADD: DefaultEditUid,
    DEL: DefaultTableUid,
    EDIT: DefaultEditUid,
    QUERY: DefaultTableUid,
    IMPORT: DefaultSearchUid,
    EXPORT: DefaultSearchUid,
    CANCEL: SelfComponentConst,
    RESET: SelfComponentConst,
    EXPAND: DefaultTableUid,
    SUBMIT: SelfComponentConst,
    DOWNLOAD: DefaultTableUid,
    DETAIL: DefaultDetailUid,
    AJAX: SelfComponentConst,
    OPEN: "",
    LINK: ""
  },

  ChildFuncNameMeta: {
    SET: "SET",
    LOADING: "LOADING",
    BATCH: "BATCH",
    CHILD: "CHILD",
    CONFIRM: "CONFIRM"
  },

  FuncBtnTypeMaps: {
    ADD: { type: "primary" },
    DEL: { type: "primary", danger: true },
    EDIT: { type: "#3b5999" },
    QUERY: { type: "primary" },
    VIEW: { type: "primary" },
    IMPORT: { type: "primary", shape: "round" },
    EXPORT: { type: "dashed", shape: "round" },
    EXPAND: { type: "primary", ghost: true },
    CANCEL: { type: "default" },
    DETAIL: { type: "#87d068" },
    RESET: { type: "primary", ghost: true },
    DEFAULT: { type: "default" },
    SUBMIT: { type: "primary" },
    LINK: { type: "primary" }
  },

  FuncTagColorMaps: {
    ADD: "#2db7f5",
    DEL: "#f50",
    EDIT: "#3B5999D5",
    QUERY: "#108ee9",
    IMPORT: "default",
    EXPORT: "orange",
    CANCEL: "red",
    DETAIL: "#87d068",
    RESET: "warning",
    DEF: "default",
    SUBMIT: "blue",
    VIEW: "#108ee9",
    AJAX: "#1890ff",
    LINK: "#2db7f5"
  }
};
