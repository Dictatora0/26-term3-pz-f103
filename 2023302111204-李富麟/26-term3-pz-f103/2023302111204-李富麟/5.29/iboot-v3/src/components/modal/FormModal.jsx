import {defineComponent, inject, provide, ref} from "vue";
import {FuncContextKey, LinkViewContextKey, LinkViewPosition, PositionConst, ViewContextKey} from "@/utils/ProvideKeys";
import MixinsEditItem from "@/components/edit/MixinsEditItem";
import {EditContext} from "@/components/view/Context";
import CoreConsts from "@/components/CoreConsts";

export default defineComponent({
  name: 'UFormModal',
  props: {
    title: String,
    bodyStyle: Object,
    span: {type: Array}, // labelCol 和wrapperCol简写 如：[6, 18]
    afterClose: Function,
    width: {default: 520},
    destroyOnClose: Boolean,
    getContainer: {type: Function},
    maskClosable: {default: true},
    centered: {type: Boolean, default: true},
    closable: {type: Boolean, default: false},
    forceRender: {type: Boolean, default: true},
    wrapClassName: {type: String, default: 'u-form-modal'},
    uid: {type: String, required: true, default: CoreConsts.DefaultEditUid}
  },
  mixins: [MixinsEditItem],
  setup(props, {attrs, slots}) {
    let refs = ref(null);
    let formRef = ref(null);
    let visible = ref(false);
    let spinning = ref(false);
    let spinTip = ref("数据处理中...");

    let labelCol = attrs.labelCol, wrapperCol = attrs.wrapperCol;
    if(props.span) {
      if(!labelCol) {
        labelCol = {span: props.span[0]};
      }

      if(!wrapperCol) {
        wrapperCol = {span: props.span[1]}
      }
    }

    /**
     * @type {LinkContext}
     */
    let linkContext = inject(LinkViewContextKey);
    let editContext = new EditContext(linkContext);
    if(linkContext) {
      editContext.uid = props.uid;
      linkContext.addChildrenContext(editContext)
    }

    provide(FuncContextKey, editContext);
    provide(LinkViewPosition, PositionConst.Modal);

    // 暴露EditContext
    if(attrs['onUpdate:context'] instanceof Function) {
      attrs['onUpdate:context'](editContext);
    }
    return {formRef, refs, spinning, spinTip, visible, labelCol, wrapperCol, editContext, linkContext}
  },
  render() {
    let model = {}, context = this.editContext;

    if(this.formRef) {
      model =  this.formRef.getEditModel();
    }

    let slots = {
      title: () => {
        let func = context.openType;
        return this.$slots.title ? this.$slots.title({model, func}) : this.title
      },
      footer: () => this.$slots.footer ? this.$slots.footer({model, context}) : null
    }

    return <AModal v-model={[this.visible, 'open', ["modifier"]]}
                   {...this.$props} v-slots={slots} forceRender={true} ref="iemRef">
      <ASpin size="small" tip={this.spinTip} spinning={this.spinning}>
        <UForm {...this.$attrs} labelCol={this.labelCol} wrapperCol={this.wrapperCol} ref="iemFormRef">
          {this.$slots.default ? this.$slots.default({model, context}) : []}
        </UForm>
      </ASpin>
    </AModal>
  }
})
