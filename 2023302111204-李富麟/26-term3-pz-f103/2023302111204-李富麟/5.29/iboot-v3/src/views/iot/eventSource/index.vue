<template>
  <UView name="事件源">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <USelectItem field="productId" label="产品" url="/iot/product/list" allowClear/>
        <UTreeSelectItem field="deviceGroupId" label="所属设备组" url="/iot/deviceGroup/tree"
                         labelField="name" valueField="id" treeDefaultExpandAl allowClear/>
        <UInputItem field="name" label="事件名称" allowClear/>
        <USelectItem field="type" label="事件类型" url="/iot/eventSource/types" />
        <UButton func="query" url="/iot/eventSource/view">搜索</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="add" url="/iot/eventSource/add" v-auth="'iot:eventSource:add'">新增</UButton>
        <UButton func="open" uid="running" :data="{}">运行日志</UButton>
      </URow>
    </UViewSearch>
    <UDrawerLogger uid="running"/>
<!--    <UFormDrawer uid="sourceConfig" title="事件源管理" :span="[6, 16]" v-model="editModel" :rules="rules" width="100%">-->
<!--      <template #extra>-->
<!--        <div>注：同一事件源下的被动模型事件接口将统一采集</div>-->
<!--      </template>-->
<!--      <template #default="{model}">-->
<!--        <URow :responsive="2">-->
<!--          <URadioItem field="type" label="事件类型" url="/iot/eventSource/types" optionType="button"-->
<!--                      buttonStyle="solid" defaultValue="event"/>-->
<!--          <UTreeSelectItem field="deviceGroupId" label="设备组" url="/iot/eventSource/deviceGroups"-->
<!--                 treeDefaultExpandAll treeCheckable extra="用于限定触发事件和执行动作的设备范围">-->
<!--            <template #title="{label, extra}">-->
<!--              <div>-->
<!--                <span>{{label}}</span>-->
<!--&lt;!&ndash;                <span style="float: right;">({{extra}})</span>&ndash;&gt;-->
<!--              </div>-->
<!--            </template>-->
<!--          </UTreeSelectItem>-->
<!--          <UTransferItem label="物模型接口" field="modelApiIds" :span="24" :labelCol="{span: 3}" :wrapperCol="{span: 20}"-->
<!--                         url="/iot/deviceGroup/listModelApi?deviceGroupId={deviceGroupId}" :show-select-all="false" @change="modelApiIdsChange">-->
<!--            <template #children="{direction, filteredItems, selectedKeys, disabled: listDisabled, onItemSelectAll, onItemSelect}">-->
<!--              <a-table :row-selection="getRowSelection({disabled: listDisabled, selectedKeys,onItemSelectAll, onItemSelect})"-->
<!--                       :columns="direction === 'left' ? leftColumns : rightColumns"-->
<!--                       :data-source="filteredItems" size="small" row-key="value" bordered :pagination="false"-->
<!--                       :style="{ pointerEvents: listDisabled ? 'none' : null }"-->
<!--                       :custom-row="({ key, disabled: itemDisabled }) => ({-->
<!--                      onClick: () => {-->
<!--                        if (itemDisabled || listDisabled) return;-->
<!--                        onItemSelect(key, !selectedKeys.includes(key));-->
<!--                      },-->
<!--                    })"/>-->
<!--            </template>-->
<!--          </UTransferItem>-->
<!--        </URow>-->
<!--      </template>-->
<!--      <template #footer>-->
<!--        <UButton func="cancel">取消</UButton>-->
<!--        <UButton func="submit" url="/iot/eventSource/config">提交</UButton>-->
<!--        <UButton func="reset">重置</UButton>-->
<!--      </template>-->
<!--    </UFormDrawer>-->
    <UViewDrawer title="事件源管理" :span="[6, 16]" :rules="rules" v-model="editModel" width="100%">
      <URow :responsive="3">
        <UInputItem field="name" label="事件名称" />
        <URadioItem field="type" label="事件类型" url="/iot/eventSource/types" optionType="button"
                    buttonStyle="solid" defaultValue="event" :disabled="editModel['id'] != null"/>

          <UInputItem field="cron" label="采集周期" extra="用于指定采集物模型被动事件的周期">
            <template #addonAfter>
              <a-popover trigger="click">
                <template #content>
                  <UCron @change="(value) => editModel['cron'] = value" :cron="editModel['cron']"/>
                </template>
              <span style="cursor: pointer">选择</span>
              </a-popover>
            </template>
          </UInputItem>
        <USelectItem field="productIds" label="产品列表" url="/iot/product/list" mode="multiple" :maxTagCount="10" :disabled="editModel['hasBindLinkage']"
                     :defaultValue="[]" :span="24" :labelCol="{span: 2}" :wrapperCol="{span: 22}" extra="产品列表和设备组如果绑定联动项则不允许修改" />
        <USelectItem field="deviceGroupIds" label="设备组" url="/iot/eventSource/deviceGroups" :defaultValue="[]"
                         :span="24" :labelCol="{span: 2}" :wrapperCol="{span: 22}" :disabled="editModel['hasBindLinkage']"
                   extra="用于限定触发事件和执行动作的设备范围" @loaded="deviceGroupLoaded" mode="multiple" :maxTagCount="10">
        </USelectItem>
      </URow>
      <template #footer>
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" url="/iot/eventSource/saveOrUpdate" :params="resolveSubmitParams">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewDrawer>
    <UViewTable :columns="columns" :scroll="{x: 1000}">
      <template #status="{record, text, value}">
        <ABadge v-if="text=='enabled'" status="processing" color="#87d068" text="启用" />
        <ABadge v-else status="warning" text="停用" />
      </template>
      <template #action="{record}">
        <UTag func="edit" :data="record" url="/iot/eventSource/edit" v-auth="'iot:eventSource:edit'">修改</UTag>
        <UTag func="del" :data="record" url="/iot/eventSource/del" v-auth="'iot:eventSource:del'"
              :config="{confirmTitle: '确认要删除此事件源吗(不可恢复)?', confirmContent: '注：将一并删除和此事件源相关的场景联动配置项'}">删除</UTag>
        <UTag func="ajax:confirm" :data="{id: record['id']}" :url="`/iot/eventSource/switch/${record['status'] == 'enabled' ? 'disabled' : 'enabled'}`" method="GET"
              v-auth="'iot:eventSource:switch'" :config="{confirmTitle: `确认${record['status'] == 'start' ? '停用' : '启用'}事件源[${record.name}]`, confirmContent: `停用后此事件源将失效`}">
          {{record['status'] == 'enabled' ? '停用' : '启用'}}
        </UTag>
      </template>
    </UViewTable>
  </UView>
</template>
<!-- 事件源功能 -->
<script>
import {ref, reactive} from "vue";
export default {
  name: "EventSource",
  setup() {
    let columns = ref([
      {field: "name", title: "名称"},
      {field: "type", title: "源类型", url: "/iot/eventSource/types"},
      {field: "cron", title: "采集周期"},
      // {field: "deviceGroupId", title: "所属设备组", url: "/iot/deviceGroup/tree"},
      {field: "reason", title: "失败原因", width: 220},
      {field: "createTime", title: "创建时间", type:  'date', format: 'YYYY-MM-DD'},
      {field: "status", title: "状态", width: 80, fixed: 'right'},
      {field: 'action', title: '操作', type: 'action', fixed: 'right', width: 180},
    ]);
    const leftColumns = ref([
      {dataIndex: 'label', title: '接口名称', align: 'center', width: 180},
      {dataIndex: 'extra', title: '接口代码', align: 'center', width: 180},
    ]);
    const rightColumns = ref([
      {dataIndex: 'label', title: '接口名称', align: 'center', width: 180},
      {dataIndex: 'extra', title: '接口代码', align: 'center', width: 180},
    ]);
    let rules = reactive({
      name: {required: true, message: '事件名称必填'},
      type: {required: true, message: '事件类型必填'},
      // cron: {required: true, message: '采集周期必填'},
      productIds: {required: true, message: '产品列表必填'},
      deviceGroupIds: {required: true, message: '设备组必填'},
    })

    let editModel = ref({});
    let searchModel = ref({});
    return {columns, rules, editModel, searchModel, leftColumns, rightColumns}
  },
  methods: {
    modelApiIdsChange(targetKeys, targetItems, direction, moveKeys) {
      if(targetItems instanceof Array) {
          this.editModel['hasPassive'] = targetItems.filter(item => item.config['trigger'] == 'passive').length > 0;
      }
    },
    deviceGroupLoaded(data) {
      if(data) {
        Object.values(data).forEach(item => {
          // 只允许选择设备组和产品组
          if(item['type'] == 'archive') {
            item['selectable'] = false;
          }
        })
      }
    },
    resolveSubmitParams(data) {
      if(data['hasPassive']) {
        if(!data['cron']) {
          this.$msg.warn("包含被动事件必须配置采集周期");
          return Promise.reject('');
        }
      }
      return Promise.resolve(data);
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
  }
}
</script>
<style scoped> </style>