<template>
  <UView name="设备分组">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="分组名称" allowClear/>
        <UButton func="query" url="/iot/deviceGroup/tree" ref="searchRef" :callback="queryCall">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" v-auth="'iot:deviceGroup:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1200}">
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/iot/deviceGroup/edit" v-auth="'iot:deviceGroup:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/iot/deviceGroup/del" v-auth="'iot:deviceGroup:del'">删除</UTag>
      </template>
    </UViewTable>

    <UViewModal title="设备分组管理" :span="[5, 18]" v-model="editModel" :rules="rules">
      <template #default="{model}">
        <UTreeSelectItem field="pid" label="父级" :defaultValue="0" url="/iot/deviceGroup/tree"
                         treeDefaultExpandAll labelField="name" valueField="id" ref="deviceGroupParentRef">
          <template #title="{name, type, pid}">
            <div v-if="pid != null">
              <span style="float: left">{{name}}</span>
            </div>
            <div v-else>顶级设备组</div>
          </template>
        </UTreeSelectItem>
        <UInputItem field="name" label="分组名称" :maxlength="32" />
<!--        <UTreeSelectItem field="productIds" label="产品列表" url="/iot/productType/productTree" multiple-->
<!--                         treeDefaultExpandAll labelField="name" :defaultValue="[]" valueField="id" @loaded="productLoaded"/>-->
<!--        <UInputItem field="lon" label="经度" />-->
<!--        <UInputItem field="lat" label="纬度" />-->
        <UInputItem field="address" label="分组地址" readonly>
          <template #addonAfter>
            <span style="cursor: pointer" @click="() => {mapOpen = true}">地图</span>
          </template>
        </UInputItem>
        <UInputItem field="remark" label="备注" :maxlength="64" />
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/deviceGroup/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <AMapModal v-model:open="mapOpen" :zoom="12" :center="centerPoint" @click="selectPoint" >
      <template #marker="{position, address}">
        <AMarker v-if="centerPoint" title="点击选中地址" :position="centerPoint"
                 @click="() => selectMarker(position, address)" id="current"></AMarker>
      </template>
    </AMapModal>
  </UView>
</template>
<!-- 设备分组功能 -->
<script>
import {ref, reactive, watch} from "vue";
import CoreConsts from "@/components/CoreConsts";
export default {
  name: "DeviceGroup",
  setup() {
    let columns = ref([
      {field: "name", title: "分组名称", width: 180, align: 'left'},
      {field: "pid", title: "父级分组", url: "/iot/deviceGroup/tree", labelField: 'name', valueField: 'id', width: 180},
      {field: "address", title: "分组地址", width: 260},
      // {field: "productNames", title: "产品列表", width: 320},
      {field: "remark", title: "备注"},
      {field: "createTime", title: "创建时间", width: 160, type: 'date'},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 180},
    ]);

    let rules = reactive({
      pid: {required: true, message: '父级必填'},
      type: {required: true, message: '组类型必填'},
      name: {required: true, message: '分组名称必填'},
    })

    let mapOpen = ref(false);
    let parent = ref({});
    let editModel = ref({});
    let searchModel = ref({});
    let centerPoint = ref(null);

    watch(() => editModel.value['lon'], (newVal) => {
      if(newVal) {
        centerPoint.value = [editModel.value['lon'], editModel.value['lat']];
      }
    })

    let deviceGroupParentRef = ref();
    let deviceGroupOptions = ref([]);
    return {columns, rules, editModel, searchModel, parent, mapOpen, centerPoint, deviceGroupOptions, deviceGroupParentRef}
  },
  methods: {
    productLoaded(data) {
      if(data) {
        Object.values(data).forEach(item => {
          if(item['type'] == 'productType') {
            item['selectable'] = false;
          }
        })
      }
    },
    getRowSelection({disabled, selectedKeys, onItemSelectAll, onItemSelect}) {
      return {
        getCheckboxProps: item => ({
          disabled: disabled || item.disabled,
        }),

        onSelectAll(selected, selectedRows) {
          const treeSelectedKeys = selectedRows.filter(item => !item.disabled).map(({key}) => key);
          onItemSelectAll(treeSelectedKeys, selected);
        },

        onSelect({ key}, selected) {
          onItemSelect(key, selected);
        },

        selectedRowKeys: selectedKeys,
      };
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
    },
    queryCall({code, data}) {
      if(code == CoreConsts.SuccessCode) {
        this.deviceGroupParentRef['refreshOptions']();
      }
    }
  }
}
</script>
<style scoped> </style>
