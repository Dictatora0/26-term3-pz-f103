<template>
  <a-layout-header class="iv-header u-header-bar iv-primary-divider">
    <ARow class="iv-header-row">
      <!--头部菜单-->
      <ACol class="u-header-col u-header-col-left" :xs="0" :sm="0" :md="10" :lg="10" :xl="8">
        <a-breadcrumb :routes="breadcrumbRoutes" class="iv-header-breadcrumb">
          <template #itemRender="{ route, paths }">
            <router-link v-if="route.index == 0" :to="route.url">
              <HomeOutlined />
            </router-link>
            <router-link v-else-if="route.url" :to="route.url">
              {{ route.name }}
            </router-link>
            <span v-else>
              {{ route.name }}
            </span>
          </template>
        </a-breadcrumb>
      </ACol>
      <ACol class="u-header-col ivz-header-col-right" :xs="24" :sm="24" :md="14" :lg="14" :xl="16">
        <ASpace :size="0">
          <a-tooltip title="全局搜索">
            <div @click="() => searchStatus = true" class="u-opera-col">
              <SearchOutlined :style="{fontSize: '14px'}" />
              <AModal class="iv-global-search" v-model:open="searchStatus" :closable="false" :footer="null">
                <template #title>
                  <div style="font-size: 14px">
                    <span>全局搜索</span>
                    <span style="font-size: 12px; color: #d1d1d1; float: right; font-weight: normal;">快捷键(shift + shift)</span>
                  </div>
                </template>
                <AInput placeholder="请输入要搜索的关键字" @change="searchKeywordHandle" autocomplete="off" />
                <div class="iv-search-content">
                  <template v-for="item in searchResultModel" :key="item.key">
                    <div class="iv-context-item" @click.native="() => searchRecordClickHandle(item)">
                      <span>{{item.label}}</span>
                      <span style="float: right; color: #959595; font-size: 12px">{{item.filter.name}}</span>
                    </div>
                  </template>
                </div>
                <a-divider dashed>
                  <span style="font-size: 12px; color: #d1d1d1">搜索历史</span>
                </a-divider>
                <div class="iv-search-history">

                </div>
              </AModal>
            </div>
          </a-tooltip>
          <a-tooltip title="全屏">
            <div @click="fullScreen" class="u-opera-col">
              <FullscreenOutlined :style="{fontSize: '14px'}"/>
            </div>
          </a-tooltip>
          <div class="u-opera-col">
            <a-dropdown placement="bottom">
              <a-avatar :src="$apiUrl + user.avatar" :size="32">
                <template #icon><UserOutlined /></template>
              </a-avatar>
              <template #overlay>
                <a-menu @click="quickOpera">
                  <AMenuItem style="text-align: center; cursor: auto;">
                    <span class="iv-header-username">{{user.name}}</span>
                  </AMenuItem>
                  <a-menu-divider />
                  <a-menu-item key="profile">
                    <UIcon type="iz-icon-profile" style="font-size: 15px"/><span>&nbsp;个人资料</span>
                  </a-menu-item>
                  <a-menu-item key="pwd">
                    <LockFilled style="font-size: 14px"/><span>&nbsp;修改密码</span>
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout">
                    <LogoutOutlined style="font-size: 14px" /><span>&nbsp;退出登录</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <a-tooltip title="风格配置" class="u-opera-col">
            <div class="u-opera-col" @click="themeConfig['switchOpen']">
              <LayoutOutlined style="font-size: 14px"/>
            </div>
          </a-tooltip>
        </ASpace>
      </ACol>
    </ARow>
    <a-drawer v-model:open="themeConfig['open']" placement="right" :closable="false">
      <a-divider dashed orientation="left">系统主题风格</a-divider>
      <div style="margin: 8px 0px 24px 0px; display: flex">
        <ATooltip title="纯白风格" class="iv-style-item iv-style-item-light">
          <div @click="() => themeConfig.switchStyle('light')">
            <CheckOutlined class="iv-style-item-checkbox-icon" v-show="themeConfig.style == 'light'"/>
          </div>
        </ATooltip>
        <ATooltip title="暗黑风格" class="iv-style-item iv-style-item-dark">
          <div @click="() => themeConfig.switchStyle('dark')">
            <CheckOutlined class="iv-style-item-checkbox-icon" v-show="themeConfig.style == 'dark'"/>
          </div>
        </ATooltip>
      </div>
      <a-divider dashed orientation="left">
        系统主题颜色
      </a-divider>
      <div>
        <a-segmented v-model:value="themeConfig.colorPrimary" :options="themeConfig.candidateColorPrimary" @change="colorPrimaryChange">
          <template #label="{value}">
            <span :style="{background: value, color: '#ffffff', padding: '2px', borderRadius: '3px'}">{{value}}</span>
          </template>
        </a-segmented>
        <h4 style="margin-top: 36px">
          顶栏应用主题色：
          <a-switch v-model:checked="themeConfig.headerColorPrimary" @change="headerColorPrimaryChange"/>
        </h4>
      </div>
    </a-drawer>
  </a-layout-header>
</template>

<script>
import {logout} from "@/api";
import screenfull from 'screenfull'
import {msgError} from "@/utils/message";
import {mapGetters, mapMutations} from "vuex";
import {
  UserOutlined,
  CheckOutlined,
  DownOutlined,
  FullscreenOutlined,
  HomeOutlined,
  LockFilled,
  LogoutOutlined,
  NotificationFilled,
  ReloadOutlined,
  LayoutOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue'
import {inject, reactive, ref} from "vue";
import {GlobalConfigKey, ThemeConfigKey} from "@/utils/ProvideKeys";

export default {
  name: "LayoutHeader",
  computed: {
    ...mapGetters({
      user: 'sys/user',
      sysName: 'sys/sysName',
      sysLogo: 'sys/sysLogo',
      taskBarData: 'sys/taskBarData',
      idMenuMaps: 'sys/idMenuMaps',
      selectedKeys: 'sys/selectedKeys',
      activityMenu: 'sys/activityMenu',
      activityView: 'sys/activityView',
      breadcrumbRoutes: 'sys/breadcrumbRoutes',
      searchResultModel: 'sys/getSearchResultModel',
    })
  },
  components: {
    ReloadOutlined, LockFilled, LogoutOutlined, DownOutlined, CheckOutlined, UserOutlined
    , HomeOutlined, NotificationFilled, FullscreenOutlined, LayoutOutlined, SearchOutlined},
  setup() {
    let msgCount = 0
    let waitCount = 0;
    let workMenu = {};

    let searchStatus = ref(false);
    let themeConfig = inject(ThemeConfigKey);
    let colorData =themeConfig.candidateColorPrimary;

    // 监听shift + shift
    let shiftCountRef = ref(0);
    document.addEventListener('keyup',(e)=>{
      if(e.key == 'Shift'){
        shiftCountRef.value = shiftCountRef.value + 1;
        if(shiftCountRef.value == 2) {
          searchStatus.value = true;
        } else {
          let timeout = setTimeout(() => {
            shiftCountRef.value = 0;
            clearTimeout(timeout);
          }, 250);
        }
      }
    })
    return {workMenu, msgCount, waitCount, themeConfig, colorData, searchStatus}
  },
  methods: {
    ...mapMutations({
      logout: 'sys/logout',
      removeTask: 'sys/removeTask',
      onGlobalSearch: 'sys/onGlobalSearch',
      toggleUserVisible: 'sys/toggleUserVisible',
      switchActiveViewTo: 'sys/switchActiveViewTo',
      pushAndSwitchTask: 'sys/pushAndSwitchTask',
    }),
    switchTask (url) { // 切换任务菜单处理
      this.pushAndSwitchTask(url);
    },
    fullScreen() {
      screenfull.toggle()
    },
    /**
     * 搜索关键字处理
     * @param value
     */
    searchKeywordHandle(value) {
      this.onGlobalSearch(value.target.value);
    },
    /**
     * 搜索结果记录单机处理
     * @param model {SearchResultModel}
     */
    searchRecordClickHandle(model) {
      model.filter.onClick(model).then(() => {
        this.searchStatus = false
      }).catch(reason => reason);
    },
    quickOpera({key}) {
      if(key == 'logout') {
        // 注销动作
        logout().then(({code, message}) => {
          if(code == 200) {
            // 跳转到登录页面
            this.$router.push('/login').then(resp=>{
              this.logout(); // 注销成功回调处理
            });
          } else {
            msgError(message);
          }
        }).catch(reason => console.error(reason))
        // 其他快速操作
        // this.toggleUserVisible({visible: true, key});
      } else if(key == 'profile' || key == 'pwd'){
        this.$router.push({path: '/profile', query: {key}});
      }
    },

    colorPrimaryChange(value) {
      this.themeConfig.switchColorPrimary(value)
    },

    headerColorPrimaryChange(value) {
      this.themeConfig.switchHeaderColorPrimary(value);
    }
  }
}
</script>

<style scoped>
.u-header-bar {
    height: var(--header-height);
    line-height: var(--header-height);
    padding-inline: 0px;
    color: var(--header-color);
    background-color: var(--header-background-color);
}
.iv-header-breadcrumb {
  height: var(--header-height);
  line-height: var(--header-height);
}
.u-header-bar * {
  color: var(--header-color);
}
.iv-header-row {
  height: var(--header-height);
}
.iv-style-item-checkbox-icon {
  position: absolute;
  right: 8px;
  bottom: 8px;
  color: #1890ff;
  font-weight: 700;
  font-size: 14px;
  pointer-events: none;
}
.iv-style-item {
  position: relative;
  width: 44px;
  height: 36px;
  margin-right: 16px;
  overflow: hidden;
  background-color: #ebeef1;
  border-radius: 2px;
  box-shadow: 0 1px 2.5px #0000002e;
  cursor: pointer;
}
.iv-style-item:before {
  position: absolute;
  top: 0;
  left: 0;
  width: 33%;
  height: 100%;
  background-color: #fff;
  content: "";
}
.iv-style-item-dark {
  z-index: 1;
  content: "";
  background-color: #ebeef1;
}
.iv-style-item-dark:before {
  z-index: 1;
  background-color: #001529;
  content: "";
}
.iv-style-item-dark:after {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 25%;
  background-color: #fff;
  content: "";
}
.iv-style-item-light:before {
  content: "";
  background-color: #fff;
}
.iv-style-item-light:after {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 25%;
  background-color: #fff;
  content: "";
}
.iv-header-username {
  font-size: 15px!important;
}
.iv-global-search .iv-search-content {
  min-height: 80px;
  max-height: 180px;
  overflow-y: auto;
  margin-top: 8px;
  overflow-x: hidden;
}
.iv-global-search .iv-search-history {
  min-height: 120px;
}
.iv-search-content .iv-context-item {
  padding: 3px;
  cursor: pointer;
}
.iv-search-content .iv-context-item:hover {
  border-radius: 3px;
  background-color: #f9f9f9;
}
.u-opera-col {
  padding: 0px 12px;
}
.u-opera-col:hover {
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.06);
}
.u-header-col-left {
  text-align: left;
}
.ivz-header-col-right {
  right: 8px;
  text-align: right;
}
.ivz-header-col-right .anticon {
  font-size: 18px;
}
.ivz-header-col-right .u-opera-col {
  float: right;
}

</style>
