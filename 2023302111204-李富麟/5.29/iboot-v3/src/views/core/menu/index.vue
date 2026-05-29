<template>
  <UView name="菜单">

    <U2Col :gutter="8">
      <template #left>
        <ACard :bordered="false">
          <UTree url="/core/menu/parent" selectable blockNode showLine defaultExpandAll @select="selectParent" />
        </ACard>
      </template>
      <template #right>
        <UViewSearch v-model="searchModel">
          <URow col="search">
            <UInputItem field="name" label="菜单名称" :allowClear="true" />
            <USelectItem field="status" label="状态" dict="sys_func_status"/>
            <USelectItem field="type" label="菜单类型" :options="type" :allowClear="true"/>
            <USelectItem field="msn" label="所属模块" :allowClear="true" :refresh="false"
                         url="/core/menu/msn" valueField="msn" labelField="name">
              <template #option="{value, label}">
                <div>
                  <span>{{label}}</span>
                  <span style="float: right; margin-right: 8px">{{value}}</span>
                </div>
              </template>
            </USelectItem>
            <UInputItem field="perms" label="权限标识" :allowClear="true" />
            <UButton func="query" url="/core/menu/view" ref="searchRef" :callback="queryCall">搜索</UButton>
            <UButton func="reset">重置</UButton>
          </URow>
        </UViewSearch>
        <UViewTable :columns="columns" :pagination="false" :scroll="{x: 900}">
          <template #func>
            <UButton func="add" v-auth="'core:menu:add'">新增</UButton>
          </template>
          <template #action="{record}">
            <UTag func="edit" :data="record" url="/core/menu/edit" v-auth="'core:menu:edit'">修改</UTag>
            <UTag func="del" :data="record" url="/core/menu/del" v-auth="'core:menu:del'">删除</UTag>
          </template>
        </UViewTable>
      </template>
    </U2Col>
    <UViewDrawer :rules="rules" width="720" title="菜单管理" v-model="editModel"
                 layout="vertical" placement="left" @edit="edit">
      <URow :gutter="12" col="drawer">
        <UInputItem field="name" label="菜单名称" :maxlength="20" />
        <UTreeSelectItem field="pid" label="父菜单" :defaultValue="0"
             url="/core/menu/parent" labelField="name" valueField="id"
             treeNodeFilterProp="label" @change="changeParent" ref="parentRef"/>
        <USelectItem field="msn" label="所属模块" :allowClear="true" :refresh="false"
                   url="/core/menu/msn" valueField="msn" labelField="name"/>
        <USelectItem field="type" label="菜单类型" :options="type"/>
        <UInputItem field="url" label="菜单URL" :maxlength="120"/>
        <UInputItem field="perms" label="权限标识" />
        <USelectItem field="target" label="打开模式" :options="position" defaultValue="_self" />
        <a-popover trigger="click" placement="bottom">
          <template #content>
            <div style="width: 520px; height: 260px; overflow-y: auto; overflow-x: hidden">
              <template v-for="item in iconList" :key="item">
                <span style="display: inline-block; padding: 12px" @click="() => iconSelect(item)">
                  <UIcon :type="item" style="font-size: 22px; cursor: pointer" :title="item"></UIcon>
                </span>
              </template>
            </div>
          </template>
          <UInputItem field="icon" label="图标" autocomplete="off">
            <template #addonAfter>
              <span style="cursor: pointer;">选择</span>
            </template>
          </UInputItem>
        </a-popover>
        <UInputNumberItem field="sort" label="排序" :defaultValue="68"/>
        <URadioItem field="status" label="状态" dict="sys_func_status"
                    defaultValue="enabled" button-style="solid" optionType="button"/>
      </URow>
      <template #footer="{model}">
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" :url="model && model.id ? '/core/menu/edit' : '/core/menu/add'">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewDrawer>
  </UView>
</template>

<script>
import {BooleanStatus} from "@/utils/StatusConsts";
import {reactive, ref} from "vue";
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "Menu",
  setup() {
    let type = [
      {label: '目录', value: 'M'},
      {label: '分组', value: 'G'},
      {label: '视图', value: 'V'},
      {label: '功能', value: 'A'},
    ]

    let position = [
      {label: '任务栏', value: '_self'},
      {label: '新标签', value: '_blank'},
    ];

    const columns = reactive([
      {field: 'name', title: '菜单名称', align: 'left', width: 180},
      {field: 'type', title: '菜单类型', options: type, width: 80},
      {field: 'url', title: '菜单URL', resizable: true, width: 120, ellipsis: true},
      {field: 'msn', title: '所属模块', resizable: true, width: 120, ellipsis: true
          , url: '/core/menu/msn', valueField: 'msn', labelField: 'name'},
      {field: 'perms', title: '权限标识', width: 160, ellipsis: true},
      {field: 'status', title: '菜单状态', dict: 'sys_func_status', width: 80},
      {field: 'sort', title: '排序', width: 80},
      {field: 'action', title: '操作', type: 'action', width: 130, fixed: 'right'},
    ])
    let rules = {
      pid: {type: 'number', required: true},
      name: {required: true, message: '菜单名称'},
      target: {required: true, message: '打开模式必填'},
      msn: {required: true, message: '所属模块必填'},
      type: {required: true, message: '菜单类型必填'},
      status: {required: true, message: '状态必填'},
    }

    let parentRef = ref();
    let editModel = ref({});
    let searchModel = ref({pid: null})
    let disabledPermType = ref(false);

    // 获取iconfont.js下面的所有icon
    let iconList = [];
    let svgIcon = document.body.querySelector("svg")
    if(svgIcon instanceof SVGSVGElement) {
      let children = svgIcon.children;
      for (let childrenKey in children) {
        let child = children[childrenKey];
        if(child instanceof Element) {
          iconList.push(child.id);
        }
      }
    }

    return {columns, rules, position, type, disabledPermType, BooleanStatus, searchModel, editModel, iconList, parentRef}
  },
  // 路由进入的时候合并查询参数
  beforeRouteEnter(to, form, next) {
    if(Object.keys(to.query).length > 0) {
      next(vm => {
        Object.assign(vm.searchModel, to.query);
      })
    } else {
      next();
    }
  },
  // 路由更新的时候合并查询参数
  beforeRouteUpdate(to) {
    if(Object.keys(to.query).length > 0) {
      Object.assign(this.searchModel, to.query)
      this.$refs['searchRef'].trigger();
    }
  },
  methods: {
    edit(model) {
      if(this.searchModel.pid) {
        this.editModel.pid = this.searchModel.pid;
        let parent = this.$refs.parentRef.getSelectedModel(this.searchModel.pid);

        if(parent) {
          this.changeParent(model, parent);
        }
      }
    },
    disabled(record) {
      return record.type == 'A';
    },
    changeParent(value, parent) {
      if(!this.editModel.id) { // 新增
        // 父级是菜单类型
        if(parent.type == 'V') {
          // 子级只能是权限类型
          this.editModel.type = 'A'
        } else if(parent.type == 'M') { // 父级是目录
          this.editModel.type = 'V'; // 子级是菜单
        }

        // 权限的所属模块默认和父级保持一致
        this.editModel.msn = parent.msn;
      }
    },
    selectParent(selectedKeys) {
      this.searchModel.pid = selectedKeys[0];
      this.$refs['searchRef'].trigger();
    },
    iconSelect(item) {
      this.editModel['icon'] = item;
    },
    queryCall({code}) {
      if(code == CoreConsts.SuccessCode) {
        this.parentRef['refreshOptions']();
      }
    }
  }
}
</script>

<style scoped>

</style>
