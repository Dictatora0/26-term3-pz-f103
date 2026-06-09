import {defineComponent, inject, ref} from "vue";
import {LinkViewContextKey} from "@/utils/ProvideKeys";
import {VisibleContext} from "@/components/view/Context";
import CoreConsts from "@/components/CoreConsts";
import MixinsVisible from "@/components/mixins/MixinsVisible";

export default defineComponent({
  name: 'UModal',
  props: {
    uid: {type: String, required: true, default: CoreConsts.DefaultEditUid}
  },
  mixins: [MixinsVisible],
  setup(props, {attrs, slots}) {
    let refs = ref(null);
    let visible = ref(false);

    /**
     * @type {LinkContext}
     */
    let linkContext = inject(LinkViewContextKey);
    let visibleContext = new VisibleContext(linkContext);
    if(linkContext) {
      visibleContext.uid = props.uid;
      linkContext.addChildrenContext(visibleContext)
    }

    // 暴露EditContext
    if(attrs['onUpdate:context'] instanceof Function) {
      attrs['onUpdate:context'](visibleContext);
    }

    return {refs, visible, visibleContext}
  },
  render() {
    return <AModal {...this.$attrs} v-model={[this.visible, 'open', ["modifier"]]} v-slots={this.$slots} />
  }
})
