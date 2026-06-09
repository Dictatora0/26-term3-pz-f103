<template>
  <UView name="设备">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="设备名称" />
        <UInputItem field="deviceSn" label="设备编号" />
        <USelectItem field="productId" label="所属产品" @change="productChange" :allowClear="true"
                     url="/iot/dtu/products?deviceType=Gateway" labelField="name" valueField="id" />
        <UTreeSelectItem field="deviceGroupId" label="所属分组" treeDefaultExpandAll
             url="/iot/deviceGroup/tree" labelField="name" valueField="id" :allowClear="true"/>
        <URadioItem field="status" label="设备状态" dict="iot_device_status" optionType="button" buttonStyle="solid"/>
        <UButton func="query" url="/iot/dtu/view?deviceType=Gateway" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewModal title="Dtu设备管理" :span="[8, 16]" v-model="editModel" :rules="rules" width="680px" @edit="gatewayEdit">
      <template #default="{model}">
        <URow :responsive="2">
          <UInputItem field="name" label="设备名称" :maxlength="32" />
          <USelectItem field="productId" label="所属产品" @change="productChange" v-model:source="productSource"
                       url="/iot/dtu/products?deviceType=Gateway" labelField="name" valueField="id" :disabled="model.id != null">
          </USelectItem>
          <UInputItem field="deviceSn" label="DTU编号" :maxlength="32" extra="必须和DTU配置项(注册内容)一致" />
          <UTreeSelectItem field="deviceGroupId" label="所属组" treeDefaultExpandAll
                           url="/iot/deviceGroup/tree?productId={productId}" labelField="name" valueField="id" />
          <UInputItem field="address" label="设备地址" readonly :span="24" :labelCol="{span: 4}" :wrapperCol="{span: 20}">
            <template #addonAfter>
              <span style="cursor: pointer" @click="() => {mapOpen = true}">地图</span>
            </template>
          </UInputItem>
        </URow>
      </template>
      <template #footer="{model}">
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/dtu/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <AMapModal v-model:open="mapOpen" :zoom="12" :center="centerPoint" @click="selectPoint" >
      <template #marker="{position, address}">
        <AMarker v-if="centerPoint" title="点击选中地址" :position="centerPoint"
                 @click="() => selectMarker(position, address)" id="current"></AMarker>
      </template>
    </AMapModal>
    <UViewTable :columns="columns" :scroll="{x: 1230}">
      <template #productName="{record,value}">
        <router-link :to="`/iot/product?code=${record.productCode}`">{{value}}</router-link>
      </template>
      <template #status="{record, value}">
        <a-badge v-if="record.status=='online'" status="processing" color="#87d068" :text="value" />
        <a-badge v-else status="warning" :text="value" />
      </template>
      <template #action="{record}">
        <UFuncTag func="edit" :data="record" url="/iot/dtu/edit" v-auth="'iot:dtu:edit'">修改</UFuncTag>
        <UTag func="open" uid="child" :data="record" color="#2db7f5" v-auth="'iot:dtu:child'">子设备</UTag>
        <UFuncTag func="del" :data="record" url="/iot/dtu/del" v-auth="'iot:dtu:del'">删除</UFuncTag>
      </template>
    </UViewTable>
    <UDrawer uid="child" placement="top" :title="'子设备管理('+editModel.name+')'" @open="childOpen" height="100%">
      <ULinkView uid="childView">
        <UViewSearch v-model="childSearchModel">
          <URow col="search">
            <UInputItem field="name" label="设备名称" />
            <UInputItem field="deviceSn" label="设备编号" />
            <UButton func="query" :url="`/iot/device/view?pid=${editModel['id'] ? editModel['id'] : ''}`">搜索</UButton>
            <UButton func="reset">重置</UButton>
            <UButton func="add" url="/iot/device/add">新增</UButton>
          </URow>
        </UViewSearch>
        <UViewTable :columns="childColumns" :scroll="{x: 1200}" >
          <template #productName="{record,value}">
            <router-link :to="`/iot/product?code=${record.productCode}`">{{value}}</router-link>
          </template>
          <template #status="{record, value}">
            <a-badge v-if="record.status=='online'" status="processing" color="#87d068" :text="value" />
            <a-badge v-else status="warning" :text="value" />
          </template>
          <template #action="{record}">
            <UFuncTag func="edit" :data="record" url="/iot/dtu/edit">修改</UFuncTag>
            <UFuncTag func="del" :data="record" url="/iot/dtu/del">删除</UFuncTag>
          </template>
        </UViewTable>
        <UViewModal title="子设备管理" :span="[8, 16]" v-model="childModel" :rules="childRules" width="680px" @edit="childEdit">
          <template #default="{model}">
            <URow :responsive="2">
              <UInputItem field="name" label="设备名称" :maxlength="32" />
              <UInputNumberItem field="deviceSn" label="从站地址" :maxlength="3" :min="0" :max="247" :step="1" />
              <USelectItem field="productId" label="所属产品" @change="childProductChange" v-model:source="productSource"
                           url="/iot/dtu/products?deviceType=Child" labelField="name" valueField="id">
              </USelectItem>
              <USelectItem field="config.dataFormat" label="数据格式" required defaultValue="ABCD" :options="dataFormat" />
              <UTreeSelectItem field="deviceGroupId" label="所属组" treeDefaultExpandAll
                               url="/iot/deviceGroup/tree" labelField="name" valueField="id" />
              <UInputItem field="address" label="设备地址" readonly>
                <template #addonAfter>
                  <span style="cursor: pointer" @click="() => {mapOpen = true}">地图</span>
                </template>
              </UInputItem>
            </URow>
          </template>
          <template #footer="{model}">
            <UButton func="cancel">取消</UButton>
            <UButton func="submit" url="/iot/dtu/saveOrUpdate">提交</UButton>
            <UButton func="reset">重置</UButton>
          </template>
        </UViewModal>
      </ULinkView>
    </UDrawer>
  </UView>
</template>
<!-- 设备功能 -->
<script>
import {ref, reactive, watch, nextTick} from "vue";
import {useRoute} from "vue-router";
import {GET} from "@/utils/request";
import CoreConsts from "@/components/CoreConsts";
import {msgWarn} from "@/utils/message";
export default {
  name: "Dtu",
  setup() {
    let route = useRoute();
    let columns = ref([
      {field: "uid", title: "设备标识", width: 170},
      {field: "name", title: "设备名称", width: 180},
      {field: "deviceSn", title: "DTU编号", width: 150},
      {field: "gatewayName", title: "网关名称", width: 150},
      {field: "productName", title: "所属产品", width: 150},
      {field: "productCode", title: "产品编码", width: 150, resizable: true},
      {field: "deviceGroupName", title: "所属分组", width: 120},
      {field: "config", title: "网关配置", width: 260, formatter: ({value}) => {
          return value != null ? JSON.stringify(value) : '';
        }, resizable: true},
      {field: "createTime", title: "创建时间", type: 'date', width: 100, format: 'YYYY-MM-DD'},
      {field: "switchTime", title: "上/下线时间", type: 'date', width: 160},
      {field: "status", title: "设备状态", dict: 'iot_device_status', width: 80, fixed: 'right'},
      {field: 'action', title: '操作', type: 'action' , width: 230, fixed: 'right'},
    ]);

    let childColumns = ref([
      {field: "name", title: "设备名称", width: 180},
      // {field: "parentDeviceSn", title: "网关设备编号", width: 200},
      {field: "deviceSn", title: "从站地址", width: 80},
      // {field: "productTypeName", title: "产品类型", width: 120},
      {field: "productName", title: "所属产品", width: 120},
      {field: "productCode", title: "产品编码", width: 120, resizable: true},
      {field: "deviceTypeName", title: "设备类型", width: 100},
      {field: "deviceGroupName", title: "所属分组", width: 120},
      // {field: "ip", title: "设备ip", width: 180},
      // {field: "port", title: "设备端口", width: 80},
      {field: "createTime", title: "创建时间", type: 'date', width: 100, format: 'YYYY-MM-DD'},
      {field: "switchTime", title: "上/下线时间", type: 'date', width: 160},
      {field: "status", title: "设备状态", dict: 'iot_device_status', width: 80, fixed: 'right'},
      {field: 'action', title: '操作', type: 'action' , width: 150, fixed: 'right'},
    ]);

    let rules = reactive({
        name: {required: true, message: '设备名称必填'},
        pid: {required: false, message: '网关设备必填'},
        status: {required: true, message: '设备状态必填'},
        deviceSn: {required: true, message: '设备编号必填'},
        productId: {required: true, message: '所属产品必填'},
        productTypeId: {required: true, message: '产品类型必填'},
    })
    let childRules = reactive({
        name: {required: true, message: '设备名称必填'},
        pid: {required: true, message: '网关设备必填'},
        status: {required: true, message: '设备状态必填'},
        deviceSn: {required: true, message: '从站地址必填'},
        productId: {required: true, message: '所属产品必填'},
        productTypeId: {required: true, message: '产品类型必填'},
    })

    let dataFormat = ref([
      {label: "ABCD", value: 'ABCD'},
      {label: "BADC", value: 'BADC'},
      {label: "CDAB", value: 'CDAB'},
      {label: "DCBA", value: 'DCBA'},
    ])

    let mapOpen = ref(false);
    let mapOpenType = ref('gateway'); // 网关设备类型和子设备类型
    let editModel = ref({});
    let searchRef = ref(null);
    let centerPoint = ref(null);
    let searchModel = ref({});
    let productSource = ref({});
    let selectedProduct = ref({}); // 选中的产品

    let childSearchModel = ref({uid: null});
    let childModel = ref({deviceSn: null, name: '', uid: null});

    // 经纬度变化
    watch(() => mapOpen.value, (newVal) => {
      if(newVal) {
        if(mapOpenType.value == 'gateway') {
          if(editModel.value['lon'] && editModel.value['lat']) {
            centerPoint.value = [editModel.value['lon'], editModel.value['lat']];
          }
        } else {
          if(childModel.value['lon'] && childModel.value['lat']) {
            centerPoint.value = [childModel.value['lon'], childModel.value['lat']];
          }
        }
      } else {
        centerPoint.value = null;
      }
    })

    watch(() => route.query, (newValue) => {
      let deviceSn = newValue['deviceSn'];
      if(deviceSn) {
        searchModel.value['deviceSn'] = deviceSn;
        if(searchRef.value) {
          searchRef.value.trigger();
        }
      }
    }, {immediate: true})

    return {columns, rules, editModel, searchModel, selectedProduct, productSource, searchRef, mapOpen
      , centerPoint, childSearchModel, childModel, childColumns, childRules, mapOpenType, dataFormat}
  },
  methods: {
    productChange(value, model) {
      this.selectedProduct = model;
      if(model['deviceType'] == 'Child') { // 用于触发网关设备
        this.editModel['gatewayId'] = model['gatewayId']
      }
    },
    childProductChange(value, model) {
      this.selectedProduct = model;
      if(model['deviceType'] == 'Child') { // 用于触发网关设备
        this.childModel['gatewayId'] = model['gatewayId']
      }
    },
    selectPoint({instance, position}) {
      this.centerPoint = position;
    },
    selectMarker(position, address) {
      this.mapOpen = false;
      position = position || this.centerPoint;
      if(this.mapOpenType == 'gateway') {
        this.editModel['address'] = position ? address : this.editModel['address'];
        this.editModel['lon'] = position[0];
        this.editModel['lat'] = position[1];
      } else {
        this.childModel['address'] = position ? address : this.childModel['address'];
        this.childModel['lon'] = position[0];
        this.childModel['lat'] = position[1];
      }
    },
    childOpen(data) {
      this.editModel = data;
      // this.childSearchModel.uid = data.uid;
    },
    gatewayEdit(editModel) {
      this.centerPoint = null;
      this.mapOpenType = 'gateway';
    },
    childEdit(editModel) {
      if(!editModel.id) {
        editModel.pid = this.editModel.id;
        editModel.lon = this.editModel.lon;
        editModel.lat = this.editModel.lat;
        editModel.address = this.editModel.address;
        editModel.gatewayId = this.editModel.gatewayId;
        editModel.deviceGroupId = this.editModel.deviceGroupId;
      }

      this.centerPoint = null;
      this.mapOpenType = 'child';
    },
  }
}
</script>
<style scoped> </style>
