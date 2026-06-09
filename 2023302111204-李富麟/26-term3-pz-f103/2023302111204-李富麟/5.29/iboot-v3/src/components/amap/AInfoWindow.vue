<template>
  <div style="display: none">
    <div ref="content">
      <slot />
    </div>
  </div>
</template>

<script>
import {getCurrentInstance, inject, ref, watch} from "vue";

export default {
  name: "AInfoWindow",
  props: {
    position: {type: Object}, // {lng: '', lat: ''}
    offset: {type: Object, default: () => {return {x: 0, y: 0}}}, // {x: 0, y: 0}; 距离position的偏移坐标
    open: {type: Boolean, default: false, required: true},
    onOpen: {type: Function},
    onClose: {type: Function}
  },
  setup(props, {emit}) {
    let content = ref();
    let infoWindow = ref();
    let amap = inject("AMAP");
    let map = ref(amap.instance);
    watch(() => props.open, (newValue) => {
      if(newValue) {
        if(!infoWindow.value) {
          infoWindow.value = new AMap.InfoWindow({
            content: content.value  //使用默认信息窗体框样式，显示信息内容
          });

          infoWindow.value.on('open', (e) => {
            emit("open", {map: map.value, instance: infoWindow.value});
          })

          infoWindow.value.on('close', (e) => {
            emit("update:open", false);
          })
        }

        let position = map.value.getCenter(); // 不指定获取中心点
        if(props.position instanceof AMap.LngLat) {
          position = props.position;
        } else if(typeof props.position == 'object') {
          position = new AMap.LngLat(props.position.lng, props.position.lat);
        } else if(props.position instanceof Array) {
          position = new AMap.LngLat(props.position[0], props.position[1]);
        } else {
          return console.error(`错误的格式[position=${props.position}], 支持{lng: x, lat: x} 或 [lng, lat] 或 LngLat对象`)
        }

        infoWindow.value.open(map.value, position);
      } else {
        // map.value.clearInfoWindow()
        emit("close", {map: map.value, instance: infoWindow.value});
      }
    });

    return {map, content, infoWindow}
  },
  mounted() {
    // let instance = this.instance;
  },
  unmounted() {
    if(this.infoWindow) {
      this.infoWindow = null;
      this.map.clearInfoWindow();
    }
  }
}
</script>

<style scoped>

</style>