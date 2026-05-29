<template>
  <a-config-provider :locale="zhCN" :theme="theme">
    <router-view />
  </a-config-provider>
</template>

<script>
import './theme/index.css'
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import ThemeConfig from './theme/index.js'
import zhCN from 'ant-design-vue/es/locale/zh_CN';
import {computed, provide, reactive} from "vue";
import {GlobalConfigKey, ThemeConfigKey} from "@/utils/ProvideKeys";

dayjs.locale('zh-cn');

export default {
  name: 'App',
  setup() {

    let themeConfig = reactive(ThemeConfig);
    let colorPrimary = computed(() => themeConfig.colorPrimary)
    let theme = reactive({
      token: {
        colorPrimary: colorPrimary
      }
    })
    /**
     * ivzone全局配置
     */
    let globalConfig = reactive({
      // 刷新功能
      reload (router, route) {
        let keepAlive = route.meta.keepAlive;
        route.meta.keepAlive = null;
        router.push('/refresh').then(() => {
          route.meta.keepAlive = keepAlive;
        })
      }
    });

    // 提供全局配置
    provide(GlobalConfigKey, globalConfig)

    // 提供主题配置
    provide(ThemeConfigKey, themeConfig)
    return {zhCN, theme, themeConfig}
  }
}
</script>

<style>
.iv-primary {
  --divider-color: #d9d9d9;
  --primary-radius: 8px;
  --primary-color: v-bind('themeConfig.colorPrimary');
  --primary-background-color: v-bind('themeConfig.colorBackgroundPrimary');
}

.iv-header {
  --header-height: 64px;
  --header-color: v-bind('themeConfig.headerColor');
  --header-background-color: v-bind('themeConfig.headerBackgroundColor');
  --header-logo-background-color: v-bind('themeConfig.logoBackgroundColor');
}
.iv-primary-divider {
  border-bottom: 1px solid var(--divider-color);
}
.iv-view-search {
  margin-bottom: 8px;
}
.iv-view-border {
  padding: 8px;
  background-color: #ffffff;
  border-radius: 8px;
}

.iv-text-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.iv-group-addon-padding-0 .ant-input-number-group-addon {
  padding: 0px 0px;
}
.iv-drawer .ant-drawer-content-wrapper {
  background: #ffffff;
  border-left: 1px solid #f0f0f0;
}
.ant-drawer-open.iv-basic-drawer .ant-drawer-content-wrapper, .ant-drawer-open.iv-form-drawer .ant-drawer-content-wrapper {
  border-top: 1px dashed var(--primary-background-color);
}
.ant-menu-inline-collapsed >.ant-menu-submenu>.ant-menu-submenu-title {
    padding-inline: calc(50% - 14px);
}
#app {
  height: 100%;
  position: relative;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
