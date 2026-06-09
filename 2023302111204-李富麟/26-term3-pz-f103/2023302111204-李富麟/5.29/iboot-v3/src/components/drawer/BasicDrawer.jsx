import {defineComponent, inject, provide, ref} from "vue";
import {LinkViewContextKey, LinkViewPosition, PositionConst, VisibleContextKey} from "@/utils/ProvideKeys";
import {VisibleContext} from "@/components/view/Context";
import MixinsVisible from "@/components/mixins/MixinsVisible";

export default defineComponent({
    name: 'UDrawer',
    props: {
        title: String,
        bodyStyle: Object,
        centered: Boolean,
        afterClose: Function,
        width: {default: 680},
        height: {default: 452},
        zIndex: {default: 1000},
        keyboard: {default: true},
        closable: {default: true},
        //todo 此参数必须设置为 true, 或者刷新按钮会报异常
        forceRender: {default: true},
        placement: {default: 'right'},
        maskClosable: {default: false},
        headerStyle: {type: Object},
        destroyOnClose: {default: true},
        uid: {type: String, required: true},
        modelValue: {type: Object},
        'onUpdate:modelValue': {type: Function},
        footerStyle: {type: Object, default: () => { return {textAlign: 'center'}}}
    },
    mixins: [MixinsVisible],
    setup(props, {attrs}) {
        let formRef = ref(null);
        let visible = ref(false);
        let spinning = ref(false);
        let routerCall = () => null;
        let spinTip = ref("");

        /**
         * @type {LinkContext}
         */
        let linkContext = inject(LinkViewContextKey);
        let visibleContext = new VisibleContext(linkContext);
        if(linkContext) {
            visibleContext.uid = props.uid;
            linkContext.addChildrenContext(visibleContext);
        }

        provide(VisibleContextKey, visibleContext);
        provide(LinkViewPosition, PositionConst.Drawer);

        // 暴露VisibleContext
        if(attrs['onUpdate:context'] instanceof Function) {
            attrs['onUpdate:context'](visibleContext);
        }
        return {formRef, spinning, spinTip, visible, routerCall, linkContext, visibleContext}
    },
    render() {
        return(<ADrawer v-model={[this.visible, 'open', ["modifier"]]} rootClassName="iv-drawer iv-basic-drawer" rootStyle={{position: 'absolute'}}
                {...this.$props} v-slots={this.$slots} getContainer=".u-main-task" forceRender={true}/>)
    },
    mounted() {
        this.routerCall = this.$router.beforeEach((from, to, next) => {
            if(this.visible) {
                this.visible = false;
            }

            next();
        })
    },
    unmounted() {
        this.routerCall();
    }
})

