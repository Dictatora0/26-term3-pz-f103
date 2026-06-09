<template>
  <div class="u-link-view u-child-view">
    <slot></slot>
  </div>
</template>
<script>
import {defineComponent, inject, provide, watch} from "vue";
import {LinkContext, ViewContext, ViewVoidContext, VisibleContext} from "@/components/view/Context";
import {
  FuncContextKey,
  LinkViewContextKey,
  LinkViewPosition,
  PositionConst,
  ViewContextKey,
  VisibleContextKey
} from "@/utils/ProvideKeys";

export default defineComponent({
    name: "ULinkView",
    props: {
        // 功能名称 比如 用户管理
        name: {type: String, default: ''},
        uid: {type: String, required: true},
        rowKey: {type: String, default: 'id'},
    },
    setup({uid}) {
        let viewContext = inject(ViewContextKey);
        if(viewContext == null) {
          throw new Error(`ULinkView组件必须作为UView的子组件`);
        }

        let parentContext = inject(LinkViewContextKey);
        const linkContext = new LinkContext(uid, viewContext, parentContext || viewContext);
        provide(FuncContextKey, new ViewVoidContext(linkContext));

        /**
         * @param key {String}
         * @param config {FuncConfig}
         */
        let registerFuncToContext = (key, config) => {
          // let toUid = key.split(":")[2];
        }

        // watch(linkContext.funcs, (a, b) => {
          // registerFuncToContext()
        // })

        let visibleContext;
        // 收集所有子LinkView上下文
        let position = inject(LinkViewPosition);
        if(position != PositionConst.Page) {
          visibleContext = inject(VisibleContextKey);
          if(visibleContext instanceof VisibleContext) {
            visibleContext.childLinkContext.push(linkContext);
          }
        }

        provide(LinkViewContextKey, linkContext);
        return {linkContext, viewContext, visibleContext, registerFuncToContext}
    },
    mounted() {
      /**
       * @type LinkContext
       */
      let linkContext = this.linkContext;
      // for (let key in linkContext.funcs) {
      //   this.registerFuncToContext(key, linkContext.funcs[key]);
      // }

      // LinkView组件不在UDrawer或UModal组件下面
      // if(!(this.visibleContext instanceof VisibleContext)) {
        linkContext.getQueryFunc().forEach(item => item.trigger());
      // }
    },
    methods: {
        /**
         * @return {ViewContext}
         */
        getViewContext() {
            return this.viewContext;
        },

        /**
         * @return {LinkContext}
         */
        getLinkContext() {
            return this.linkContext;
        },
    }
})
</script>
