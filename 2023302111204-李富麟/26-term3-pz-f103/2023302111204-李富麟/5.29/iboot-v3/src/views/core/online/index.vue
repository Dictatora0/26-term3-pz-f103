<template>
  <UView>
    <a-row :gutter="16" style="padding: 0px 12px; margin-bottom: 8px">
      <a-col class="iv-session-item" span="8">
        <div>
          <UIcon type="iz-icon-online" style="font-size: 32px"></UIcon>
          <h3 class="iv-item-text">当前在线量: {{countToday.currentOnline}}</h3>
        </div>
      </a-col>
      <a-col class="iv-session-item" span="8">
        <div>
          <UIcon type="iz-icon-account-count" style="font-size: 32px"></UIcon>
          <h3 class="iv-item-text">当天账号量:
            <span style="font-size: 15px">&nbsp;{{countToday.loginAccount}}</span>
          </h3>
        </div>
      </a-col>
      <a-col class="iv-session-item" span="8">
        <div>
          <UIcon type="iz-icon-user-count" style="font-size: 32px"></UIcon>
          <h3 class="iv-item-text">当天访问量: {{countToday.todayAccess}}</h3>
        </div>
      </a-col>
    </a-row>
    <UViewSearch>
      <URow col="search">
        <UInputItem label="登录用户" field="userNick"/>
        <USelectItem label="在线状态" field="status" :options="OnlineStatus" allowClear/>
        <UButton func="query" url="/core/online/view" v-auth="'core:online:view'">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="del:batch" url="/core/online/del" v-auth="'core:online:del'">删除</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1100}" :rowSelection="{}">
      <template #status="{record, value}">
        <ABadge v-if="record.status=='Online'" status="processing" :text="value" />
        <ABadge v-else status="default" :text="value" />
      </template>
      <template #c_action="{record}">
        <UTag func="ajax:confirm" :data="record" url="/core/online/offline" v-auth="'core:online:offline'"
            :config="{confirmTitle: '强退确认', confirmContent: '确认强制下线此用户吗?'}" color="#9718FFC8" >强退</UTag>
      </template>
    </UViewTable>
  </UView>
</template>

<script>

import {ref} from "vue";
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "Online",
  setup() {
    let OnlineStatus = [
      {label: '在线', value: 'Online'},
      {label: '离线', value: 'Offline'},
    ]

    let columns = [
      {field: 'userNick', title: '登录用户', width: 138},
      {field: 'loginTime', title: '登录时间', width: 160, type: 'date'},
      {field: 'type', title: '登录设备', width: 128},
      {field: 'os', title: '操作系统', width: 128},
      {field: 'browse', title: '浏览器/App', width: 100},
      {field: 'accessIp', title: '登录ip', width: 138},
      {field: 'updateTime', title: '最后访问时间', type: 'date', width: 160},
      {field: 'status', title: '状态', width: 78, options: OnlineStatus},
      {field: 'action', title: '操作', type: 'action', width: 88, fixed: 'right'},
    ]

    let countToday = ref({currentOnline: 0, todayAccess: 0, loginAccount: 0});
    return {columns, OnlineStatus, countToday}
  },
  created() {
    this.$http.get("/core/online/countToday").then(resp => {
      if(resp['code'] == CoreConsts.SuccessCode) {
        this.countToday = resp['data'];
      }
    })
  }
}
</script>

<style scoped>
.iv-session-item {
  text-align: center;
}
.iv-session-item div {
  height: 100%;
  padding: 8px;
  border-radius: 8px;
  background-color: #ffffff;
}
.iv-item-text {
  font-size: 13px;
  margin-top: 5px
}
</style>
