<template>
  <AModal :footer="null" :closable="false" destroy-on-close :width="width">
    <template #title>
      <AFlex justify="space-between" style="font-weight: 200">
        <div>
          {{selectPosition.formattedAddress}}
        </div>
        <div>
          关键词
          <AAutoComplete @change="triggerSearch" size="small" style="width: 180px; margin-right: 8px"/>
        </div>
      </AFlex>
    </template>
    <div id="modal-container">
      <AMap id="modal-container" :place-search="{}" :geocoder="{}" :center="center" ref="instance"
            @click="getPosition" @complete="loadingComplete" :zoom="zoom">
        <slot name="marker" :position="selectPosition.position"
              :address="selectPosition.formattedAddress" :info="selectPosition.address"/>
      </AMap>
    </div>
  </AModal>
</template>

<script>
import AMap from "@/components/amap/AMap.vue";
import {ref} from "vue";
export default {
  name: "AMapModal",
  components: {AMap},
  props: {
    width: {default: '680px'},
    center: {type: Array}, // 中心点
    zoom: {type: Number, default: 10},
    address: {type: String},
  },
  setup(props, {attrs}) {
    let instance = ref();
    let selectPosition = ref({
      address: null,
      position: null,
      formattedAddress: props.address,
    });
    // let markers = ref([])
    return {instance, selectPosition}
  },
  updated() {
    if(this.center && this.instance) {
      this.instance.setFitView();
    }
  },
  methods: {
    triggerSearch(value) {
      if(value) {
        this.instance.startPlaceSearch(value, (result, data) => {})
      } else {
        this.instance.removeAllOverlay();
      }
    },
    getPosition(e) {
      this.selectPosition.position = [e.lnglat.lng, e.lnglat.lat];
      this.instance.getGeoCoder(e.lnglat, result => {
        if(result.info == 'OK') {
          this.selectPosition.address = result.regeocode.addressComponent;
          this.selectPosition.formattedAddress = result.regeocode.formattedAddress;
          this.$emit("geo", {e, instance: this.instance, position: this.selectPosition.position, geo: result.regeocode});
        } else {
          // console.warn(`转换地址失败[${result}]`)
          this.$emit("geo", {e, instance: this.instance, position: this.selectPosition.position, geo: null});
        }
      })

      this.$emit("click", {e, instance: this.instance, position: this.selectPosition.position});
    },

    loadingComplete() {
      this.$emit("complete", this.instance);
      if(this.center) {
        this.instance.setFitView();
      }
    }
  }
}
</script>

<style scoped>
#modal-container {
  width: 100%;
  height: 360px;
}
</style>