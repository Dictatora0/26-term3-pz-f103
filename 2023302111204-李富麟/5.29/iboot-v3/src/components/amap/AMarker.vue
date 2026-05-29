<template>
  <span style="display: none">
    <slot />
  </span>
</template>

<script>
import {inject, provide, ref, watch} from "vue";

export default {
  name: "AMarker",
  props: {
    id: {}, // 用于标识此marker, 放在ext里面
    ext: {type: Object, default: () => {return {}}}, // 扩展数据
    title: {type: String, default: ''},
    position: {type: Object, required: true},
    icon: {type: String},
    offset: {type: Array},
    content: {type: String}, // 标记内容
  },
  setup(props, {attrs, emit}) {
    let amap = inject("AMAP");
    let instance = amap.instance;
    let markerRef = ref(null);

    let markerLabel;
    let callback = {
      register: (label) => {
        if(markerRef.value) {
          markerRef.value.setLabel(label);
        } else {
          markerLabel = label;
        }
      },
      updated: (config) => {
        markerRef.value.setLabel(config);
      }
    }

    let extData = props.ext; extData['id'] = props.id;
    provide("AMARKER", callback);
    let createMarker = () => {
      let config = {
        extData,
        icon: props.icon,
        map: instance.value,
        title: props.title,
        position: props.position
      };

      if(props.offset instanceof Array) {
        config['offset'] = new AMap.Pixel(props.offset[0], props.offset[1])
      }

      let marker = markerRef.value = new AMap.Marker(config);
      if(markerLabel != null) {
        marker.setLabel(markerLabel);
      }

      // 绑定单击事件
      if(attrs.onClick instanceof Function) {
        marker.on("click", (e) => {
          let mposition = marker.getPosition();
          let position = [mposition.lng, mposition.lat];
          emit("click", {e, marker, position})
        });
      }
      // 绑定鼠标移入事件
      if(attrs.onMouseover instanceof Function) {
        marker.on("mouseover", (e) => {
          let mposition = marker.getPosition();
          let position = [mposition.lng, mposition.lat];
          emit("mouseover", {e, marker, position})
        });
      }
      // 绑定鼠标移出事件
      if(attrs.onMouseout instanceof Function) {
        marker.on("mouseout", (e) => {
          let mposition = marker.getPosition();
          let position = [mposition.lng, mposition.lat];
          emit("mouseout", {e, marker, position})
        });
      }
    }
    if(!instance.value) {
      watch(instance, () => {
        createMarker();
      })
    } else {
      createMarker();
    }

    return {instance, markerRef, amap}
  },
  mounted() {
    // this.instance.
  },
  updated() {
    this.markerRef.setIcon(this.icon);
    this.markerRef.setTitle(this.title);
    if(this.offset instanceof Array) {
      this.markerRef.setOffset(new AMap.Pixel(this.offset[0], this.offset[1]));
    }

    this.markerRef.setPosition(this.position)
  },
  unmounted() {
    if(this.instance && this.markerRef) {
      this.instance.remove(this.markerRef);
    }
  },
  methods: {
    getMap() {
      return this.amap.parent;
    },
    getInstance() {
      return this.instance;
    },
    getMarker() {
      return this.markerRef;
    }
  }
}
</script>

<style scoped>

</style>