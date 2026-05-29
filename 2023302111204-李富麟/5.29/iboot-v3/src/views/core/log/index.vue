<template>
  <UView>
    <UViewSearch>
      <URow col="search">
        <UInputItem label="操作用户" field="userName" />
        <USelectItem label="状态" field="status" :options="SuccessStatus"/>
        <UInputNumberItem label="执行时间" field="millis" addonBefore=">" addonAfter="ms" :min="0" style="width: 200px"/>
        <UButton func="reset">重置</UButton>
        <UButton func="query" url="/core/log/view">搜索</UButton>
        <UButton func="del:batch" url="/core/log/del">删除</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1200}" :rowSelection="{}">
      <template #c_action="{record}">
        <UFuncTag func="del" :data="record" url="/core/log/del" v-auth="'core:log:del'">删除</UFuncTag>
      </template>
    </UViewTable>
  </UView>
</template>

<script>
import {SuccessStatus} from "@/utils/StatusConsts";
import {reactive} from "vue";

export default {
  name: "Log",
  setup() {
    let columns = reactive([
      {field: 'userName', title: '操作用户', ellipsis: true, width: 180},
      {field: 'title', title: '功能', width: 100, ellipsis: true},
      {field: 'url', title: '请求地址', width: 160, ellipsis: true, resizable: true},
      {field: 'millis', title: '执行时间(ms)', width: 120},
      {field: 'ip', title: '访问ip', ellipsis: true, width: 120},
      {field: 'errMsg', title: '日志', ellipsis: true, resizable: true, width: 280},
      {field: 'status', title: '状态', options: SuccessStatus, width: 60},
      {field: 'createTime', title: '创建时间', type: 'date', width: 160},
      {field: 'action', title: '操作', type: 'action', width: 80},
    ])

    return {columns, SuccessStatus}
  }
}
</script>

<style scoped>

</style>
