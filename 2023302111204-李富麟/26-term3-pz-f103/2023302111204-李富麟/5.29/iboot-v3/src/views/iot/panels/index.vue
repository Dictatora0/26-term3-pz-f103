<template>
  <UView>
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UTreeSelectItem field="deviceGroupId" label="所属组" url="/iot/eventSource/deviceGroups" :allowClear="true"/>
        <USelectItem field="productId" label="所属产品" url="/iot/product/list" :allowClear="true"/>
        <AButton type="primary" @click="search">搜索</AButton>
        <UButton func="reset">重置</UButton>
      </URow>
    </UViewSearch>
    <ASpin :spinning="spinning">
      <div v-if="total > 0">
        <AFlex wrap="wrap" gap="middle" justify="start">
          <template v-for="({device, options, values}, index) in devices" :key="device['uid']">
            <a-card :bordered="false" class="iv-device-card" hoverable :bodyStyle="bodyStyle" style="min-width: 280px">
              <template #title>
                <div class="iv-header">
                  <AFlex gap="10" justify="space-between">
                    <div class="iv-device-name" :title="device.name">{{device.name}}</div>
<!--                    <div class="iv-device-sn" :title="device['deviceSn']">{{device['deviceSn']}}</div>-->
                    <div style="width: 38px"></div>
                  </AFlex>
                  <div class="iv-device-status" :class="device['status']">
                    <div class="iv-status-value">
                      <template v-if="device['status'] == 'online'">
                        <UIcon type="iz-icon-device-online" :style="{fontSize: '16px'}"/>
                        <span style="margin-left: 5px">在线</span>
                      </template>
                      <template v-else>
                        <UIcon type="iz-icon-device-offline" :style="{fontSize: '16px'}"/>
                        <span style="margin-left: 5px">离线</span>
                      </template>
                    </div>
                  </div>
                </div>
              </template>
              <div class="card-bg-left" :class="device['status']"></div>
              <div class="card-bg-right" :class="device['status']"></div>
              <AFlex v-if="values && values.length > 0" justify="space-between">
                <div v-for="item in values">
                  <div class="model-attr-value">{{item['value']}}&nbsp;{{item['unit']}}</div>
                  <div class="model-attr-name">
                    <span>{{item['name']}}</span>
                  </div>
                </div>
              </AFlex>
              <AEmpty v-else :imageStyle="{height: '30px'}" description="没有模型属性"/>
              <template #actions>
                <ASwitch v-if="options && options.length == 2" v-model:checked="devices[index]['ctrlValue']"
                         :checked-children="options[0]['label']" :checked-value="options[0]['value']"
                         :un-checked-children="options[1]['label']" :un-checked-value="options[1]['value']"
                         @change="(value) => switchCtrlStatus(value, device)">
                </ASwitch>
                <ASelect v-else-if="options && options.length > 2" v-model:value="devices[index]['ctrlValue']" :options="options" size="small"
                         :bordered="false" style="width: 100%" placeholder="请选择控制项" @change="(value) => switchCtrlStatus(value, device)"></ASelect>
                <span v-else>没有控制项</span>
                <AButton type="dashed" shape="round" @click="() => profile(device)" size="small">
                  <span style="color: #0f8bc7">运行展板</span>
                </AButton>
              </template>
            </a-card>
          </template>
        </AFlex>
        <a-pagination v-model:current="searchModel.current" :total="total" v-model:pageSize="searchModel.size"
                      :page-size-options="[12, 36, 64]" style="text-align: center; padding: 8px;" />
      </div>
      <a-empty v-else style="margin-top: 132px"/>
    </ASpin>
    <ADrawer width="100%" v-model:open="isOpen" class="iv-panels-drawer" style="background-color: unset;" @close="panelsClose"
             :headerStyle="{height: '56px', padding: '0px', flex: 'unset', backgroundImage: 'url(\'/img/iot/大标题背景框6.png\')', backgroundSize: 'cover', position: 'relative'}"
             :maskStyle="{background: 'rgb(0 0 0 / 82%)', backdropFilter: 'blur(16px)'}" :closable="false">
      <template #title>
        <div class="iv-design-header">
          <div class="title">
            <span>运行展板</span>
          </div>
          <AFlex class="iv-header-content" justify="end" :gap="18">
            <div>
              <div class="iv-content-title">设备名称</div>
              <div class="iv-content-value">
                <span>{{selectDevice['name']}}</span>
              </div>
            </div>
            <div>
              <div class="iv-content-title">设备编号</div>
              <div class="iv-content-value">
                <span>{{selectDevice['deviceSn']}}</span>
              </div>
            </div>
            <div>
              <div class="iv-content-title">设备类型</div>
              <div class="iv-content-value">
                <span>{{ selectProduct['deviceTypeName'] }}</span>
              </div>
            </div>
            <div>
              <div class="iv-content-title">所属产品</div>
              <div class="iv-content-value">
                <span>{{ selectProduct['name'] }}</span>
              </div>
            </div>
            <div style="height: 52px; line-height: 52px;">
              <AButton type="primary" shape="circle" ghost @click="() => {this.isOpen = false}">退</AButton>
            </div>
          </AFlex>
        </div>
      </template>
      <div class="iv-design-left">
        <img src="/img/iot/左侧装饰1.png" width="52" height="100%"/>
      </div>
      <div class="iv-design-right">
        <img src="/img/iot/右侧装饰1.png" width="52" height="100%"/>
      </div>
      <div class="iv-design-status">
        <ABadge v-if="selectDevice['status'] == 'online'" status="processing" />
        <ABadge v-else color="yellow"/>
      </div>
      <AFlex class="iv-design-body" justify="space-between" :gap="8">
        <AFlex class="iv-design-item other" vertical :gap="18">
          <ATable v-if="selectProduct['ctrlMode'] == 'POINT'" :columns="signalColumns" :data-source="realtimeSignalData"
                  bordered :pagination="false" size="small" :rowClassName="getSignalRowClassName">
            <template #title>
              <div>
                <img src="/img/iot/环形装饰.svg" width="32" height="32"/> &nbsp; 实时点位数据
              </div>
            </template>
            <template #emptyText>
              <span style="font-size: 108px">&nbsp;</span>
            </template>
          </ATable>
        </AFlex>
        <AFlex class="iv-design-item field" vertical :gap="18">
          <ATable :columns="fieldColumns" :data-source="realtimeData" bordered :pagination="false" size="small"
                  :rowClassName="getRowClassName">
            <template #title>
              <div>
                <img src="/img/iot/环形装饰.svg" width="32" height="32"/> &nbsp; 实时模型属性数据
              </div>
            </template>
            <template #emptyText>
              <span style="font-size: 108px">&nbsp;</span>
            </template>
          </ATable>
        </AFlex>
        <AFlex class="iv-design-item api" vertical>
          <div style="height: 47px; line-height: 50px; background-image: url('/img/iot/小标题背景框.svg'); background-size: cover">
            <span style="margin-left: 60px; font-size: 16px; color: #d9d9d9">物模型接口</span>
          </div>
          <AFlex :gap="6" vertical style="padding-top: 6px; overflow-y: auto; padding-right: 3px">
            <div v-if="apis.length > 0" class="iv-item-api" v-for="api in apis">
              <a-card :bordered="false" style="background-color: unset;"
                      :headStyle="{padding: '2px 6px', minHeight: '32px'}" :bodyStyle="{padding: '2px 5px'}">
                <template #title>
                  <span>{{api.name}}</span>
                </template>
                <template #extra>
                  <AButton size="small" shape="circle" type="dashed" ghost>
                    {{api['funcType'] == 'R' ? '读' : '写'}}
                  </AButton>
                </template>
                <a-space-compact block>
                  <span v-if="!hasParam(api)" style="width: 70%; height: 32px; line-height: 34px;">无可选参数</span>
                  <ASelect v-else v-model:value="api['apiParam']" :style="{ width: '70%' }" placeholder="请选择执行参数" :bordered="false" :options="apiCodeDebugMap[api['code']]"/>
                  <AButton :style="{ width: '30%' }" :loading="api['loading']" type="primary" ghost @click="() => invokeApi(api)">执行</AButton>
                </a-space-compact>
                <template #actions>
                  <span class="iv-api-action-desc">{{api.code}}</span>
                  <span class="iv-api-action-desc">{{api.direct}}</span>
                  <span class="iv-api-action-desc">{{api.remark}}</span>
                </template>
              </a-card>
            </div>
            <AEmpty v-else style="margin-top: 26%">
              <template #image>
                <UIcon type="iz-icon-empty" style="font-size: 88px" />
              </template>
            </AEmpty>
          </AFlex>
        </AFlex>
      </AFlex>
    </ADrawer>
  </UView>
</template>
<!--运行展板-->
<script>
import {getCurrentInstance, ref, watch, watchEffect} from "vue"
import CoreConsts from "@/components/CoreConsts";
import {urlConfig} from "@/utils/request";
import {msgWarn} from "@/utils/message";
import dayjs from "dayjs";
export default {
  name: "Panels",
  setup() {
    let total = ref(0);
    let apis = ref([]);
    let isOpen = ref(false)
    let selectDevice = ref({})
    let selectProduct = ref({});
    let devices = ref([]);
    let spinning = ref(false);
    let {proxy} = getCurrentInstance();
    let apiCodeDebugMap = ref({});
    let realtimeSocket = ref(null);
    let searchModel = ref({current: 1, size: 12});
    let bodyStyle = ref({padding: '12px 8px 12px', height: '78px'});
    let fieldColumns = ref([
      {title: '名称', dataIndex: 'name', align: 'center', width: 100, ellipsis: true, resizable: true},
      {title: '属性', dataIndex: 'field', align: 'center', ellipsis: true, width: 100},
      {title: '值', dataIndex: 'value', align: 'center', ellipsis: true, width: 100, resizable: true},
      {title: '采集时间', dataIndex: 'collectTime', align: 'center', width: 120},
    ])

    let signalColumns = ref([
      {title: '名称', dataIndex: 'name', align: 'center', width: 100, ellipsis: true},
      {title: '地址', dataIndex: 'address', align: 'center', ellipsis: true, width: 100},
      {title: '值', dataIndex: 'value', align: 'center', ellipsis: true, width: 100},
      {title: '采集时间', dataIndex: 'collectTime', align: 'center', width: 120},
    ])

    let warnColumns = ref([
      // {title: '名称', dataIndex: 'name', align: 'center', width: 100, ellipsis: true},
      {title: '等级', dataIndex: 'grade', align: 'center', width: 100},
      {title: '内容', dataIndex: 'content', align: 'center', width: 230, ellipsis: true, resizable: true},
      {title: '告警时间', dataIndex: 'warnTime', align: 'center', width: 130},
    ]);
    let realtimeWarnData = ref([]);

    let realtimeData = ref([]);
    let realtimeMap = ref({});

    let realtimeSignalData = ref([]);
    let realtimeSignalMap = ref({});
    watch(() => searchModel.value.current, (newValue, oldValue) => {
      proxy.search();
    })
    return {searchModel, isOpen, devices, bodyStyle, total, spinning, selectDevice, selectProduct, apis, realtimeSignalMap
      , apiCodeDebugMap, fieldColumns, realtimeData, realtimeSocket, realtimeMap, signalColumns, realtimeSignalData, warnColumns, realtimeWarnData}
  },
  mounted() {
    this.search();
  },
  methods: {
    search() {
      this.spinning = true;
      this.$http.get('/iot/panels/devices', {params: this.searchModel}).then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          this.total = data.total;
          this.devices = data['records'];
        } else {
          this.$msg.error(message);
        }
      }).finally(() => this.spinning = false)
    },
    profile(device) {
      this.spinning = true;
      this.$http.get(`/iot/panels/detail?deviceId=${device['id']}`).then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          this.isOpen = true;
          this.realtimeData = data['attrs'];
          if(this.realtimeData != null && this.realtimeData.length > 0) {
            this.realtimeMap = {};
            this.realtimeData.forEach(item => {
              this.realtimeMap[item.field] = item;
            })
          }

          this.realtimeSignalData = data['signals'];
          if(this.realtimeSignalData != null && this.realtimeSignalData.length > 0) {
            this.realtimeSignalMap = {};
            this.realtimeSignalData.forEach(item => {
              this.realtimeSignalMap[item.address] = item;
            })
          }

          this.realtimeWarnData = data['warns'];
          this.selectDevice = data['device'];
          this.selectProduct = data['product'];
          this.apiCodeDebugMap = data['apiCodeDebugMap'] || {};
          this.apis = [...this.selectProduct['funcApis'], ...this.selectProduct['eventApis']];
          this.realtimeSocket = new WebSocket(urlConfig.getFullWsURL(`/ws/iot/realtime?uid=${device['uid']}&type=123`));
          this.realtimeSocket.onmessage =  this.realtimeMessage;
        } else {
          this.$msg.error(message);
        }
      }).finally(() => this.spinning = false)
    },
    hasParam(api) {
      let params = this.apiCodeDebugMap[api['code']];
      return params instanceof Array && params.length > 0;
    },
    invokeApi(api) {
      let apiParam = api['apiParam'];
      let hasParam = this.apiCodeDebugMap[api['code']]?.length > 0;
      if(hasParam && !apiParam) {
        return this.$msg.error("请先选择执行参数");
      }

      let paramsValueMap = {};
      if(hasParam) {
        let paramsValueSplit = apiParam.split(",");
        paramsValueSplit.forEach(item => {
          let valueSplit = item.split("::");
          paramsValueMap[valueSplit[0]] = valueSplit[1];
        })
      }

      api.loading = true;
      let uid = this.selectDevice['uid'];
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
      }

      this.$http.post(`/iot/instruct/ctrl/${api.code}`, requestParams).then(({data, message, code}) => {
        if(code != CoreConsts.SuccessCode) {
          this.$msg.error(message);
        }
      }).finally(() => api.loading = false)
    },
    realtimeMessage(message) {
      let value = JSON.parse(message.data);
      if(typeof value == 'object') {
        let {type, value: data} = value;
        if(type == 'model') {
          Object.values(data).forEach(value => {
            let realtime = this.realtimeMap[value['signalOrField']];
            if(realtime != null) {
              if(typeof value['value'] == 'boolean') {
                realtime['value'] = value['value'] + "";
              } else {
                realtime['value'] = value['value'];
              }
              realtime['status'] = value['status'];
              realtime['collectTimeLong'] = value['collectTime'] || 0;
              realtime['collectTime'] = dayjs(value['collectTime']).format("MM-DD HH:mm:ss");
            }
          })

          this.realtimeData.sort((a, b) => {
            return b['collectTimeLong'] - a['collectTimeLong'];
          })

          this.$nextTick().then(() => {
            let element = document.getElementsByClassName("iv-row-0").item(0);
            element.classList.add("update")
            element.addEventListener("animationend", () => {
              element.classList.remove("update")
            })
          })
        } else if(type == 'signal') {
          Object.values(data).forEach(value => {
            let realtime = this.realtimeSignalMap[value['signalOrField']];
            if(realtime != null) {
              realtime['value'] = value['value'];
              realtime['status'] = value['status'];
              realtime['collectTimeLong'] = value['collectTime'] || 0;
              realtime['collectTime'] = dayjs(value['collectTime']).format("MM-DD HH:mm:ss");
            }
          })

          this.realtimeSignalData.sort((a, b) => {
            return b['collectTimeLong'] - a['collectTimeLong'];
          })

          this.$nextTick().then(() => {
            let element = document.getElementsByClassName("iv-row-signal-0").item(0);
            element.classList.add("update")
            element.addEventListener("animationend", () => {
              element.classList.remove("update")
            })
          })
        } else if(type == 'status') {
          this.selectDevice['status'] = data['status'];
        } else if(type == 'warn') {
          data['warnTime'] = dayjs(data['warnTime']).format("YY-MM-DD HH:mm:ss")
          this.realtimeWarnData.splice(0, 0, data)
          this.$nextTick().then(() => {
            let element = document.getElementsByClassName("iv-row-warn-0").item(0);
            element.classList.add("update")
            element.addEventListener("animationend", () => {
              element.classList.remove("update")
            })
          })
        }
      }
    },
    getRowClassName(record, index) {
      return 'iv-row-' + index + (record['status'] != 'Success' ? ' fail' : '');
    },
    getWarnRowClassName(record, index) {
      return 'iv-row-warn-' + index;
    },
    getSignalRowClassName(record, index) {
      return 'iv-row-signal-' + index + (record['status'] != 'Success' ? ' fail' : '');
    },
    panelsClose() {
      if(this.realtimeSocket instanceof WebSocket) {
        debugger
        this.realtimeSocket.close();
      }
    },
    switchCtrlStatus(value, device) {
      this.spinning = true;
      this.$http.post(`/iot/panels/switchCtrlStatus`, {id: device.uid, status: value}).then(({code, message}) => {
        if(code == CoreConsts.SuccessCode) {
          this.search();
        } else {
          this.search();
          this.$msg.error(message);
        }
      }).finally(() => this.spinning = false)
    },
    routeTo(device, type) {
      if(type == 'device') {
        let url = '';
        let deviceType = device['deviceType'];
        if(deviceType == 'Child') {
          url = '/child';
        } else if(deviceType == 'Gateway') {
          url = '/gateway'
        }
        this.$router.push(`/iot/device${url}?deviceSn=${device['deviceSn']}`);
      } else {

      }

    }
  }
}
</script>

<style>
.iv-device-card {
  overflow: hidden;
}
.iv-device-card .iv-header {
  /*position: relative;*/
}
.iv-device-name,.iv-device-sn {
  max-width: 130px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.iv-desc-type .value, .iv-desc-product .value, .iv-item-api .ant-card .ant-card-actions>li >span
, .iv-content-value, .model-attr-name, .model-attr-value {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.iv-header .iv-device-status {
  position: absolute;
  top: 13px;
  right: -13px;
  width: 96px;
  height: 25px;
  line-height: 25px;
  text-align: center;
  transform: skew(45deg);
}
.iv-device-status.online {
  background-color: rgba(163, 243, 163, 0.15);
}
.iv-device-status.offline {
  background-color: rgba(248, 227, 164, 0.15);
}
.iv-device-status .iv-status-value {
  transform: skew(-45deg);
  font-weight: 360;
  font-size: 14px;
}
.card-bg-left {
  position: absolute;
  left: -32px;
  height: 100%;
  width: 44.65%;
  top: 0;
  transform: skew(-15deg);
  background: linear-gradient(188.4deg, rgba(71, 180, 238, 0.02) 22.94%, rgba(71, 180, 238, 0) 94.62%);
}
.card-bg-right {
  position: absolute;
  right: -32px;
  height: 100%;
  width: 55%;
  top: 0;
  transform: skew(-15deg);
}
.card-bg-right.online {
  background: linear-gradient(188.4deg, rgba(189, 246, 189, 0.15) 22.94%, rgba(163, 246, 189, 0) 94.62%);
}
.card-bg-right.offline {
  background: linear-gradient(188.4deg, rgba(229, 0, 18, .03) 22.94%, rgba(229, 0, 18, 0) 94.62%);
}
.iv-desc-type,.iv-desc-product {
  padding-top: 12px;
}
.iv-panels-drawer {
  position: relative;

}
.iv-design-header {
  color: #fafafa;
  /*background-image: url("/img/iot/大标题背景框6.png");*/
}
.iv-design-header .iv-header-content {
  top: 0px;
  right: 18px;
  /*width: 88%;*/
  height: 52px;
  position: absolute;
}
.iv-design-left {
  position: absolute;
  top: 56px;
  left: 0px;
  height: calc(100% - 56px);
}
.iv-design-right {
  top: 56px;
  right: 0px;
  position: absolute;
  height: calc(100% - 56px);
}
.iv-design-status {
  top: 8px;
  width: 100px;
  height: 100px;
  right: 50%;
  line-height: 100px;
  position: absolute;
  background: url("/img/iot/圆形装饰.svg");
  background-size: 100px 100px;
}
.iv-design-status .ant-badge-status-dot{
  width: 16px!important;
  height: 16px!important;
  margin-left: 43px;
}
.iv-design-header .title {
  top: 7px;
  left: 8%;
  /*color: #76c9fc;*/
  width: 160px;
  height: 42px;
  line-height: 41px;
  font-size: 25px;
  font-weight: 300;
  text-align: center;
  position: absolute;
  transform: skew(-45deg);
  background: linear-gradient(90deg, rgba(61, 98, 171, 0.36) 22.94%, rgba(61, 98, 171, 0.08) 94.62%);
}
.iv-design-header .title span {
  display: inline-block;
  transform: skew(45deg);
}
.iv-design-body {
  height: 100%;
  color: #d9d9d9;
  min-width: 1400px;
  padding: 16px 28px 8px;
}
.iv-content-title {
  height: 28px;
  width: 100px;
  text-align: center;
  font-size: 12px;
  font-weight: 200;
  line-height: 34px;
}
.iv-content-value {
  height: 22px;
  width: 100px;
  font-size: 14px;
  line-height: 20px;
  font-weight: 260;
  padding: 0px 12px;
  text-align: center;
  transform: skew(45deg);
  background: linear-gradient(90deg, rgba(61, 98, 171, 0.36) 22.94%, rgba(61, 98, 171, 0.08) 94.62%);
}
.iv-content-value span {
  display: inline-block;
  transform: skew(-45deg);
}
.iv-design-item.other {
  /*width: 30%;*/
  min-width: 380px;
  overflow-y: auto;
  padding: 18px 3px 0px 0px;
}
.iv-design-item.field {
  width: 35%;
  min-width: 450px;
  overflow-y: auto;
  padding: 18px 3px 0px 0px;
}
.iv-design-item.api {
  width: 35%;
  min-width: 380px;
}
.iv-item-api {
  border-radius: 5px;
  border: 1px solid #3d62ab;
}
.iv-item-api .ant-card .ant-card-actions>li >span {
  padding: 0px 3px;
}
.iv-item-api *, .iv-design-item.field *,.iv-design-item.other * {
  color: #fafafa;
}
.iv-item-api .ant-select-selection-placeholder {
  color: rgba(217, 217, 217, 0.49);
}
.iv-item-api .ant-card-head {
  border-bottom: 1px solid #f0f0f030;
}
.iv-item-api .ant-card-actions {
  border-top: 1px solid rgba(240, 240, 240, 0.42);
}
.iv-item-api .ant-card-actions {
  background-color: unset;
}
.iv-item-api .ant-card-actions li {
  margin: 5px 0px;
}
.iv-design-body .ant-table-wrapper .ant-table {
  background: unset;
}
@keyframes updateIv {
  0% {
    background-position: 100% 100%;
  }
  100% {
    background-position: 0% 100%;
  }
}
.iv-row-0.update, .iv-row-signal-0.update {
  animation: updateIv 1s linear;
  background: linear-gradient(90deg, rgba(61, 98, 171, 0) 22.94%, rgba(61, 98, 171, 0.92), rgba(61, 98, 171, 0) 94.62%);
  background-size: 200% auto;
}
.iv-design-item .ant-table-row.fail:not(.update) {
  background: linear-gradient(90deg, rgba(172,128,62, 0.12) 22.94%, rgba(172, 128, 62, 0.25), rgba(172, 128, 62, 0.12) 94.62%)
}
.iv-row-warn-0.update, .iv-row-signal-0.update.fail {
  animation: updateIv 1s linear;
  background: linear-gradient(90deg, rgba(172, 128, 62, 0) 22.94%, rgba(172, 128, 62, 0.92), rgba(172, 128, 62, 0) 94.62%);
  background-size: 200% auto;
}
.iv-design-body .ant-table-wrapper .ant-table-tbody >tr.ant-table-row:hover>td
, .iv-design-body .ant-table-wrapper .ant-table-tbody >tr >td.ant-table-cell-row-hover
, .iv-design-body .ant-table-wrapper .ant-table-thead >tr>th
, .iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered .ant-table-tbody >tr >td
, .iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-container >.ant-table-header >table >thead>tr>th
, .iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-container >.ant-table-content >table >thead>tr>th{
  color: #fafafa;
  background: unset;
  border-bottom: 1px solid #3d62ab;
  border-inline-end: 1px solid #3d62ab!important;
}
.iv-design-body .ant-table-wrapper .ant-table-thead >tr {
  background: linear-gradient(188.4deg, #0958d945 22.94%, #0958d91a 94.62%)!important;
}
.iv-design-body .ant-table-wrapper .ant-table-cell-scrollbar:not([rowspan]) {
  box-shadow: unset;
}
.iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-title {
  border: 0px solid #3d62ab;
}
.iv-design-body .ant-table-wrapper .ant-table.ant-table-small .ant-table-title {
  padding: 2px 8px;
}
.iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-container >.ant-table-content >table
,.iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-container >.ant-table-header >table {
  border-top: 1px solid #3d62ab;
}
.iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-container >.ant-table-content >table >tbody>tr>td {
  border-inline-end: 1px solid #3d62ab;
}
.iv-design-body .ant-table-wrapper .ant-table.ant-table-bordered >.ant-table-container {
  border-inline-start: 1px solid #3d62ab;
}
.model-attr-name {
  padding: 0px 12px;
  height: 20px;
  /*width: 52px;*/
  font-size: 15px;
  line-height: 20px;
  /*font-weight: 260;*/
  text-align: center;
  transform: skew(30deg);
  background: linear-gradient(360deg, rgba(71, 180, 238, 0.05) 22.94%, rgba(71, 180, 238, 0) 94.62%);
}
.model-attr-name span {
  display: inline-block;
  transform: skew(-30deg);
}
.model-attr-value {
  font-size: 13px;
  padding: 0px 0px 12px;
  text-align: center;
}
</style>