<template>
  <UView>
    <UViewSearch>
      <URow col="search">
        <UInputItem label="字典名称" field="name" />
        <UInputItem label="字典类型" field="type"/>
        <USelectItem label="字典状态" field="status" :options="status"/>
        <UButton func="query" url="/core/dictType/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/core/dictType/add" v-auth="'core:dictType:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewTable :columns="columns" :scroll="{x: 1000}">
      <template #type="{text}">
        <router-link :to="{path: 'dict/data', query: {type: text}}">{{text}}</router-link>
      </template>
      <template #action="{record}">
        <UFuncTag func="edit" :data="record" url="/core/dictType/edit" v-auth="'core:dictType:edit'">修改</UFuncTag>
        <UFuncTag func="del" :data="record" url="/core/dictType/del" v-auth="'core:dictType:del'">删除</UFuncTag>
      </template>
    </UViewTable>
    <UViewModal :rules="rules" title="字典管理" :span="[6, 16]">
      <template #default="{model}">
        <UInputItem label="字典名称" field="name" :maxlength="32" />
        <UInputItem label="字典标识" field="type" :disabled="model.id ? true : false" :maxlength="28" />
        <URadioItem label="字典状态" field="status" :options="status"/>
        <UInputItem label="备注" field="remark" :maxlength="128" />
      </template>
      <template #footer="{model}">
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" :url="model.id ? '/core/dictType/edit' : '/core/dictType/add'">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
  </UView>
</template>
<!--字典管理-->
<script>

export default {
  name: "DictType",
  setup() {
    let status = [
      {label: '启用', value: 'enabled'}, {label: '禁用', value: 'disabled'}
    ]

    let columns = [
      {field: 'name', title: '字典名称'},
      {field: 'type', title: '字典标识'},
      {field: 'status', title: '字典状态', options: status},
      {field: 'remark', title: '备注'},
      {field: 'createTime', title: '创建时间'},
      {field: 'action', title: '操作', type: 'action'},
    ]

    let rules = {
      name: {required: true, message: '字典名称必填'},
      type: {required: true, message: '字典类型必填'},
    }

    return {columns, rules, status}
  }
}
</script>

<style scoped>

</style>
