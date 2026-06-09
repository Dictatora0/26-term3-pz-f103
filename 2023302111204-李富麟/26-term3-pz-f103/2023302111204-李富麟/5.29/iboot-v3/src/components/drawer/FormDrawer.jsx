import {defineComponent, inject, provide, ref} from "vue";
import {FuncContextKey, LinkViewContextKey, LinkViewPosition, PositionConst} from "@/utils/ProvideKeys";
import {EditContext} from "@/components/view/Context";
import MixinsEditItem from "@/components/edit/MixinsEditItem";

const FooterColFuncConfig = {xs: 12, ms: 12, md: 12}
const FooterColEmptyConfig = {xs: 0, ms: 0, md: 6}
export default defineComponent({
    name: 'UFormDrawer',
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
        forceRender: {default: true},
        placement: {default: 'right'},
        maskClosable: {default: true},
        headerStyle: {type: Object},
        destroyOnClose: {default: false},
        uid: {type: String, required: true},
        footerStyle: {type: Object, default: () => { return {textAlign: 'center'}}}
    },
    mixins: [MixinsEditItem],
    setup(props, {attrs, slots}) {
        let formRef = ref(null);
        let visible = ref(false);
        let spinning = ref(false);
        let routerCall = () => null;
        let spinTip = ref("");

        /**
         * @type {LinkContext}
         */
        let linkContext = inject(LinkViewContextKey);
        let editContext = new EditContext(linkContext);
        if(linkContext) {
            editContext.uid = props.uid;
            linkContext.addChildrenContext(editContext);
        }

        let slotProxy = {
            title: () => {
                let func = editContext.openType;
                let model = formRef.value ? formRef.value.getEditModel() : {};
                return slots.title ? slots.title({model, func}) : props.title
            },
            // extra: () => slots.extra ? slots.extra() : null,
            // closeIcon: () => slots.closeIcon ? slots.closeIcon() : null,
        }
        if(slots.footer) {
            slotProxy.footer = () => {
                let model = formRef.value ? formRef.value.getEditModel() : {};
                return <ARow>
                    <ACol {...FooterColEmptyConfig}></ACol>
                    <ACol {...FooterColFuncConfig}>{slots.footer({model})}</ACol>
                    <ACol {...FooterColEmptyConfig}></ACol>
                </ARow>
            }
        }

        slots.extra ? slotProxy.extra = slots.extra : null;
        slots.closeIcon ? slotProxy.closeIcon = slots.closeIcon : null;

        provide(FuncContextKey, editContext);
        provide(LinkViewPosition, PositionConst.Drawer);

        // 暴露EditContext
        if(attrs['onUpdate:context'] instanceof Function) {
            attrs['onUpdate:context'](editContext);
        }
        return {formRef, spinning, spinTip, visible, slotProxy, editContext, routerCall, linkContext}
    },
    render() {
        let model = {}, context = this.editContext;

        if(this.formRef) {
            model =  this.formRef.getEditModel();
        } else {
            this.formRef = this.$refs['iemFormRef'];
        }

        return(<ADrawer v-model={[this.visible, 'open', ["modifier"]]} rootStyle={{position: 'absolute'}}
                {...this.$props} v-slots={this.slotProxy} getContainer=".u-main-task" ref="ADrawerRef"
                rootClassName="iv-drawer u-edit-form iv-form-drawer" destroyOnClose={false} forceRender={true}>
            <ASpin size="small" tip={this.spinTip} spinning={this.spinning}>
                <UForm {...this.$attrs} ref="iemFormRef">
                    {this.$slots.default ? this.$slots.default({model, context}) : []}
                </UForm>
            </ASpin>
        </ADrawer>)
    },
    mounted() {
        this.routerCall = this.$router.beforeEach((to, from, next) => {
            if(this.visible) {
                this.visible = false;
            }
            next();
        })
    },
    unmounted() {
        this.visible = false;
    }
})

