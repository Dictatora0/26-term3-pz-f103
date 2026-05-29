<template>
  <UView>
    <ARow :gutter="8" style="height: 100%; min-width: 1230px">
      <ACol span="6">
<!--        <a-input-search v-model:value="searchValue" style="margin-bottom: 8px" placeholder="请输入要搜索的设备" />-->
        <UTree url="/iot/debug/tree" childUrl="/iot/debug/tree?productId={id}" blockNode :resolverNode="resolverNode" ref="treeRef" style="padding: 12px;"
               :fieldNames="{title: 'name', key: 'key', children: 'children'}" showLine selectable @select="deviceSelect" @loaded="loadedData">
          <template #title="{data, title}">
            <div>
              <FolderFilled v-if="!data['isLeaf']"/>
              <span>&ensp;{{data.name}}</span>
              <span style="float: right; color: #a8a8a8; font-size: 10px">
                <template v-if="!data['isLeaf']">
                  {{data['type'] == 'productType' ? '产品类型' : '产品'}}
                </template>
                <template v-else>
                  <template v-if="data['status'] == 'online'">
                    <UIcon type="iz-icon-device-online" :style="{fontSize: '16px'}"/>
                  </template>
                  <template v-else>
                    <UIcon type="iz-icon-device-offline" :style="{fontSize: '16px'}"/>
                  </template>
                </template>
              </span>
            </div>
          </template>
        </UTree>
      </ACol>
      <ACol span="18" style="padding: 12px; background: #ffffff" class="iv-main-border radius">
        <a-spin size="large" :spinning="spinning" tip="数据加载中">
          <a-empty v-if="device['id'] == null" style="margin-top: 160px;"/>
          <div v-else>
            <a-descriptions bordered :column="{xs: 1, sm: 2, md: 3, lg: 3, xl: 4}" size="small">
              <template #title>
                <div>
                  <span style="float: left">设备详情({{device.name}})</span>
                  <span style="float: right; padding-right: 18px">
                    <ABadge v-if="device.status=='online'" status="processing" color="#87d068" text="在线" />
                    <ABadge v-else status="warning" text="离线" />
                  </span>
                </div>
              </template>
              <a-descriptions-item label="设备名称">
                <router-link :to="getToUrl(device)">{{device['name']}}</router-link>
              </a-descriptions-item>
              <a-descriptions-item label="设备编号">{{device['deviceSn']}}</a-descriptions-item>
              <a-descriptions-item label="设备类型">{{product['deviceTypeName']}}</a-descriptions-item>
              <a-descriptions-item label="网关设备">{{product['deviceTypeName']}}</a-descriptions-item>
              <a-descriptions-item label="产品类型">{{device['productTypeName']}}</a-descriptions-item>
              <a-descriptions-item label="所属产品">
                <router-link :to="`/iot/product?code=${product['code']}`">{{product['name']}}</router-link>
              </a-descriptions-item>
<!--              <a-descriptions-item label="安装地址" span="2">{{device['address']}}</a-descriptions-item>-->
              <a-descriptions-item label="接入网关">
                <router-link :to="`/iot/gateway?name=${product['gatewayName']}`">{{product['gatewayName']}}</router-link>
              </a-descriptions-item>
              <a-descriptions-item label="所属协议">
                <router-link :to="`/iot/protocol?code=${product['protocolCode']}`">{{product['protocolName']}}</router-link>
              </a-descriptions-item>
<!--              <a-descriptions-item label="备注" span="3">{{device['remark']}}</a-descriptions-item>-->
            </a-descriptions>
            <div style="border-top: 1px dashed #e3e3e3; border-bottom: 1px dashed #e3e3e3; margin-top: 8px">
              <a-tabs v-model:activeKey="activeKey" size="small">
                <a-tab-pane key="func" tab="功能接口">
                  <ATabs v-if="funcApis.length > 0" class="iv-api-item">
                    <ATabPane v-for="item in funcApis" :key="item['code']" class="iv-api-func">
                      <template #tab>
                        <div style="width: 230px">
                          <a-card :bordered="true" class="iv-model-api" hoverable>
                            <template #title>
                              <UIcon type="iz-icon-debug-api" :style="{marginRight: '3px'}"/>{{item.name}}
                            </template>
                            <div style="text-align: center">
                              {{item['code']}}
                              <p>{{item['remark']}}</p>
                            </div>
                            <template #actions>
                              <a-input-group compact v-if="apiCodeDebugMap[item['code']]?.length > 0">
                                <ASelect v-model:value="item.debugParam" :options="apiCodeDebugMap[item['code']]" style="width: 140px;"
                                         placement="topRight" :dropdownMatchSelectWidth="false"/>
                                <AButton type="primary" @click="() => submitDebug(item, true)">调试</AButton>
                              </a-input-group>
                              <AButton v-else block type="primary" @click="() => submitDebug(item, false)">调试</AButton>
                            </template>
                          </a-card>
                        </div>
                      </template>
                    </ATabPane>
                  </ATabs>
                  <a-empty v-else description="暂无功能接口"/>
                </a-tab-pane>
                <a-tab-pane key="event" tab="事件接口">
                  <ATabs v-if="eventApis.length > 0" class="iv-api-item">
                    <ATabPane v-for="item in eventApis" :key="item['code']" class="iv-api-func">
                      <template #tab>
                        <div style="width: 230px">
                          <a-card :bordered="true" class="iv-model-api" hoverable>
                            <template #title>
                              <UIcon type="iz-icon-debug-api" :style="{marginRight: '3px'}"/>{{item.name}}
                            </template>
                            <div>
                              {{item['code']}}
                              <p>{{item['remark']}}</p>
                            </div>
                            <template #actions>
                              <a-input-group compact v-if="apiCodeDebugMap[item['code']]?.length > 0">
                                <ASelect v-model:value="item.debugParam" :options="apiCodeDebugMap[item['code']]" style="width: 140px;"
                                         placement="topRight" :dropdownMatchSelectWidth="false"/>
                                <AButton type="primary" @click="() => submitDebug(item, true)">调试</AButton>
                              </a-input-group>
                              <AButton v-else block type="primary" @click="() => submitDebug(item, false)">调试</AButton>
                            </template>
                          </a-card>
                        </div>
                      </template>
                    </ATabPane>
                  </ATabs>
                  <a-empty v-else description="暂无事件接口"/>
                </a-tab-pane>
              </a-tabs>
            </div>
            <AFlex style="height: 24px; margin-top: 10px;">
              <div style="flex-grow: 1" class="iv-debug-header">调试参数</div>
              <div style="width: 250px;border-left: 1px dashed #e3e3e3;" class="iv-debug-header">请求参数</div>
            </AFlex>
            <AFlex >
              <div style="overflow: auto; flex-grow: 1">
                <div v-if="debugResult">
                  <div class="iv-debug result">
                    <span class="status-item">
                      <label class="title">[执行状态]</label>
                      <label class="value" v-if="debugResult?.['status'] == 'success'" style="color: #008d4c">
                        成功
                      </label>
                      <label v-else class="value" style="color: #d4380d">
                        失败<label>({{debugResult?.['reason']}})</label>
                      </label>
                    </span>
                  </div>
                  <div class="iv-debug result">
                    <span class="status-item">
                      <label class="title">[执行指令]</label>
                      <label class="value">{{currentDebugApi?.['code']}} -> {{currentDebugApi?.['direct']}}</label>
                    </span>
                  </div>
                  <div class="iv-debug result">
                    [请求报文] {{debugResult?.['reqMsg']}}
                  </div>
                  <div class="iv-debug result">
                    [响应报文] {{debugResult?.['respMsg']}}
                  </div>
                  <div class="iv-debug result">
                    <span class="status-item">
                      <label class="title">[执行耗时]</label>
                      <label class="value"> {{debugResult?.['respTime'] - debugResult?.['reqTime']}}(ms)</label>
                    </span>
                  </div>
                  <div class="iv-debug result">
                    [执行结果] {{debugResult?.['value']}}
                  </div>
                </div>
                <AEmpty v-else description="暂无调试参数" style="margin-top: 16px"/>
              </div>
              <div style="border-left: 1px dashed #e3e3e3; min-width: 250px;">
                <pre v-if="requestParamCompute">{{requestParamCompute}}</pre>
                <AEmpty v-else description="暂无请求参数" style="margin-top: 16px"/>
              </div>
            </AFlex>
          </div>
        </a-spin>
      </ACol>
    </ARow>
  </UView>
</template>
<!-- 设备功能 -->
<script>
import {ref, reactive, watch, computed} from "vue";
import {FolderFilled} from "@ant-design/icons-vue";
import CoreConsts from "@/components/CoreConsts";
export default {
  name: "Debug",
  components: {FolderFilled},
  setup() {

    let spinning = ref(false);
    let device = ref({name: '', status: '', uid: ''});
    let product = ref({});
    let funcApis = ref([]);
    let treeRef = ref(null);
    let eventApis = ref([]);
    let debugParam = ref(null);
    let debugResult = ref(null);
    let expandedKeys = ref([]);
    let requestParam = ref(null);
    let activeKey = ref('func');
    let currentDebugApi = ref(null);
    let apiCodeDebugMap = ref({});

    let requestParamCompute = computed(() => {
      return requestParam.value ? JSON.stringify(requestParam.value, null, 2) : '';
    })

    return {device, funcApis, spinning, eventApis, product, expandedKeys, treeRef, activeKey
      , apiCodeDebugMap, debugParam, requestParam, requestParamCompute, currentDebugApi, debugResult}
  },
  methods: {
    loadedData(data) {
      if(this.expandedKeys.length == 0) {
        Object.values(data).forEach(item => {
          if(item['type']=='productType') {
            this.expandedKeys.push(item['key']);
          }
        });

        this.treeRef.setExpandedKeys(this.expandedKeys);
      }
    },
    submitDebug(api, hasParam) {
      let debugParam = api['debugParam'];
      if(hasParam && !debugParam) {
        return this.$msg.error("请先选择调试参数");
      }

      let paramsValueMap = {};
      if(hasParam) {
        let paramsValueSplit = debugParam.split(",");
        paramsValueSplit.forEach(item => {
          let valueSplit = item.split("::");
          paramsValueMap[valueSplit[0]] = valueSplit[1];
        })
      }

      this.spinning =true;
      this.currentDebugApi = api;
      let uid = this.device['uid'];
      let requestParams = {uid, param: {}};
      let downConfig = api['downConfig'];
      if(downConfig && downConfig.length > 0) {
        downConfig.forEach(item => {
          let value = item['value'];
          if(value && value.indexOf("@") == 0) { // @开头
            let name = value.substring(1);
            value = paramsValueMap[name];
            if(value == undefined) {
              value = null;
              console.error(`没有属性[${name}]`)
            }
          }

          let protocolField = item['protocolAttrField'];
          requestParams.param[protocolField] = value
        });

        this.requestParam = requestParams.param;
      }
      this.$http.post(`/iot/instruct/debug/${api.code}`, requestParams).then(({data, message, code}) => {
        if(code == CoreConsts.SuccessCode) {
          this.debugResult = data;
        } else {
          this.$msg.error(message);
        }
      }).finally(() => this.spinning = false)
    },
    resolverNode(data) {
      // data['disableCheckbox'] = !data['status'];
    },
    deviceSelect(data, {node}) {
      let dataRef = node['dataRef'];
      if(dataRef['type'] == 'device') {
        this.spinning = true;
        this.debugParam = null;
        this.requestParam = null;
        this.debugResult = null;
        this.$http.get(`/iot/debug/detail?deviceId=${dataRef['id']}`).then(({code, message, data}) => {
          if(code == CoreConsts.SuccessCode) {
            this.device = data['device'];
            this.product = data['product']
            this.funcApis = this.product['funcApis'] || [];
            this.eventApis = this.product['eventApis'] || [];
            this.apiCodeDebugMap = data['apiCodeDebugMap'] || {};
          } else {
            this.$msg.error(message);
          }
        }).finally(() => this.spinning = false)
      }
    },
    getToUrl(device) {
      let type = device['deviceType'];
      let uri = type == 'Child' ? 'child' : (type == 'Gateway' ? "gateway" : '');
      return `/iot/device/${uri}?deviceSn=${device['deviceSn']}`
    }
  }
}
</script>
<style>
.iv-model-api .ant-card-head {
  padding: 0px 12px!important;
  min-height: 43px!important;
}
.iv-model-api .ant-card-actions li {
  margin: 3px!important;
}
.iv-api-item .ant-tabs-tab{
 padding: 0px;
}
.iv-api-item .ant-card-body {
  padding: 12px;
  height: 78px;
  word-wrap: break-word;
  overflow-y: auto;
  overflow-x: hidden;
}

.iv-debug-header {
  padding: 3px 0px;
  background: #e3e3e3;
  text-align: center;
}
.iv-debug.result {
  padding: 1px;
}
.iv-debug .status-item .title {
  color: #282727;
}
.iv-debug .status-item .value {
  margin-left: 3px;
}
</style>
