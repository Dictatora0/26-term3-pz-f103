<template>
  <UView name="报文协议">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="协议名称" :allowClear="true" :maxlength="32"/>
        <UInputItem field="code" label="协议代码" :allowClear="true" :maxlength="32"/>
        <USelectItem field="type" label="传输协议" url="/iot/gateway/transportProtocol" :refresh="false" :allowClear="true" />
        <USelectItem field="ctrlMode" label="操控方式" url="/iot/protocol/ctrlModes":refresh="false"  :allowClear="true"/>
        <UButton func="query" url="/iot/protocol/view" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/protocol/add" v-auth="'iot:protocol:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewDrawer title="报文协议管理" :span="[6, 16]" v-model="editModel" :rules="rules">
      <template #default="{model}">
        <UInputItem field="name" label="协议名称" :maxlength="32" />
        <URadioItem field="implMode" label="实现方式" url="/iot/protocol/implModes" :refresh="false"  defaultValue="Jar" optionType="button" buttonStyle="solid"/>
        <UInputItem v-if="editModel['implMode'] != 'Internal'" field="code" label="协议代码" :disabled="model['implMode'] != 'Custom'" extra="协议代码请用[a-zA-Z-_]且必须唯一" />
        <USelectItem v-else field="code" label="协议代码" url="/iot/protocol/internals" extra="协议代码请用[a-zA-Z-_]且必须唯一" @change="codeChange">
          <template #option="{value, label, extra}">
            <div style="padding: 0px 0px 5px;">
              <span style="float: left">{{label}}</span>
              <span style="float: right; color: #bbbbbb; font-weight: 0">
                {{extra['id'] ? '已添加' : ''}}
              </span>
            </div>
          </template>
        </USelectItem>
        <UUploadItem v-if="editModel['implMode'] == 'Jar'" field="jarPath" label="jar文件" :withCredentials="true"
                     :action="`${$apiUrl}/iot/protocol/loadProtocol`" filename="file" accept=".jar" :maxCount="1" @change="uploadChange">
          <a-input-group compact>
            <AInput v-model:value="editModel['jarPath']" disabled style="width: 60%"/>
            <AButton><UIcon type="iz-icon-upload" /> 点击上传</AButton>
          </a-input-group>
        </UUploadItem>
        <UInputItem field="version" label="协议版本" :disabled="model['implMode'] != 'Custom'" />
        <USelectItem field="type" label="传输协议" :refresh="false" url="/iot/gateway/transportProtocol" :disabled="model['implMode'] != 'Custom'" />
        <URadioItem field="ctrlMode" label="操控方式" :refresh="false"  url="/iot/protocol/ctrlModes" :disabled="model['implMode'] != 'Custom'"
                    optionType="button" buttonStyle="solid" defaultValue="COMMON"/>
        <URadioItem field="connectionType" label="连接类型" :options="connectType" :disabled="model['implMode'] != 'Custom'"
                    optionType="button" buttonStyle="solid" defaultValue="Server" required/>
        <URadioItem field="gatewayType" label="设备类型" :options="gatewayType" :disabled="model['implMode'] != 'Custom'"
                    optionType="button" buttonStyle="solid" defaultValue="Direct" required/>
        <UTextareaItem field="remark" label="备注" :maxlength="128" />
<!--        <UInputItem field="checkType" label="校验类型" />-->
<!--        <UInputItem field="decoderType" label="解码器类型" />-->
<!--        <UInputItem field="decoderConfig" label="解码器配置" />-->
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/protocol/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewDrawer>
    <UViewTable :columns="columns" :scroll="{x: 1200}">
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/iot/protocol/edit" v-auth="'iot:protocol:edit'">修改</UTag>
        <UTag func="open" :data="record" uid="config" url="/iot/protocol/detail" v-auth="'iot:protocol:config'">协议配置</UTag>
        <UTag func="del" :data="record" url="/iot/protocol/del" v-auth="'iot:protocol:del'">删除</UTag>
      </template>
    </UViewTable>
    <UDrawer uid="config" placement="top" height="100%" v-model:source="protocolDetail" title="协议配置">
      <div>
        <a-descriptions title="协议基础信息" bordered :column="{xs: 1, sm: 2, md: 3, lg: 3, xl: 5}">
          <a-descriptions-item label="协议名称">{{protocolDetail.name}}</a-descriptions-item>
          <a-descriptions-item label="协议代码">{{protocolDetail['code']}}</a-descriptions-item>
          <a-descriptions-item label="传输协议">{{protocolDetail['typeName']}}</a-descriptions-item>
          <a-descriptions-item label="控制方式">{{protocolDetail['ctrlModeName']}}</a-descriptions-item>
          <a-descriptions-item label="实现方式">{{protocolDetail['implModeName']}}</a-descriptions-item>
          <a-descriptions-item label="备注" span="3">{{protocolDetail['remark']}}</a-descriptions-item>
        </a-descriptions>
        <a-tabs v-model:activeKey="activeKey" style="margin-top: 16px">
          <a-tab-pane key="attr" tab="协议属性">
            <UChildView uid="attr">
              <UViewTable :columns="attrColumns" :dataSource="protocolDetail['attrs']" rowKey="field">
                <template #name="{record, label, value}">
                  {{value}}
                </template>
                <template #action="{record}">
                  <UTag func="edit" :disabled="record['implModeName'] != 'Custom'">修改</UTag>
                </template>
              </UViewTable>
            </UChildView>
          </a-tab-pane>
          <a-tab-pane key="func" tab="功能协议">
            <UChildView uid="func">
              <UViewTable :columns="apiColumns" :dataSource="protocolDetail['funcApis']" rowKey="code" :pagination="null">
                <template #action="{record}">
                  <UTag func="edit" :disabled="record['implModeName'] != 'Custom'">修改</UTag>
                </template>
              </UViewTable>
            </UChildView>
          </a-tab-pane>
          <a-tab-pane key="event" tab="事件协议">
            <UChildView uid="event">
              <UViewTable :columns="eventColumns" :dataSource="protocolDetail['eventApis']" rowKey="code" :pagination="null">
                <template #action="{record}">
                  <UTag func="edit" :disabled="record['implModeName'] != 'Custom'">修改</UTag>
                </template>
              </UViewTable>
            </UChildView>
          </a-tab-pane>
        </a-tabs>
      </div>
    </UDrawer>
    <AModal v-model:open="apiConfigVisible" title="接口配置">
      <UTable></UTable>
    </AModal>
  </UView>
</template>
<!-- 报文协议功能 -->
<script>
import {ref, reactive, watch} from "vue";
import {useRoute} from "vue-router";
import CoreConsts from "@/components/CoreConsts";
export default {
  name: "Protocol",
  setup() {
    let columns = ref([
      {field: "name", title: "协议名称", width: 256},
      {field: "code", title: "协议代码", width: 256},
      {field: "version", title: "协议版本", width: 256},
      {field: "type", title: "传输协议", url: '/iot/gateway/transportProtocol', width: 120},
      {field: "ctrlMode", title: "操控方式", url: "/iot/protocol/ctrlModes", width: 120},
      {field: "implMode", title: "实现方式", url: "/iot/protocol/implModes", width: 120},
      // {field: "jarPath", title: "jar文件路径"},
      {field: "remark", title: "备注", ellipsis: true},
      // {field: "checkType", title: "校验类型"},
      // {field: "decoderType", title: "解码器类型"},
      // {field: "decoderConfig", title: "解码器配置"},
      {field: "createTime", title: "创建时间", type: 'date', format: 'YYYY-MM-DD', width: 120},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 260},
    ]);
    let gatewayType = ref([
      {label: '设备直连', value: 'Direct'},
      {label: '边缘网关', value: 'Gateway'},
    ])

    let connectType = ref([
      {label: '服务端', value: 'Server'},
      {label: '客户端', value: 'Client'},
    ])
    let rules = reactive({
      name: {required: true, message: '协议名称必填'},
      code: [{required: true, message: '协议代码必填'}, {pattern: /[a-zA-Z-_]/, message: '协议码只能使用字符[a-zA-Z-_]'}],
      type: {required: true, message: '传输协议必填'},
      version: {required: true, message: '协议版本必填'},
      ctrlMode: {required: true, message: '操控方式必填'},
      jarPath: {required: true, message: 'jar文件必填'},
      implMode: {required: true, message: '实现方式必填'},
    })

    let attrColumns = ref([
      {field: "name", title: "属性名称"},
      {field: "field", title: "属性标识"},
      {field: "type", title: "属性类型"},
      {field: "accuracy", title: "精度"},
      {field: "unit", title: "单位"},
      {field: "remark", title: "属性说明"},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 260},
    ])

    let apiColumns = ref([
      {field: "name", title: "功能名称"},
      {field: "code", title: "功能标识"},
      {field: "remark", title: "功能说明"},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 260},
    ])

    let eventColumns = ref([
      {field: "name", title: "事件名称"},
      {field: "code", title: "事件标识"},
      {field: "remark", title: "事件说明"},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 260},
    ])

    let activeKey = ref('attr');
    let editModel = ref({});
    let searchRef = ref(null);
    let searchModel = ref({});
    let protocolDetail = ref({});
    let apiConfigVisible = ref(false);
    let route = useRoute();
    watch(() => route.query, (newValue) => {
      let code = newValue['code'];
      if(code) {
        searchModel.value['code'] = code;
        if(searchRef.value) {
          searchRef.value.trigger();
        }
      }
    }, {immediate: true})

    return {columns, rules, searchRef, editModel, searchModel, protocolDetail, activeKey
      , attrColumns, apiColumns, eventColumns, apiConfigVisible, gatewayType, connectType}
  },
  methods: {
    codeChange(code, model) {
      Object.keys(this.editModel).forEach(key => {
        if(key != 'name') {
          this.editModel[key] = model['extra'][key];
        }
      })
    },
    uploadChange(event) {
      let fileList = event['fileList'];
      if(fileList instanceof Array && fileList.length > 0) {
        fileList.filter(item => item['status'] == 'done').forEach(item => {
          let {code, message, data} = item['response'];
          if(code == CoreConsts.SuccessCode) {
            Object.keys(this.editModel).forEach(key => {
              if(key != 'name') {
                this.editModel[key] = data[key];
              }
            })
          } else {
            fileList.length = 0;
            this.$msg.error(message)
          }
        })
      }
    },
  }
}
</script>
<style scoped> </style>
