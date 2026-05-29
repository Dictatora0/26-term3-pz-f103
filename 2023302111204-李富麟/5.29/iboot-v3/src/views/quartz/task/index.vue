<template>
    <UView rowKey="id">
        <UViewSearch>
          <URow col="search">
            <UInputItem label="任务名" field="name" />
            <UFuncBtn func="query" url="/quartz/task/view">查询</UFuncBtn>
            <UFuncBtn func="add" url="/quartz/task/add">新增</UFuncBtn>
          </URow>
        </UViewSearch>
        <UViewTable :columns="columns" :scroll="{ x: '100%'}">
            <template #status="{record}">
                <ABadge v-if="record.status=='PAUSED'" color="purple" text="暂停" />
                <ABadge v-else-if="record.status=='COMPLETE'" status="success" text="完成" />
                <ABadge v-else-if="record.status=='ACQUIRED'" status="processing" text="执行中" />
                <ABadge v-else-if="record.status=='ERROR'" status="error" text="错误" />
                <ATooltip v-else-if="record.status=='WAITING'" placement="topLeft" title="等待下一次被调度">
                    <ABadge status="warning" text="等待" />
                </ATooltip>
                <ATooltip v-else placement="topLeft" title="等待上一次任务执行完成">
                    <ABadge status="default" text="阻塞" />
                </ATooltip>
            </template>
            <template #action="{record}">
                <UFuncTag func="edit" url="/quartz/task/detail" :data="record">修改</UFuncTag>
                <UFuncTag func="del" url="/quartz/task/del" :data="record">删除</UFuncTag>
                <UFuncTag func="ajax" :url="`/quartz/task/status/${record.status=='PAUSED' ? 'resume' : 'paused'}`" :data="record">
                    {{ record.status=='PAUSED' ? '恢复' : '暂停' }}
                </UFuncTag>
                <UFuncTag func="ajax" url="/quartz/task/once" :data="record">执行一次</UFuncTag>
            </template>
        </UViewTable>
        <UViewModal title="定时任务管理" :span="[6, 16]" :rules="rules" width="700px">
            <template #default="{model}">
                <UInputItem field="name" label="任务名"/>
                <UInputItem field="cron" label="cron" />
                <URadioItem field="concurrent" label="是否并发" :defaultValue="false"
                        :options="BooleanStatus" help="指同一任务是否允许同时执行" />
                <USelectItem field="method" label="执行方法" url="/quartz/task/taskMethods" />
              <UTextareaItem field="params" label="执行参数">
                <template #extra>
                  <span>支持以下格式：</span><br />
                  <span>1. 字符串 'hello, ivzone'</span><br />
                  <span>2. 数值 3</span><br />
                  <span>3. 数组 [3, "5"]</span><br />
                  <span>4. json {"a": 3, "b": "hello"}</span>
                </template>
              </UTextareaItem>
                <UTextareaItem field="remark" label="备注" span="24" />
            </template>
            <template #footer="{model}">
                <UFuncBtn func="cancel">取消</UFuncBtn>
                <UFuncBtn func="submit" url="/quartz/task/addOrUpdate">提交</UFuncBtn>
                <UFuncBtn func="reset">重置</UFuncBtn>
            </template>
        </UViewModal>
    </UView>
</template>

<script>
import { reactive, ref } from "vue";
import { BooleanStatus } from '@/utils/StatusConsts'
export default {
    name: "TaskJob",
    setup () {
        let rules = reactive({
            jobName: { required: true, message: '作业名必填' },
            cron: { required: true, message: 'cron必填' },
            method: { required: true, message: '执行方法' }
        })
        let state = [
            { label: '暂停', value: 'PAUSED' },
            { label: '完成', value: 'COMPLETE' },
            { label: '错误', value: 'ERROR' },
            { label: '等待', value: 'WAITING' },
            { label: '正常', value: 'ACQUIRED' },
            { label: '阻塞', value: 'BLOCKED' },
        ]
        let columns = ref([
            { field: 'name', title: '任务名称', width: 200 },
            { field: 'cron', title: 'cron', width: 200 },
            { field: 'method', title: '执行方法', width: 280 },
            // { field: 'startTime', title: '开始时间', width: 150, type: 'date', ellipsis: true },
            // { field: 'nextFireTime', title: '下次时间', width: 150, type: 'date', },
            { field: 'remark', title: '说明', width: 280, ellipsis: true },
            { field: 'status', title: '状态', width: 100, fixed: 'right' },
            { field: 'action', type: 'action', title: '操作', width: 280, fixed: 'right' }
        ])
        return { columns, rules, BooleanStatus }
    },
    methods: {

    }
}
</script>

<style scoped>
</style>
