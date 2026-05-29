import {computed, defineComponent, inject, reactive, ref, watch} from "vue";
import {mapActions, useStore} from "vuex";
import {MetaConst, getMetaValue} from "@/utils/MetaUtils";
import CoreConsts from "@/components/CoreConsts";
import {FormContext} from "@/components/form/basic/FormContext";
import SysUtils from "@/utils/SysUtils";

export default defineComponent({
    props: {
        url: String,
        dict: String,
        options: Array,
        onChange: null,
        onSelect: null,
        source: null,
        refresh: {default: true}, // 是否需要重新加载指定url的数据
        'onUpdate:source': null,
        onLoaded: {default: () => { return () => null}}, // 数据加载完成
        labelField: {default: CoreConsts.Options_LabelField},
        valueField: {default: CoreConsts.Options_ValueField},
    },
    data() {
        return {
            isOption: true,
            dataSource: [],
            valueModelMap: {},
            optionsMaps: null,
            isReloadOptions: false,
        }
    },
    created() {
        let valueField = this.$attrs.fieldNames ? this.$attrs.fieldNames.value || this.$attrs.fieldNames.key : this.valueField;
        let labelField = this.$attrs.fieldNames ? this.$attrs.fieldNames.label || this.$attrs.fieldNames.title : this.labelField;
        if(!this.options) {
            let formContext = this.formContext;

            if(this.dict) {
                let {options, valueModelMap} = this.optionsMaps = this.$store.getters['sys/getOptionsByDictType'](this.dict, labelField, valueField, this.onLoaded);
                this.dataSource = ref(options); this.valueModelMap = valueModelMap;
            } else if(this.url) {// 实现动态级联获取 /test?type={typeId}
                let url = this.url;
                let fields = this.url.match(/\{([^}]+)\}/g);
                if(fields instanceof Array) {
                    let editModel = ref({})
                    if(formContext instanceof FormContext) {
                        editModel = formContext.getEditModel();
                    }

                    // 监控表单数据对象变化, 校验是否需要重新获取数据
                    watch(formContext.getEditModel, (newVal, oldVal) => {
                        if(typeof newVal == 'object') {
                            // 根据最新的model获取最新的url
                            let newUrl = this.getUrl(fields, newVal);
                            if(url != newUrl) { // url地址不一样, 说明需要重新获取数据
                                let {options, valueModelMap} = this.optionsMaps = this.$store.getters['sys/getOptionsByUrl'](url = newUrl, labelField
                                    , valueField, MetaConst.DefaultLabelField, MetaConst.DefaultValueField, this.onLoaded);
                                this.dataSource = ref(options); this.valueModelMap = valueModelMap;

                                // 更新父组件options的值
                                this.$emit("update:source", this.valueModelMap)
                                // 只对同一条记录切换的时候才重置值
                                if(newVal == oldVal) {
                                    // 重置当前表单的数据
                                    formContext.setFieldValue(this.namePath, null)
                                }
                            }
                        } else {
                            console.error(`不能解析url${this.url}, 参数错误`)
                        }
                    }, {deep: true, immediate: true})
                } else {
                    let {options, valueModelMap} = this.optionsMaps = this.$store.getters['sys/getOptionsByUrl'](url, labelField
                        , valueField, MetaConst.DefaultLabelField, MetaConst.DefaultValueField, this.onLoaded);
                    this.dataSource = ref(options); this.valueModelMap = valueModelMap;
                }
            }

            // 更新父组件options的值
            this.$emit("update:source", this.valueModelMap)
        } else {
            this.dataSource = this.options;
            if(labelField && valueField) {
                SysUtils.resolverOptions(this.options, labelField, valueField
                    , MetaConst.DefaultLabelField, MetaConst.DefaultValueField, {}, this.valueModelMap)
                this.$emit("update:source", this.valueModelMap);
            }
        }
    },
    beforeUpdate() {
        // 如果options改变则重新赋值到dataSource
        // 因为options是由父组件传进来的, 改变说明父组件的options改变了
        if(this.options instanceof Array) {
            this.dataSource = this.options;
        }
    },
    activated() {
        if(this.refresh && !this.options && this.url && this.isReloadOptions) {
            this.doRefreshOptions(); // 重新获取url的数据
        }
    },
    deactivated() {
        if(this.refresh && !this.options && this.url) {
            // this.dataSource = [];
            this.isReloadOptions = true; // 重新初始化数据
            this.optionsMaps['options'] = null; // 重新加载
            this.optionsMaps['valueLabelMap'] = {};
            this.optionsMaps['valueModelMap'] = this.valueModelMap = {};
        }
    },
    methods: {
        getUrl(fields, editModel) {
            let url = this.url;
            fields.forEach(field => {
                let fieldStr = field.substr(1, field.length - 2);
                let fieldValue = editModel[fieldStr] == null ? '' : editModel[fieldStr];
                url = url.replace(field, fieldValue);
            })

            return url;
        },
        getDataSource() {
            return this.dataSource;
        },
        /**
         * 重新加载url数据
         */
        refreshOptions() {
            this.optionsMaps['options'] = null; // 重新加载
            this.doRefreshOptions();
        },
        doRefreshOptions() {
            let url = this.url;
            let fields = url.match(/\{([^}]+)\}/g);
            let valueField = this.$attrs.fieldNames ? this.$attrs.fieldNames.value || this.$attrs.fieldNames.key : this.valueField;
            let labelField = this.$attrs.fieldNames ? this.$attrs.fieldNames.label || this.$attrs.fieldNames.title : this.labelField;
            if(fields instanceof Array) {
                let editModel = this.formContext.getEditModel();
                url = this.getUrl(fields, editModel);
            }

            let {options, valueModelMap} = this.optionsMaps = this.$store.getters['sys/getOptionsByUrl'](url, labelField
                , valueField, MetaConst.DefaultLabelField, MetaConst.DefaultValueField, this.onLoaded);
            this.dataSource = options; this.valueModelMap = valueModelMap;
        },
        /**
         * 返回当前选中项对应的对象
         */
        getSelectedModel(value) {
            if(this.formContext instanceof FormContext) {
                if(!value) {
                    let editModel = this.formContext.getEditModel();
                    value = getMetaValue(this.namePath, editModel);
                }
                if(value instanceof Array) {
                    return value.map(item => this.valueModelMap[item]);
                } else {
                    return this.valueModelMap[value];
                }
            }

            return null;
        },
        /**
         * 对options组件做事件代理
         * @param attrs
         */
        proxyOptionsEvent(attrs) {
            if("isOption" in this) {
                let onChange = this.$props['onChange'];
                if(onChange instanceof Function) {
                    attrs['onChange'] = (value, e, v) => {
                        // 处理返回的是事件对象
                        if(typeof value == 'object' && value['target']) {
                            value = value['target'].value;
                        }

                        let model = this.getSelectedModel(value);
                        onChange(value, model, e, v);
                    }
                }

                let onSelect = this.$props['onSelect'];
                if(onSelect instanceof Function) {
                    attrs['onSelect'] = (value, e, v) => {
                        let model = this.getSelectedModel(value);
                        onChange(value, model, e, v);
                    }
                }
            }

            return attrs;
        },
    }
})
