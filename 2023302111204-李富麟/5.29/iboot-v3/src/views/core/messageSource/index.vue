<template>
  <UView name="消息源">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="name" label="消息源名称" :allowClear="true"/>
        <USelectItem field="type" label="消息类型" :allowClear="true" url="/core/messageSource/types" style="width: 200px"/>
        <UButton func="query" url="/core/messageSource/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/core/messageSource/add" v-auth="'core:messageSource:add'">新增</UButton>
      </URow>
    </UViewSearch>
    <UViewModal title="消息源管理" :span="[6, 18]" v-model="editModel" :rules="rules" width="600px" @edit="editHandle">
      <template #default="{model}">
        <UInputItem field="name" label="消息源名称" />
        <URadioItem field="type" label="消息类型" url="/core/messageSource/types" optionType="button" buttonStyle="solid"
            @change="typeChange" v-model:source="typesList" :disabled="editModel.id != null"/>
        <USelectItem field="channel" label="消息通道" :options="channelOptions"/>
        <template v-for="item in extraConfig" :key="item['field']">
          <UInputItem :field="'config.' + item['field']" :label="item['label']"
                :required="item['required']" :extra="item['extra']" :defaultValue="item['defaultValue']"/>
        </template>
      </template>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/core/messageSource/saveOrUpdate">提交</UButton>
      </template>
    </UViewModal>
    <UViewTable :columns="columns" :scroll="{x: 1200}">
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/core/messageSource/edit" v-auth="'core:messageSource:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/core/messageSource/del" v-auth="'core:messageSource:del'">删除</UTag>
        <UTag func="edit:set" :config="{copy: ['id']}" uid="debug" :data="record" color="#87d068" v-auth="'core:messageSource:debug'">调试</UTag>
      </template>
    </UViewTable>
    <UFormModal uid="debug" v-model="DebugModel" title="消息源调试" :span="[6, 16]">
<!--      <USelectItem field="accept" label="收件人" required extra="多个收件人用逗号隔开"/>-->
      <UInputItem field="content" label="发送内容" />
      <UInputItem field="title" label="内容标题" extra="用于发送邮件等方式"/>
      <UInputItem field="templateId" label="templateId" extra="用于模板短信" />
      <UInputItem field="templateName" label="templateName" extra="用于模板短信"/>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" :url="`/core/messageSource/debug/${DebugModel['id']}`">发送</UButton>
      </template>
    </UFormModal>
  </UView>
</template>
<!-- 消息源功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "MessageSource",
  setup() {
    let columns = ref([
        {field: "name", title: "消息源名称"},
        {field: "type", title: "消息类型", url: "/core/messageSource/types"},
        {field: "channel", title: "通道类型", url: "/core/messageSource/types"},
        {field: "createTime", title: "创建时间", type: 'date', format: 'YYYY-MM-DD'},
        {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 230},
      ]);

    let rules = reactive({
        type: {required: true, message: '消息类型必填'},
        name: {required: true, message: '消息源名称必填'},
        channel: {required: true, message: '消息通道必填'},
    })

    let debugRow = ref({});
    let DebugModel = ref({});
    let editModel = ref({});
    let searchModel = ref({});
    let typesList = ref({});
    let extraConfig = ref([]);
    let channelOptions = ref([]);
    return {columns, rules, editModel, searchModel, typesList, channelOptions, extraConfig, DebugModel, debugRow}
  },
  methods: {
    editHandle(model) {
      let channel = null;
      if(this.typesList && model['type']) {
        model['type'] = model['type'] || this.typesList[0].value;
        channel = this.typesList[model['channel']];
        this.channelOptions = this.typesList[model['type']]['children'];
      }

      if(channel) {
        this.extraConfig = channel['extra'];
      }
    },

    typeChange(value, model) {
      this.channelOptions = this.typesList[value]['children'];
      this.editModel['channel'] = this.channelOptions[0].value;
      // let config = JSON.parse(this.channelOptions[0]['extra']);
      this.extraConfig = this.channelOptions[0]['extra'];
    }
  }
}
</script>
<style scoped> </style>
