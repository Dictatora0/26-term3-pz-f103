<template>
  <UView rowKey="name">
    <UViewSearch>
      <URow col="search">
        <UInputItem field="name" label="表名" />
        <USelectItem field="prefix" label="表前缀" url="/lcd/gen/prefix" />
        <UButton func="query" url="/lcd/gen/list">查询</UButton>
        <UButton func="reset">重置</UButton>
        <UButton func="edit:set" uid="config" :config="{copy: 'tables=name'}" type="dashed" :params="getParam">生成代码</UButton>
        <UButton func="download:batch" type="link" url="/lcd/gen/vue"
                  :config="{fileName: 'ivzone.zip'}" :params="getParam">下载Vue代码</UButton>
      </URow>
    </UViewSearch>
    <UViewTable v-model:context="tableContext" :columns="columns" :rowSelection="rowSelection">
      <template #action="{record}">
        <UTag func="edit:set" uid="config" :data="record"
                  :config="{copy: 'tables[]=name'}" :params="getParam">生成</UTag>
        <UTag func="download" url="/lcd/gen/vue" :data="record"
                  :config="{fileName: 'ivzone.zip'}" :params="getParam">下载vue代码</UTag>
        <ATag @click="() => preview(record)">预览</ATag>
      </template>
      <template #title>
        <div style="color: #bbbbbb">
          <span style="margin: 0px 8px">
            <label style="color: #ed4014">写出到模块：</label>
            <UCascader url="/lcd/gen/msn" labelField="name" valueField="module" @change="moduleChange" style="width: 230px" />
            <!--            <label style="color: #d46b08">-->
            <!--              {{genInit.msn}}-->
            <!--            </label>-->
          </span>
          <span style="margin: 0px 8px">项目路径：<label style="color: #d46b08">{{genInit.projectPath}}</label></span>
          <span style="margin: 0px 8px">包路径：<label style="color: #d46b08">{{genInit.parentPath}}</label></span>
          <span style="margin: 0px 8px">Vue路径：<label style="color: #d46b08">{{genInit.webProjectPath}}</label></span>
        </div>
      </template>
    </UViewTable>
    <UFormModal v-model="tableDto" title="代码生成配置" uid="config" :span="[5, 15]" @edit="genEdit">
      <URadioItem field="fileOverride" label="覆写文件" :options="fileOverrideOptions" />
      <UTreeSelectItem field="menuId" label="父菜单" url="/lcd/gen/parent" :allowClear="true" @change="changeMenu"
        labelField="name" valueField="id" extra="选择父菜单后将导入菜单(执行导入sql语句)" placeholder="请选择父菜单" />
      <UInputItem field="webOutDir" label="web根路径" extra="配置后将生成vue文件到指定路径" :allowClear="true"/>
      <div style="color: #ff7f00; text-align: center">注意：生成的代码不会自动加入svn</div>
      <template #footer>
        <UButton func="submit" url="/lcd/gen/java" :params="getParam">生成代码</UButton>
      </template>
    </UFormModal>

    <ADrawer v-model:open="visible" title="生成代码预览" width="980" :bodyStyle="{paddingTop: '0px'}">
      <template #extra>
        <a-typography-paragraph :copyable="copyable">点击复制</a-typography-paragraph>
      </template>
      <ASpin :spinning="spinning">
        <ATabs v-model:activeKey="activeKey" @change="tabsChange">
          <ATabPane key="html" tab="vue">
            <pre><code>{{previewModel.html}}</code></pre>
          </ATabPane>
          <ATabPane key="entity" tab="entity">
            <pre><code>{{previewModel.entity}}</code></pre>
          </ATabPane>
          <ATabPane key="menuSql" tab="menuSql">
            <pre><code>{{previewModel.menuSql}}</code></pre>
          </ATabPane>
          <ATabPane key="mapper" tab="mapper">
            <pre><code>{{previewModel.mapper}}</code></pre>
          </ATabPane>
          <ATabPane key="mapperXml" tab="mapperXml">
            <pre><code>{{previewModel.mapperXml}}</code></pre>
          </ATabPane>
          <ATabPane key="service" tab="service">
            <pre><code>{{previewModel.service}}</code></pre>
          </ATabPane>
          <ATabPane key="serviceImpl" tab="serviceImpl">
            <pre><code>{{previewModel.serviceImpl}}</code></pre>
          </ATabPane>
          <ATabPane key="controller" tab="controller">
            <pre><code>{{previewModel.controller}}</code></pre>
          </ATabPane>
        </ATabs>
      </ASpin>
    </ADrawer>
  </UView>
</template>
<!-- 代码生成器 -->
<script>

import {reactive, ref} from "vue";
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "Code",
  setup() {
    let fileOverrideOptions = ref([
      {label: '否', value: false},
      {label: '是', value: true},
    ])
    let columns = ref([
      {field: 'name', title: '表名'},
      {field: 'comment', title: '表说明'},
      {field: 'action', title: '操作'},
    ])

    let visible = ref(false);
    let spinning = ref(false);
    let previewModel = ref({
      html: null,
      entity: null,
      mapper: null,
      mapperXml: null,
      service: null,
      serviceImpl: null,
      controller: null,
      menuSql: null
    });
    let genInit = reactive({
      msn: '-',
      outDir: '',
      parent: '',
      parentPath: '-',
      projectPath: '-',
      webProjectPath: '-',
      packageName: null,
    })
    let tableDto = reactive({
      msn: null,
      parent: null,
      packageName: null,
      menuId: null,
      outDir: '',
      fileOverride: false,
      tables: []
    })
    let copyable = reactive({
      text: '',
    })
    let rowSelection = {};

    /**
     * @type {Ref<TableContext>}
     */
    let tableContext = ref({});
    let activeKey = ref('html');
    return {columns, visible, previewModel, spinning, rowSelection, genInit
      , tableDto, copyable, activeKey, fileOverrideOptions, tableContext}
  },
  created() {
    this.$http.get("/lcd/gen/init").then(({code, message, data}) => {
      if(code == CoreConsts.SuccessCode) {
        if(data['webProjectPath']) {
          this.genInit['webOutDir'] = data['webProjectPath'];
          this.genInit['webProjectPath'] = data['webProjectPath'];
        }
      } else {
        this.$msg.warn(message);
      }
    })
  },
  methods: {
    getParam(data, child) {
      if(!this.genInit.packageName) {
        this.$msg.warn(`请先选择模块`);
        return Promise.reject('请选择模块');
      }

      if(child[0] == CoreConsts.FuncNameMeta.EDIT) {
        let {data: record, formModel} = data;
        Object.assign(formModel, this.genInit);
        if(record instanceof Array) {
          formModel['tables'] = record.map(item => item['name']);
        } else if(record){
          formModel['tables'] = [record['name']];
        } else {
          return Promise.reject('请选择要生成的表')
        }

        return Promise.resolve(formModel);
      } else if(child[0] == CoreConsts.FuncNameMeta.DOWNLOAD) {
        let formModel = {};
        Object.assign(formModel, this.genInit);
        if(data instanceof Array) {
          formModel['tables'] = data.map(item => item['name']);
        } else if(data){
          formModel['tables'] = [data['name']];
        } else {
          return Promise.reject('请选择要生成的表')
        }

        return Promise.resolve(formModel);
      } else if(child[0] == CoreConsts.FuncNameMeta.SUBMIT) {
        return Promise.resolve(data);
      }
    },
    preview(record) {
      if(!this.genInit.packageName) {
        return this.$msg.warn(`请先选择模块`);
      }

      let submitModel = {tables: [record['name']]}
      Object.assign(submitModel, this.genInit);
      this.visible = true;
      this.spinning = true;
      this.$http.get('/lcd/gen/review', {params: submitModel}).then(resp => {
        let {data, code, message} = resp;
        if(code == CoreConsts.SuccessCode) {
          this.previewModel = data;
          this.copyable.text = this.previewModel[this.activeKey];
        } else {
          this.$msg.warn(message)
        }
      }).finally(() => this.spinning = false)
    },
    tabsChange(key) {
      this.copyable.text = this.previewModel[key];
    },
    changeMenu(value, model) {
      // 使用上级菜单的编号
      this.genInit.msn = model['msn'];
    },
    moduleChange(value, model) {
      if(model instanceof Array) {
        let item = model[1];
        this.genInit.packageName = item['module'];
        this.genInit.outDir = item['path'];
        if(item['plugin']) {
          this.genInit.parent = "com.iteaj.iboot.plugin";
          this.genInit.parentPath = "com.iteaj.iboot.plugin." + item['module'];
        } else {
          this.genInit.parent = "com.iteaj.iboot.module";
          this.genInit.parentPath = "com.iteaj.iboot.module." + item['module'];
        }

        this.genInit.projectPath = item['path']
      } else {
        this.genInit.parent = '',
        this.genInit.packageName = null;
        this.genInit.outDir = null;
        this.genInit.parentPath = '-';
        this.genInit.projectPath = '-';
      }
    },
    genEdit(model) {
      // model.outDir = this.genInit.projectPath + "\\src\\main\\java";
    }
  }
}
</script>

<style scoped>
.code-preview .ant-drawer-body{
  padding-top: 0px;
}
</style>
