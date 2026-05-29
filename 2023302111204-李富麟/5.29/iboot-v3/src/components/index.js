import './index.css'
import UIcon from '@/components/icon'
import UForm from '@/components/form/basic/Form'
import AMap from '@/components/amap'
import UCron from '@/components/cron/UCron.vue'
import IvzViewComponents from '@/components/view'
import IvzBasicComponents from '@/components/basic'
import UBasicFormComponents from "@/components/form/basic/index.jsx";
import UBasicFormItemComponents from "@/components/form/basic/formItem.jsx";

import UTable from "@/components/table/BasicTable.jsx";
import UEditableTable from "@/components/table/EditableTable.jsx";
import USearch from "@/components/search/BasicSearch.vue";
import UFormModal from "@/components/modal/FormModal.jsx";
import UBasicModal from "@/components/modal/BasicModal.jsx";
import UFormDrawer from "@/components/drawer/FormDrawer.jsx";
import UBasicDrawer from "@/components/drawer/BasicDrawer.jsx";
import UDrawerLogger from "@/components/logger/DrawerLogger.vue";
import URunningLogger from "@/components/logger/RunningLogger.vue";

import UCropper from '@/components/cropper/ImageCropper.vue';

export default {
    install(app) {
        app.use(UIcon) // 图标组件
        app.use(AMap) // 高德地图组件
        app.use(IvzViewComponents) // 视图组件
        app.use(IvzBasicComponents) // 基础组件
        app.use(UBasicFormComponents) // 表单组件
        app.use(UBasicFormItemComponents) // 表单项组件

        app.component(UCron.name, UCron) // Cron组件
        app.component(UTable.name, UTable)
        app.component(UEditableTable.name, UEditableTable)

        app.component(UForm.name, UForm)
        app.component(UCropper.name, UCropper)
        app.component(UFormModal.name, UFormModal)
        app.component(UBasicModal.name, UBasicModal)
        app.component(UFormDrawer.name, UFormDrawer)
        app.component(UBasicDrawer.name, UBasicDrawer)
        app.component(USearch.name, USearch)
        app.component(UDrawerLogger.name, UDrawerLogger)
        app.component(URunningLogger.name, URunningLogger)
    }
}
