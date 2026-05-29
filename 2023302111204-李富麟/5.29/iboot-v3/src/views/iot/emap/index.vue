<template>
  <UView class="iv-emap">
    <AFlex vertical justify="space-between" style="height: 100%">
      <UViewSearch v-model="searchModel">
        <URow col="search">
          <UTreeSelectItem field="deviceGroupId" label="所属组" url="/iot/eventSource/deviceGroups" :allowClear="true"/>
          <USelectItem field="productId" label="所属产品" url="/iot/product/list" :allowClear="true"/>
          <AButton type="primary" @click="search">搜索</AButton>
          <UButton func="reset">重置</UButton>
        </URow>
      </UViewSearch>
      <div id="map-container" style="flex-grow: 1; height: 100%;">
        <AMap id="map-container" ref="map">
          <AMarker v-for="device in devices" :key="device.uid" :position="[device.lon, device.lat]" icon="/img/map-marker-c.png" @click="() => deviceDetail(device)">
            <AMarkerLabel>
              <div class="iv-device-label animate__animated" :class="[device.type, device.repeat, device['animate']]">
                {{device.name}}[{{device['typeName']}}]
              </div>
            </AMarkerLabel>
          </AMarker>
        </AMap>
      </div>
    </AFlex>
    <ADrawer v-model:open="open" :title="selectDevice.name" placement="left" width="460px" style="background-color: rgba(0,0,0,0.59);backdropFilter: blur(16px)"
             :mask="false" :bodyStyle="{padding: '0px'}" destroy-on-close>
      <ASpin :spinning="spinning" tip="数据加载中..." style="height: 100%" wrapperClassName="iv-emap-spin">
        <AFlex vertical gap="3" justify="space-between" style="height: 100%"
               class="animate__animated" :class="spinning ? 'animate__fadeOutDown' : 'animate__fadeInDown'">
          <div class="iv-efunc-item data" v-if="attrs.length > 0">
            <div class="title">
              实时数据
            </div>
            <div class="content">
              <AFlex gap="8" justify="space-between">
                <div v-for="(attr) in attrs" :key="attr['field']" class="iv-eattr-item">
                  <div>{{attr['value'] ? attr['value'] : '-'}}</div>
                  <div>{{attr['name']}}{{attr['unit'] ? `(${attr['unit']})` : ''}}</div>
                </div>
              </AFlex>
            </div>
          </div>
          <div class="iv-efunc-item warn" v-if="warns.length > 0">
            <div class="title">
              最新告警
            </div>
            <div class="content">
              <AFlex v-for="(warn, index) in warns" :key="index" gap="3">
                <div class="iv-ewarn-item iv-text-ellipsis time">{{warn['warnTime']}}</div>
                <div class="iv-ewarn-item iv-text-ellipsis content" :title="warn['content']">{{warn['content']}}</div>
                <div class="iv-ewarn-item iv-text-ellipsis grade">{{warn['grade']}}</div>
              </AFlex>
            </div>
          </div>
          <div class="iv-efunc-item ctrl" v-if="ctrlApi">
            <div class="title">
              状态接口
            </div>
            <div class="content">
              <div class="iv-eapi-item">
                <a-card :bordered="false" style="background-color: unset;"
                        :headStyle="{padding: '2px 6px', minHeight: '32px'}" :bodyStyle="{padding: '6px 5px'}">
                  <template #title>
                    <AFlex style="width: 100%;" justify="space-between">
                      <div class="iv-text-ellipsis" style="width: 25%">
                        <AButton size="small" shape="circle" type="primary" danger>写</AButton>
                        {{ctrlApi.name}}
                      </div>
                      <div class="iv-text-ellipsis" style="width: 50%; text-align: center">{{ctrlApi['remark']}}</div>
                      <div class="iv-text-ellipsis" style="width: 25%">{{ctrlApi['code']}}</div>
                    </AFlex>
                  </template>
                  <div style="text-align: center">
                    <a-radio-group v-model:value="ctrlAttrValue" button-style="solid" optionType="button"
                                   :options="ctrlAttrs" @change="switchCtrlStatus"/>
                  </div>
                    <!--                  <ASelect v-else v-model:value="ctrlApi['apiParam']" :style="{ width: '70%' }"-->
                    <!--                           placeholder="请选择执行参数" :bordered="false" :options="apiCodeDebugMap[ctrlApi['code']]"/>-->
<!--                    <AButton :style="{ width: '30%' }" :loading="ctrlApi['loading']" type="primary" @click="() => invokeApi(ctrlApi)">控制</AButton>-->
                </a-card>
              </div>
            </div>
          </div>
          <div class="iv-efunc-item api" v-if="apis.length > 0" >
            <div class="title">
              功能接口
            </div>
            <div class="content">
              <AFlex :gap="10" vertical style="padding-top: 6px;">
                <div class="iv-eapi-item" v-for="api in apis">
                  <a-card :bordered="false" style="background-color: unset;"
                          :headStyle="{padding: '2px 6px', minHeight: '32px'}" :bodyStyle="{padding: '6px 5px'}">
                    <template #title>
                      <AFlex style="width: 100%;" justify="space-between">
                        <div class="iv-text-ellipsis" style="width: 25%">
                          <AButton size="small" shape="circle" type="primary">
                            {{api['funcType'] == 'R' ? '读' : '写'}}
                          </AButton>
                          {{api.name}}
                        </div>
                        <div class="iv-text-ellipsis" style="width: 50%; text-align: center">{{api['remark']}}</div>
                        <div class="iv-text-ellipsis" style="width: 25%">{{api['code']}}</div>
                      </AFlex>
                    </template>
                    <a-space-compact block>
                      <span v-if="!hasParam(api)" style="width: 70%; height: 32px; line-height: 34px;">无可选参数</span>
                      <ASelect v-else v-model:value="api['apiParam']" :style="{ width: '70%' }"
                             placeholder="请选择执行参数" :bordered="false" :options="apiCodeDebugMap[api['code']]"/>
                      <AButton :style="{ width: '30%' }" :loading="api['loading']" type="primary" @click="() => invokeApi(api)">
                        {{api['funcType'] == 'R' ? '读取' : '写入'}}
                      </AButton>
                    </a-space-compact>
                  </a-card>
                </div>
              </AFlex>
            </div>
          </div>
        </AFlex>
      </ASpin>
    </ADrawer>
  </UView>
</template>

<script>
import {ref} from "vue";
import CoreConsts from "@/components/CoreConsts";
import {urlConfig} from "@/utils/request";

export default {
  name: "EMap",
  setup() {
    let map = ref();
    let selectDevice = ref({uid: null});
    let open = ref(false);
    let warns = ref([]);
    let attrs = ref([]);
    let apis = ref([]);
    let ctrlApi = ref(null);
    let ctrlAttrs = ref([]);
    let ctrlAttrValue = ref(null);
    let apiCodeDebugMap = ref({});
    let devices = ref([]);
    let spinning = ref(false);
    let deviceMap = ref({});
    let searchModel = ref({});
    return {searchModel, devices, map, deviceMap, open, selectDevice, spinning, warns, attrs, apis, ctrlApi, ctrlAttrs, ctrlAttrValue, apiCodeDebugMap}
  },
  mounted() {
    this.search();
    this.realtimeSocket = new WebSocket(urlConfig.getFullWsURL(`/ws/iot/realtime?uid=-1&type=123`));
    this.realtimeSocket.onmessage = (message) => {
      let {type, uid, value} = JSON.parse(message.data);
      let device = this.deviceMap[uid];
      if(device) {
        if(type == 'status') {
          device['type'] = value['status'];
          if(value['status'] == 'offline') {
            device['typeName'] = '离线';
            device['animate'] = 'animate__shakeX';
            device['repeat'] = "animate__repeat-2"; // 播放三次
          } else {
            device['repeat'] = ""
            device['animate'] = 'animate__flipInY';
            device['typeName'] = '在线';
          }
        } else if(type == 'warn') {
          let grade = value['grade'];
          if(grade != 'status') {
            device['animate'] = 'animate__flash';
            if(grade == 'notice' || grade == 'normal') {
              device['repeat'] = "animate__repeat-2";
            } else {
              device['repeat'] = "animate__repeat-3";
            }

            device['type'] = grade; // 根据告警等级显示颜色
            device['typeName'] = value['gradeName'];
          }
        }
      }
    }
  },
  updated() {
    if(this.map.instance) {
      this.map.setFitView();
    }
  },
  methods: {
    search() {
      this.$http.get('/iot/emap/devices', {params: this.searchModel}).then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          this.devices = data || [];
          this.deviceMap = {};
          // animate__repeat-3 动画次数
          // infinite 无限循环
          this.devices.forEach(device => {
            device['type'] = device['status']; // 区分类型
            device['repeat'] = device['']; // 动画次数
            device['typeName'] = device['status'] == 'online' ? '在线' : '离线'; //
            this.deviceMap[device['uid']] = device;
          })
        } else {
          this.$msg.error(message);
        }
      })
    },
    hasParam(api) {
      let params = this.apiCodeDebugMap[api['code']];
      return params instanceof Array && params.length > 0;
    },
    deviceDetail(device) {
      this.open = true;
      this.spinning = true;
      this.selectDevice = device;
      this.$http.get('/iot/emap/detail', {params: {uid: device.uid}})
          .then(({code, message, data}) => {
            if(code == CoreConsts.SuccessCode) {
              let {attrs, warns, ctrlApi, funcApis, eventApis, ctrlAttrs, ctrlAttrValue, apiCodeDebugMap} = data;
              this.attrs = attrs || [];
              this.warns = warns || [];
              this.ctrlApi = ctrlApi;
              this.apis = funcApis || [];
              this.ctrlAttrs = ctrlAttrs;
              this.ctrlAttrValue = ctrlAttrValue;
              this.apiCodeDebugMap = apiCodeDebugMap || {};

            } else {
              this.$msg.error(message);
            }
          }).finally(() => this.spinning = false)
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

      api.loading = true;
      this.$http.post(`/iot/instruct/ctrl/${api.code}`, requestParams).then(({data, message, code}) => {
        if(code != CoreConsts.SuccessCode) {
          this.$msg.error(message);
        }
      }).finally(() => api.loading = false)
    },
    switchCtrlStatus(value) {
      this.spinning = true;
      this.$http.post(`/iot/panels/switchCtrlStatus`, {id: this.selectDevice.uid, status: this.ctrlAttrValue}).then(({code, message}) => {
        if(code == CoreConsts.SuccessCode) {
          this.search();
        } else {
          this.search();
          this.$msg.error(message);
        }
      }).finally(() => this.spinning = false)
    }
  }
}
</script>

<style scoped>
.iv-device-label {
  padding: 3px 6px;
  box-shadow: 0px 0px 6px 0px #8e8e8e;
}
.iv-device-label.online,.iv-device-label.normal {
  background: #a9fc88;
}
.iv-device-label.offline {
  background: #e2e1e1;
}
/*严重*/
.iv-device-label.fatal {
  background: rgba(253, 95, 95, 0.78);
}
/*紧急*/
.iv-device-label.urgent {
  background: #EA8857BA;
}
/*告警*/
.iv-device-label.warn {
  background: #fab06b;
}
.iv-efunc-item {
  padding: 3px;
}
.iv-efunc-item .title {
  color: rgba(227, 227, 227, 0.49);
  height: 24px;
  font-size: 13px;
  text-align: center;
  line-height: 24px;
}
.iv-efunc-item>.content {
  padding: 6px;
  border-radius: 6px;
  overflow-x: auto;
  height: calc(100% - 24px);
  backdropFilter: blur(16px);
  background: rgba(252, 251, 251, 0.2);
}
.iv-efunc-item.warn {
  height: 120px;
}
.iv-efunc-item.data {
  height: 124px;
}
.iv-efunc-item.ctrl {
  height: 130px;
}
.iv-efunc-item.api {
 flex-grow: 1;
}
.iv-ewarn-item {
  padding: 3px;
  height: 24px;
  text-align: center;
  line-height: 24px;
}
.iv-ewarn-item.grade {
  width: 46px;
}
.iv-ewarn-item.time {
  width: 130px;
}
.iv-eattr-item {

}
.iv-efunc-item.data .content>div {
  padding: 18px 8px;
  background: rgba(62, 62, 62, 0.07);
}
.iv-eattr-item div {
  padding: 3px 6px;
  text-align: center;
}
.iv-eattr-item div:first-child {
  font-weight: 600;
  font-size: 16px;
}
.iv-eapi-item {
  padding: 6px;
  border-radius: 3px;
  background: rgba(64, 64, 64, 0.11);
}
.iv-eapi-item .ant-card-actions {
  background-color: unset;
}
.iv-eapi-item .ant-card-actions li {
  margin: 5px 0px;
}
</style>