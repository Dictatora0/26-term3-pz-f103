<template>
  <UView name="消息模板">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="type" label="消息类型" :allowClear="true"/>
        <UInputItem field="templateName" label="消息模板名称" :allowClear="true"/>
        <UButton func="query" url="/message/messageTemplate/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/message/messageTemplate/add" v-auth="'message:messageTemplate:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewModal title="消息模板管理" :span="[7, 14]" v-model="editModel" :rules="rules">
      <template #default="{model}">
        <UInputItem field="type" label="消息类型" />
        <UInputItem field="templateId" label="短信模板id" />
        <UInputItem field="templateName" label="消息模板名称" />
        <UInputItem field="accepts" label="接收人列表" />
        <UInputItem field="content" label="发送内容" />
        <UInputItem field="remark" label="备注" />
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/message/messageTemplate/saveOrUpdate">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewModal>
    <UViewTable :columns="columns" :scroll="{x: 1200}">
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/message/messageTemplate/edit" v-auth="'message:messageTemplate:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/message/messageTemplate/del" v-auth="'message:messageTemplate:del'">删除</UTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 消息模板功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "MessageTemplate",
  setup() {
    let columns = ref([
          {field: "type", title: "消息类型"},
          {field: "templateId", title: "短信模板id"},
          {field: "templateName", title: "消息模板名称"},
          {field: "accepts", title: "接收人列表"},
          {field: "content", title: "发送内容"},
          {field: "remark", title: "备注"},
          {field: "createTime", title: ""},
          {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 180},
      ]);

    let rules = reactive({
        type: {required: true, message: '消息类型必填'},
        createTime: {required: true, message: '必填'},
    })

    let editModel = ref({});
    let searchModel = ref({});
    return {columns, rules, editModel, searchModel}
  },
}
</script>
<style scoped> </style>
