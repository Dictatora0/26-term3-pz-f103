<template>
  <UView name="串口设备">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="设备名称" :allowClear="true"/>
        <UInputItem field="status" label="状态" :allowClear="true"/>
        <UButton func="query" url="/iot/serial/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/serial/add" v-auth="'iot:serial:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewModal title="串口设备管理" :span="[7, 14]" v-model="editModel" :rules="rules">
      <template #default="{model}">
        <UInputItem field="name" label="串口名称" />
        <USelectItem field="com" label="串口" url="/iot/serial/available" />
        <UInputNumberItem field="baudRate" label="波特率" :defaultValue="9600"/>
        <UInputNumberItem field="dataBits" label="数据位" :defaultValue="8"/>
        <UInputNumberItem field="stopBits" label="停止位" :defaultValue="1" />
        <UInputNumberItem field="parity" label="校验位" :defaultValue="0" />
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/serial/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <UViewTable :columns="columns">
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/iot/serial/edit" v-auth="'iot:serial:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/iot/serial/del" v-auth="'iot:serial:del'">删除</UTag>
        <UTag func="ajax" :data="record" color="#55acee" v-auth="'iot:serial:connect'"
              :url="'/iot/serial/connect/'+(record.status=='open' ? 'close' : 'open')">
          {{record.status=='open' ? '关闭' : '打开'}}
        </UTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 串口设备功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "Serial",
  setup() {
    let columns = ref([
      {field: "name", title: "串口名称"},
      {field: "com", title: "串口"},
      {field: "baudRate", title: "波特率"},
      {field: "dataBits", title: "数据位"},
      {field: "parity", title: "校验位"},
      {field: "stopBits", title: "停止位"},
      {field: "status", title: "状态"},
      {field: "createTime", title: "创建时间", type: 'date', format: 'YYYY-MM-DD'},
      {field: 'action', title: '操作', type: 'action'},
    ]);

    let rules = reactive({
      name: {required: true, message: '设备名称必填'},
      com: {required: true, message: '串口必填'},
      baudRate: {required: true, message: '波特率必填'},
      dataBits: {required: true, message: '数据位必填'},
      status: {required: true, message: '状态必填'},
      createTime: {required: true, message: '必填'},
    })

    let editModel = ref({});
    let searchModel = ref({});
    return {columns, rules, editModel, searchModel}
  },
}
</script>
<style scoped> </style>
