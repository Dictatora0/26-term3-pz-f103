<template>
  <UView name="协议网关">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="网关名称" :allowClear="true"/>
        <USelectItem field="status" label="网关状态" :allowClear="true" :options="gatewayStatus"/>
        <USelectItem field="protocolType" label="传输协议" url="/iot/gateway/transportProtocol" :refresh="false" />
        <USelectItem field="protocolId" label="报文协议" url="/iot/protocol/list" labelField="name" valueField="id"/>
        <UButton func="query" url="/iot/gateway/view" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/gateway/add" v-auth="'iot:gateway:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewDrawer title="协议网关管理" v-model="editModel" :rules="rules" placement="left" width="860" :span="[7, 16]" @edit="editHandle">
        <template #default="{model}">
          <URow :gutter="12" :responsive="2">
            <UInputItem field="name" label="网关名称" :maxlength="32"/>
            <USelectItem field="protocolType" label="传输协议" url="/iot/gateway/transportProtocol" defaultValue="TCP"
                         v-model:source="transportProtocolSource" :refresh="false"/>
            <URadioItem field="type" label="设备类型" :options="gatewayType" defaultValue="Direct" optionType="button" buttonStyle="solid"/>
            <URadioItem field="connectType" label="连接类型" :options="connectType" defaultValue="Server" optionType="button" buttonStyle="solid"/>
            <USelectItem field="protocolId" label="应用协议" url="/iot/gateway/protocols?protocolType={protocolType}&type={type}&connectionType={connectType}"
                         v-model:source="protocolSource" @change="protocolTypeChange" @loaded="protocolLoaded">
              <template #option="{value, label, config}">
                <div style="padding: 0px 0px 5px;">
                  <span style="float: left">{{label}}</span>
                  <span style="float: right">{{config?.bind}}</span>
                </div>
              </template>
            </USelectItem>
            <UTextareaItem field="remark" label="备注" :maxlength="128" />
            <a-divider v-if="protocolTypeConfig" style="margin-bottom: 12px" dashed>应用协议({{selectProtocol['label']}})配置, 重启组件后生效</a-divider>
            <template v-if="protocolTypeConfig" v-for="item in protocolTypeConfig">
              <UInputItem v-if="item['type']=='string'" :field="'config.'+item['field']" :label="item['label']" :required="item['required']"
                          :defaultValue="item['defaultValue']" :extra="item['extra']"/>
              <UInputNumberItem v-else-if="item['type']=='number'" :field="'config.'+item['field']" :label="item['label']" :required="item['required']"
                          :defaultValue="item['defaultValue']" :extra="item['extra']"/>
              <USelectItem v-else-if="item['type'] == 'select'" :field="'config.'+item['field']" :label="item['label']" :required="item['required']"
                           :defaultValue="item['defaultValue']" :extra="item['extra']" :placeholder="item['placeholder']" :options="item['options']" />
            </template>
        </URow>
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/gateway/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewDrawer>
    <UViewTable :columns="columns" :scroll="{x: 1200}">
      <template #status="{record, text, value}">
        <ABadge v-if="text=='start'" status="processing" color="#87d068" :text="value" />
        <ABadge v-else status="warning" :text="value" />
      </template>
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/iot/gateway/edit" v-auth="'iot:gateway:edit'">修改</UTag>
        <UTag func="ajax:confirm" :data="record" :url="`/iot/gateway/switch/${record['id']}/${record['status'] == 'start' ? 'stop' : 'start'}`"
              v-auth="'iot:gateway:switch'" :config="{confirmTitle: `确认${record['status'] == 'start' ? '停用' : '启用'}网关[${record.name}]`, confirmContent: `如果停止将导致绑定此网关的所有产品暂停服务`}">
          {{record['status'] == 'start' ? '停用' : '启用'}}
        </UTag>
        <UTag func="del" :data="record" url="/iot/gateway/del" v-auth="'iot:gateway:del'">删除</UTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 协议网关功能 -->
<script>
import {ref, reactive, watch} from "vue";
import {useRoute} from "vue-router";
export default {
  name: "Gateway",
  setup() {

    let sslStatus = ref([
      {label: '是', value: "true"},
      {label: '否', value: "false"},
    ]);

    let gatewayType = ref([
      {label: '设备直连', value: 'Direct'},
      {label: '边缘网关', value: 'Gateway'},
    ])

    let connectType = ref([
      {label: '服务端', value: 'Server'},
      {label: '客户端', value: 'Client'},
    ])

    let gatewayStatus = ref([
      {label: '启用', value: 'start'},
      {label: '停用', value: 'stop'},
    ])

    let columns = ref([
      {field: "name", title: "网关名称", width: 180},
      // {field: "type", title: "网关类型", options: gatewayType.value, width: 80},
      // {field: "host", title: "网关主机", width: 180},
      // {field: "port", title: "网关端口", width: 80},
      // {field: "username", title: "用户名"},
      {field: "protocolType", title: "传输协议", url: "/iot/gateway/transportProtocol", width: 80},
      {field: "protocolId", title: "应用协议", width: 160, url: '/iot/protocol/list', labelField: 'name', valueField: 'id'},
      {field: "config", title: "配置", width: 220, resizable: true, formatter: ({value}) => {
          return value != null ? JSON.stringify(value) : '';
        }
      },
      {field: "reason", title: "失败原因", width: 220, resizable: true},
      {field: "remark", title: "备注", width: 220, resizable: true},
      {field: "createTime", title: "创建时间", type: 'date', format: 'YYYY-MM-DD', width: 100},
      // {field: "updateTime", title: "更新时间", type: 'date', format: 'YYYY-MM-DD', width: 100},
      {field: "status", title: "状态", options: gatewayStatus.value, fixed: 'right', width: 80},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 180},
    ]);


    let rules = reactive({
      name: {required: true, message: '网关名称必填'},
      type: {required: true, message: '网关类型必填'},
      ssl: {required: true, message: '是否启用ssl必填'},
      // host: {message: '主机/串口必填'},
      // port: {required: true, message: '网关端口必填'},
      protocolId: {required: true, message: '应用协议必填'},
      connectType: {required: true, message: '连接类型必填'},
      protocolType: {required: true, message: '协议类型必填'},
    })

    let editModel = ref({});
    let searchModel = ref({});
    let searchRef = ref(null);
    let selectProtocol = ref({});
    let protocolSource = ref({});
    let protocolTypeConfig = ref([]);
    let transportProtocolSource = ref({});

    let route = useRoute();
    watch(() => route.query, (newValue) => {
      let name = newValue['name'];
      if(name) {
        searchModel.value['name'] = name;
        if(searchRef.value) {
          searchRef.value.trigger();
        }
      }
    }, {immediate: true})
    watch(() => protocolSource.value, () => {
      if(protocolSource.value && editModel.value['protocolId']) {
        selectProtocol.value = protocolSource.value[editModel.value['protocolId']];
        if(selectProtocol.value && selectProtocol.value['config']) {
          let config = selectProtocol.value['config']?.config;
          if(config instanceof Array && config.length > 0) {
            protocolTypeConfig.value = config; return;
          }
        }
      }
      protocolTypeConfig.value = null;
    })
    return {columns, rules, editModel, searchModel, gatewayType, gatewayStatus, sslStatus, protocolSource
      , connectType, transportProtocolSource, protocolTypeConfig, searchRef, selectProtocol}
  },
  methods: {
    editHandle(editModel) {
      this.selectProtocol = this.protocolSource[editModel['protocolId']];
      if(this.selectProtocol) {
        let config = this.selectProtocol['config']?.config;
        if(config instanceof Array && config.length > 0) {
          this.protocolTypeConfig = config;
        } else {
          this.editModel['config'] = {};
          this.protocolTypeConfig = null;
        }
      } else {
        this.selectProtocol = {};
        this.editModel['config'] = {};
        this.protocolTypeConfig = null;
      }
    },
    protocolTypeChange(value, model) {
      this.selectProtocol = model;
      let config = this.selectProtocol['config']?.config;
      if(config instanceof Array && config.length > 0) {
        this.protocolTypeConfig = config;
      } else {
        this.editModel['config'] = {};
        this.protocolTypeConfig = null;
      }
    },
    protocolLoaded(map) {
      this.protocolSource = {...map};
    }
  }
}
</script>
<style scoped> </style>
