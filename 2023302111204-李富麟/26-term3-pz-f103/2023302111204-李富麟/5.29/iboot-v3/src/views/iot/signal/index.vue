<template>
  <UView name="寄存器">
    <UViewModal title="寄存器管理" :span="[7, 14]" v-model="editModel" :rules="rules" @edit="edit">
      <template #default="{model}">
        <USelectItem field="productId" label="所属产品" labelField="name" valueField="id"
          url="/iot/product/listByPoint" extra="只显示点位协议的产品" @change="productChange" v-model:source="productSourceMap"/>
        <USelectItem field="direct" label="协议指令" url="/iot/protocol/apis?type=event&id={protocolId}" valueField="code" labelField="name">
          <template #option="{value, label}">
            <div style="padding: 0px 0px 5px;">
              <span style="float: left">{{label}}</span>
              <span style="float: right">{{value}}</span>
            </div>
          </template>
        </USelectItem>
        <UInputItem field="address" label="点位地址" />
        <UInputItem field="name" label="点位名称" />
<!--        <UInputItem field="fieldName" label="字段标识" extra="请使用英文字符" />-->
        <UInputNumberItem field="num" label="读寄存器数量" extra="字符串类型必填,其他不需要指定" :min="1" :step="1" :precision="0"/>
        <!--        <URadioItem field="type" label="类型" :options="type" :defaultValue="1"/>-->
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/signal/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="点位名称" :allowClear="true"/>
        <UInputItem field="fieldName" label="字段标识" :allowClear="true"/>
        <UButton func="query" url="/iot/signal/view" ref="searchRef">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/signal/add" v-auth="'iot:signal:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1000}">
      <template #action="{record}">
        <UFuncTag func="edit" :data="record" url="/iot/signal/edit" v-auth="'iot:signal:edit'">修改</UFuncTag>
        <UFuncTag func="del" :data="record" url="/iot/signal/del" v-auth="'iot:signal:del'">删除</UFuncTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 寄存器点位功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "Signal",
  setup() {
    let type = ref([
      {label: '点位', value: 1},
      {label: '自定义', value: 2},
    ]);
    let columns = ref([
      {field: "name", title: "点位名称", width: 150},
      {field: "address", title: "点位地址", width: 120},
      // {field: "fieldName", title: "字段标识", width: 150},
      {field: "productName", title: "所属产品", width: 150},
      {field: "direct", title: "协议指令", width: 120},
      {field: "num", title: "寄存器数量", width: 100},
      // {field: "type", title: "点位类型", width: 90, options: type},
          // {field: "encode", title: "报文编码", width: 90},
          // {field: "message", title: "自定义报文", width: 280},
      {field: 'action', title: '操作', type: 'action', width: 160, fixed: 'right'},
    ]);
    // HEX, UTF8, ASCII
    let encode = ref([
      {label: 'HEX', value: 'HEX'},
      {label: 'UTF8', value: 'UTF8'},
      {label: 'ASCII', value: 'ASCII'},
    ])
    let rules = reactive({
        name: {required: true, message: '点位名称必填'},
        address: {required: true, message: '点位地址必填'},
        productId: {required: true, message: '所属产品必填'},
        direct: [{required: true, message: '协议指令必填'}],
        encode: [{required: true, message: '报文编码必填'}],
        fieldName: {required: true, message: '字段名称必填'},
        fieldType: {required: true, message: '字典类型必填'},
    })

    let editModel = ref({typeId: null});
    let searchModel = ref({typeId: null});
    let productSourceMap = ref({});
    return {columns, rules, editModel, searchModel, type, encode, productSourceMap}
  },
  methods: {
    edit(editModel) {
      if(this.searchModel['productId']) {
        editModel.productId = this.searchModel['productId']
        let product = this.productSourceMap[editModel.productId];
        if(product) {
          editModel.protocolId=product['protocolId'];
        }
      }
    },
    productChange(value, select) {
      this.editModel['protocolId'] = select['protocolId'];
    },
    selectType(selectedKeys, {node}) {
      if(node['type'] == 'product') {
        this.searchModel.productTypeId = null;
        this.searchModel.productId = selectedKeys[0];
      } else {
        this.searchModel.productId = null;
        this.searchModel.productTypeId = selectedKeys[0];
      }

      this.$refs['searchRef'].trigger();
    }
  }
}
</script>
<style scoped> </style>
