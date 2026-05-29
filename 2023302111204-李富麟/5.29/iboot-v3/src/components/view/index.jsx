import '@/components/view/index.css'
import UView from "@/components/view/View.jsx";
import ULinkView from "@/components/view/LinkView.vue";
import UTable from "@/components/table/BasicTable.jsx";
import UEditableTable from "@/components/table/EditableTable.jsx";
import UBasicSearch from "@/components/search/BasicSearch.vue";
import {defineComponent, inject} from "vue";
import UFormModal from "@/components/modal/FormModal";
import UFormDrawer from "@/components/drawer/FormDrawer";
import {ViewContextKey} from "@/utils/ProvideKeys";
import CoreConsts from "@/components/CoreConsts";

export const UViewSearch = defineComponent({
    name: 'UViewSearch',
    components: {UBasicSearch},
    props: {
        tid: {type: String, default: CoreConsts.DefaultTableUid} // 表组件uid 默认主表组件的uid
    },
    render() {
        return (<div class="iv-view-search iv-view-border">
            <UBasicSearch  {...this.$attrs} tid={this.tid} v-slots={this.$slots} uid={CoreConsts.DefaultSearchUid}/>
        </div>)
    }
})

export const UViewModal = defineComponent({
    name: 'UViewModal',
    components: {UFormModal},
    render() {
        return <div class="u-view-modal">
            <UFormModal {...this.$attrs} uid={CoreConsts.DefaultEditUid} v-slots={this.$slots} />
        </div>
    }
})

export const UViewDrawer = defineComponent({
    name: 'UViewDrawer',
    components: {UFormDrawer},
    render() {
        return <div class="u-view-drawer">
            <UFormDrawer {...this.$attrs}  uid={CoreConsts.DefaultEditUid} v-slots={this.$slots} />
        </div>
    }
})

export const UViewTable = defineComponent({
    name: 'UViewTable',
    components: {UTable},
    props: {
        rowKey: {default: null}
    },
    setup(props, {attrs}) {
        let viewContext = inject(ViewContextKey);

        let viewRowKey = viewContext.getRowKey();
        return {viewContext, viewRowKey}
    },
    render() {
        let rowKey = this.rowKey || this.viewRowKey;
        return (
            <div class="iv-view-table iv-view-border">
                <UTable {...this.$attrs} uid={CoreConsts.DefaultTableUid} rowKey={rowKey} v-slots={this.$slots}/>
            </div>)
    }
})

export const UViewEditable = defineComponent({
    name: 'UViewEditable',
    components: {UEditableTable},
    props: {
        rowKey: {default: null}
    },
    setup(props, {attrs}) {
        let viewContext = inject(ViewContextKey);

        let viewRowKey = viewContext.getRowKey();
        return {viewContext, viewRowKey}
    },
    render() {
        let rowKey = this.rowKey || this.viewRowKey;
        return (
            <div class="iv-view-editable-table iv-view-border">
                <UEditableTable {...this.$attrs} uid={CoreConsts.DefaultTableUid} rowKey={rowKey} v-slots={this.$slots}/>
            </div>)
    }
})

export default {
    install(app) {
        app.component(UView.name, UView);
        app.component("UChildView", ULinkView);
        app.component(ULinkView.name, ULinkView);
        app.component(UViewModal.name, UViewModal);
        app.component(UViewTable.name, UViewTable);
        app.component(UViewDrawer.name, UViewDrawer);
        app.component(UViewSearch.name, UViewSearch);
        app.component(UViewEditable.name, UViewEditable);
    }
}
