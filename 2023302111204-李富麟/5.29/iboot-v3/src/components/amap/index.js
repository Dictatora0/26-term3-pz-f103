import {defineAsyncComponent} from "vue";

let AMap = defineAsyncComponent(() => import('@/components/amap/AMap.vue'));
let AMarker = defineAsyncComponent(() => import('@/components/amap/AMarker.vue'));
let AMapModal = defineAsyncComponent(() => import('@/components/amap/AMapModal.vue'));
let AInfoWindow = defineAsyncComponent(() => import('@/components/amap/AInfoWindow.vue'));
let AMarkerLabel = defineAsyncComponent(() => import('@/components/amap/AMarkerLabel.vue'));

export default {
  install(app) {
    app.component("AMap", AMap);
    app.component("AMarker", AMarker);
    app.component("AMapModal", AMapModal);
    app.component("AInfoWindow", AInfoWindow);
    app.component("AMarkerLabel", AMarkerLabel);
  }
}