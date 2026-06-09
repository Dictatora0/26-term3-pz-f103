<template>
  <div class="u-task-bar">
    <div class="u-task-more">
      &nbsp;&nbsp
      <MenuUnfoldOutlined v-if="collapsed" class="u-task-icon trigger" @click="collapsedChange" />
      <MenuFoldOutlined v-else class="u-task-icon trigger" @click="collapsedChange" />
      <ReloadOutlined class="u-task-icon u-tba-reload" title="刷新" @click="() => refresh()"/>
    </div>
    <a-tabs :active-key="activeRoute.path" @change="switchTask" :hide-add="true" @edit="closeTask"
            type="editable-card" size="small" class="u-task-tabs">
      <template v-for="menu in taskBarData" :key="menu.path">
        <a-tab-pane :closable="menu.meta.closable != false">
          <template #tab>
            <span class="u-tba-dot" @click.prevent="() => openSubMenu(menu)" />
            <span class="u-tba-title">{{menu.name}}</span>
          </template>
        </a-tab-pane>
      </template>
    </a-tabs>
  </div>
</template>

<script>
import {mapGetters, mapMutations} from "vuex";
import {ReloadOutlined, MenuFoldOutlined, MenuUnfoldOutlined} from "@ant-design/icons-vue";
import {computed, inject, ref} from "vue";
import {GlobalConfigKey, ThemeConfigKey} from "@/utils/ProvideKeys";

export default {
  name: "TaskBar",
  components: {ReloadOutlined, MenuFoldOutlined, MenuUnfoldOutlined},
  computed: {
    ...mapGetters({
      user: 'sys/user',
      collapsed: 'sys/collapsed',
      taskBarData: 'sys/taskBarData',
      idMenuMaps: 'sys/idMenuMaps',
      activeRoute: 'sys/activeRoute',
      selectedKeys: 'sys/selectedKeys',
      activityMenu: 'sys/activityMenu',
      activityView: 'sys/activityView',
      breadcrumbRoutes: 'sys/breadcrumbRoutes'
    }),
  },
  setup() {
    let global = inject(GlobalConfigKey);
    let theme = inject(ThemeConfigKey);
    let reload = global.reload;

    return {reload, theme};
  },
  methods: {
    ...mapMutations({
      logout: 'sys/logout',
      removeTask: 'sys/removeTask',
      toggleUserVisible: 'sys/toggleUserVisible',
      switchActiveViewTo: 'sys/switchActiveViewTo',
      pushAndSwitchTask: 'sys/pushAndSwitchTask',
      openTaskAndParentMenu: 'sys/openTaskAndParentMenu',
    }),
    refresh() {
      let path = this.$route.path;
      let route = this.taskBarData.find(route => route.path == path);
      this.reload(this.$router, route);
    },
    switchTask (url) { // 切换任务菜单处理
      this.pushAndSwitchTask(url);
    },
    /**
     * 打开子菜单
     */
    openSubMenu(route) {
      this.openTaskAndParentMenu(route.path);
    },
    closeTask (url, action) { // 关闭任务处理
      let prevTemp = null; // 用来保存当前关闭的上一个任务
      this.taskBarData.forEach((route, index, ori) => {
        if(route['path'] === url) {
          prevTemp = ori[index-1]; // 获取要删除的前一个
          this.removeTask(url); // 移除任务
          if(!prevTemp) prevTemp = ori[index];
        }
      });

      if(prevTemp) {
        this.switchTask(prevTemp['path']);
      } else {
        this.switchTask('/');
      }
    },
    taskBarCloseMoreOpera (item) { // 任务栏菜单关闭处理
      let start = this.workMenu ? 1 : 0;
      if (item.key === 'all') {
        if(this.workMenu) {
          // this.switchActiveMenuTo(this.workMenu);
          this.selectedKeys[0] = this.activityMenu['url']
        }

        this.taskBarData.splice(start, this.taskBarData.length)
      } else { // 关闭除当前激活的任务以外的所有任务
        let position = 1
        for(let index=0; index < this.taskBarData.length; index++) {
          let item = this.taskBarData[index];
          if (item === this.activityMenu) {
            position = index; break;
          }
        }
        this.taskBarData.splice(position + 1, this.taskBarData.length - position - 1)
        this.taskBarData.splice(start, Math.abs(position - start))
      }
    },
    collapsedChange() {
      this.$store.state['sys'].collapsed = !this.collapsed;
    }
  }
}
</script>

<style>
.u-task-bar {
    background: #ffffff;
    box-shadow: 0 3px 3px rgba(0,21,41,.08);
}
.u-task-more {
  left: 0px;
  width: 72px;
  z-index: 10;
  line-height: 40px;
  position: absolute;
}
.u-task-more .u-task-icon {
  margin: 0px 6px;
}
.u-task-bar .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab.ant-tabs-tab-active {
  background: var(--primary-background-color);
}
.u-task-bar .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab .ant-tabs-tab-remove {
  margin-left: 3px;
  visibility: hidden;
  margin-right: -16px;
}
.u-task-bar .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab:hover .ant-tabs-tab-remove {
  visibility: visible;
}
.u-task-bar .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-remove {
  visibility: visible;
}
.u-task-bar .ant-tabs-nav-list {
  height: 40px;
}
.u-task-bar .u-task-tabs, .u-task-bar .ant-tabs-card > .ant-tabs-nav .ant-tabs-tab {
  border: unset;
  background: #f0f2f5;
  border-radius: 6px!important;
}
.u-task-bar .ant-tabs-top > .ant-tabs-nav {
  margin: 0px 0px;
}
.u-task-bar .ant-tabs-nav-wrap {
  margin-left: 78px;
}
.u-task-bar .ant-tabs-tab {
  height: 30px;
  margin: 5px 4px 0px 0px!important;
}
.u-task-bar .ant-tabs-nav {
  background: #ffffff;
}
.u-task-bar .ant-tabs-top > .ant-tabs-nav::before {
  border-bottom: unset;
}
.u-task-bar .ant-tabs-tab>div{
  /*transform: skewX(28deg);*/
}
.u-task-bar .ant-tabs-nav .ant-tabs-tab .anticon {
  margin-right: 0px!important;
}

.u-tba-reload {
  margin-left: 8px;
}
.u-tba-reload:hover {
  transform: rotate(180deg);
  -webkit-transform: rotate(180deg)
}
.u-tba-dot {
  width: 10px;
  height: 10px;
  background: #e5e5e5;
  border-radius: 50%;
  display: inline-block;
}
.u-tba-title {
  color: #000000;
  margin-left: 3px;
}

.ant-tabs-tab .u-tba-reload {
  display: none;
  font-size: 19px;
}
.ant-tabs-tab-active .u-tba-dot {
  background: var(--primary-color);
}
.ant-tabs-tab-active .u-tba-title {
  color: #3e3e3e;
}

:root .ant-tabs-tab-prev-icon-target, :root .ant-tabs-tab-next-icon-target {
  font-size: 14px;
}
</style>
