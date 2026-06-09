export default {
    Seconds:{
        name:'秒',
        every:'每秒',
        interval:['从','秒开始, 每隔','秒执行'],
        specific:'指定',
        cycle:['周期从','到','秒']
    },
    Minutes:{
        name:'分',
        every:'每分钟',
        interval:['从','分开始, 每隔 ','分执行一次'],
        specific:'指定',
        cycle:['周期从',' - ','分']
    },
    Hours:{
        name:'时',
        every:'每小时',
        interval:['从','小时开始, 每隔', '小时执行一次'],
        specific:'指定',
        cycle:['周期从',' - ','小时']
    },
    Day:{
        name:'天',
        every:'每天',
        none: '不指定',
        intervalWeek:['每隔','周执行 从','开始'],
        intervalDay:['从第','天开始, 每隔','天执行一次'],
        specific:'指定',
        lastDay:'在这个月的最后一天',
        lastWeekday:'在这个月的最后一个工作日',
        beforeEndMonth:['在本月底前','天'],
        nearestWeekday:['每月','号最近的那个工作日'],
    },
    Week: {
        name: '周',
        none: '不指定',
        every:'每周',
        specific:'指定',
        lastWeek:['本月最后一个星期'],
        someWeekday:['第','周的星期'],
        value: ['天','一','二','三','四','五','六'],
        valueText: ['天','一','二','三','四','五','六'].map(val=>'星期'+val)
    },
    Month:{
        name:'月',
        every:'每月',
        none: '不指定',
        interval:['从','月开始, 每隔','月执行一次'],
        specific:'指定',
        cycle:['从','到','月之间的每个月']
    },
    Year:{
        name:'年',
        every:'每年',
        interval:['每隔','年执行 从','年开始'],
        specific:'具体年份(可多选)',
        cycle:['从','到','年之间的每一年']
    },
    Save:'保存',
    Close:'关闭',
    Reset: '重置'
}