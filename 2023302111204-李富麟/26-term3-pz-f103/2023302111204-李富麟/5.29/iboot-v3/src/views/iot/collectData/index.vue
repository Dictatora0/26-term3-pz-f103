<template>
  <UView name="采集数据">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UTreeSelectItem field="productId" label="所属产品" url="/iot/product/list"/>
        <USelectItem field="status" label="状态" :options="statusOptions"/>
<!--        <USelectItem field="collectTaskId" label="所属任务" labelField="name" valueField="id"-->
<!--                 url="/iot/collectTask/list"/>-->
        <UButton func="query" url="/iot/collectData/signal" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1100}">
      <template #status="{record, text}">
        <a-tag color="#87d068" v-if="text">成功</a-tag>
        <a-tag color="#f50" v-else>失败</a-tag>
      </template>
<!--          <template #action="{record}">-->
<!--            <UFuncTag func="del" :data="record" url="/iot/device/del">删除</UFuncTag>-->
<!--          </template>-->
    </UViewTable>
  </UView>
</template>
<!-- 设备功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "CollectData",
  setup() {
    let columns = ref([
      {field: "deviceName", title: "设备名称", width: 120},
      {field: "deviceSn", title: "设备编号", width: 120},
      {field: "fieldName", title: "点位名称", width: 100, resizable: true},
      {field: "address", title: "点位地址", width: 100, resizable: true},
      {field: "value", title: "采集值", width: 120},
      {field: "collectTime", title: "采集时间", width: 100, type: 'date', format: 'YYYY-MM-DD HH:mm:ss', fixed: 'right'},
      {field: "reason", title: "失败原因", width: 160, resizable: true},
      {field: "status", title: "采集状态", width: 60, fixed: 'right'},
      // {field: 'action', title: '操作', type: 'action' , width: 150, fixed: 'right'},
    ]);

    let statusOptions = [
      {label: '成功', value: true},
      {label: '失败', value: false},
    ];

    // 只显示点位采集的数据
    let searchModel = ref({});
    return {columns, searchModel, statusOptions}
  },
  beforeRouteEnter(to, form, next) {
    next(vm => {
      if(to.query.taskId) {
        vm.searchModel['collectTaskId'] = to.query.taskId;
      }
    })
  },
  methods: {
    selectParent(selectedKeys) {
      this.searchModel.deviceTypeId = selectedKeys[0]
      this.$refs['searchRef'].trigger();
    }
  }
}
</script>
<style scoped> </style>
