<template>
  <div style="width: 100%; height: 100%;" class="u-core-index">
    <AFlex justify="space-between" gap="18" style="height: 150px;">
      <div class="iv-work iv-work-protocol" style="width: 30%;">
        <div class="iv-work-bg"></div>
        <a-card size="small">
          <template #title>
            <div class="iv-work-title">
              <span>接入协议</span>
            </div>
          </template>
          <ASpace :size="12">
            <div class="iv-item-logo iv-item-protocol">
              <UIcon type="iz-icon-mc-protocol" style="font-size: 36px;"></UIcon>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.protocolNum }}</div>
              <div class="iv-item-title">协议接入数</div>
            </div>
          </ASpace>
        </a-card>
      </div>
      <div class="iv-work iv-work-product" style="width: 30%;">
        <a-card size="small">
          <template #title>
            <div class="iv-work-title">
              <span>产品信息</span>
            </div>
          </template>
          <ASpace :size="12">
            <div class="iv-item-logo iv-item-product">
              <UIcon type="iz-icon-mc-product" style="font-size: 36px;"></UIcon>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.productNum }}</div>
              <div class="iv-item-title">产品数</div>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.enabledProductNum }}</div>
              <div class="iv-item-title">启用产品数</div>
            </div>
          </ASpace>
        </a-card>
      </div>
      <div class="iv-work iv-work-event" style="width: 30%;">
        <a-card size="small">
          <template #title>
            <div class="iv-work-title">
              <span>事件源信息</span>
            </div>
          </template>
          <ASpace :size="12">
            <div class="iv-item-logo iv-item-event">
              <UIcon type="iz-icon-mc-event" style="font-size: 36px;"></UIcon>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.eventSourceNum }}</div>
              <div class="iv-item-title">总数</div>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.eventSourceNum }}</div>
              <div class="iv-item-title">运行中</div>
            </div>
          </ASpace>
        </a-card>
      </div>
      <div class="iv-work iv-work-device" style="width: 30%;">
        <a-card size="small">
          <template #title>
            <div class="iv-work-title">
              <span>设备信息</span>
            </div>
          </template>
          <ASpace :size="12">
            <div class="iv-item-logo iv-item-device">
              <UIcon type="iz-icon-mc-device" style="font-size: 36px;"></UIcon>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.deviceNum }}</div>
              <div class="iv-item-title">总数</div>
            </div>
            <div class="iv-item-col">
              <div class="iv-item-value">{{ profileCount.onlineNum }}</div>
              <div class="iv-item-title">在线数</div>
            </div>
          </ASpace>
        </a-card>
      </div>
    </AFlex>
    <AFlex justify="space-between" style="height: 360px; margin-top: 18px;" gap="18">
      <ACard title="产品设备数量统计" class="u-row" size="small" style="width: 60%">
        <div id="iv-index-product" style="height: 270px"></div>
      </ACard>
      <ACard title="类型设备数量统计" class="u-row" size="small" style="width: 40%">
        <div id="iv-index-device-type" style="height: 270px"></div>
      </ACard>
    </AFlex>
    <div style="background-color: #ffffff; padding: 16px">
      <div id="iot-collect-data" style="width: 100%; height: 290px"></div>
    </div>
  </div>
</template>
<!--首页或者工作台-->
<script>
import * as echarts from 'echarts/core'
import {LineChart, PieChart, BarChart} from 'echarts/charts';
import {CanvasRenderer} from 'echarts/renderers';
import {UniversalTransition} from 'echarts/features';
import {
  ToolboxComponent,
  DatasetComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent
} from 'echarts/components'
import {getCurrentInstance, onMounted, onUnmounted, ref} from "vue";
import {HealthWebsocket} from "@msn/iot/debug/websocket";
import CoreConsts from "@/components/CoreConsts";
import dayjs from "dayjs";
echarts.use([CanvasRenderer, TitleComponent, GridComponent, VisualMapComponent, LineChart, BarChart
  , UniversalTransition, TooltipComponent, PieChart, LegendComponent, DatasetComponent, ToolboxComponent])

export default {
  name: "Index",
  setup() {
    let cpuCharts = null; // cpu使用率图表
    let profileCount = ref({
      deviceNum: 0, // 设备数量
      onlineNum: 0, // 在线设备数量
      productNum: 0, // 产品数量
      enabledProductNum: 0, // 启用的产品数量
      eventSourceNum: 0, // 事件源数量
      runningEventSourceNum: 0, // 运行中的事件源
      protocolNum: 0, // 接入的协议数量
    });
    let productOption = {
      title: {
        show: false,
        text: '各产品设备数量',
        // subtext: '纯属虚构',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c} ({d}%)'
      },
      legend: {
        type: 'scroll',
        orient: 'vertical',
        right: 18,
        top: 20,
        bottom: 20,
        data: []
      },
      series: [
        {
          name: '设备数量',
          type: 'pie',
          radius: ['30%', '60%'],
          center: ['40%', '50%'],
          data: [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    };
    const labelOption = {
      show: true,
      position: 'insideBottom',
      distance: 15,
      align: 'left',
      verticalAlign: 'middle',
      rotate: 90,
      formatter: '{name|{a}} {c}台',
      fontSize: 16,
      color: '#ffffff',
      rich: {
        name: {}
      }
    };
    let deviceTypeOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['在线', '离线']
      },
      grid: {
        top: '12%',
        left: '5%',
        right: '2%',
        bottom: '0',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          inside: true,
          axisTick: { show: false },
          data: ['直连设备', '网关设备', '网关子设备']
        }
      ],
      yAxis: [
        {
          name: '台',
          type: 'value',
          axisLabel: {//这是y轴文字颜色
            lineStyle: {
              color: "#fff",
            }
          },
        }
      ],
      series: [
        {
          name: '在线',
          type: 'bar',
          barGap: 0,
          // showBackground: true,
          label: labelOption,
          barWidth: '32px',
          emphasis: {
            focus: 'series',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#2378f7' },
                { offset: 0.7, color: '#2378f7' },
                { offset: 1, color: '#83bff6' }
              ])
            }
          },
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          data: [0, 0, 0]
        },
        {
          name: '离线',
          type: 'bar',
          barWidth: '32px',
          // showBackground: true,
          label: labelOption,
          emphasis: {
            focus: 'series',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#f7a223' },
                { offset: 0.7, color: '#f3c85d' },
                { offset: 1, color: '#f1dda3' }
              ])
            }
          },
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f1dda3' },
              { offset: 0.5, color: '#f3c85d' },
              { offset: 1, color: '#f7a223' }
            ])
          },
          data: [0, 0, 0]
        }
      ]
    };
    let collectDataOptions = {
      title: {
        text: '近一个月采集数据量',
        left: 'center',
        show: true
      },
      visualMap: [
        {
          show: false,
          type: 'continuous',
          seriesIndex: 0,
          min: 0,
          max: 20
        }
      ],
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        data: []
      },
      grid: {
        top: '10%',
        left: '5%',
        right: '7%',
        bottom: '5%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          type: 'line',
          data: []
        }
      ]
    };
    let instance = getCurrentInstance();
    onMounted(() => {
      instance.proxy.$http.get("/iot/index/numCount").then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          profileCount.value = data;
        } else {
          instance.proxy.$msg.error(message);
        }
      })

      let productEcharts = echarts.init(document.getElementById("iv-index-product"));
      productEcharts.setOption(productOption);
      instance.proxy.$http.get("/iot/index/productDeviceCount").then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          if(data instanceof Array) {
            let names = data.map(item => item.name);
            productOption.legend.data = names;
            productOption.series[0].data = data;
            productEcharts.setOption(productOption);
          }
        } else {
          instance.proxy.$msg.error(message);
        }
      })

      let deviceTypeEcharts = echarts.init(document.getElementById("iv-index-device-type"));
      deviceTypeEcharts.setOption(deviceTypeOption);
      instance.proxy.$http.get("/iot/index/deviceTypeCount").then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          if(data instanceof Array) {
            let names = data.map(item => item['typeName']);
            let online = data.map(item => item.online);
            let offline = data.map(item => item.total - item.online);
            deviceTypeOption.xAxis[0].data = names;
            deviceTypeOption.series[0].data = online;
            deviceTypeOption.series[1].data = offline;
            deviceTypeEcharts.setOption(deviceTypeOption);
          }
        } else {
          instance.proxy.$msg.error(message);
        }
      })

      let collectDataEcharts = echarts.init(document.getElementById("iot-collect-data"));
      collectDataEcharts.setOption(collectDataOptions);
      instance.proxy.$http.get("/iot/index/countDataWithLastMonth").then(({code, message, data}) => {
        if(code == CoreConsts.SuccessCode) {
          if(data instanceof Array) {
            let xAxis = data.map(item => item.category);
            let values = data.map(item => item.value);
            collectDataOptions.xAxis.data = xAxis;
            collectDataOptions.series[0].data = values;
            collectDataEcharts.setOption(collectDataOptions);
          }
        } else {
          instance.proxy.$msg.error(message);
        }
      })
    })

    return { profileCount}
  }
}
</script>

<style scoped>
.u-core-index {
 padding: 0px 12px;
}
.u-row {
  margin: 8px 0px 16px;
}
.iv-work {
  overflow: hidden;
  text-overflow: ellipsis;
}
.iv-work-title {}
.iv-work-title span {}
.iv-work-bg { }

.iv-item-logo {
  padding: 24px;
  background-color: rgba(61, 98, 171, 0.02);
}
.iv-item-col {
  text-align: center;
  padding-left: 8px;
}
.iv-work .iv-item-title, .iv-work iv-item-value {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.iv-work .iv-item-value {
  font-size: 18px;
  font-weight: 600;
}
</style>
