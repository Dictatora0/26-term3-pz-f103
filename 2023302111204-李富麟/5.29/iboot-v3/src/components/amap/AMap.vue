<template>
  <div style="width: 100%; height: 100%">
    <slot />
    <ADrawer v-if="drawer" v-model:open="drawer.open" :title="drawer.title" :width="drawer.width" destroyOnClose>
      <div id="view-panel"></div>
    </ADrawer>
  </div>
</template>

<script>
import Env from '@/env/index'
import {getCurrentInstance, provide, reactive, ref, watch} from "vue";
import AMapLoader from '@amap/amap-jsapi-loader'

window._AMapSecurityConfig = {
  securityJsCode: Env.amap.security
}
export default {
  name: "AMap",
  props: {
    id: {type: String, required: true},
    zoom: {type: Number, default: 10},
    center: {type: Array},
    toolBar: {type: Boolean, default: true},
    toolPosition: {type: Object, default: () => {
        return {top: '20px', right: '51px'}
      }
    },
    controlBar: {type: Boolean, default: false},
    controlPosition: {type: Object, default: () => {
        return {top: '20px', right: '20px'}
      }
    },
    /**
     * 是否将搜索结果显示在drawer上
     * {
     *   width: '420',
     *   open: false, 开关
     *   title: '', 标题
     * }
     */
    drawer: {type: Object},
    /**
     * 是否启用geo插件
     * {
     *   city: '',
     *   radius: 1000 //范围，默认：500
     * }
     */
    geocoder: {type: Object}, // geo地址编码转换
    /**
     * 是否启用搜索插件
     * {
     *    type: '餐饮服务',
     *    keyword: '', 搜索关键字(多关键词用 | 隔开)
     *    open: true, // 打开或关闭结果框
     *    title: '', // 默认自动
          pageSize: 5, // 单页显示结果条数
          pageIndex: 1, // 页码
          city: "010", // 兴趣点城市
          citylimit: true,  //是否强制限制在设置的城市内搜索
          map: instance.value, // 展现结果的地图实例(自动获取)
          panel: "view-panel", // 结果列表将在此容器中进行展示(自动获取)
          autoFitView: true // 是否自动调整地图视野使绘制的 Marker点都处于视口的可见范围
        }
     */
    placeSearch: {type: Object},
    onClick: {type: Function}, // 单击事件
    onDblclick: {type: Function}, // 双击事件
    onMousemove: {type: Function}, // 鼠标移入事件
    onComplete: {type: Function}, // 地图加载完成事件
  },
  setup(props, {emit}) {
    let placeSearchRef = ref();
    let instance =ref(null);
    let geocoderRef = ref(null);
    let satelliteLayer = ref(null); // 卫星图层

    let initGeoCoder = () => {
      if(props.geocoder) {
        let options = {
          city: Env.amap.city, //城市设为北京，默认：“全国”
          radius: 1000 //范围，默认：500
        }
        AMap.plugin(["AMap.Geocoder"], function() {
          Object.assign(options, props.geocoder);
          geocoderRef.value = new AMap.Geocoder(options);
        })
      }
    }

    let initPlaceSearch = () => {
      if(props.placeSearch) {
        AMap.plugin(["AMap.PlaceSearch"], function() {
          let searchOptions = {
            pageSize: 8, // 单页显示结果条数
            pageIndex: 1, // 页码
            city: Env.amap.city, // 兴趣点城市
            citylimit: true,  //是否强制限制在设置的城市内搜索
            map: instance.value, // 展现结果的地图实例
            // panel: "view-panel", // 结果列表将在此容器中进行展示。
            // extensions: 'base',
            autoFitView: true // 是否自动调整地图视野使绘制的 Marker点都处于视口的可见范围
          };

          if(typeof props.placeSearch == 'object') {
            searchOptions = Object.assign(searchOptions, props.placeSearch);
          }

          // 结果显示到抽屉
          if(props.drawer) {
            searchOptions['panel'] = 'view-panel';
          }

          //构造地点查询类
          placeSearchRef.value = new AMap.PlaceSearch(searchOptions);
        });
      }
    }

    // 监听关键字变化
    watch(() => props.placeSearch && props.placeSearch['keyword'], (keyword) => {
      if(keyword) {
        placeSearchRef.value.search(keyword);

      }
    }, {immediate: true});

    watch(() => props.drawer, () => {
      if(props.drawer) {
        Object.assign(props.drawer, {width: '460'}, props.drawer)
      }
    }, {immediate: true})

    let currentInstance = getCurrentInstance();
    provide("AMAP", {
      parent: currentInstance.proxy,
      instance: instance,
      register: (marker) => {

      }, remove: (marker) => {

      }
    });

    AMapLoader.load({
      key: Env.amap.key,
      version: '2.0',
      plugins: [
        // "AMap.PlaceSearch",
        // "AMap.DistrictSearch",
        // "AMap.DistrictExplorer",
        // "AMap.GeoJSON",
        // "AMap.Gradient",
        "AMap.ToolBar",
        "AMap.ControlBar",
        // 'AMap.AutoComplete',
        // 'AMap.Geocoder',
        // 'AMap.Marker'
      ]
    }).then(AMap => {
      let map = instance.value = new AMap.Map(props.id, {
        viewMode: "3D",  //  是否为3D地图模式
        zoom: props.zoom, //  地图显示的缩放级别
        zooms:[2,22], // 地图缩放范围
        center: props.center, //  地图中心点坐标 此处填【经度，纬度】
        // layers: [new AMap.TileLayer.Satellite()],  //设置图层,可设置成包含一个或多个图层的数组
        // mapStyle: 'amap://styles/whitesmoke',  //设置地图的显示样式
        resizeEnable: true  //  是否监控地图容器尺寸变化
      });

      // 绑定常用事件
      props.onComplete && map.on('complete', function() {
        emit("complete", map);
      })
      props.onClick && map.on('click', function(e) {
        emit("click", e, map);
      })
      props.onDblclick && map.on('dblclick', function(e) {
        emit("dblclick", e, map);
      })
      props.onMousemove && map.on('mousemove', function() {
        emit("mousemove", map);
      })

      // 增加工具栏
      props.toolBar && map.addControl(new AMap.ToolBar({
        position: props.toolPosition
      }));

      props.controlBar && map.addControl(new AMap.ControlBar({
        position: props.controlPosition
      }))
      initGeoCoder();
      initPlaceSearch();
    }).catch(reason => console.error(reason));

    return {instance, satelliteLayer, placeSearchRef, geocoderRef}
  },
  methods: {
    /**
     * 新增卫星图层
     */
    addSatellite() {
      this.satelliteLayer = new AMap.TileLayer.Satellite();
      this.instance.add(this.satelliteLayer);
    },
    /**
     * 移除卫星图层
     */
    removeSatellite() {
      if(this.satelliteLayer) {
        this.instance.remove(this.satelliteLayer);
      }
    },
    /**
     * 增加标记
     * {
     *   icon: '',
     *   position: [lng, lat]
     * }
     */
    addMarker(config) {
      config['map'] = this.instance;
      return new AMap.Marker(config);
    },
    /**
     * 获取所有标记点
     * @return Array
     */
    getMarkers() {
      let overlays = this.instance.getAllOverlays('marker');
      if(overlays instanceof Array) {
        return overlays;
      } else {
        return [];
      }
    },
    /**
     * 组件AMarker的id必填
     * 获取指定id的标记点
     */
    getMarker(id) {
      if(!id) {
        return null;
      }

      let markers = this.getMarkers();
      for (let i = 0; i < markers.length; i++) {
        let marker = markers[i];
        let extData = marker.getExtData();
        if(extData['id'] == id) {
          return marker;
        }
      }
    },
    /**
     * 缩放到标记点位置
     */
    setFitView() {
      this.instance.setFitView();
    },
    /**
     * 移除地图上的所有覆盖物
     */
    removeAllOverlay() {
      this.instance.removeAllOverlay();
    },
    /**
     *获取地理位置编码
     */
    getGeoCoder(lnglat, callback) {
      if(this.geocoderRef) {
        this.geocoderRef.getAddress(lnglat, function(status, result) {
          if(callback instanceof Function) {
            if (status === 'complete' && result.info === 'OK') {
              callback(result);
            }
          }
        })
      }
    },
    /**
     * 获取搜索详情
     * @param keyword
     * @param callback {Function} 可空
     */
    getPlaceSearchDetails(keyword, callback) {
      if(this.placeSearchRef) {
        this.placeSearchRef.getDetails(keyword, function(status, result) {
          if (status === 'complete' && result.info === 'OK') {
            callback(result);
          }
        });
      }
    },
    /**
     * 开始搜索
     * @param keyword
     * @param callback {Function} 可空
     * @param custom {Function} 自定义选项
     */
    startPlaceSearch(keyword, callback, custom) {
      if(this.placeSearchRef) {
        if(custom instanceof Function) {
          custom(this.placeSearchRef);
        }

        if(callback instanceof Function) {
          this.placeSearchRef.search(keyword, callback);
        } else {
          this.placeSearchRef.search(keyword);
          if(this.drawer) {
            this.drawer.open = true;
            this.drawer.title = `关键字 "${keyword}" 搜索结果`;
          }
        }
      } else {
        console.warn('未启用搜索功能, 请配置参数[placeSearch={}]')
      }
    }
  }
}
</script>

<style>
.amap-marker-label {
  border: unset;
  padding: unset;
}
</style>