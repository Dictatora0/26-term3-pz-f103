<template>
  <UView name="产品类型">
    <U2Col :gutter="8">
      <template #left>
        <ACard :bordered="false">
          <UTree url="/iot/productType/tree" selectable blockNode showLine defaultExpandAll @select="selectParent" />
        </ACard>
      </template>
      <template #right>
        <UViewSearch v-model="searchModel">
          <URow col="search">
            <UInputItem field="name" label="类型名称" :allowClear="true"/>
            <UButton func="query" url="/iot/productType/view" ref="searchRef" :callback="queryCall">搜索</UButton>
            <UButton func="reset">重置</UButton>
            <UButton func="add" url="/iot/productType/add" v-auth="'iot:productType:add'">新增</UButton>
          </URow>
        </UViewSearch>
        <UViewTable :columns="columns" :scroll="{x: 1200}">
          <template #action="{record}">
            <UTag func="edit" :data="record" url="/iot/productType/edit" v-auth="'iot:productType:edit'">修改</UTag>
            <UTag func="del" :data="record" url="/iot/productType/del" v-auth="'iot:productType:del'">删除</UTag>
          </template>
        </UViewTable>
      </template>
    </U2Col>
    <UViewModal title="产品类型管理" :span="[7, 14]" v-model="editModel" :rules="rules" @edit="editOpen">
      <template #default="{model}">
        <UInputItem field="name" label="类型名称" :maxlength="32" />
        <UTreeSelectItem field="pid" label="父类型" url="/iot/productType/tree" ref="productTypeParentRef"
                         treeDefaultExpandAll labelField="name" valueField="id"/>
        <UInputItem field="alias" label="类型别名" :maxlength="18" />
<!--        <UInputItem field="image" label="类型图地址" />-->
        <UInputItem field="remark" label="类型说明" :maxlength="128" />
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/productType/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
  </UView>
</template>
<!-- 产品类型功能 -->
<script>
import {ref, reactive} from "vue";
import CoreConsts from "@/components/CoreConsts";
export default {
  name: "ProductType",
  setup() {
    let columns = ref([
      {field: "name", title: "类型名称", width: 280},
      {field: "parentName", title: "父类型"},
      {field: "alias", title: "类型别名"},
      // {field: "image", title: "类型图地址"},
      {field: "remark", title: "类型说明"},
      {field: "createTime", title: "创建时间", type: 'date'},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 180},
    ]);

    let rules = reactive({
        pid: {required: true, message: '父id必填'},
        name: {required: true, message: '类型名称必填'},
        createTime: {required: true, message: '必填'},
    })

    let editModel = ref({pid: 0});
    let searchModel = ref({});
    let productTypeParentRef = ref();
    return {columns, rules, editModel, searchModel, productTypeParentRef}
  },
  methods: {
    editOpen(editModel) {
      if(!editModel['pid']) {
        editModel['pid'] = this.searchModel['pid'] || 0;
      }
    },
    selectParent(selectedKeys) {
      this.searchModel.pid = selectedKeys[0];
      this.$refs['searchRef'].trigger();
    },
    queryCall({code}) {
      if(code == CoreConsts.SuccessCode) {
        this.productTypeParentRef['refreshOptions']();
      }
    }
  }
}
</script>
<style scoped> </style>
