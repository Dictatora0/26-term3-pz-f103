<template>
  <UView name="角色管理" urlAuth>
    <UViewSearch ref="ivzForm">
      <UInputItem label="角色名称" field="name" />
      <URadioItem label="状态" field="status" :options="status"/>
      <template #func>
        <UButton func="query" url="/core/role/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/core/role/add" v-auth="'core:role:add'">新增</UButton>
      </template>
    </UViewSearch>
    <UViewModal :span="[5, 16]" :rules="rules" title="角色管理">
      <UInputItem label="角色名称" field="name"  :maxlength="18"/>
      <URadioItem label="状态" field="status" :options="status" defaultValue="enabled"/>
      <UInputNumberItem label="排序" field="sort" :defaultValue="10"/>
      <UTextareaItem label="备注" field="remark" :maxlength="256" />
      <template #footer="{model}">
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" :url="model.id ? '/core/role/edit' : '/core/role/add'">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <UViewTable :columns="columns" :scroll="{x: 800}">
      <template #c_action="{record}">
        <UTag func="edit" :data="record" url="/core/role/edit" v-auth="'core:role:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/core/role/del" v-auth="'core:role:del'">删除</UTag>
        <UTag func="edit" url="/core/role/func" uid="funcPerm"
            :data="record" v-auth="'core:role:perm'">分配权限</UTag>
      </template>
    </UViewTable>
    <UFormDrawer v-model="permModel" uid="funcPerm" title="分配功能权限" placement="left" width="580">
      <template #default="{model}">
        &nbsp;<a-button @click="() => expanded('close')">折叠</a-button>
        &nbsp;<a-button type="primary" @click="() => expanded('open')">展开</a-button>
        <a-checkbox style="float: right" v-model:checked="checkedValue" @change="checked">全选</a-checkbox>

        <UTree field="menuIds" url="/core/role/allMenus" :checkStrictly="false"
               showLine checkable ref="funcMenus" style="margin-top: 12px"/>
      </template>
      <template #footer>
        <UButton func="submit" url="/core/role/perm">提交</UButton>
        <UButton func="cancel">取消</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UFormDrawer>
  </UView>
</template>

<script>
import {ref} from "vue";

export default {
  name: "Role",
  setup() {
    let status = [
      {label: '启用', value: 'enabled'}, {label: '禁用', value: 'disabled'}
    ]
    let columns = [
      {field: 'name', title: '名称', width: 180},
      {field: 'status', title: '状态', options: status, width: 56},
      {field: 'sort', title: '排序', width: 56},
      {field: 'remark', title: '备注'},
      {field: 'createTime', title: '创建时间', type: 'datetime', picker: 'date', width: 160},
      {field: 'action', title: '操作', type: 'action', width: 230}
    ]
    let rules = {
      name: {required: true, message: '角色名称必填'}
    }

    let permModel = ref({menuIds: []});
    let filterValue = ref('');
    let checkedValue = ref(false);

    return {columns, rules, status, filterValue, checkedValue, permModel}
  },

  methods: {
    getCheckedUrl(model) {
      let param = model.id ? `?id=${model.id}` : ''
      return '/core/role/func' + param;
    },
    checked(e) {
      if(e.target.checked) {
        let allKeys = this.$refs['funcMenus'].getAllKeys();
        this.permModel.menuIds = allKeys;
      } else {
        this.permModel.menuIds = [];
      }
    },
    expanded(type) {
      if(type == 'open') {
        this.$refs['funcMenus'].setExpandedAllKeys();
      } else {
        this.$refs['funcMenus'].setExpandedKeys([])
      }
    }
  }
}
</script>

<style scoped>

</style>
