import {createFromIconfontCN} from '@ant-design/icons-vue';

// 项目中使用到的图标
const UIcon = createFromIconfontCN({
    extraCommonProps: {style: {fontSize: '16px'}},
    scriptUrl: '/icon/iconfont.js',
});

export default {
    install(app) {
        app.component("UIcon", UIcon);
    }
}

export {UIcon}
