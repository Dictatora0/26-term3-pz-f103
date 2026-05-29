<template>
  <UView>
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <USelectItem label="字典标识" field="type" @change="loadDictData"
                 url="/core/dictType/list" labelField="name" valueField="type"
                 :defaultValue="type"/>
        <UInputItem label="数据名称" field="label" />
        <UButton func="query" url="/core/dictData/view" ref="queryFunc">查询</UButton>&nbsp;
        <UButton func="add" url="/core/dictData/add">新增</UButton>&nbsp;
      </URow>
    </UViewSearch>
    <UViewDrawer title="新增" :span="[6, 16]" :rules="rules" @edit="edit">
        <UInputItem field="type" label="字典标识" disabled/>
        <UInputItem field="label" label="数据名称" :maxlength="32" />
        <UInputItem field="value" label="数据值" :maxlength="50" />
        <URadioItem field="status" label="状态" dict="sys_func_status" :defaultValue="'enabled'"/>
        <UInputNumberItem field="sort" label="排序" :defaultValue="10"/>
        <UInputItem field="remark" label="备注" :maxlength="128"/>
      <template #footer="{model}">
        <div style="text-align: center">
          <UButton func="cancel">取消</UButton>
          <UButton func="submit" :url="model.id ? '/core/dictData/edit' : '/core/dictData/add'">提交</UButton>&nbsp;
          <UButton func="reset">重置</UButton>&nbsp;
        </div>
      </template>
    </UViewDrawer>
    <UViewTable :columns="columns" rowKey="id" :scroll="{x: 1000}">
      <template #c_action="{record}">
        <UTag func="edit" :data="record" url="/core/dictData/edit">编辑</UTag>
        <UTag func="del" :data="record" url="/core/dictData/del">删除</UTag>
      </template>
    </UViewTable>
  </UView>
</template>

<script>
/*字典数据页面*/
import {useRouter} from "vue-router";
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "DictData",
  setup() {
    let columns = [
      {title: '字典标识', field: 'type'},
      {title: '数据名称', field: 'label'},
      {title: '数据值', field: 'value'},
      {title: '状态', field: 'status', dict: 'sys_func_status'},
      {title: '排序', field: 'sort'},
      {title: '备注', field: 'remark'},
      {title: '操作', field: 'action', type: 'action'},
    ]

    let rules = {
      label: {required: true, message: '字典标签必填'},
      value: {required: true, message: '标签值必填'},
    }

    let searchModel = {};
    let type = useRouter().currentRoute.value.query.type;
    return {columns, rules, type, searchModel};
  },
  watch: {
    $route(to, form) {
      let type = to.query.type;
      if(type) {
        if(this.searchModel.type != type) {
          this.searchModel.type = type;
          this.query();
        }
      }
    }
  },
  methods: {
    query() {
      this.$refs['queryFunc'].trigger();
    },

    edit(model) {
      model.type = this.searchModel.type;
    },

    loadDictData() {
      this.query();
    },
  }
}
</script>

<style scoped>

</style>
