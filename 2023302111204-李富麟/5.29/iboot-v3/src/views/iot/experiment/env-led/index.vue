<template>
  <UView name="IotEnvLedLab">
    <UViewSearch v-model="searchModel">
      <URow col="search">
        <UInputItem field="deviceSn" label="设备编号" />
        <UTreeSelectItem field="deviceGroupId" label="所属组" url="/iot/eventSource/deviceGroups" :allowClear="true" />
        <USelectItem field="productId" label="所属产品" url="/iot/product/list" :allowClear="true" />
        <AButton type="primary" @click="search">搜索</AButton>
        <AButton @click="resetSearch">重置</AButton>
      </URow>
    </UViewSearch>

    <ARow :gutter="[16, 16]" class="lab-layout">
      <ACol :xs="24" :xl="8">
        <ACard title="设备列表" :bordered="false">
          <template v-if="devices.length > 0">
            <AFlex vertical :gap="12">
              <div
                v-for="item in devices"
                :key="item.device.uid"
                class="lab-device"
                :class="{ active: currentDevice.uid === item.device.uid }"
                @click="selectDevice(item)"
              >
                <div class="lab-device-head">
                  <div>
                    <div class="lab-device-name">{{ item.device.name }}</div>
                    <div class="lab-device-sn">{{ item.device.deviceSn }}</div>
                  </div>
                  <ABadge
                    :status="item.device.status === 'online' ? 'processing' : 'default'"
                    :text="item.device.status === 'online' ? '在线' : '离线'"
                  />
                </div>
                <div class="lab-device-values">
                  <div v-for="value in item.values || []" :key="value.field" class="lab-device-value">
                    <span class="label">{{ value.name }}</span>
                    <span class="value">{{ formatValue(value.value) }}{{ value.unit || "" }}</span>
                  </div>
                </div>
                <div class="lab-device-led">
                  <span>LED</span>
                  <span>{{ resolveCtrlLabel(item.ctrlValue, item.options) }}</span>
                </div>
              </div>
            </AFlex>
            <a-pagination
              v-model:current="searchModel.current"
              v-model:pageSize="searchModel.size"
              :total="total"
              :page-size-options="[12, 24, 48]"
              style="margin-top: 16px; text-align: right"
            />
          </template>
          <AEmpty v-else description="没有匹配的设备" />
        </ACard>
      </ACol>

      <ACol :xs="24" :xl="16">
        <ASpin :spinning="detailLoading || loading">
          <template v-if="currentDevice.uid">
            <div class="lab-hero">
              <div>
                <div class="lab-eyebrow">iBOOT 实验面板</div>
                <div class="lab-title">{{ currentDevice.name || "-" }}</div>
                <div class="lab-subtitle">{{ currentDevice.deviceSn || "-" }} / {{ currentProduct.name || "-" }}</div>
              </div>
              <div class="lab-status" :class="currentDevice.status">
                {{ currentDevice.status === "online" ? "在线" : "离线" }}
              </div>
            </div>

            <ARow :gutter="[16, 16]">
              <ACol :xs="24" :md="8">
                <ACard class="lab-metric temperature" :bordered="false">
                  <div class="metric-label">温度</div>
                  <div class="metric-value">{{ metricValue(temperatureAttr) }}</div>
                  <div class="metric-meta">{{ metricTime(temperatureAttr) }}</div>
                </ACard>
              </ACol>
              <ACol :xs="24" :md="8">
                <ACard class="lab-metric humidity" :bordered="false">
                  <div class="metric-label">湿度</div>
                  <div class="metric-value">{{ metricValue(humidityAttr) }}</div>
                  <div class="metric-meta">{{ metricTime(humidityAttr) }}</div>
                </ACard>
              </ACol>
              <ACol :xs="24" :md="8">
                <ACard class="lab-metric led" :bordered="false">
                  <div class="metric-label">LED</div>
                  <div class="metric-value">{{ resolveCtrlLabel(currentCtrlValue, currentCtrlOptions) }}</div>
                  <div class="metric-meta">{{ metricTime(ledAttr) }}</div>
                </ACard>
              </ACol>
            </ARow>

            <AAlert
              v-if="missingFields.length > 0"
              type="warning"
              show-icon
              style="margin-top: 16px"
              :message="`未自动识别字段：${missingFields.join('、')}。可通过 URL 参数 tempField / humidityField / ledField 指定。`"
            />

            <ACard title="LED 控制" :bordered="false" style="margin-top: 16px">
              <ASpace wrap>
                <AButton
                  v-for="option in currentCtrlOptions"
                  :key="option.value"
                  :type="String(currentCtrlValue) === String(option.value) ? 'primary' : 'default'"
                  :disabled="!canControlLed || currentDevice.status !== 'online' || switchLoading"
                  :loading="switchLoading && String(pendingCtrlValue) === String(option.value)"
                  @click="switchLed(option.value)"
                >
                  {{ option.label }}
                </AButton>
              </ASpace>
              <div class="lab-tip">当前实验页复用现有 iBOOT 控制接口和实时数据推送链路，仅补充了角色级控制。</div>
            </ACard>

            <ACard title="实时属性数据" :bordered="false" style="margin-top: 16px">
              <ATable :columns="attrColumns" :data-source="displayAttrs" :pagination="false" row-key="field" size="small" />
            </ACard>
          </template>
          <AEmpty v-else description="请先从左侧选择一个设备" />
        </ASpin>
      </ACol>
    </ARow>
  </UView>
</template>

<script>
import dayjs from "dayjs";
import CoreConsts from "@/components/CoreConsts";
import { urlConfig } from "@/utils/request";

const FIELD_KEYWORDS = {
  temperature: {
    field: ["temperature", "temp", "wendu", "wen_du"],
    name: ["温度"]
  },
  humidity: {
    field: ["humidity", "humid", "shidu", "shi_du"],
    name: ["湿度"]
  },
  led: {
    field: ["led", "light", "lamp", "deng"],
    name: ["led", "灯", "照明"]
  }
};

export default {
  name: "IotEnvLedLab",
  data() {
    return {
      total: 0,
      loading: false,
      detailLoading: false,
      switchLoading: false,
      pendingCtrlValue: null,
      devices: [],
      detailAttrs: [],
      currentRecord: {},
      currentDevice: {},
      currentProduct: {},
      currentCtrlOptions: [],
      currentCtrlValue: null,
      resolvedFields: { temperature: null, humidity: null, led: null },
      overrideFields: { temperature: null, humidity: null, led: null },
      realtimeSocket: null,
      searchModel: { current: 1, size: 12, deviceType: "Child", deviceSn: "" },
      attrColumns: [
        { title: "属性名称", dataIndex: "name", key: "name", width: 140 },
        { title: "字段", dataIndex: "field", key: "field", width: 140 },
        { title: "值", dataIndex: "value", key: "value", width: 140 },
        { title: "单位", dataIndex: "unit", key: "unit", width: 100 },
        { title: "采集时间", dataIndex: "collectTime", key: "collectTime", width: 140 }
      ]
    };
  },
  computed: {
    temperatureAttr() {
      return this.getAttrByKind("temperature");
    },
    humidityAttr() {
      return this.getAttrByKind("humidity");
    },
    ledAttr() {
      return this.getAttrByKind("led");
    },
    canControlLed() {
      const roleNames = this.$store.getters["sys/user"]?.roleNames;
      const roles = Array.isArray(roleNames)
        ? roleNames
        : String(roleNames || "")
            .split(",")
            .map(item => item.trim())
            .filter(Boolean);
      return roles.includes("管理员") || roles.includes("OPERATOR");
    },
    displayAttrs() {
      const first = [this.temperatureAttr, this.humidityAttr, this.ledAttr].filter(Boolean);
      const used = new Set(first.map(item => item.field));
      return [...first, ...this.detailAttrs.filter(item => !used.has(item.field))];
    },
    missingFields() {
      const results = [];
      if (!this.temperatureAttr) results.push("温度");
      if (!this.humidityAttr) results.push("湿度");
      if (!this.ledAttr) results.push("LED");
      return results;
    }
  },
  watch: {
    "searchModel.current"() {
      this.search();
    },
    "searchModel.size"() {
      this.search();
    },
    "$route.query": {
      handler() {
        this.applyRouteQuery();
      },
      immediate: true
    }
  },
  mounted() {
    this.search();
  },
  beforeUnmount() {
    this.closeRealtime();
  },
  methods: {
    applyRouteQuery() {
      this.overrideFields = {
        temperature: this.$route.query.tempField || this.$route.query.temperatureField || null,
        humidity: this.$route.query.humidityField || null,
        led: this.$route.query.ledField || null
      };

      if (this.$route.query.deviceSn) {
        this.searchModel.deviceSn = this.$route.query.deviceSn;
      }
    },
    resetSearch() {
      this.searchModel = { current: 1, size: 12, deviceType: "Child", deviceSn: "" };
      this.applyRouteQuery();
      this.search();
    },
    search() {
      this.loading = true;
      this.$http
        .get("/iot/panels/mqtt/devices", { params: this.searchModel })
        .then(({ code, message, data }) => {
          if (code === CoreConsts.SuccessCode) {
            this.total = data?.total || 0;
            this.devices = data?.records || [];
            this.ensureSelectedDevice();
          } else {
            this.$msg.error(message);
          }
        })
        .finally(() => {
          this.loading = false;
        });
    },
    ensureSelectedDevice() {
      if (!(this.devices instanceof Array) || this.devices.length === 0) {
        this.currentRecord = {};
        this.currentDevice = {};
        this.currentProduct = {};
        this.detailAttrs = [];
        this.currentCtrlOptions = [];
        this.currentCtrlValue = null;
        this.closeRealtime();
        return;
      }

      const targetId = this.$route.query.deviceId;
      const targetSn = this.$route.query.deviceSn;
      let matched = null;
      if (targetId) {
        matched = this.devices.find(item => String(item.device?.id) === String(targetId));
      }
      if (!matched && targetSn) {
        matched = this.devices.find(item => item.device?.deviceSn === targetSn);
      }
      if (!matched && this.currentDevice.uid) {
        matched = this.devices.find(item => item.device?.uid === this.currentDevice.uid);
      }

      this.selectDevice(matched || this.devices[0]);
    },
    selectDevice(item) {
      if (!item || !item.device) {
        return;
      }

      if (this.currentDevice.uid === item.device.uid && this.detailAttrs.length > 0) {
        this.currentRecord = item;
        this.currentCtrlOptions = item.options || [];
        this.currentCtrlValue = item.ctrlValue;
        return;
      }

      this.currentRecord = item;
      this.currentDevice = { ...item.device };
      this.currentProduct = item.product || {};
      this.currentCtrlOptions = item.options || [];
      this.currentCtrlValue = item.ctrlValue;
      this.loadDetail(item.device.id, item.device.uid);
    },
    loadDetail(deviceId, uid) {
      this.detailLoading = true;
      this.closeRealtime();
      this.$http
        .get("/iot/panels/detail", { params: { deviceId } })
        .then(({ code, message, data }) => {
          if (code === CoreConsts.SuccessCode) {
            this.currentDevice = data.device || this.currentDevice;
            this.currentProduct = data.product || this.currentProduct;
            this.detailAttrs = data.attrs || [];
            this.resolvedFields = this.resolveFields(this.detailAttrs, this.overrideFields);
            this.syncCtrlValueFromAttr();
            this.openRealtime(uid);
          } else {
            this.$msg.error(message);
          }
        })
        .finally(() => {
          this.detailLoading = false;
        });
    },
    openRealtime(uid) {
      if (!uid) {
        return;
      }

      this.realtimeSocket = new WebSocket(urlConfig.getFullWsURL(`/ws/iot/realtime?uid=${uid}&type=123`));
      this.realtimeSocket.onmessage = this.handleRealtime;
    },
    closeRealtime() {
      if (this.realtimeSocket instanceof WebSocket) {
        this.realtimeSocket.close();
      }
      this.realtimeSocket = null;
    },
    handleRealtime(message) {
      const payload = JSON.parse(message.data);
      if (typeof payload !== "object" || !payload) {
        return;
      }

      if (payload.type === "status") {
        this.currentDevice.status = payload.value?.status;
        const record = this.devices.find(item => item.device?.uid === this.currentDevice.uid);
        if (record?.device) {
          record.device.status = this.currentDevice.status;
        }
        return;
      }

      if (payload.type !== "model" || typeof payload.value !== "object") {
        return;
      }

      Object.values(payload.value).forEach(item => {
        const attr = this.detailAttrs.find(detail => detail.field === item.signalOrField);
        if (attr) {
          attr.value = typeof item.value === "boolean" ? String(item.value) : item.value;
          attr.collectTime = item.collectTime ? dayjs(item.collectTime).format("MM-DD HH:mm:ss") : attr.collectTime;
        }
      });

      this.syncCtrlValueFromAttr();
      this.syncListCardValues(payload.value);
    },
    syncListCardValues(realtimeValues) {
      const record = this.devices.find(item => item.device?.uid === this.currentDevice.uid);
      if (!record || !(record.values instanceof Array)) {
        return;
      }

      Object.values(realtimeValues).forEach(item => {
        const matched = record.values.find(value => value.field === item.signalOrField);
        if (matched) {
          matched.value = typeof item.value === "boolean" ? String(item.value) : item.value;
        }
      });

      if (this.resolvedFields.led) {
        const ledValue = realtimeValues[this.resolvedFields.led];
        if (ledValue) {
          record.ctrlValue = typeof ledValue.value === "boolean" ? String(ledValue.value) : String(ledValue.value);
          this.currentCtrlValue = record.ctrlValue;
        }
      }
    },
    syncCtrlValueFromAttr() {
      if (!this.resolvedFields.led) {
        return;
      }

      const led = this.detailAttrs.find(item => item.field === this.resolvedFields.led);
      if (led && led.value != null) {
        this.currentCtrlValue = String(led.value);
      }
    },
    switchLed(value) {
      if (!this.currentDevice.uid) {
        return;
      }

      this.switchLoading = true;
      this.pendingCtrlValue = value;
      this.$http
        .post("/iot/panels/switchCtrlStatus", { id: this.currentDevice.uid, status: value })
        .then(({ code, message }) => {
          if (code === CoreConsts.SuccessCode) {
            this.currentCtrlValue = String(value);
            if (this.ledAttr) {
              this.ledAttr.value = String(value);
              this.ledAttr.collectTime = dayjs().format("MM-DD HH:mm:ss");
            }

            const record = this.devices.find(item => item.device?.uid === this.currentDevice.uid);
            if (record) {
              record.ctrlValue = String(value);
            }
          } else {
            this.$msg.error(message);
          }
        })
        .finally(() => {
          this.switchLoading = false;
          this.pendingCtrlValue = null;
        });
    },
    getAttrByKind(kind) {
      const field = this.resolvedFields[kind];
      if (!field) {
        return null;
      }
      return this.detailAttrs.find(item => item.field === field) || null;
    },
    resolveFields(attrs, overrideFields) {
      return {
        temperature: this.detectField(attrs, overrideFields?.temperature, FIELD_KEYWORDS.temperature),
        humidity: this.detectField(attrs, overrideFields?.humidity, FIELD_KEYWORDS.humidity),
        led: this.detectField(attrs, overrideFields?.led, FIELD_KEYWORDS.led)
      };
    },
    detectField(attrs, explicitField, keywords) {
      if (!(attrs instanceof Array) || attrs.length === 0) {
        return null;
      }

      if (explicitField) {
        const explicit = attrs.find(item => item.field === explicitField);
        if (explicit) {
          return explicit.field;
        }
      }

      const fieldMatched = attrs.find(item => this.containsAny(item.field, keywords.field));
      if (fieldMatched) {
        return fieldMatched.field;
      }

      const nameMatched = attrs.find(item => this.containsAny(item.name, keywords.name));
      return nameMatched ? nameMatched.field : null;
    },
    containsAny(source, values) {
      if (!source) {
        return false;
      }

      const lowerSource = String(source).toLowerCase();
      return values.some(item => lowerSource.includes(String(item).toLowerCase()));
    },
    metricValue(attr) {
      if (!attr) {
        return "--";
      }
      return `${this.formatValue(attr.value)}${attr.unit || ""}`;
    },
    metricTime(attr) {
      return attr?.collectTime || "暂无数据";
    },
    formatValue(value) {
      if (value === null || value === undefined || value === "") {
        return "--";
      }
      return value;
    },
    resolveCtrlLabel(value, options) {
      if (value === null || value === undefined || value === "") {
        return "--";
      }

      const matched = (options || []).find(item => String(item.value) === String(value));
      return matched ? matched.label : String(value);
    }
  }
};
</script>

<style scoped>
.lab-layout {
  align-items: stretch;
}

.lab-device {
  padding: 14px;
  border: 1px solid #e8edf4;
  border-radius: 14px;
  background:
    radial-gradient(circle at top right, rgba(40, 167, 69, 0.08), transparent 34%),
    linear-gradient(135deg, #ffffff, #f7fafc);
  cursor: pointer;
  transition: 0.2s ease;
}

.lab-device:hover,
.lab-device.active {
  border-color: #35a66b;
  box-shadow: 0 12px 32px rgba(53, 166, 107, 0.12);
  transform: translateY(-2px);
}

.lab-device-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.lab-device-name {
  font-size: 16px;
  font-weight: 600;
  color: #16324f;
}

.lab-device-sn {
  margin-top: 4px;
  color: #6b7a8c;
  font-size: 12px;
}

.lab-device-values {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.lab-device-value {
  padding: 10px;
  border-radius: 10px;
  background: rgba(22, 50, 79, 0.05);
}

.lab-device-value .label {
  display: block;
  font-size: 12px;
  color: #6b7a8c;
}

.lab-device-value .value {
  display: block;
  margin-top: 4px;
  font-weight: 600;
  color: #16324f;
}

.lab-device-led {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  color: #16324f;
  font-weight: 600;
}

.lab-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 20px 24px;
  border-radius: 20px;
  background:
    linear-gradient(135deg, #16324f, #24567a),
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.25), transparent 32%);
  color: #ffffff;
}

.lab-eyebrow {
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
  opacity: 0.78;
}

.lab-title {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
}

.lab-subtitle {
  margin-top: 6px;
  opacity: 0.82;
}

.lab-status {
  padding: 10px 16px;
  border-radius: 999px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.16);
}

.lab-metric {
  min-height: 160px;
  border-radius: 18px;
  overflow: hidden;
}

.lab-metric :deep(.ant-card-body) {
  height: 100%;
}

.lab-metric.temperature {
  background: linear-gradient(135deg, #fff1dd, #ffd4a8);
}

.lab-metric.humidity {
  background: linear-gradient(135deg, #dff5ff, #b5e6ff);
}

.lab-metric.led {
  background: linear-gradient(135deg, #eef8dd, #d8f0aa);
}

.metric-label {
  font-size: 14px;
  color: #516071;
}

.metric-value {
  margin-top: 18px;
  font-size: 34px;
  font-weight: 700;
  color: #162b3f;
}

.metric-meta {
  margin-top: 18px;
  font-size: 12px;
  color: #60758a;
}

.lab-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #6b7a8c;
}
</style>
