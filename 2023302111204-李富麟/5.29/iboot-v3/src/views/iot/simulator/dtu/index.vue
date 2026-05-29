<template>
  <UView>
    <UForm v-model="editModel" :span="[7, 17]">
      <ARow :gutter="8">
        <ACol span="5">
          <div class="u-dtu-left">
            <a-divider dashed plain>服务器配置</a-divider>
            <UInputItem field="ip" label="ip地址" defaultValue="127.0.0.1" />
            <UInputItem field="port" label="端口" defaultValue="7078"/>
            <UInputItem field="register" label="注册包" defaultValue="123456"/>
            <a-divider dashed plain>dtu心跳配置</a-divider>
            <UInputItem field="heartbeatMsg" label="心跳包" defaultValue="123456"/>
            <UInputNumberItem field="interval" label="心跳间隔(s)" :defaultValue="60" :min="0" :step="1"/>
            <URadioItem field="heartbeat" label="发送心跳" dict="sys_boolean_status" defaultValue="true"/>
            <a-divider dashed />
            <div style="text-align: right">
              <ASpace>
                <AButton type="warn">断开</AButton>
                <AButton type="primary">连接</AButton>
              </ASpace>
            </div>
          </div>
        </ACol>
        <ACol span="14">
          <div class="u-dtu-center">
            <a-divider dashed plain orientation="left">接收服务端的网络数据</a-divider>
            <ATextarea :autosize="{minRows: 6}"></ATextarea>
            <a-divider dashed plain orientation="left">接收客户端的网络数据</a-divider>
            <ATextarea :autosize="{minRows: 6}"></ATextarea>
            <ATextarea  :autosize="{minRows: 3}"/>
            <AButton>发送</AButton>
          </div>
        </ACol>
        <ACol span="5">
          <div class="u-dtu-right">
            <a-divider dashed plain>串口配置</a-divider>
            <USelectItem field="serial" label="串口名" />
            <USelectItem field="baudRate" label="波特率" :options="baudRateOptions" :defaultValue="9600"/>
            <USelectItem field="parity" label="校验位" :options="parityOptions" :defaultValue="2"/>
            <USelectItem field="dataBits" label="数据位" :options="dataBitsOptions" :defaultValue="8"/>
            <USelectItem field="stopBits" label="停止位" :options="stopBitsOptions" :defaultValue="1"/>
            <URadioItem label="转发至串口" field="toSerial" dict="sys_boolean_status" defaultValue="false"/>
          </div>
        </ACol>
      </ARow>
    </UForm>
  </UView>
</template>
<!-- DTU模拟器 -->
<script>
import {ref} from "vue";

export default {
  name: "SimulatorDtu",
  setup() {
    let editModel = ref({});
    let baudRateOptions = ref([
      {label: "2400", value: 2400},
      {label: "4800", value: 4800},
      {label: "9600", value: 9600},
      {label: "19200", value: 19200},
      {label: "38400", value: 38400},
      {label: "43000", value: 43000},
      {label: "56000", value: 56000},
      {label: "57600", value: 57600},
      {label: "115200", value: 115200},
    ]);

    let parityOptions = ref([
      {label: 'None', value: 0},
      {label: 'Od', value: 1},
      {label: 'Even', value: 2},
      {label: 'Mark', value: 3},
      {label: 'Space', value: 4},
    ]);
    let dataBitsOptions = ref([
      {label: '5', value: 5},
      {label: '6', value: 6},
      {label: '7', value: 7},
      {label: '8', value: 8},
    ]);
    let stopBitsOptions = ref([
      {label: '1', value: 1},
      {label: '1.5', value: 2},
      {label: '2', value: 3},
    ]);
    return {editModel, baudRateOptions, parityOptions, dataBitsOptions, stopBitsOptions}
  }
}
</script>

<style scoped>
.u-dtu-left,.u-dtu-right{
  padding: 8px;
  background: #ffffff;
}
</style>
