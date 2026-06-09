<template>
  <UView name="岗位管理">
    <U2Col>
      <template #left>
        <ACard :bordered="false">
          <UTree url="/core/org/parent" selectable blockNode showLine defaultExpandAll @select="selectParent" />
        </ACard>
      </template>
      <template #right>
        <UViewSearch v-model="searchModel">
          <URow col="search">
            <UInputItem field="name" label="岗位名称" :allowClear="true"/>
            <UButton func="query" url="/core/post/view" ref="searchRef">搜索</UButton>
            <UButton func="reset">重置</UButton>
            <UButton func="add" url="/core/post/add" v-auth="'core:post:add'">新增</UButton>
          </URow>
        </UViewSearch>
        <UViewTable :columns="columns" :scroll="{x: 1000}">
          <template #action="{record}">
            <UTag func="edit" :data="record" url="/core/post/edit" v-auth="'core:post:edit'">修改</UTag>
            <UTag func="del" :data="record" url="/core/post/del" v-auth="'core:post:del'">删除</UTag>
          </template>
        </UViewTable>
      </template>
    </U2Col>

    <UViewModal title="岗位管理管理" :span="[7, 14]" v-model="editModel" :rules="rules" @edit="editOpen">
      <template #default="{model}">
        <UInputItem field="name" label="岗位名称" :maxlength="32" />
        <UTreeSelectItem field="orgId" label="所属机构" url="/core/org/parent" labelField="name" valueField="id"/>
        <UInputNumberItem field="sort" label="排序" :defaultValue="0"/>
        <UTextareaItem field="remark" label="岗位介绍" :maxlength="128" />
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/core/post/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
  </UView>
</template>
<!-- 岗位管理功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "Post",
  setup() {
    let columns = ref([
      {field: "name", title: "岗位名称"},
      {field: "orgName", title: "所属机构"},
      {field: "sort", title: "排序", width: 80},
      {field: "remark", title: "岗位介绍", width: 280},
      {field: "createTime", title: "创建时间", type: 'date', format: 'YYYY-MM-DD'},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 180},
    ]);

    let rules = reactive({
      name: {required: true, message: '岗位名称必填'},
      orgId: {required: true, message: '所属机构必填'},
      createTime: {required: true, message: '创建时间必填'},
    })

    let editModel = ref({});
    let searchModel = ref({});
    return {columns, rules, editModel, searchModel}
  },
  methods: {
    editOpen(editModel) {
      editModel['orgId'] = this.searchModel['orgId']
    },
    selectParent(selectedKeys) {
      this.searchModel['orgId'] = selectedKeys[0];
      this.$refs['searchRef'].trigger();
    },
  }
}
</script>
<style scoped> </style>
