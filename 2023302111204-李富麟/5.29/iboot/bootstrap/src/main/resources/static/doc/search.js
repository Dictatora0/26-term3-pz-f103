let api = [];
const apiDocListSize = 3
api.push({
    name: '系统管理',
    order: '1',
    list: []
})
api[0].list.push({
    alias: 'CaptchaAutoConfiguration',
    order: '1',
    link: '校验码功能',
    desc: '校验码功能',
    list: []
})
api[0].list[0].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/valid/code',
    methodId: '857f4a5f809dd81878b64092448d44a5',
    desc: '生成code',
});
api[0].list[0].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/valid/captcha',
    methodId: 'c2383c513fed9e4f6246a04bb498133a',
    desc: '获取验证码',
});
api[0].list.push({
    alias: 'AdminCenterController',
    order: '2',
    link: '后台用户管理中心',
    desc: '后台用户管理中心',
    list: []
})
api[0].list[1].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/center/editUser',
    methodId: 'b161a33588ee441ed6c08d475615c9f3',
    desc: '修改用户',
});
api[0].list[1].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/center/pwd',
    methodId: '9e90f0babc32f929bf2906cc4493a36b',
    desc: '修改用户密码',
});
api[0].list.push({
    alias: 'AdminController',
    order: '3',
    link: '用户管理',
    desc: '用户管理',
    list: []
})
api[0].list[2].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/view',
    methodId: 'fddb4167ff2c13ce19deca507801a707',
    desc: '查询用户管理列表',
});
api[0].list[2].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/add',
    methodId: 'a69149f8c8b368bb25b183821d94083d',
    desc: '新增用户及角色',
});
api[0].list[2].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/edit',
    methodId: '4e93b171a169e685d14de7a692212def',
    desc: '获取用户详情',
});
api[0].list[2].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/edit',
    methodId: 'a087469f5d78194b59bb8ba54e35c0ee',
    desc: '修改用户及角色',
});
api[0].list[2].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/detail',
    methodId: '6d9247bfd9fb48e689692375d46af1b3',
    desc: '获取用户详情',
});
api[0].list[2].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/del',
    methodId: '2b70e9029806a53b13180868750cbdc1',
    desc: '删除用户',
});
api[0].list[2].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/import',
    methodId: 'e85e1b080b750ab2b1290bcc5b6efd04',
    desc: '导入用户',
});
api[0].list[2].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/export',
    methodId: '53de77ff8467d1850862159b78146e9f',
    desc: '导出用户',
});
api[0].list[2].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/modUserInfo',
    methodId: '1c23732f2fc30b3eaa8f40cd344c3d80',
    desc: '修改用户信息',
});
api[0].list[2].list.push({
    order: '10',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/pwd',
    methodId: 'eecde41cd256c7a5e46c3041d014fd86',
    desc: '设置用户密码',
});
api[0].list[2].list.push({
    order: '11',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/admin/updateCurrentPwd',
    methodId: 'ac08ebf8323b74b6bc108aeb74e94cde',
    desc: '设置用户密码',
});
api[0].list.push({
    alias: 'ConfigController',
    order: '4',
    link: '系统配置管理',
    desc: '系统配置管理',
    list: []
})
api[0].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/config/view',
    methodId: '2f2bd144644e3ea275fb9252ad941f9e',
    desc: '查询配置列表',
});
api[0].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/config/list',
    methodId: '13f81b428dd5cee136048c87987880ab',
    desc: '查询配置列表',
});
api[0].list[3].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/config/add',
    methodId: 'dd603a8c6cdf3afde977b0f8d2cc00f7',
    desc: '新增配置',
});
api[0].list[3].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/config/edit',
    methodId: '470b5d2663eb27364f617d7bcf37e3f8',
    desc: '获取编辑详情',
});
api[0].list[3].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/config/edit',
    methodId: '4916846405a9874d6bf468d7d7bb9f68',
    desc: '修改配置',
});
api[0].list[3].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/config/del',
    methodId: '72b9ee559bd297cbdadcb9c74c95f516',
    desc: '删除记录',
});
api[0].list.push({
    alias: 'CoreStatisticsController',
    order: '5',
    link: '系统功能统计',
    desc: '系统功能统计',
    list: []
})
api[0].list[4].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/statistics/online',
    methodId: 'dc8fd03a2bd10e42ae6bc84f721aa153',
    desc: '当天在线人数和在前在线人数',
});
api[0].list[4].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/statistics/inRecentMonth',
    methodId: 'a988d69b27856666a890bf0be2ac7e0f',
    desc: '近一个月在线用户统计',
});
api[0].list.push({
    alias: 'DictDataController',
    order: '6',
    link: '字典数据管理',
    desc: '字典数据管理',
    list: []
})
api[0].list[5].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictData/view',
    methodId: '4a9ebb5501ec73992f1595b0c9e4511e',
    desc: '获取字典数据列表',
});
api[0].list[5].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictData/listByType',
    methodId: '67cc7a8898f9bfb52ae973e86dc24695',
    desc: '获取指定类型的字典数据',
});
api[0].list[5].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictData/add',
    methodId: 'bc27fe6ca3f9c5a1e37479cdfb595872',
    desc: '新增一条字典数据记录',
});
api[0].list[5].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictData/del',
    methodId: 'c2c3919fd4509bde67800457b3bc001b',
    desc: '删除字典数据',
});
api[0].list[5].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictData/edit',
    methodId: '9ebf032d5caff3a2caf141e691f4a911',
    desc: '获取字典数据详情',
});
api[0].list[5].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictData/edit',
    methodId: 'a2911884f42cb5ec4bebf83cb05752fa',
    desc: '修改字典数据记录',
});
api[0].list.push({
    alias: 'DictTypeController',
    order: '7',
    link: '字典类型管理',
    desc: '字典类型管理',
    list: []
})
api[0].list[6].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictType/view',
    methodId: '0738938f236f3564d413193a6a3dfa81',
    desc: '查询字典列表(分页)',
});
api[0].list[6].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictType/list',
    methodId: 'e02270bb0de41bfc66f66df8a36f18f1',
    desc: '获取所有字典类型记录',
});
api[0].list[6].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictType/add',
    methodId: 'd9162f6be14b1b96ac57664b3389c869',
    desc: '新增字典记录',
});
api[0].list[6].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictType/edit',
    methodId: 'a24f9194f78b52707a6eca4adefe4279',
    desc: '获取字典记录',
});
api[0].list[6].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictType/edit',
    methodId: '63ae136f95f304958343f26e004d0a72',
    desc: '修改字典记录',
});
api[0].list[6].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/dictType/del',
    methodId: 'ca3496910bf50194fb94c5dd4588352b',
    desc: '删除字典',
});
api[0].list.push({
    alias: 'LoginController',
    order: '8',
    link: '用户管理中心接口',
    desc: '用户管理中心接口',
    list: []
})
api[0].list[7].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/login',
    methodId: '1044b16631907bfeb29433d84b647347',
    desc: '系统登录',
});
api[0].list[7].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/logout',
    methodId: '7935dbde8aca2940f4c5b81294c82e9c',
    desc: '系统注销',
});
api[0].list.push({
    alias: 'MenuController',
    order: '9',
    link: '系统菜单管理',
    desc: '系统菜单管理',
    list: []
})
api[0].list[8].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/view',
    methodId: '9c3cd811741bfee1919cfefcd6b7fb14',
    desc: '获取菜单列表',
});
api[0].list[8].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/bars',
    methodId: '6f864400097919b7491bb477bd2795d6',
    desc: '菜单栏菜单列表',
});
api[0].list[8].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/permissions',
    methodId: '9050dffdf5448269a52011a708ba8c78',
    desc: '获取权限列表',
});
api[0].list[8].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/parent',
    methodId: 'ba68c96be006f3d6e12f8af395571220',
    desc: '获取除权限外的菜单',
});
api[0].list[8].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/add',
    methodId: '024a4b1cb9f3cbe0a8aeea00abec12a7',
    desc: '新增菜单',
});
api[0].list[8].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/del',
    methodId: 'd7ccef88fcd34b9e6dcdc591e7d4ab7e',
    desc: '删除菜单',
});
api[0].list[8].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/edit',
    methodId: 'be9e22ae7d5ac841b4b44a8887c38372',
    desc: '获取编辑详情',
});
api[0].list[8].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/edit',
    methodId: '358a2ebce0914e35f0480e195a0ef43d',
    desc: '编辑菜单',
});
api[0].list[8].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/menu/msn',
    methodId: '1ac736a0436787c033e73cc2b9cfba60',
    desc: '获取模块列表',
});
api[0].list.push({
    alias: 'NotifyController',
    order: '10',
    link: '系统通知管理',
    desc: '系统通知管理',
    list: []
})
api[0].list[9].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notify/view',
    methodId: 'bf3d56141210c6798f7cd113efdc6fbc',
    desc: '列表查询',
});
api[0].list[9].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notify/edit',
    methodId: 'd6470bbd4d9f9fde0a65cf80c63781b3',
    desc: '获取编辑记录',
});
api[0].list[9].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notify/saveOrUpdate',
    methodId: '46b218d77c9a8ccb35b8f1272c40354d',
    desc: '新增或者更新记录',
});
api[0].list[9].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notify/del',
    methodId: '540472e69599c6ede7f98f3d1c882101',
    desc: '删除指定记录',
});
api[0].list.push({
    alias: 'NotifyUserController',
    order: '11',
    link: '消息通知用户管理',
    desc: '消息通知用户管理',
    list: []
})
api[0].list[10].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notifyUser/view',
    methodId: 'd188f79d55ff5ae92fd34948c9adc012',
    desc: '列表查询',
});
api[0].list[10].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notifyUser/edit',
    methodId: 'f962718cca140ab905895122ccc11f09',
    desc: '获取编辑记录',
});
api[0].list[10].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notifyUser/saveOrUpdate',
    methodId: 'abf267d36fcea85ccac0c16af4ef5424',
    desc: '新增或者更新记录',
});
api[0].list[10].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/notifyUser/del',
    methodId: 'd68f7cf8281ed47165138d26bc5d21c2',
    desc: '删除指定记录',
});
api[0].list.push({
    alias: 'OnlineUserController',
    order: '12',
    link: '在线用户管理',
    desc: '在线用户管理',
    list: []
})
api[0].list[11].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/online/view',
    methodId: 'ed6458c5ebc02056c0e6bc6ab4e525cf',
    desc: '列表查询',
});
api[0].list[11].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/online/del',
    methodId: 'e0f183d3b69e8d73b591e5b6170d352b',
    desc: '删除在线记录',
});
api[0].list[11].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/online/offline',
    methodId: '81061469af1e8465bd54c2e72ff5e215',
    desc: '剔除用户下线',
});
api[0].list[11].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/online/countToday',
    methodId: 'e62fec5838b811d1dbb68b8af5afc553',
    desc: '统计当天用户信息',
});
api[0].list[11].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/online/countLastMonth',
    methodId: '7e8f2f52fe569976f06c1eb9980971c7',
    desc: '统计最近一个月的访问用户',
});
api[0].list.push({
    alias: 'OperaLogController',
    order: '13',
    link: '日志管理功能',
    desc: '日志管理功能',
    list: []
})
api[0].list[12].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/log/view',
    methodId: '31a105a174243e07b20a7ff6d2981bf9',
    desc: '获取日志记录',
});
api[0].list[12].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/log/del',
    methodId: 'cf5c5b48c9bc7d340968c6c9cf329ad9',
    desc: '删除日志记录',
});
api[0].list.push({
    alias: 'OrgController',
    order: '14',
    link: '组织部门管理',
    desc: '组织部门管理',
    list: []
})
api[0].list[13].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/org/view',
    methodId: '9ff4ccea05d678168a240eab4b7339ae',
    desc: '获取部门列表',
});
api[0].list[13].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/org/parent',
    methodId: '3ff76cdfe4914eb0be9b75566f8d0b21',
    desc: '',
});
api[0].list[13].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/org/add',
    methodId: '8b448bc6b14262f1076d22f39eb1f587',
    desc: '新增部门',
});
api[0].list[13].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/org/edit',
    methodId: 'b03e53068cca5f4307cf65070433a674',
    desc: '获取编辑详情',
});
api[0].list[13].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/org/edit',
    methodId: '36385540e0cf40390d61cb5d1fa293a3',
    desc: '修改部门',
});
api[0].list[13].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/org/del',
    methodId: '4ea110dd2c322b3e694dc5bd96bcdb84',
    desc: '删除部门',
});
api[0].list.push({
    alias: 'PostController',
    order: '15',
    link: '岗位管理管理',
    desc: '岗位管理管理',
    list: []
})
api[0].list[14].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/post/view',
    methodId: '82abfc02db616b93c585f03acfd950ee',
    desc: '列表查询',
});
api[0].list[14].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/post/list',
    methodId: '423277692aaf0f78cd3d390c4ddb8f06',
    desc: '获取岗位列表',
});
api[0].list[14].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/post/edit',
    methodId: '120130905592e9624f64b7aa136f65ae',
    desc: '获取编辑记录',
});
api[0].list[14].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/post/saveOrUpdate',
    methodId: '0d8f0d6f06a76b4d51a17a6657390109',
    desc: '新增或者更新记录',
});
api[0].list[14].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/post/del',
    methodId: '0fa7b1eafe6b29241876ad2d5b71d02e',
    desc: '删除指定记录',
});
api[0].list.push({
    alias: 'RegionController',
    order: '16',
    link: '行政区管理',
    desc: '行政区管理',
    list: []
})
api[0].list[15].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/region/view',
    methodId: 'dc3146ebb705ac33250a328b629017ce',
    desc: '获取列表',
});
api[0].list[15].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/region/getById',
    methodId: '07adc5e0e07ac0e9134f016d997aa900',
    desc: '获取指定记录',
});
api[0].list.push({
    alias: 'RoleController',
    order: '17',
    link: '角色管理',
    desc: '角色管理',
    list: []
})
api[0].list[16].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/view',
    methodId: '1b33138a63f52dc23c41f6a15e537453',
    desc: '角色列表',
});
api[0].list[16].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/list',
    methodId: '45bb412c9eeaf3e4580ed9a35290b319',
    desc: '获取所有角色列表',
});
api[0].list[16].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/allMenus',
    methodId: '36c5c4b5a8260999a4fda5a115ea062b',
    desc: '所有功能菜单',
});
api[0].list[16].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/add',
    methodId: '25d181babb484702dc83c8b5b2aca7d1',
    desc: '新增角色记录',
});
api[0].list[16].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/edit',
    methodId: '4e94a4eb06694e7fa0dbdb08df85d926',
    desc: '获取详情记录',
});
api[0].list[16].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/edit',
    methodId: '3b1e17615d23ae76974ee6d7bb475f3c',
    desc: '保存编辑记录',
});
api[0].list[16].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/func',
    methodId: 'e1239c2b0b78c3af26de64d9e02f9cff',
    desc: '返回此角色拥有的权限列表',
});
api[0].list[16].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/perm',
    methodId: '08f88067b351611eb857180cbcfa4211',
    desc: '修改角色权限',
});
api[0].list[16].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/core/role/del',
    methodId: 'c9c4bafe71dc956d1a0f6f2453543091',
    desc: '删除记录',
});
api[0].list.push({
    alias: 'UploadController',
    order: '18',
    link: '通用的文件上传',
    desc: '通用的文件上传',
    list: []
})
api[0].list[17].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/common/upload',
    methodId: '358b90bc051d0c2c9bf869ba1a2870c1',
    desc: '文件上传  todo url',
});
api[0].list[17].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/common/upload/batch',
    methodId: '84c964b616ce82e66f577f1996ad0b24',
    desc: '文件批量上传',
});
api[0].list[17].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/common/upload/avatar',
    methodId: '7133f4e46dfbd836373398d63dca875b',
    desc: '上传用户头像',
});
api.push({
    name: '物联网模块',
    order: '2',
    list: []
})
api[1].list.push({
    alias: 'CollectDataController',
    order: '1',
    link: '采集数据管理',
    desc: '采集数据管理',
    list: []
})
api[1].list[0].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectData/signal',
    methodId: '8ef1be47f15da7c48dc7b8b508bf47b0',
    desc: '获取点位数据',
});
api[1].list[0].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectData/model',
    methodId: '1ea128c0e2b96cdc75da670cd132f11e',
    desc: '获取模型数据',
});
api[1].list.push({
    alias: 'CollectDetailController',
    order: '2',
    link: '采集详情管理',
    desc: '采集详情管理',
    list: []
})
api[1].list[1].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectDetail/details',
    methodId: 'f90a7b3731f86872f789d7a7a4782dbd',
    desc: '获取采集详情',
});
api[1].list[1].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectDetail/edit',
    methodId: 'b92442ce1f10abdde546409726bd680c',
    desc: '新增或者更新记录',
});
api[1].list[1].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectDetail/saveOrUpdate',
    methodId: 'e204edacad594a80d4c452cdbb92a04a',
    desc: '新增或者更新记录',
});
api[1].list[1].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectDetail/del',
    methodId: '637f4e976f11e3921c12cbdbef839084',
    desc: '删除指定记录',
});
api[1].list.push({
    alias: 'CollectTaskController',
    order: '3',
    link: '数据采集任务管理',
    desc: '数据采集任务管理',
    list: []
})
api[1].list[2].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/view',
    methodId: 'b0539fc814f1fa18e980a3661eb52c83',
    desc: '列表查询',
});
api[1].list[2].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/edit',
    methodId: '8d5367e7799fa6b3dcdcc18e5de1ca10',
    desc: '获取编辑记录',
});
api[1].list[2].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/saveOrUpdate',
    methodId: '3ea6606fdf0d9e043a5a7b9ceee2a8eb',
    desc: '新增或者更新记录',
});
api[1].list[2].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/del',
    methodId: '2be7e590b6aa3eee2c42c70edb6693f5',
    desc: '删除指定记录',
});
api[1].list[2].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/status',
    methodId: '7d0a83fd147cf54ea05a7235f7491de7',
    desc: '切换采集状态',
});
api[1].list[2].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/list',
    methodId: '4141caad58ec15dc876d8127e76f3af8',
    desc: '采集任务列表',
});
api[1].list[2].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/collectTask/storeActions',
    methodId: '4ffa5122abcf4554ef6ab691ddc38339',
    desc: '存储动作',
});
api[1].list.push({
    alias: 'DeviceChildController',
    order: '4',
    link: '子设备管理',
    desc: '子设备管理',
    list: []
})
api[1].list[3].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceChild/view',
    methodId: 'a8dab1df2f6d018279416041ae04220c',
    desc: '列表查询',
});
api[1].list[3].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceChild/edit',
    methodId: 'ef39913ff6ab2984fc1a23e1021e951c',
    desc: '获取编辑记录',
});
api[1].list[3].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceChild/saveOrUpdate',
    methodId: '29d6283465b9e39c01268edad623fdcb',
    desc: '新增或者更新记录',
});
api[1].list[3].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceChild/del',
    methodId: '77b44e44db46c934039a5bd95cc2aec3',
    desc: '删除指定记录',
});
api[1].list[3].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceChild/listByUid',
    methodId: '9c75fd8377d1a499bdc443210f895f7c',
    desc: '或者指定设备下的子设备列表',
});
api[1].list.push({
    alias: 'DeviceController',
    order: '5',
    link: '设备管理',
    desc: '设备管理',
    list: []
})
api[1].list[4].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/view',
    methodId: '86ca3dd969c4db0857649dfbf9a525e6',
    desc: '列表查询',
});
api[1].list[4].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/edit',
    methodId: 'b3fca9a025be64e1062147636c18ddf8',
    desc: '获取编辑记录',
});
api[1].list[4].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/edit',
    methodId: 'aa80cdb1c79951063032cfa94bd03599',
    desc: '修改记录',
});
api[1].list[4].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/add',
    methodId: 'af961cf3a02ea843888a5a3652afde25',
    desc: '新增记录',
});
api[1].list[4].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/del',
    methodId: '726192f22a595450944ff07e1375ce4c',
    desc: '删除指定记录',
});
api[1].list[4].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/listByModel',
    methodId: '84a0062bf2848924bbb4538358dc0495',
    desc: '获取指定型号下面的设备列表',
});
api[1].list[4].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/listOfGateway',
    methodId: '0752a69a3bf7bcc8d4b6a4f89cf75d08',
    desc: '获取网关设备',
});
api[1].list[4].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/listOfProducts',
    methodId: '66bfde050a60aa135fb7099742bc760a',
    desc: '获取指定产品列表下的所有设备',
});
api[1].list[4].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/device/connect/{status}',
    methodId: '2f4605f7b2a04a0e1100a70329f2d2de',
    desc: '设备连接',
});
api[1].list.push({
    alias: 'DeviceCtrlController',
    order: '6',
    link: '设备指令接口管理',
    desc: '设备指令接口管理',
    list: []
})
api[1].list[5].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/instruct/ctrl/{modelApiCode}',
    methodId: '8aef91933d379602e52f862e47f95ecc',
    desc: '设备控制',
});
api[1].list[5].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/instruct/debug/{modelApiCode}',
    methodId: '0d60a1f994b979d606d703a985fcf2f6',
    desc: '设备调试接口',
});
api[1].list.push({
    alias: 'DeviceDebugController',
    order: '7',
    link: '设备调试管理',
    desc: '设备调试管理',
    list: []
})
api[1].list[6].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/debug/tree',
    methodId: 'e2335b0f052193bec807ae093e34aa82',
    desc: '产品类型、产品、设备树',
});
api[1].list[6].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/debug/detail',
    methodId: '709069f69b86665ceef1054df6fc2383',
    desc: '调试详情',
});
api[1].list.push({
    alias: 'DeviceGroupController',
    order: '8',
    link: '设备分组管理',
    desc: '设备分组管理',
    list: []
})
api[1].list[7].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/view',
    methodId: '78e2a261a19c98a876407ff2ef6d071e',
    desc: '列表查询',
});
api[1].list[7].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/list',
    methodId: '089e9f15986d51c40fd3c02e9b94eba0',
    desc: '获取所有组列表',
});
api[1].list[7].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/tree',
    methodId: 'c477526f1c6b3c623f8a8bcf2c4c611b',
    desc: '树结构分组列表',
});
api[1].list[7].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/edit',
    methodId: '6d8f81e298ce21597e4f1f5fc77f3fc1',
    desc: '获取编辑记录',
});
api[1].list[7].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/saveOrUpdate',
    methodId: 'a584f75231335aeb5a85a0b979b6968d',
    desc: '新增或者更新记录',
});
api[1].list[7].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/del',
    methodId: '5b569de94524025ae91fea798aaa6a41',
    desc: '删除指定记录',
});
api[1].list[7].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/deviceGroup/listModelApi',
    methodId: '275394ef83c24c109188099de654187b',
    desc: '获取物模型接口列表',
});
api[1].list.push({
    alias: 'EMapController',
    order: '9',
    link: '电子地图模块',
    desc: '电子地图模块',
    list: []
})
api[1].list[8].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/emap/devices',
    methodId: '04099bcc2c217267c92e89880765aae6',
    desc: '获取设备列表',
});
api[1].list[8].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/emap/detail',
    methodId: '0c5405cdfd1ffc047f4e9209d98c1c09',
    desc: '获取设备详情',
});
api[1].list.push({
    alias: 'EventSourceController',
    order: '10',
    link: '事件源管理',
    desc: '事件源管理',
    list: []
})
api[1].list[9].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/view',
    methodId: '125eaf54be004415867ee806866d7e57',
    desc: '列表查询',
});
api[1].list[9].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/list',
    methodId: '4e213fb73e9c3b137d5cfb9911af1ea5',
    desc: '事件源列表',
});
api[1].list[9].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/edit',
    methodId: 'a5fb595d8120080fb15cf7697fb56e83',
    desc: '获取编辑记录',
});
api[1].list[9].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/saveOrUpdate',
    methodId: '5f5c96f39707ff03b2768d660f71b31b',
    desc: '新增或者更新记录',
});
api[1].list[9].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/config',
    methodId: 'ff582adc664c1fb1404b4d1a58120c48',
    desc: '配置物模型接口',
});
api[1].list[9].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/del',
    methodId: 'f8dc28ce778d2cdf8aab1920d1acec07',
    desc: '删除指定记录',
});
api[1].list[9].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/types',
    methodId: '87b92cec8f028ff927bbb4a9d6e12d8e',
    desc: '事件源类型',
});
api[1].list[9].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/deviceGroups',
    methodId: '4f69a4c1b1025d25f5b9d2e4e691bc75',
    desc: '获取设备组列表',
});
api[1].list[9].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSource/switch/{status}',
    methodId: '038cfcb1235306721a77fcd2f1dde967',
    desc: '切换事件源状态',
});
api[1].list.push({
    alias: 'EventSourceDetailController',
    order: '11',
    link: '事件源详情管理',
    desc: '事件源详情管理',
    list: []
})
api[1].list[10].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSourceDetail/view',
    methodId: 'd39a412db476ebf8cc63406e36c37ba7',
    desc: '列表查询',
});
api[1].list[10].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSourceDetail/edit',
    methodId: 'e63a7071c1e12bfff3649bae9f201ab1',
    desc: '获取编辑记录',
});
api[1].list[10].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSourceDetail/saveOrUpdate',
    methodId: '82d351669b073cb1b9ea2b73958554e8',
    desc: '新增或者更新记录',
});
api[1].list[10].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/eventSourceDetail/del',
    methodId: '9d88d4fb32707ab6b15f01c5628c4417',
    desc: '删除指定记录',
});
api[1].list.push({
    alias: 'GatewayController',
    order: '12',
    link: '协议网关管理',
    desc: '协议网关管理',
    list: []
})
api[1].list[11].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/view',
    methodId: '4901d4a153069d4d653733ddb2602571',
    desc: '列表查询',
});
api[1].list[11].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/list',
    methodId: '7df4b8878bfa495620a712e999b7def8',
    desc: '获取网关列表',
});
api[1].list[11].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/protocols',
    methodId: '5b855689ef094b0f76c0c1b02820f3ee',
    desc: '协议列表',
});
api[1].list[11].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/edit',
    methodId: 'c7b2b0dbb2997deb0d64b2540a177514',
    desc: '获取编辑记录',
});
api[1].list[11].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/saveOrUpdate',
    methodId: '0b8411908600d3905d6dc81446b6750d',
    desc: '新增或者更新记录',
});
api[1].list[11].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/del',
    methodId: 'af79bc7b852f3d1bec5af909c864db9b',
    desc: '删除指定记录',
});
api[1].list[11].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/transportProtocol',
    methodId: '4d05efc3620bfbeb1640f9bf2f1d2655',
    desc: '传输协议列表',
});
api[1].list[11].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/gateway/switch/{id}/{status}',
    methodId: 'bed2cff3fa32539a7051452898512249',
    desc: '切换网关状态',
});
api[1].list.push({
    alias: 'DeviceDtuController',
    order: '13',
    link: 'dtu设备管理',
    desc: 'dtu设备管理',
    list: []
})
api[1].list[12].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/dtu/view',
    methodId: 'f4b63f07cd8d6d495ee0a77b2d02287a',
    desc: '列表查询',
});
api[1].list[12].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/dtu/edit',
    methodId: 'b8d43c58daa1ba88230d336550023ef7',
    desc: '获取编辑记录',
});
api[1].list[12].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/dtu/saveOrUpdate',
    methodId: '75212b094a37460ae6f65e54db0e91ca',
    desc: '新增或者修改记录',
});
api[1].list[12].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/dtu/del',
    methodId: '22bb6c5c2202cdde4992a9828d2ad41f',
    desc: '删除指定记录',
});
api[1].list[12].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/dtu/products',
    methodId: '606249574584beaa0652d51825c319ea',
    desc: '获取Dtu产品列表',
});
api[1].list.push({
    alias: 'DeviceModbusController',
    order: '14',
    link: 'modbus设备管理',
    desc: 'modbus设备管理',
    list: []
})
api[1].list[13].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modbus/view',
    methodId: 'f57eefcc6f975e3af0d62f7ce5bff67a',
    desc: '列表查询',
});
api[1].list[13].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modbus/edit',
    methodId: 'bb5b5a7a9d9a6a315da30dc1bd1f7e29',
    desc: '获取编辑记录',
});
api[1].list[13].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modbus/saveOrUpdate',
    methodId: 'b78e770161cea7b640cd73b6e27e8ed7',
    desc: '新增或者修改记录',
});
api[1].list[13].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modbus/del',
    methodId: 'ac1e9d0dddadba9d197a78f45a6afeae',
    desc: '删除指定记录',
});
api[1].list[13].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modbus/products',
    methodId: 'ddbd714c2b2c7c322740196cc3c63378',
    desc: '获取Modbus产品列表',
});
api[1].list.push({
    alias: 'DeviceMqttController',
    order: '15',
    link: 'mqtt管理',
    desc: 'mqtt管理',
    list: []
})
api[1].list[14].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/mqtt/view',
    methodId: 'd7869d1ebcc36fe6ee2ca0bce56fe3fb',
    desc: '列表查询',
});
api[1].list[14].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/mqtt/edit',
    methodId: '208e9e1684b64e079a104e44cd7cf5d1',
    desc: '获取编辑记录',
});
api[1].list[14].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/mqtt/saveOrUpdate',
    methodId: '9baebdddfc7825f07d72e60b73d505b7',
    desc: '新增或者修改记录',
});
api[1].list[14].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/mqtt/del',
    methodId: '66fcbe79f94827adc73bf1e60cc148c1',
    desc: '删除指定记录',
});
api[1].list[14].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/mqtt/products',
    methodId: '3ae752cb14ed5eeb7c09c505cc50fde5',
    desc: '获取Mqtt产品列表',
});
api[1].list.push({
    alias: 'DevicePlcController',
    order: '16',
    link: 'plc设备管理',
    desc: 'plc设备管理',
    list: []
})
api[1].list[15].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/plc/view',
    methodId: 'a0d3f6439801c1fc6099cddc3eb80bb0',
    desc: '列表查询',
});
api[1].list[15].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/plc/edit',
    methodId: 'eb05ee53aac27fe87f5cf40e509c1f8b',
    desc: '获取编辑记录',
});
api[1].list[15].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/plc/saveOrUpdate',
    methodId: '6cf4f67ea5f8a010e1a1dbb10b8f62a3',
    desc: '新增或者修改记录',
});
api[1].list[15].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/plc/del',
    methodId: '93c14b01eddeaf0c5f1ae0ad12c9d47c',
    desc: '删除指定记录',
});
api[1].list[15].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/plc/products',
    methodId: '713f4617edc1df1645cdc2d55454548a',
    desc: '获取Dtu产品列表',
});
api[1].list.push({
    alias: 'SerialController',
    order: '17',
    link: '串口设备管理',
    desc: '串口设备管理',
    list: []
})
api[1].list[16].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/view',
    methodId: '1ee92f39111f5a0975894be97bebca7e',
    desc: '列表查询',
});
api[1].list[16].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/list',
    methodId: '5ae198a24ea8840a05b878b9f77a9cce',
    desc: '列表查询',
});
api[1].list[16].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/edit',
    methodId: '41716c6fb55c82863c2f83402213ed7c',
    desc: '获取编辑记录',
});
api[1].list[16].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/getByCom',
    methodId: 'e307f16aff0f800bf559aec8670d1502',
    desc: '获取记录',
});
api[1].list[16].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/saveOrUpdate',
    methodId: 'ab46ece1b0e891342ef46c9d9913b0e0',
    desc: '新增或者更新记录',
});
api[1].list[16].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/del',
    methodId: 'cea94b0939862b478c67d38c41afdb4f',
    desc: '删除指定记录',
});
api[1].list[16].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/available',
    methodId: '73bda6179cf79c1a66c8b8ae78b938a7',
    desc: '可用的串口列表',
});
api[1].list[16].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/serial/connect/{status}',
    methodId: 'bff8b5178ca7ea25542a8858c1787f19',
    desc: '',
});
api[1].list.push({
    alias: 'IotIndexController',
    order: '18',
    link: '系统功能统计',
    desc: '系统功能统计',
    list: []
})
api[1].list[17].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/index/numCount',
    methodId: 'e36357d4960162a462a6ded693982c73',
    desc: '各组件的产品统计',
});
api[1].list[17].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/index/productDeviceCount',
    methodId: '91093bc164ca51a58d0e0804a3a48dd1',
    desc: '各个产品下面的设备数量统计',
});
api[1].list[17].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/index/deviceTypeCount',
    methodId: 'c851b5085b66f3dd96ac95631eba3096',
    desc: '各个设备类型下面的设备数量统计',
});
api[1].list[17].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/index/countDataWithLastMonth',
    methodId: 'c1b9a368327d2757edc6f7cd7c825539',
    desc: '统计最近一个月采集的数据',
});
api[1].list.push({
    alias: 'ModelApiConfigController',
    order: '19',
    link: '物模型接口配置管理',
    desc: '物模型接口配置管理',
    list: []
})
api[1].list[18].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApiConfig/view',
    methodId: '57d9e458d84e936745d58ac4f900789b',
    desc: '列表查询',
});
api[1].list[18].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApiConfig/edit',
    methodId: '7f7b216234a42ead32241f400216ebd6',
    desc: '获取编辑记录',
});
api[1].list[18].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApiConfig/saveOrUpdate',
    methodId: '1e0378fb707262c44b80f1f7724bfedd',
    desc: '新增或者更新记录',
});
api[1].list[18].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApiConfig/del',
    methodId: '30ff49e48774ab6851085252341f891d',
    desc: '删除指定记录',
});
api[1].list.push({
    alias: 'ModelApiController',
    order: '20',
    link: '物模型接口管理',
    desc: '物模型接口管理',
    list: []
})
api[1].list[19].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/view',
    methodId: '50b2803db344abe5fca2fb848df56cac',
    desc: '列表查询',
});
api[1].list[19].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/list',
    methodId: 'db72caa133e68f28e24560ecb65d62df',
    desc: '获取接口列表',
});
api[1].list[19].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/detailsOfProductId',
    methodId: '171ef916286504e57e5801b598e61303',
    desc: '获取指定产品下面的接口列表详情',
});
api[1].list[19].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/edit',
    methodId: '5ed1ab6655f1a687de39fb0212c383b1',
    desc: '获取编辑记录',
});
api[1].list[19].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/saveOrUpdate',
    methodId: '129466b793abda9416ea360a04541b40',
    desc: '新增或者更新记录',
});
api[1].list[19].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/del',
    methodId: 'fc6c072d133b9d636eb0c779facf79a2',
    desc: '删除指定记录',
});
api[1].list[19].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelApi/asStatus',
    methodId: '1a4c3e41908993de99547c879f37828b',
    desc: '修改默认状态位',
});
api[1].list.push({
    alias: 'ModelAttrController',
    order: '21',
    link: '物模型属性管理',
    desc: '物模型属性管理',
    list: []
})
api[1].list[20].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/view',
    methodId: '9c386b30ee450c7bf8ff3c602a3bf642',
    desc: '列表查询',
});
api[1].list[20].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/dict/view',
    methodId: '2abb8d59bc278e6fff72a1e47ede0634',
    desc: '物模型属性字典列表查询',
});
api[1].list[20].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/list',
    methodId: '5a53456bbb2dc57f10df1b2819e81ed7',
    desc: '获取物模型属性列表',
});
api[1].list[20].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/edit',
    methodId: '1917e648ceaf9acd77bce9d38945db6d',
    desc: '获取编辑记录',
});
api[1].list[20].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/saveOrUpdate',
    methodId: '51392f7da74ac719f07e77d3afd07a75',
    desc: '新增或者更新记录',
});
api[1].list[20].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/dict/saveOrUpdate',
    methodId: '5f6a618a94e117ec4956fcdbe8d8dbb3',
    desc: '新增或者更新记录',
});
api[1].list[20].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/del',
    methodId: 'cac19256352607b865844d4bfaf459cd',
    desc: '删除指定记录',
});
api[1].list[20].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/dict/del',
    methodId: '0cdc8f137be5858ab3c4ead5fbbcd137',
    desc: '删除指定物模型字典记录',
});
api[1].list[20].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/ctrlStatus',
    methodId: 'a988c3a33ddd4b6d3a8d16175aaef082',
    desc: '设置属性指定属性为控制属性',
});
api[1].list[20].list.push({
    order: '10',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/modelAttr/listByProductId',
    methodId: '95a11b6f8bbca287c39f9c1b7f5971e7',
    desc: '获取指定产品下面的物模型属性',
});
api[1].list.push({
    alias: 'PanelsController',
    order: '22',
    link: '运行展板模块',
    desc: '运行展板模块',
    list: []
})
api[1].list[21].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/panels/devices',
    methodId: 'e090baebd4eac40f818b2face144bf36',
    desc: '设备列表',
});
api[1].list[21].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/panels/detail',
    methodId: 'd826e74c17d6624dcacddf0758166ac9',
    desc: '调试详情',
});
api[1].list[21].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/panels/switchCtrlStatus',
    methodId: 'c1b7edcb0ee05e28391360d8222a59e2',
    desc: '切换控制状态',
});
api[1].list.push({
    alias: 'PointGroupController',
    order: '23',
    link: '点位组管理',
    desc: '点位组管理',
    list: []
})
api[1].list[22].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/pointGroup/view',
    methodId: '548d61d4b8f8c775e5a2420f7018058b',
    desc: '列表查询',
});
api[1].list[22].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/pointGroup/edit',
    methodId: '64caa5f5fb3393cb5fdd5cd3a2b85819',
    desc: '获取编辑记录',
});
api[1].list[22].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/pointGroup/saveOrUpdate',
    methodId: '99894f8f966f5dae99e81fccf8b970d3',
    desc: '新增或者更新记录',
});
api[1].list[22].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/pointGroup/del',
    methodId: '24411a3d71a98a8be0bb0fedf6900131',
    desc: '删除指定记录',
});
api[1].list[22].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/pointGroup/list',
    methodId: '77be73ec0cacbc36ba47b386679bd0d9',
    desc: '获取点位组列表',
});
api[1].list.push({
    alias: 'ProductController',
    order: '24',
    link: '产品管理',
    desc: '产品管理',
    list: []
})
api[1].list[23].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/view',
    methodId: 'ece093ce6e375adf7fcf3d3a84726039',
    desc: '列表查询',
});
api[1].list[23].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/list',
    methodId: '6c2a249ee626539ef8171aac5f22b90e',
    desc: '返回产品列表',
});
api[1].list[23].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/listByType',
    methodId: '61b25a4040765cf4497cb4f83a413872',
    desc: '获取指定类型下面的列表',
});
api[1].list[23].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/parent',
    methodId: '9c156edcf8499e4599aae78ad7f39bef',
    desc: '父产品列表',
});
api[1].list[23].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/edit',
    methodId: '6692f67174146d4dd2fba649fccdf028',
    desc: '获取编辑记录',
});
api[1].list[23].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/saveOrUpdate',
    methodId: '863035264e015554a5ae4ea0efa40d59',
    desc: '新增或者更新记录',
});
api[1].list[23].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/del',
    methodId: '9f465ffb348f1a190ceeb6fbf87b2243',
    desc: '删除指定记录',
});
api[1].list[23].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/deviceTypes',
    methodId: 'e00ff18e42e4f68b2d3e445cb84426d4',
    desc: '设备类型列表',
});
api[1].list[23].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/switch/{id}/{status}',
    methodId: '15b59513b4c924fd9e6d16be2a940660',
    desc: '切换产品状态',
});
api[1].list[23].list.push({
    order: '10',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/listByPoint',
    methodId: 'a6df2c070ab42206df0e39b805a1a99f',
    desc: '获取点位协议的产品列表',
});
api[1].list[23].list.push({
    order: '11',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/product/resolvers',
    methodId: '882e16dd25c68cd3155fccd4f18efde7',
    desc: '解析器列表',
});
api[1].list.push({
    alias: 'ProductTypeController',
    order: '25',
    link: '产品类型管理',
    desc: '产品类型管理',
    list: []
})
api[1].list[24].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/view',
    methodId: '330aab653ee6a435825b57687c81a4d5',
    desc: '分页列表查询',
});
api[1].list[24].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/tree',
    methodId: '5fa1c09a2fd39b5c5a5b0b77ddd5e537',
    desc: '树结构列表',
});
api[1].list[24].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/productTree',
    methodId: 'e0e41db98a63dbfd7322057fc9496022',
    desc: '产品类型-产品树',
});
api[1].list[24].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/edit',
    methodId: 'd10f3a668694f6ed01ccadb3f05b160b',
    desc: '获取编辑记录',
});
api[1].list[24].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/edit',
    methodId: 'cff83217e987e64eb0702b71afc8e145',
    desc: '修改记录',
});
api[1].list[24].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/add',
    methodId: '1f51926f12377e49ca36285c76cd2c02',
    desc: '新增记录',
});
api[1].list[24].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/saveOrUpdate',
    methodId: '71a745e5388873fb138d407e3529b102',
    desc: '新增或者修改',
});
api[1].list[24].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/del',
    methodId: 'c79a6110f3a16273565791a8b38180c1',
    desc: '删除指定记录',
});
api[1].list[24].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/productType/alias',
    methodId: '7e97d402af86e28e09e4b99db18e3ec8',
    desc: '获取设备类型别名列表',
});
api[1].list.push({
    alias: 'ProtocolApiConfigController',
    order: '26',
    link: '协议接口配置管理',
    desc: '协议接口配置管理',
    list: []
})
api[1].list[25].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApiConfig/view',
    methodId: '617491dde95ea5210e8d1a9bd6e05eb6',
    desc: '列表查询',
});
api[1].list[25].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApiConfig/edit',
    methodId: '3239f2295e92f6c731060e579da3e62b',
    desc: '获取编辑记录',
});
api[1].list[25].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApiConfig/saveOrUpdate',
    methodId: '15d669033fe6a13e7d225010c01d56a2',
    desc: '新增或者更新记录',
});
api[1].list[25].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApiConfig/del',
    methodId: '3723972bdc718e5e0fe238e1f9a82127',
    desc: '删除指定记录',
});
api[1].list.push({
    alias: 'ProtocolApiController',
    order: '27',
    link: '协议接口管理',
    desc: '协议接口管理',
    list: []
})
api[1].list[26].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApi/view',
    methodId: '5080f3250d4e0c69383d1bd170ebe137',
    desc: '列表查询',
});
api[1].list[26].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApi/edit',
    methodId: '8c9fcbe1991b87a4ef11102e9f53795a',
    desc: '获取编辑记录',
});
api[1].list[26].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApi/saveOrUpdate',
    methodId: 'abb6f868cfb4968c4c6c1f5e6484ede5',
    desc: '新增或者更新记录',
});
api[1].list[26].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolApi/del',
    methodId: '239b90e4a0a3c9d7739535d41d9f1714',
    desc: '删除指定记录',
});
api[1].list.push({
    alias: 'ProtocolAttrController',
    order: '28',
    link: '协议属性管理',
    desc: '协议属性管理',
    list: []
})
api[1].list[27].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolAttr/view',
    methodId: '80d5de087a1ba0f0104af4478024f1a4',
    desc: '列表查询',
});
api[1].list[27].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolAttr/edit',
    methodId: 'b563ae6311ba5c1451fbc1e4ace4d94c',
    desc: '获取编辑记录',
});
api[1].list[27].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolAttr/saveOrUpdate',
    methodId: '506175b522b8abaf0e982ccfa0c24a71',
    desc: '新增或者更新记录',
});
api[1].list[27].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocolAttr/del',
    methodId: '660372ecc72d57cb107476085cc54b4b',
    desc: '删除指定记录',
});
api[1].list.push({
    alias: 'ProtocolController',
    order: '29',
    link: '报文协议管理',
    desc: '报文协议管理',
    list: []
})
api[1].list[28].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/view',
    methodId: '0c7e64abbbee491257c02bb2758b7729',
    desc: '列表查询',
});
api[1].list[28].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/list',
    methodId: '0a4906623b0bccf9be035d867bd27355',
    desc: '获取协议列表',
});
api[1].list[28].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/edit',
    methodId: 'ca00c8351d66a39466b97a04b809d594',
    desc: '获取编辑记录',
});
api[1].list[28].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/saveOrUpdate',
    methodId: '85b8d97ca0eed3777cf4dd3aceeb4e7b',
    desc: '新增或者更新记录',
});
api[1].list[28].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/del',
    methodId: '2af0ee311d443aa8246f5356778826dc',
    desc: '删除指定记录',
});
api[1].list[28].list.push({
    order: '6',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/loadProtocol',
    methodId: '7072f9d5906ed1daba3c4fdb75247d68',
    desc: '导入Jar协议',
});
api[1].list[28].list.push({
    order: '7',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/detail',
    methodId: '47e2672ea3537228fae9d7347fe2b5a0',
    desc: '获取协议详情',
});
api[1].list[28].list.push({
    order: '8',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/implModes',
    methodId: 'e1ac7e4176d9c934e5d52588673642f8',
    desc: '协议实现方式列表',
});
api[1].list[28].list.push({
    order: '9',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/ctrlModes',
    methodId: 'cf4eb8b65318ff32a38d5742703b1ae7',
    desc: '操控方式列表',
});
api[1].list[28].list.push({
    order: '10',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/apis',
    methodId: '29ef8c5d3755654f5ae01a0fa8481820',
    desc: '获取协议对应的api列表',
});
api[1].list[28].list.push({
    order: '11',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/internals',
    methodId: '2aaf8d378ba187a96a4bcc7fcf412d7d',
    desc: '系统内置协议列表',
});
api[1].list[28].list.push({
    order: '12',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/listProtocolConfigByProductId',
    methodId: 'a062ca8269e6405939a98ad2f716533a',
    desc: '获取产品对应协议的额外配置列表',
});
api[1].list[28].list.push({
    order: '13',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/protocol/listProtocolConfig',
    methodId: 'db92a6c62c4bf85844de4e80c0816e66',
    desc: '获取协议额外配置列表',
});
api[1].list.push({
    alias: 'RealtimeDataController',
    order: '30',
    link: '实时数据管理',
    desc: '实时数据管理',
    list: []
})
api[1].list[29].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/realtime/listByDeviceId',
    methodId: 'f288e653f270d73ae0d85e6006cbedfc',
    desc: '获取指定设备下面的所有事件或者点位数据',
});
api[1].list[29].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/realtime/listByDevice',
    methodId: '20d893fb7abdb438812b655ae9e60f84',
    desc: '获取指定设备下面的所有事件或者点位数据',
});
api[1].list[29].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/realtime/get',
    methodId: 'bf3c47b5986e9b7b4fdd598d0c3650bd',
    desc: '获取指定设备点位或者字段实时数据',
});
api[1].list[29].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/realtime/getByDeviceId',
    methodId: '22ab45c2dd8a32c8d49a4a2275ef1fbc',
    desc: '获取指定设备下某个事件或者点位字段的实时数据',
});
api[1].list.push({
    alias: 'SignalController',
    order: '31',
    link: '寄存器点位管理',
    desc: '寄存器点位管理',
    list: []
})
api[1].list[30].list.push({
    order: '1',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/signal/view',
    methodId: '506115a45c16ef02e07ecdd6f02d45b1',
    desc: '列表查询',
});
api[1].list[30].list.push({
    order: '2',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/signal/listByProductIds',
    methodId: '299babd18bf6649419d1e0d1dd333ba7',
    desc: '获取指定产品下面的点位列表',
});
api[1].list[30].list.push({
    order: '3',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/signal/edit',
    methodId: 'ee034c1f1e8e894e53d84566e033c0a6',
    desc: '获取编辑记录',
});
api[1].list[30].list.push({
    order: '4',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/signal/saveOrUpdate',
    methodId: '224c354df5e0b25b0cde8ae0c7193940',
    desc: '新增或者更新记录',
});
api[1].list[30].list.push({
    order: '5',
    deprecated: 'false',
    url: 'http://127.0.0.1:8085/api/iot/signal/del',
    methodId: '7e550616f212ac57cf705d610a80f414',
    desc: '删除指定记录',
});
api.push({
    name: 'gb28181监控模块',
    order: '3',
    list: []
})
document.onkeydown = keyDownSearch;
function keyDownSearch(e) {
    const theEvent = e;
    const code = theEvent.keyCode || theEvent.which || theEvent.charCode;
    if (code === 13) {
        const search = document.getElementById('search');
        const searchValue = search.value.toLocaleLowerCase();

        let searchGroup = [];
        for (let i = 0; i < api.length; i++) {

            let apiGroup = api[i];

            let searchArr = [];
            for (let i = 0; i < apiGroup.list.length; i++) {
                let apiData = apiGroup.list[i];
                const desc = apiData.desc;
                if (desc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                    searchArr.push({
                        order: apiData.order,
                        desc: apiData.desc,
                        link: apiData.link,
                        alias: apiData.alias,
                        list: apiData.list
                    });
                } else {
                    let methodList = apiData.list || [];
                    let methodListTemp = [];
                    for (let j = 0; j < methodList.length; j++) {
                        const methodData = methodList[j];
                        const methodDesc = methodData.desc;
                        if (methodDesc.toLocaleLowerCase().indexOf(searchValue) > -1) {
                            methodListTemp.push(methodData);
                            break;
                        }
                    }
                    if (methodListTemp.length > 0) {
                        const data = {
                            order: apiData.order,
                            desc: apiData.desc,
                            link: apiData.link,
                            alias: apiData.alias,
                            list: methodListTemp
                        };
                        searchArr.push(data);
                    }
                }
            }
            if (apiGroup.name.toLocaleLowerCase().indexOf(searchValue) > -1) {
                searchGroup.push({
                    name: apiGroup.name,
                    order: apiGroup.order,
                    list: searchArr
                });
                continue;
            }
            if (searchArr.length === 0) {
                continue;
            }
            searchGroup.push({
                name: apiGroup.name,
                order: apiGroup.order,
                list: searchArr
            });
        }
        let html;
        if (searchValue === '') {
            const liClass = "";
            const display = "display: none";
            html = buildAccordion(api,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        } else {
            const liClass = "open";
            const display = "display: block";
            html = buildAccordion(searchGroup,liClass,display);
            document.getElementById('accordion').innerHTML = html;
        }
        const Accordion = function (el, multiple) {
            this.el = el || {};
            this.multiple = multiple || false;
            const links = this.el.find('.dd');
            links.on('click', {el: this.el, multiple: this.multiple}, this.dropdown);
        };
        Accordion.prototype.dropdown = function (e) {
            const $el = e.data.el;
            let $this = $(this), $next = $this.next();
            $next.slideToggle();
            $this.parent().toggleClass('open');
            if (!e.data.multiple) {
                $el.find('.submenu').not($next).slideUp("20").parent().removeClass('open');
            }
        };
        new Accordion($('#accordion'), false);
    }
}

function buildAccordion(apiGroups, liClass, display) {
    let html = "";
    if (apiGroups.length > 0) {
        if (apiDocListSize === 1) {
            let apiData = apiGroups[0].list;
            let order = apiGroups[0].order;
            for (let j = 0; j < apiData.length; j++) {
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#' + apiData[j].alias + '">' + apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                html += '<ul class="sectlevel2" style="'+display+'">';
                let doc = apiData[j].list;
                for (let m = 0; m < doc.length; m++) {
                    let spanString;
                    if (doc[m].deprecated === 'true') {
                        spanString='<span class="line-through">';
                    } else {
                        spanString='<span>';
                    }
                    html += '<li><a href="#' + doc[m].methodId + '">' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                }
                html += '</ul>';
                html += '</li>';
            }
        } else {
            for (let i = 0; i < apiGroups.length; i++) {
                let apiGroup = apiGroups[i];
                html += '<li class="'+liClass+'">';
                html += '<a class="dd" href="#_'+apiGroup.order+'_' + apiGroup.name + '">' + apiGroup.order + '.&nbsp;' + apiGroup.name + '</a>';
                html += '<ul class="sectlevel1">';

                let apiData = apiGroup.list;
                for (let j = 0; j < apiData.length; j++) {
                    html += '<li class="'+liClass+'">';
                    html += '<a class="dd" href="#' + apiData[j].alias + '">' +apiGroup.order+'.'+ apiData[j].order + '.&nbsp;' + apiData[j].desc + '</a>';
                    html += '<ul class="sectlevel2" style="'+display+'">';
                    let doc = apiData[j].list;
                    for (let m = 0; m < doc.length; m++) {
                       let spanString;
                       if (doc[m].deprecated === 'true') {
                           spanString='<span class="line-through">';
                       } else {
                           spanString='<span>';
                       }
                       html += '<li><a href="#' + doc[m].methodId + '">'+apiGroup.order+'.' + apiData[j].order + '.' + doc[m].order + '.&nbsp;' + spanString + doc[m].desc + '<span></a> </li>';
                   }
                    html += '</ul>';
                    html += '</li>';
                }

                html += '</ul>';
                html += '</li>';
            }
        }
    }
    return html;
}