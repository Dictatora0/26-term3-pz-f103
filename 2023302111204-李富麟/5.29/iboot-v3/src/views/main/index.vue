<template>
    <a-layout class="u-layout iv-primary">
      <!--侧边菜单栏-->
      <LayoutSider />
      <a-layout class="u-layout-view">
        <!--右侧头栏-->
        <LayoutHeader />
        <!--右侧视图页-->
        <a-layout-content class="u-main-container">
          <!--  任务栏  -->
          <TaskBar></TaskBar>
          <div class="u-main-task">
            <div class="iv-task-container">
            <router-view v-slot="{ Component }">
              <transition name="slide-fade" mode="out-in">
                <keep-alive :include="alive">
                  <component :is="Component" />
                </keep-alive>
              </transition>
            </router-view>
            </div>
          </div>

          <!-- 用户中心 -->
          <UserCenter />

        </a-layout-content>

      </a-layout>
    </a-layout>
</template>

<script>
import {provide, reactive, ref} from 'vue'
import { mapGetters, useStore } from "vuex";
import LayoutSider from "@msn/main/LayoutSider.vue";
import LayoutHeader from "@msn/main/LayoutHeader.vue";
import UserCenter from "@msn/main/UserCenter.vue";
import {GlobalConfigKey, RowContextKey, ViewContextKey} from "@/utils/ProvideKeys";
import TaskBar from "@msn/main/TaskBar.vue";

export default {
    name: "index",
    components: {TaskBar, UserCenter, LayoutSider, LayoutHeader },
    setup () {

        // 初始化后台菜单信息到路由
        useStore().dispatch('sys/initMenus')

        // 初始化当前登入的用户信息
        useStore().dispatch('sys/initUser')

        // 初始化系统配置信息
        useStore().dispatch('sys/initConfig')

        // 初始化菜单权限
        useStore().dispatch('sys/initPermissions')
        /**
         * URow组件将覆盖此对象, 使用在表单组件(表单组件使用了ACol)
         */
        provide(RowContextKey, {});

        /**
         * 视图组件使用的上下文
         * @see UView.jsx
         * @see IvzFuncView.vue
         * @see IvzMenuView.vue
         * @param value 必须为 null
         */
        provide(ViewContextKey, null);

      let aliveComponents = ref([]);
      return { aliveComponents };
    },

    computed: {
        ...mapGetters({
            theme: 'sys/theme',
            taskBarData: 'sys/taskBarData',
        }),
        // 用于缓存视图页
        alive () {
            return this.taskBarData
                .filter(route => route.meta['keepAlive'] != null)
                .map(route => route.meta.keepAlive);
        }
    }
}
</script>

<style>
.u-layout {
    width: 100%;
    height: 100%;
}

/*任务栏样式*/
.u-main-container {
  width: 100%;
  height: 100%;
  display: flex;
  display: -webkit-flex;
  position: relative;
  flex-direction: column;
}
.u-main-task {
  width: 100%;
  height: 100%;
  display: flex;
  overflow: auto;
  display: -ms-flex;
  position: relative;
  flex-direction: column;
  display: -webkit-flex; /* Safari */
  justify-content: flex-start;
}
.iv-task-container {
  height: 100%;
  padding: 12px;
  overflow: auto;
}
body ::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}

body ::-webkit-scrollbar-thumb {
    border-radius: 3px;
    background: hsl(0deg 0% 0% / 22%);
    -webkit-box-shadow: inset 0 0 5px hsl(0deg 0% 100% / 5%);
}

body ::-webkit-scrollbar-track {
    border-radius: 3px;
    background: hsl(0deg 0% 83%);
    -webkit-box-shadow: inset 0 0 5px rgb(37 37 37 / 5%);
}

.ivz-theme-dark .ivz-avatar {
    background-color: #001529;
}
.ivz-theme-dark .ivz-avatar .ivz-avatar-name {
    color: #ffffff;
}
.ivz-theme-dark .ivz-avatar .ant-avatar {
    background-color: #ffffff;
}

/*动画*/
.slide-fade-enter-active {
    transition: all 0.5s ease-out;
}

.slide-fade-leave-active {
    transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from, .slide-fade-leave-to {
    transform: translateX(20px);
    opacity: 0;
}
</style>
