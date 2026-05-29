<template>
  <UView name="数据采集任务">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="任务名称" :allowClear="true"/>
        <UInputItem field="status" label="任务状态" :allowClear="true"/>
        <USelectItem field="pointGroupId" label="所属组" url="/iot/pointGroup/list" labelField="name" valueField="id"/>
        <UButton func="query" url="/iot/collectTask/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/collectTask/add" v-auth="'iot:collectTask:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewModal title="数据采集任务管理" :span="[7, 14]" v-model="editModel" :rules="rules" @edit="onEdit">
      <template #default="{model}">
        <UInputItem field="name" label="任务名称" />
        <UInputItem field="cron" label="调度" />
        <UInputItem field="remark" label="任务说明" />
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/collectTask/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <UDrawer uid="config" v-model:source="detailModel" title="采集任务配置" placement="top" height="100%" @open="onOpenDetail">
      <ULinkView uid="configView">
        <UViewSearch v-model="detailSearchModel">
          <URow col="search">
            <USelectItem field="pointGroupId" label="点位组" labelField="name" valueField="id" url="/iot/pointGroup/list" allowClear/>
            <UButton func="query" url="/iot/collectDetail/details">搜索</UButton>
            <UButton func="reset">重置</UButton>
            <UButton func="add">新增</UButton>
          </URow>
        </UViewSearch>
        <UViewModal title="任务详情管理" width="820px" :span="[7, 14]" v-model="detailEditModel" :rules="detailRules" @edit="onDetail">
          <template #default="{model}">
            <URow col="modal">
              <USelectItem field="pointGroupId" label="点位组" labelField="name" valueField="id" url="/iot/pointGroup/list"/>
              <USelectItem field="storeAction" label="存储动作" url="/iot/collectTask/storeActions" />
              <UTextareaItem field="extend" :span="22" :labelCol="{span: 4}" :wrapperCol="{span: 20}"
                         label="存储配置" defaultValue="{}" :rows="7"/>
            </URow>
          </template>
          <template #footer="{model}">
            <UFuncBtn func="cancel">取消</UFuncBtn>
            <UFuncBtn func="submit" url="/iot/collectDetail/saveOrUpdate">提交</UFuncBtn>
            <UFuncBtn func="reset">重置</UFuncBtn>
          </template>
        </UViewModal>
        <UViewTable :columns="detailColumns" :scroll="{x: 1000}">
          <template #action="{record}">
            <UFuncTag func="edit" :data="record" url="/iot/collectDetail/edit">修改</UFuncTag>
            <UFuncTag func="del" :data="record" url="/iot/collectDetail/del">删除</UFuncTag>
          </template>
        </UViewTable>
      </ULinkView>
    </UDrawer>
    <UViewTable :columns="columns" :scroll="{x: 800}">
      <template #status="{record, value, text}">
        <span>
          <ABadge v-if="text=='run'" status="processing"/>
          <ABadge v-else-if="text=='fail'" status="error"/>
          <ABadge v-else color="orange"/>
          {{value}}
        </span>
      </template>
      <template #action="{record}">
        <UFuncTag func="edit" :data="record" url="/iot/collectTask/edit" v-auth="'iot:collectTask:edit'">修改</UFuncTag>
        <UFuncTag func="open" uid="config" :data="record" color="#87d068" v-auth="'iot:collectTask:config'">配置</UFuncTag>
        <UFuncTag func="del" :data="record" url="/iot/collectTask/del" v-auth="'iot:collectTask:del'">删除</UFuncTag>
        <UFuncTag func="ajax:confirm" url="/iot/collectTask/status" v-auth="'iot:collectTask:status'" :data="record" :params="getStatusParams"
                  :config="{confirmTitle: '采集任务切换', confirmContent: `确认切换到[${record.status=='run' ? '停止' : '运行'}]?`}">
          {{record.status=='run' ? '停止' : '运行'}}
        </UFuncTag>
        <UFuncTag func="link" :url="'/iot/collectData?taskId='+record.id" :data="record" v-auth="'iot:collectTask:data'">数据</UFuncTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 数据采集任务功能 -->
<script>
import {reactive, ref} from "vue";

export default {
  name: "CollectTask",
  setup() {
    let columns = ref([
      {field: "name", title: "任务名称", ellipsis: true, width: 160},
      {field: "cron", title: "任务调度", ellipsis: true, width: 160},
      // {field: "modelName", title: "设备型号"},
      // {field: "deviceName", title: "采集设备"},
      {field: "updateTime", title: "启动/停止时间", type: 'date', format: 'YYYY-MM-DD HH:mm:ss', width: 160},
      {field: "remark", title: "采集任务说明", width: 220, ellipsis: true},
      {field: "reason", title: "失败原因", width: 250, ellipsis: true},
      {field: "createTime", title: "创建时间", type: 'date', format: 'YYYY-MM-DD', width: 100},
      {field: "status", title: "任务状态", dict: 'iot_collect_status', width: 80, fixed: 'right'},
      {field: 'action', title: '操作', type: 'action', width: 270, fixed: 'right'},
    ]);

    let detailColumns = ref([
      {field: "pointGroupName", title: "点位组", width: 220, ellipsis: true},
      {field: "signalNum", title: "点位数量", width: 120, ellipsis: true},
      {field: "storeAction", title: "存储动作", width: 280, url: "/iot/collectTask/storeActions"},
      {field: "extend", title: "存储配置"},
      {field: "action", type:'action', title: "操作", width: 160, fixed: 'right'}
    ]);

    let rules = reactive({
      name: {required: true, message: '任务名称必填'},
      cron: {required: true, message: '任务调度必填'},
      modelId: {required: true, message: '设备型号必填'},
    })

    let detailRules = reactive({
      uid: {required: true, message: '采集设备必填'},
      pointGroupId: {required: true, message: '点位组必填'},
      storeAction: {required: true, message: '存储动作必填'},
      collectAction: {required: true, message: '采集动作必填'},
    })

    let editModel = ref({});
    let searchModel = ref({});
    let detailModel = ref({});
    let detailEditModel = ref({});
    let detailSearchModel = ref({});
    return {columns, rules, editModel, searchModel, detailModel, detailColumns, detailSearchModel, detailEditModel, detailRules}
  },
  methods: {
    onEdit(editModel) {
      if(!editModel.details) {
        editModel.details = [];
      }
    },
    onDetail(model) {
      if(!model['collectTaskId']) {
        model['collectTaskId'] = this.detailModel['id'];
      }
    },
    onOpenDetail(model) {
      this.detailSearchModel.id = model.id;

    },
    getStatusParams(record) {
      return Promise.resolve({id: record.id, status: record.status == 'run' ? 'stop' : 'run'})
    }
  }
}
</script>
<style scoped> </style>
