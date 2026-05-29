<template>
  <AFlex style="position: relative; height: 100%" vertical>
    <div style="padding: 3px 0;">
      等级: <ASelect style="width: 160px;" v-model:value="level" :options="levelOptions"></ASelect>
      类型: <a-checkbox-group v-model:value="filter" name="checkboxgroup" :options="filterOptions" />
      过滤: <a-auto-complete v-model:value="content" :options="subOptions" style="width: 200px" placeholder="请输入要过滤的值" allow-clear />

      <AButton type="dashed" style="margin-left: 12px" @click="clear">清空</AButton>
      <span style="margin-left: 3px; font-size: 12px; color: #d46b08">注: 查看完后请及时关闭</span>
    </div>
    <div style="background-color: #5a5a5a; color: #e3e3e3; overflow: auto; padding: 3px 0 6px; flex-grow: 1">
      <template v-for="(logger, index) in loggers" :key="logger.id">
        <div>
          <span style="width: 42px; display: inline-block; text-align: right; padding: 0 4px 0 0; border-right: 1px solid #a0a0a0;">{{index}}</span>
          <span style="margin-left: 2px;" v-if="logger.message">
            <span class="iv-logger-item" style="width: 60px;">{{logger.timestamp}}</span>
            <span :class="['iv-logger-item', `iv-logger-${logger.level}`]" style="width: 56px;">[{{logger.level}}]</span>
            <span class="iv-logger-item iv-logger-thread" style="width: 90px;">[<span class="iv-logger-text">{{logger.thread}}</span>]</span>
            <span class="iv-logger-item" style="margin-left: 12px">- &nbsp;&nbsp; {{logger.message}}</span>
          </span>
          <span class="iv-logger-exception" v-else v-html="logger.exception" />
        </div>
      </template>
    </div>
  </AFlex>
</template>
<script>
import {computed, defineComponent, ref, watch} from "vue";
import env from "@/env";
import dayjs from "dayjs";
import store from "@/store";
import CoreConsts from "@/components/CoreConsts";
const levelOptions = [
  {label: 'DEBUG', value: 10},
  {label: 'INFO', value: 20},
  {label: 'WARN', value: 30},
  {label: 'ERROR', value: 40},
]
function getLevelValue(level) {
  switch (level) {
    case 'DEBUG': return 10;
    case 'INFO': return 20;
    case 'WARN': return 30;
    case 'ERROR': return 40;
  }
}
export default defineComponent({
  name: 'URunningLogger',
  props: {
    url: {default: '/ws/logger/realtime'},
    typeCall: {type: Function, default: (filter) => true}
  },
  setup(props) {
    let level = ref(10);
    let filter = ref([]);
    let content = ref(null);
    let loggerConfig = store.getters['sys/loggerConfig'];
    let filterOptions = ref(loggerConfig.filters);
    if(loggerConfig.websocket == null) {
      let websocket = loggerConfig.websocket = new WebSocket(env.http.getFullWsURL(props.url));
      websocket.onopen = () => {
        let params = {level: level.value, filter: filter.value};
        websocket.send(JSON.stringify(params));
      }

      websocket.onmessage = (message) => {
        let row = 1, id = 0;
        let logger = JSON.parse(message.data);
        if(loggerConfig.loggers.length > 0) {
          id = loggerConfig.loggers[loggerConfig.loggers.length - 1].id + 1;
        }

        logger['timestamp'] = dayjs(parseInt(logger['timestamp'])).format('HH:mm:ss')
        loggerConfig.loggers.push({id, ...logger});
        if(logger['stackTrace'] instanceof Array) {
          logger['stackTrace'].forEach(exception => {
            row ++;
            loggerConfig.loggers.push({id: ++id, level: logger.level, exception: `<span style="margin-left: 32px">${exception}</span>`})
          })
        }

        if(loggerConfig.loggers.length >= 1000) {
          loggerConfig.loggers.splice(0, row)
        }
      }
    }

    let clear = function () {
      loggerConfig.loggers.length = 0;
    }

    watch([level, filter], (newValue) => {
      loggerConfig.websocket.send(JSON.stringify({level: level.value, filter: filter.value}));
    })

    loggerConfig.refNum ++;
    let loggers = computed(() => {
      return loggerConfig.loggers.filter(item => {
        let typeFilter = props.typeCall(filter.value);
        let contentFilter = content.value && item.message ? item.message.indexOf(content.value) > -1 : true;
        return getLevelValue(item.level) >= level.value && contentFilter && typeFilter;
      });
    })
    let subOptions = computed(() => {
      if(filter.value.length > 0) {
        let results = [];
        filterOptions.value.forEach(item => {
          if(filter.value.findIndex(find => item.value === find) >= 0) {
            if(item.children && item.children.length > 0) {
              results.push(...item.children);
            }
          }
        });

        return results;
      }
      return []
    })
    return {level, filter, loggers, levelOptions, content, loggerConfig, filterOptions, clear, subOptions};
  },
  created() {
    if(this.loggerConfig.filters.length === 0) {
      this.$http.get("/core/framework/loggers").then(({code, data, message}) => {
        if(code === CoreConsts.SuccessCode) {
          if(this.loggerConfig.filters.length === 0) {
            if(data instanceof Array) {
              data.forEach(item => this.filterOptions.push(item))
            }
          }
        } else {
          this.$msg.error(message);
        }
      })
    }
  },
  unmounted() {
    this.loggerConfig.refNum = this.loggerConfig.refNum - 1;
    if(this.loggerConfig.refNum === 0) { // 没有任何引用暂停此链接的数据推送
      this.loggerConfig.websocket.close(1000);
      this.loggerConfig.websocket = null;
    }
  }
})
</script>


<style scoped>
.iv-logger-item {
  margin: 0 2px;
  text-align: right;
  display: inline-block;
}
.iv-logger-text {
  overflow: hidden;
  white-space: nowrap;
  width: 80px;
  vertical-align: bottom;
  display: inline-block;
  text-overflow: ellipsis;
}
.iv-logger-INFO {
  color: #008d4c;
}
.iv-logger-WARN {
  color: #fab06b;
}
.iv-logger-ERROR {
  color: #d4380d;
}
.iv-logger-thread {
  color: #19B0FE;
}
.iv-logger-exception {
  padding-left: 10px;
}
</style>