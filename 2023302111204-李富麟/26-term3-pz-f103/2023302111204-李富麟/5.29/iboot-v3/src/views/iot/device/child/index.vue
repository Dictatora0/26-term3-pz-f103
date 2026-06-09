<template>
  <UView name="设备">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="设备名称" />
        <UInputItem field="deviceSn" label="设备编号" />
        <USelectItem field="productId" label="所属产品" @change="productChange" :allowClear="true"
                     url="/iot/product/listByType?deviceType=Child" labelField="name" valueField="id" />
        <UTreeSelectItem field="deviceGroupId" label="所属分组" treeDefaultExpandAll
             url="/iot/deviceGroup/tree" labelField="name" valueField="id" :allowClear="true"/>
        <URadioItem field="status" label="设备状态" dict="iot_device_status" optionType="button" buttonStyle="solid"/>
        <UButton func="query" url="/iot/device/view?deviceType=Child" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
<!--        <UButton func="add">新增</UButton>-->
      </URow>
    </UViewSearch>
    <UViewModal title="设备管理" :span="[8, 16]" v-model="editModel" :rules="rules" width="680px">
      <template #default="{model}">
        <URow :responsive="2">
          <UInputItem field="name" label="设备名称" :maxlength="32" />
          <UInputItem field="deviceSn" label="设备编号" :maxlength="32" disabled />
<!--          <USelectItem field="productId" label="所属产品" @change="productChange" v-model:source="productSource" extra="只显示网关子设备产品"-->
<!--                       url="/iot/product/listByType?productTypeId={productTypeId}&deviceType=Child" labelField="name" valueField="id">-->
<!--          </USelectItem>-->
          <USelectItem field="pid" label="父网关设备" url="/iot/device/listOfGateway?gatewayId={gatewayId}"
                       labelField="name" valueField="id"/>
          <UTreeSelectItem field="deviceGroupId" label="所属组" treeDefaultExpandAll
                           url="/iot/deviceGroup/tree" labelField="name" valueField="id" />
        </URow>
      </template>
      <template #footer="{model}">
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" :url="model.id ? '/iot/device/edit' : '/iot/device/add'">提交</UButton>
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
        <UTag func="edit" :data="record" url="/iot/device/edit" v-auth="'iot:device:child:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/iot/device/del" v-auth="'iot:device:child:del'">删除</UTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 设备功能 -->
<script>
import {ref, reactive, watch, nextTick} from "vue";
import {useRoute} from "vue-router";
import CoreConsts from "@/components/CoreConsts";
import {GET} from "@/utils/request";
import {msgWarn} from "@/utils/message";
export default {
  name: "ChildDevice",
  setup() {
    let route = useRoute();
    let columns = ref([
      {field: "uid", title: "设备标识", width: 170},
      {field: "name", title: "设备名称", width: 180},
      {field: "parentDeviceSn", title: "网关设备编号", width: 200},
      {field: "deviceSn", title: "设备编号", width: 160},
      {field: "productName", title: "所属产品", width: 120},
      {field: "productCode", title: "产品编码", width: 120, resizable: true},
      // {field: "deviceTypeName", title: "设备类型", width: 100},
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
        pid: {required: true, message: '网关设备必填'},
        status: {required: true, message: '设备状态必填'},
        deviceSn: {required: true, message: '设备编号必填'},
        productId: {required: true, message: '所属产品必填'},
        productTypeId: {required: true, message: '产品类型必填'},
    })

    let mapOpen = ref(false);
    let editModel = ref({});
    let searchRef = ref(null);
    let centerPoint = ref(null);
    let searchModel = ref({});
    let productSource = ref({});
    let protocolConfig = ref([]);
    let selectedProduct = ref({}); // 选中的产品

    // watch(() => mapOpen.value, (newVal) => {
    //   if(newVal && editModel.value['lon'] && editModel.value['lat']) {
    //     centerPoint.value = [editModel.value['lon'], editModel.value['lat']];
    //   } else {
    //     centerPoint.value = null;
    //   }
    // })
    //
    // watch(() => editModel.value['productId'], (newValue) => {
    //   if(!newValue) {
    //     selectedProduct.value = {};
    //     editModel.value['pid'] = null;
    //   } else {
    //     GET(`/iot/protocol/listProtocolConfigByProductId?type=child&productId=${newValue}`).then(({code, message, data}) => {
    //       if(code == CoreConsts.SuccessCode) {
    //         protocolConfig.value = data;
    //       } else {
    //         msgWarn(message);
    //       }
    //     })
    //   }
    // })
    //
    // watch(() => route.query, (newValue) => {
    //   let deviceSn = newValue['deviceSn'];
    //   if(deviceSn) {
    //     searchModel.value['deviceSn'] = deviceSn;
    //     if(searchRef.value) {
    //       searchRef.value.trigger();
    //     }
    //   }
    // }, {immediate: true})

    return {columns, rules, editModel, searchModel, selectedProduct, productSource, searchRef, mapOpen, centerPoint, protocolConfig}
  },
  methods: {
    productChange(value, model) {
      this.selectedProduct = model;
      if(model['deviceType'] == 'Child') { // 用于触发网关设备
        this.editModel['gatewayId'] = model['gatewayId']
      } else { // 直连产品和边缘网关产品没有pid
        this.editModel['pid'] = null;
      }
    },
    selectPoint({instance, position}) {
      this.centerPoint = position;
    },
    selectMarker(position, address) {
      this.mapOpen = false;
      this.editModel['address'] = position ? address : this.editModel['address'];
      position = position || this.centerPoint;
      this.editModel['lon'] = position[0];
      this.editModel['lat'] = position[1];
    }
  }
}
</script>
<style scoped> </style>
