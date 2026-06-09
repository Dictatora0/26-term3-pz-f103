import {defineComponent, provide} from "vue";
import {$View, ViewContext} from "@/components/view/Context";
import {
    EditableTableKey,
    LinkViewContextKey,
    LinkViewPosition,
    PositionConst,
    ViewContextKey
} from "@/utils/ProvideKeys";
import CoreConsts from "@/components/CoreConsts";
import ULinkView from "@/components/view/LinkView.vue";

export default defineComponent({
    name: "UView",
    props: {
        // 功能名称 比如 用户管理
        name: {type: String, default: ''},
        // 数据记录的唯一标识(用于编辑、删除、获取、设置等)
        rowKey: {type: String, default: CoreConsts.DefaultRowKey},
    },
    components: {ULinkView},
    setup(props) {
        const viewContext = new ViewContext(props);
        provide(LinkViewContextKey, null);
        provide(ViewContextKey, viewContext);
        provide(LinkViewPosition, PositionConst.Page);
        provide(EditableTableKey, false);
        return {viewContext}
    },
    created() {
        let $parent = this.$parent;
        $parent.$view = new $View(this.viewContext);
    },
    render() {
        return <ULinkView {...this.$props} uid={CoreConsts.PrimaryUid} class="u-page-view">
            {this.$slots.default ? this.$slots.default() : []}
        </ULinkView>
    },
    methods: {
        /**
         * @return {$View|void|*}
         */
        getView() {
            return this.$parent.$view;
        }
    }
})
