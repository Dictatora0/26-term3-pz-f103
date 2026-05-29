<template>
  <div class="u-search u-bs-search">
    <UForm class="u-search-form" ref="ivzBasicForm" layout="inline" v-model="modelValue">
      <template #default="{model}">
        <slot :model="model"></slot>
        <slot name="func"></slot>
      </template>
    </UForm>
  </div>
</template>
<!-- 基础搜索组件 -->
<script>
import {inject, provide, ref, watch} from "vue";
import UForm from "@/components/form/basic/Form";
import {FuncContextKey, LinkViewContextKey} from "@/utils/ProvideKeys";
import {SearchContext} from "@/components/view/Context";
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "USearch",
  components: {UForm},
  props: {
    tid: {type: String, default: null},
    uid: {type: String, required: true, default: CoreConsts.DefaultSearchUid}
  },
  setup({uid}, {attrs, emit}) {
    /**
     * @type {LinkContext}
     */
    let linkContext = inject(LinkViewContextKey);
    let searchContext = new SearchContext(linkContext);

    if(linkContext) {
      searchContext.uid = uid;
      linkContext.addChildrenContext(searchContext)
    }

    provide(FuncContextKey, searchContext);

    let modelValue = ref(attrs.modelValue);
    watch(modelValue, (newVal) => {
      emit('update:modelValue', newVal);
    })

    // 暴露SearchContext
    if(attrs['onUpdate:context'] instanceof Function) {
      attrs['onUpdate:context'](searchContext);
    }
    return {searchContext, modelValue}
  },
  created() {
    this.searchContext['getFormContext'] = this.getFormContext;
  },
  methods: {
    getSearchContext() {
      return this.searchContext;
    },

    getFormContext() {
      return this.$refs['ivzBasicForm'].getFormContext();
    },

    /**
     * 重置搜索数据
     */
    resetSearchModel() {
      this.$refs['ivzBasicForm'].resetFields();
    },

    // 获取当前的搜索数据
    getSearchModel() {
      return this.$refs['ivzBasicForm'].getEditModel();
    },

    // 设置当前的搜索数据
    setSearchModel(searchModel) {
      this.$refs['ivzBasicForm'].setEditModel(searchModel);
    }
  }
}
</script>

<style scoped> </style>
