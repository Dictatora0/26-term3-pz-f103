<template>
  <UView name="用户" rowKey="id">
    <U2Col>
      <template #left>
        <ACard :bordered="false">
          <UTree url="/core/org/parent" selectable blockNode showLine defaultExpandAll @select="selectParent" />
        </ACard>
      </template>
      <template #right>
        <UViewSearch v-model="searchModel">
          <URow col="search">
            <UInputItem field="name" label="用户昵称"/>
            <UInputItem field="account" label="用户帐号"/>
            <UInputItem field="phone" label="用户手机"/>
            <URadioItem field="status" label="用户状态" dict="sys_func_status"/>
            <UTreeSelectItem field="orgId" label="所属部门" url="/core/org/parent"
                             labelField="name" valueField="id"/>
            <UButton func="query" url="/core/admin/view" ref="searchRef">搜索</UButton>
            <UButton func="reset">重置</UButton>
          </URow>
        </UViewSearch>
        <UViewTable :columns="columns" :scroll="{x: 1000}">
          <template #func>
            <UButton func="add" v-auth="'core:admin:add'">新增</UButton>
<!--            <UButton func="import" url="/core/admin/import" v-auth="'core:admin:import'"-->
<!--                     :config="{uploadConfig: {templateUrl:  `${this.$env.apiUrl}/static/excel/用户导入模板.xlsx`}}">导入</UButton>-->
<!--            <UButton func="export" url="/core/admin/export" v-auth="'core:admin:export'">导出</UButton>-->
          </template>
          <template #action="{record}">
            <UTag func="edit" :data="record" url="/core/admin/edit" v-auth="'core:admin:edit'">修改</UTag>
            <UTag func="del" :data="record" url="/core/admin/del" v-auth="'core:admin:del'">删除</UTag>
            <UTag func="edit:set" uid="modPwd" :data="record" v-auth="'core:admin:pwd'"
                  :config="{copy: ['id']}" url="/core/admin/pwd">设置密码</UTag>
          </template>
        </UViewTable>
      </template>
    </U2Col>

    <UViewDrawer width="780" layout="vertical" :rules="rules" placement="left" title="用户管理" @edit="editOpen">
      <template #default="{model}">
        <URow :gutter="16" col="drawer">
          <UInputItem field="account" label="用户帐号" :disabled="model.id != null" :maxlength="20"/>
          <UInputItem field="name" label="用户昵称" required :maxlength="20"/>
          <UTreeSelectItem field="orgId" label="所属部门" treeNodeFilterProp="label"
                           url="/core/org/parent" labelField="name" valueField="id"/>
          <USelectItem field="postId" label="所属岗位" required
             url="/core/post/list?orgId={orgId}" valueField="id" labelField="name"/>
          <UInputItem field="phone" label="用户手机" :maxlength="11"/>
          <UInputItem field="email" label="用户邮箱" :maxlength="40" />
          <URadioItem field="status" label="用户状态" defaultValue="enabled" dict="sys_func_status"/>
          <URadioItem field="sex" label="用户性别" dict="core_sex" defaultValue="3"/>
          <UCheckboxItem field="roleIds" label="用户角色" url="/core/role/list"
                        labelField="name" valueField="id" :span="24" @loaded="rolesLoaded"/>
          <UTextareaItem field="remark" label="用户简介" :span="24" :maxlength="256" />
        </URow>
      </template>
      <template #footer="{model}">
        <UButton func="cancel">取消</UButton>
        <UButton func="submit" :url="model.id ? '/core/admin/edit' : '/core/admin/add'">提交</UButton>
        <UButton func="reset">重置</UButton>
      </template>
    </UViewDrawer>
    <!--  修改密码  -->
    <UFormModal uid="modPwd" v-model="pwdModel" title="修改密码" :span="[6, 15]" :rules="pwdRules">
      <UInputPasswordItem label="密码" field="password" />
      <UInputPasswordItem label="确认密码" field="surePwd" />
      <template #footer>
        <UButton func="submit" url="/core/admin/pwd">提交</UButton>
        <UButton func="cancel">取消</UButton>
      </template>
    </UFormModal>
  </UView>
</template>

<script>

import {reactive, ref} from "vue";

export default {
  name: "Admin",
  setup() {

    let columns = reactive([
      {title: '用户账号', field: 'account', width: 150},
      {title: '用户昵称', field: 'name', width: 150, ellipsis: true},
      {title: '所属部门', field: 'orgName', width: 120, ellipsis: true},
      {title: '所属岗位', field: 'postName', width: 180, ellipsis: true},
      {title: '手机', field: 'phone', width: 110},
      {title: '邮箱', field: 'email', ellipsis: true, width: 130},
      {title: '性别', field: 'sex', dict: 'core_sex', width: 60},
      {title: '状态', field: 'status', dict: 'sys_func_status', width: 60},
      {title: '创建时间', field: 'createTime', width: 120},
      {title: '操作', field: 'action', type: 'action', width: 200, fixed: 'right'},
    ])

    let rules = {
      name: {required: true, message: '用户昵称必填'},
      orgId: {required: true, message: '用户部门必填'},
      roleIds: {type: 'array', required: true, message: '请选择用户角色'},
      account: {required: true, message: '用户帐号必填'},
    }

    let pwdModel = ref({password: null, surePwd: null});
    let validator = (a,b,c) => {
      return new Promise((resolve, reject) => {
        if(b == null || b == '') {
          reject("请输入确认密码");
        } else if(pwdModel.value.password != pwdModel.value.surePwd) {
          reject("两次密码不一致");
        }else {
          resolve();
        }
      })
    }
    let pwdRules = {
      surePwd: {required: true, validator},
      password: {required: true, message: '请输入密码'}
    }

    let searchModel = ref({});
    return {columns, rules, status, pwdRules, pwdModel, searchModel}
  },
  methods: {
    editOpen(editModel) {
      if(!editModel['id']) {
        editModel['orgId'] = this.searchModel['orgId']
      }
    },

    selectParent(selectedKeys) {
      this.searchModel['orgId'] = selectedKeys[0];
      this.$refs['searchRef'].trigger();
    },

    rolesLoaded(data) {
      if(typeof data ==  'object') {
        Object.values(data).forEach(item => {
          if(item.status == 'disabled') {
            item.disabled = true;
          }
        })
      }
    }
  }
}
</script>

<style scoped>

</style>
