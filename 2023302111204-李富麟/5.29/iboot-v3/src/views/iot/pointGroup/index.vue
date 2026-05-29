<template>
  <UView name="点位组">
    <UViewDrawer title="点位组管理" :span="[2, 22]" v-model="editModel" :rules="rules" placement="top" height="100%" @edit="edit">
      <template #default="{model}">
        <UInputItem field="name" label="组名称" />
        <USelectItem field="productIds" label="所属产品" mode="multiple" :defaultValue="[]"
                     url="/iot/product/list" />
        <UTransferItem label="点位列表" field="signalIds" labelField="name" valueField="id" :span="24" :wrapperCol="{span: 24}"
                   url="/iot/signal/listByProductIds?productIds={productIds}" :show-select-all="false">
          <template #children="{direction, filteredItems, selectedKeys, disabled: listDisabled, onItemSelectAll, onItemSelect}">
            <a-table :row-selection="getRowSelection({disabled: listDisabled, selectedKeys,onItemSelectAll, onItemSelect})"
                     :columns="direction === 'left' ? leftColumns : rightColumns"
                     :data-source="filteredItems" size="small" row-key="id" bordered :pagination="false"
                     :style="{ pointerEvents: listDisabled ? 'none' : null }"
                     :custom-row="({ key, disabled: itemDisabled }) => ({
                      onClick: () => {
                        if (itemDisabled || listDisabled) return;
                        onItemSelect(key, !selectedKeys.includes(key));
                      },
                    })"/>
          </template>
        </UTransferItem>
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/pointGroup/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewDrawer>
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="组名称" allowClear/>
        <USelectItem field="signalId" label="点位" allowClear showSearch :options="signalOptions"
                     optionFilterProp="label" @search="signalSearch"/>
        <UButton func="query" url="/iot/pointGroup/view" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/pointGroup/add" v-auth="'iot:pointGroup:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns">
      <template #action="{record}">
        <UFuncTag func="edit" :data="record" url="/iot/pointGroup/edit" v-auth="'iot:pointGroup:edit'">修改</UFuncTag>
        <UFuncTag func="del" :data="record" url="/iot/pointGroup/del" v-auth="'iot:pointGroup:del'">删除</UFuncTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 点位组功能 -->
<script>
import {ref, reactive} from "vue";
import CoreConsts from "@/components/CoreConsts";
export default {
  name: "PointGroup",
  setup() {
    let columns = ref([
      {field: "name", title: "组名称", width: 180},
      {field: "signalNum", title: "点位数量", width: 60},
      {field: "productNames", title: "产品列表", width: 280, resizable: true},
      {field: 'action', title: '操作', type: 'action', width: 120},
    ]);

    let rules = reactive({
      name: {required: true, message: '组名称必填'},
      productIds: {required: true, message: '所属产品必填'},
      signalIds: {required: true, message: '点位列表必填'},
    })

    let editModel = ref({
      groupIds: []
    });

    const leftColumns = ref([
      {dataIndex: 'name', title: '点位名称', align: 'center', width: 180},
      // {dataIndex: 'fieldName', title: '字段名称', align: 'center', width: 180},
      {dataIndex: 'productName', title: '所属产品', align: 'center', width: 180},
    ]);
    const rightColumns = ref([
      {dataIndex: 'name', title: '点位名称', align: 'center', width: 180},
      // {dataIndex: 'fieldName', title: '字段名称', align: 'center', width: 180},
      {dataIndex: 'productName', title: '所属产品', align: 'center', width: 180},
    ]);

    let signalSearchValue = ref('');
    let signalOptions = ref([]);
    let searchModel = ref({typeId: null});
    return {columns, rules, editModel, searchModel, leftColumns, rightColumns, signalOptions, signalSearchValue}
  },
  methods: {
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
    edit(editModel) {

    },
    signalSearch(value) {
      this.signalSearchValue = value;
      if(!this.searchModel['productId'] && value) {
        this.$http.get(`/iot/signal/listByProductIds?name=${value}`).then(({code, message, data}) => {
          if(code == CoreConsts.SuccessCode) {
            if(data instanceof Array) {
              if(value == this.signalSearchValue) {
                this.signalOptions = data.map(item => {return {label: item.name, value: item.id}});
              }
            }
          }
        })
      }
    },
    selectType(selectedKeys, {node}) {
      if(node['type'] == 'product') {
        this.searchModel.productId = selectedKeys[0]
      } else {
        this.searchModel.productId = null;
      }

      let productId = this.searchModel['productId'] || 0;
      this.$http.get(`/iot/signal/listByProductIds?productIds=${productId}`).then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          if(data instanceof Array) {
            this.signalOptions = data.map(item => {return {label: item.name, value: item.id}});
          }
        }
      })
      this.$refs['searchRef'].trigger();
    }
  }
}
</script>
<style scoped> </style>
