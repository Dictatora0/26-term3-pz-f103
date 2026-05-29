<template>
  <div class="iv-cron-container">
    <ATabs v-model:activeKey="activeKey" type="card" size="small" centered>
      <template #rightExtra>
        <AButton type="dashed" @click="resetAll">{{text.Reset}}</AButton>
<!--        <AButton type="primary" @click="close">{{text.Close}}</AButton>-->
      </template>
      <ATabPane key="second">
        <template #tab>
          <span><i class="el-icon-date"></i> {{text.Seconds.name}}</span>
        </template>
        <div class="tabBody">
          <ARadioGroup v-model:value="second.cronEvery">
            <div class="iv-cron-item">
              <ARadio value="1">{{text.Seconds.every}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="2">{{text.Seconds.interval[0]}}
                <AInputNumber size="small" v-model:value="second.incrementStart" :min="0" :max="59"></AInputNumber>
                {{text.Seconds.interval[1]||''}}
                <AInputNumber size="small" v-model:value="second.incrementIncrement" :min="1" :max="59"></AInputNumber>
                {{text.Seconds.interval[2]||''}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="4">{{text.Seconds.cycle[0]}}
                <AInputNumber size="small" v-model:value="second.rangeStart" :min="0" :max="59"></AInputNumber>
                {{text.Seconds.cycle[1]||''}}
                <AInputNumber size="small" v-model:value="second.rangeEnd" :min="second.rangeStart ? parseInt(second.rangeStart) + 1 : 0" :max="59"></AInputNumber>
                {{text.Seconds.cycle[2]||''}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio class="long" value="3">
                <ASpace>
                  <div style="width: 32px;">{{text.Seconds.specific}}</div>
                  <a-checkbox-group v-model:value="second.specificSpecific">
                    <ACheckbox v-for="(val) in minutes" :key="val" :value="val">{{val}}</ACheckbox>
                  </a-checkbox-group>
                </ASpace>
              </ARadio>
            </div>
          </ARadioGroup>
        </div>
      </ATabPane>
      <ATabPane key="minute">
        <template #tab>
          <span><i class="el-icon-date"></i> {{text.Minutes.name}}</span>
        </template>
        <div class="tabBody">
          <ARadioGroup v-model:value="minute.cronEvery">
            <div class="iv-cron-item">
              <ARadio value="1">{{text.Minutes.every}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="2">{{text.Minutes.interval[0]}}
                <AInputNumber size="small" v-model:value="minute.incrementStart" :min="0" :max="59"></AInputNumber>
                {{text.Minutes.interval[1]||''}}
                <AInputNumber size="small" v-model:value="minute.incrementIncrement" :min="1" :max="59"></AInputNumber>
                {{text.Minutes.interval[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="4">{{text.Minutes.cycle[0]}}
                <AInputNumber size="small" v-model:value="minute.rangeStart" :min="0" :max="59"></AInputNumber>
                {{text.Minutes.cycle[1]}}
                <AInputNumber size="small" v-model:value="minute.rangeEnd" :min="minute.rangeStart ? parseInt(minute.rangeStart) + 1 : 0" :max="59"></AInputNumber>
                {{text.Minutes.cycle[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio class="long" value="3">
                <ASpace>
                  <div style="width: 32px;">{{text.Minutes.specific}}</div>
                  <a-checkbox-group v-model:value="minute.specificSpecific">
                    <ACheckbox v-for="(val) in minutes" :key="val" :value="val">{{val}}</ACheckbox>
                  </a-checkbox-group>
                </ASpace>
              </ARadio>
            </div>
          </ARadioGroup>
        </div>
      </ATabPane>
      <ATabPane key="hour">
        <template #tab>
          <span><i class="el-icon-date"></i> {{text.Hours.name}}</span>
        </template>
        <div class="tabBody">
          <ARadioGroup v-model:value="hour.cronEvery">
            <div class="iv-cron-item">
              <ARadio value="1">{{text.Hours.every}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="2">{{text.Hours.interval[0]}}
                <AInputNumber size="small" v-model:value="hour.incrementStart" :min="0" :max="23"></AInputNumber>
                {{text.Hours.interval[1]}}
                <AInputNumber size="small" v-model:value="hour.incrementIncrement" :min="1" :max="23"></AInputNumber>
                {{text.Hours.interval[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="4">{{text.Hours.cycle[0]}}
                <AInputNumber size="small" v-model:value="hour.rangeStart" :min="0" :max="23"></AInputNumber>
                {{text.Hours.cycle[1]}}
                <AInputNumber size="small" v-model:value="hour.rangeEnd" :min="hour.rangeStart ? parseInt(hour.rangeStart) + 1 : 0" :max="23"></AInputNumber>
                {{text.Hours.cycle[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio class="long" value="3">
                <ASpace>
                  <div style="width: 32px;">{{text.Hours.specific}}</div>
                  <a-checkbox-group v-model:value="hour.specificSpecific">
                    <ACheckbox v-for="(val) in hours" :key="val" :value="val">{{val}}</ACheckbox>
                  </a-checkbox-group>
                </ASpace>
              </ARadio>
            </div>
          </ARadioGroup>
        </div>
      </ATabPane>
      <ATabPane key="day">
        <template #tab>
          <span><i class="el-icon-date"></i> {{text.Day.name}}</span>
        </template>
        <div class="tabBody">
          <ARadioGroup v-model:value="day.cronEvery">
            <div class="iv-cron-item">
              <ARadio value="1">{{text.Day.every}}</ARadio>
            </div>

            <div class="iv-cron-item">
              <ARadio value="2">{{text.Day.intervalDay[0]}}
                <AInputNumber size="small" v-model:value="day.incrementStart" :min="1" :max="31"></AInputNumber>
                {{text.Day.intervalDay[1]}}
                <AInputNumber size="small" v-model:value="day.incrementIncrement" :min="1" :max="31"></AInputNumber>
                {{text.Day.intervalDay[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="4">{{text.Day.cycle[0]}}
                <AInputNumber size="small" v-model:value="day.rangeStart" :min="1" :max="31"></AInputNumber>
                {{text.Day.cycle[1]}}
                <AInputNumber size="small" v-model:value="day.rangeEnd" :min="day.rangeStart ? parseInt(day.rangeStart) + 1 : 1" :max="31"></AInputNumber>
                {{text.Day.cycle[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="5">{{text.Day.none}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="6">{{text.Day.lastDay}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="8">{{text.Day.nearestWeekday[0]}}
                <AInputNumber size="small" v-model:value="day.cronDaysNearestWeekday" :min="1" :max="31"></AInputNumber>
                {{text.Day.nearestWeekday[1]}}
              </ARadio>
            </div>
<!--            <div class="iv-cron-item">-->
<!--              <ARadio value="9">-->
<!--                {{text.Day.beforeEndMonth[0]}}-->
<!--                <AInputNumber size="small" v-model:value="day.cronDaysBeforeEomMinus" :min="1" :max="31"></AInputNumber>-->
<!--                {{text.Day.beforeEndMonth[1]}}-->
<!--              </ARadio>-->
<!--            </div>-->
            <div class="iv-cron-item">
              <ARadio class="long" value="3">
                <ASpace>
                  <div style="width: 32px;">{{text.Day.specific}}</div>
                  <a-checkbox-group v-model:value="day.specificSpecific">
                    <ACheckbox v-for="(val) in days" :key="val" :value="val">{{val}}</ACheckbox>
                  </a-checkbox-group>
                </ASpace>
              </ARadio>
            </div>
          </ARadioGroup>
        </div>
      </ATabPane>
      <ATabPane key="month">
        <template #tab>
          <span><i class="el-icon-date"></i> {{text.Month.name}}</span>
        </template>
        <div class="tabBody">
          <ARadioGroup v-model:value="month.cronEvery">
            <div class="iv-cron-item">
              <ARadio value="1">{{text.Month.every}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="2">
                {{text.Month.interval[0]}}
                <AInputNumber size="small" v-model:value="month.incrementStart" :min="1" :max="12"></AInputNumber>
                {{text.Month.interval[1]}}
                <AInputNumber size="small" v-model:value="month.incrementIncrement" :min="1" :max="12"></AInputNumber>
                {{text.Month.interval[2]}}
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="4">{{text.Month.cycle[0]}}
                <AInputNumber size="small" v-model:value="month.rangeStart" :min="1" :max="12"></AInputNumber>
                {{text.Month.cycle[1]}}
                <AInputNumber size="small" v-model:value="month.rangeEnd" :min="month.rangeStart ? parseInt(month.rangeStart) + 1 : 1" :max="12"></AInputNumber>
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio class="long" value="3">
                <ASpace>
                  <div style="width: 32px;">{{text.Month.specific}}</div>
                  <a-checkbox-group v-model:value="month.specificSpecific">
                    <ACheckbox v-for="(val) in months" :key="val" :value="val">{{val}}</ACheckbox>
                  </a-checkbox-group>
                </ASpace>
              </ARadio>
            </div>
          </ARadioGroup>
        </div>
      </ATabPane>
      <ATabPane key="week">
        <template #tab>
          <span><i class="el-icon-date"></i> {{text.Week.name}}</span>
        </template>
        <div class="tabBody">
          <ARadioGroup v-model:value="week.cronEvery">
            <div class="iv-cron-item">
              <ARadio value="1">{{text.Week.every}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="5">{{text.Week.none}}</ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="11">{{text.Week.someWeekday[0]}}
                <AInputNumber size="small" v-model:value="week.cronNthDayNth" :min="1" :max="4"></AInputNumber>
                {{text.Week.someWeekday[1]}}
                <span>
                  <ARadioGroup v-model:value="week.cronNthDayDay" button-style="solid" size="small">
                    <ARadioButton v-for="(val, index) in 7" :key="index" :value="val + ''">{{text.Week.value[val-1]}}</ARadioButton>
                  </ARadioGroup>
                </span>
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio value="8">{{text.Week.lastWeek[0]}}
                <ARadioGroup v-model:value="week.cronLastSpecificDomDay" button-style="solid" size="small">
                  <ARadioButton v-for="(val, index) in 7" :key="index" :value="val + ''">{{text.Week.value[val-1]}}</ARadioButton>
                </ARadioGroup>
              </ARadio>
            </div>
            <div class="iv-cron-item">
              <ARadio class="long" value="3">
                <ASpace>
                  <div style="width: 32px;">{{text.Week.specific}}</div>
                  <a-checkbox-group v-model:value="week.specificSpecific">
                    <ACheckbox v-for="(val, index) in text.Week.valueText" :key="val" :value="index + 1">{{val}}</ACheckbox>
                  </a-checkbox-group>
                </ASpace>
              </ARadio>
            </div>
          </ARadioGroup>
        </div>
      </ATabPane>
    </ATabs>
    <div style="margin-top: 6px; padding: 3px; text-align: center; background-color: rgba(217,217,217,0.15)">
      <span class="value">{{this.cronValue}}</span>
    </div>
  </div>
</template>

<script>
import Language from './i18n'
import {reactive, ref} from "vue";
import dayjs from "dayjs";

export default {
  name: "UCron",
  props:['cron','i18n', 'onChange'],
  setup(props) {
    let time = new dayjs();
    const minutes = ref([]);
    for (let i = 0; i < 60; i++) {
      minutes.value[i] = (i + "").padStart(2, '0');
    }
    const hours = ref([]);
    for (let i = 0; i <= 23; i++) {
      hours.value[i] = (i + "").padStart(2, '0');
    }
    const days = ref([]);
    for (let i = 1; i <= 31; i++) {
      days.value[i - 1] = (i + "").padStart(2, '0');
    }
    const months = ref([]);
    for (let i = 1; i <= 12; i++) {
      months.value[i - 1] = (i + "").padStart(2, '0');
    }

    let activeKey = ref('second');
    let second = reactive({
      cronEvery:'',
      incrementStart:'0',
      incrementIncrement:'30',
      rangeStart:'1',
      rangeEnd:'59',
      specificSpecific:[],
    });
    let minute = reactive({
      cronEvery:'',
      incrementStart:'0',
      incrementIncrement:'1',
      rangeStart:'1',
      rangeEnd:'59',
      specificSpecific:[],
    });
    let hour = reactive({
      cronEvery:'',
      incrementStart:'1',
      incrementIncrement:'1',
      rangeStart:'',
      rangeEnd:'',
      specificSpecific:[],
    });
    let day = reactive({
      cronEvery:'',
      incrementStart:'1',
      incrementIncrement:'1',
      rangeStart:'',
      rangeEnd:'',
      specificSpecific:[],
      cronDaysNearestWeekday:'',
      cronDaysBeforeEomMinus:'',
    });
    let week = reactive({
      cronEvery:'5',
      incrementStart:'1',
      incrementIncrement:'1',
      specificSpecific:[],
      cronNthDayDay:1,
      cronNthDayNth:'1',
      cronLastSpecificDomDay:1,
    });
    let month = reactive({
      cronEvery:'',
      incrementStart:time.month(),
      incrementIncrement:'1',
      rangeStart:time.month(),
      rangeEnd:'',
      specificSpecific:[],
    });
    let year = reactive({
      cronEvery:'1',
      incrementStart:'2024',
      incrementIncrement:'1',
      rangeStart:'2024',
      rangeEnd:'2028',
      specificSpecific:[],
    });

    let resolver = (value, model) => {
      if(value == '*') {
        model.cronEvery = '1';
      } else if(value.indexOf('/') > 0) {
        model.cronEvery = '2';
        let split1 = value.split('/');
        model.incrementStart = split1[0];
        model.incrementIncrement = split1[1];
      } else if(value.indexOf(',') > 0) {
        model.cronEvery = '3';
        model.specificSpecific = value.split(',');
      } else if(value.indexOf('-') > 0) {
        model.cronEvery = '4';
        let split1 = value.split('-');
        model.rangeStart = split1[0];
        model.rangeEnd = split1[1];
      } else if(value == '?') {
        model.cronEvery = '5';
      } else {
        model.cronEvery = '3';
        model.specificSpecific = [value.padStart(2, '0')];
      }
    }

    if(props.cron) {
      let split = props.cron.split(' ');
      if(split.length >= 6) {
        resolver(split[0], second);
        resolver(split[1], minute);
        resolver(split[2], hour);
        resolver(split[3], day);
        if(split[3] == 'L') {
          day.cronEvery = '6';
        } else if(split[3].endsWith('W')) {
          day.cronEvery = '8';
          day.cronDaysNearestWeekday = split[3].substring(0, split[3].length - 1);
        }
        resolver(split[4], month);
        resolver(split[5], week);
        if(split[5].endsWith('L')) {
          week.cronEvery = '8';
          week.cronLastSpecificDomDay = split[5].substring(0, split[5].length - 1);
        } else if(split[5].indexOf('#') > 0) {
          week.cronEvery = '11';
          let split2 = split[5].split('#');
          week.cronNthDayDay = split2[0];
          week.cronNthDayNth = split2[1];
        }
      }
    }

    return {days, hours, months, minutes, activeKey, second, minute, hour, day, week, month, year}
  },
  computed: {
    text(){
      return Language[this.i18n||'cn']
    },
    secondsText() {
      let seconds = '';
      let cronEvery=this.second.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          seconds = '*';
          break;
        case '2':
          seconds = this.second.incrementStart+'/'+this.second.incrementIncrement;
          break;
        case '3':
          this.second.specificSpecific.map(val=> {
            seconds += parseInt(val)+','
          });
          seconds = seconds.slice(0, -1);
          break;
        case '4':
          seconds = this.second.rangeStart+'-'+this.second.rangeEnd;
          break;
      }
      return seconds;
    },
    minutesText() {
      let minutes = '';
      let cronEvery=this.minute.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          minutes = '*';
          break;
        case '2':
          this.linkage('second');
          minutes = this.minute.incrementStart+'/'+this.minute.incrementIncrement;
          break;
        case '3':
          this.minute.specificSpecific.map(val=> {
            minutes += parseInt(val)+','
          });
          minutes = minutes.slice(0, -1);
          break;
        case '4':
          minutes = this.minute.rangeStart+'-'+this.minute.rangeEnd;
          break;
      }
      return minutes;
    },
    hoursText() {
      let hours = '';
      let cronEvery=this.hour.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          hours = '*';
          break;
        case '2':
          this.linkage('second');
          this.linkage('minute');
          hours = this.hour.incrementStart+'/'+this.hour.incrementIncrement;
          break;
        case '3':
          this.hour.specificSpecific.map(val=> {
            hours += parseInt(val)+','
          });
          hours = hours.slice(0, -1);
          break;
        case '4':
          hours = this.hour.rangeStart+'-'+this.hour.rangeEnd;
          break;
      }
      return hours;
    },
    daysText() {
      let days='';
      if(this.day.cronEvery != '5') {
        this.week.cronEvery = '5';
      }
      let cronEvery=this.day.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          days = '*';
          break;
        case '2':
          this.linkage('second');
          this.linkage('minute');
          this.linkage('hour');
          days = this.day.incrementStart+'/'+this.day.incrementIncrement;
          break;
        case '3':
          this.day.specificSpecific.map(val=> {
            days += parseInt(val)+','
          });
          days = days.slice(0, -1);
          break;
        case '4':
          days = this.day.rangeStart+'-'+this.day.rangeEnd;
          break;
        case '5':
          days = '?';
          break;
        case '6':
          days = "L";
          break;
        case '7':
          days = "LW";
          break;
        case '8':
          days = this.day.cronDaysNearestWeekday+"W";
          break;
        case '9':
          days = 'L-' + this.day.cronDaysBeforeEomMinus;
          break;
      }
      return days;
    },
    weeksText() {
      let weeks = '';
      if(this.week.cronEvery != 5) {
        this.day.cronEvery = '5';
      }
      let cronEvery=this.week.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          weeks = '*'; break;
        case '2':
          this.linkage('second');
          this.linkage('minute');
          this.linkage('hour');
          weeks = this.week.incrementStart+'/'+this.week.incrementIncrement;
          break;
        case '3':
          this.week.specificSpecific.map(val=> {
            weeks += val+','
          });

          weeks = weeks.slice(0, -1);
          break;
        case '5':
          weeks = '?'; break;
        case '8':
          weeks = this.week.cronLastSpecificDomDay + 'L';
          break;
        case '11':
          weeks = this.week.cronNthDayDay+"#"+this.week.cronNthDayNth;
          break;
      }
      return weeks;
    },
    monthsText() {
      let months = '';
      let cronEvery=this.month.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          months = '*';
          break;
        case '2':
          this.linkage('second');
          this.linkage('minute');
          this.linkage('hour');
          this.day.cronEvery = '3';
          this.day.specificSpecific = ['01'];
          months = this.month.incrementStart+'/'+this.month.incrementIncrement;
          break;
        case '3':
          this.month.specificSpecific.map(val=> {
            months += parseInt(val)+','
          });
          months = months.slice(0, -1);
          break;
        case '4':
          months = this.month.rangeStart+'-'+this.month.rangeEnd;
          break;
      }
      return months;
    },
    yearsText() {
      let years = '';
      let cronEvery=this.year.cronEvery;
      switch (cronEvery.toString()){
        case '1':
          years = '*';
          break;
        case '2':
          years = this.year.incrementStart+'/'+this.year.incrementIncrement;
          break;
        case '3':
          this.year.specificSpecific.map(val=> {
            years += parseInt(val)+','
          });
          years = years.slice(0, -1);
          break;
        case '4':
          years = this.year.rangeStart+'-'+this.year.rangeEnd;
          break;
      }
      return years;
    },
    cronValue(){
      return `${this.secondsText||'*'} ${this.minutesText||'*'} ${this.hoursText||'*'} ${this.daysText||'*'} ${this.monthsText||'*'} ${this.weeksText||'?'}`
      // return `${this.secondsText||'*'} ${this.minutesText||'*'} ${this.hoursText||'*'} ${this.daysText||'*'} ${this.monthsText||'*'} ${this.weeksText||'?'} ${this.yearsText||'*'}`
    },
  },
  watch: {
    cronValue(newValue) {
      this.$emit("change", newValue);
    }
  },
  methods: {
    getValue(){
      return this.cronValue;
    },
    change(){
      this.$emit('change',this.cronValue);
      this.close();
    },
    close(){
      this.$emit('close')
    },
    linkage(type) {
      this[type]['cronEvery'] = '3';
      this[type]['specificSpecific'] = ['00'];
    },
    resetAll() {
      this.rest(this.second);
      this.rest(this.minute);
      this.rest(this.hour);
      this.rest(this.day);
      this.rest(this.month);
      this.rest(this.week);
      this.rest(this.year);
    },
    rest(data){
      for(let i in data){
        if(data[i] instanceof Object){
          this.rest(data[i])
        }else{
          switch(typeof data[i]){
            case 'object':data[i]=[];break;
            case 'string':data[i]='';break;
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.iv-cron-container {
  width: 630px;
  height: 360px;
  padding: 8px 16px;
}
.iv-cron-item {
  padding: 3px;
}
</style>