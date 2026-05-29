import {defineComponent, mergeProps, ref, watch} from "vue";
import {useStore} from "vuex";
import {MetaConst} from "@/utils/MetaUtils";
import CoreConsts from "@/components/CoreConsts";
import SysUtils from "@/utils/SysUtils";

export default defineComponent({
    props: {
        url: String,
        dict: String,
        options: Array,
        onChange: null,
        onSelect: null,
        onLoaded: {default: () => { return () => null}}, // 数据加载完成
        labelField: {default: CoreConsts.Options_LabelField},
        valueField: {default: CoreConsts.Options_ValueField},
    },
    data() {
        return {
            isOption: true,
            dataSource: [],
            valueModelMap: {},
        }
    },
    created() {
        let valueField = this.$attrs.fieldNames ? this.$attrs.fieldNames.value || this.$attrs.fieldNames.key : this.valueField;
        let labelField = this.$attrs.fieldNames ? this.$attrs.fieldNames.label || this.$attrs.fieldNames.title : this.labelField;
        if(!this.options) {
            if(this.dict) {
                let {options, valueModelMap} = this.$store.getters['sys/getOptionsByDictType'](this.dict, labelField, valueField, this.onLoaded);
                this.dataSource = ref(options); this.valueModelMap = valueModelMap;

                // 更新父组件options的值
                this.$emit("update:source", this.valueModelMap)
            } else if(this.url) {
                let url = this.url;
                watch(() => this.url, (newUrl) => {
                    let {options, valueModelMap} = this.$store.getters['sys/getOptionsByUrl'](url, labelField
                        , valueField, MetaConst.DefaultLabelField, MetaConst.DefaultValueField, this.onLoaded);
                    this.dataSource = ref(options); this.valueModelMap = valueModelMap;

                    // 更新父组件options的值
                    this.$emit("update:source", this.valueModelMap)
                }, {immediate: true})
            }

        } else {
            this.dataSource = this.options;
            if(labelField && valueField) {
                SysUtils.resolverOptions(this.options, labelField, valueField
                    , MetaConst.DefaultLabelField, MetaConst.DefaultValueField, {}, this.valueModelMap);
                this.$emit("update:source", this.valueModelMap);
            }
        }
    },
    methods: {
        getDataSource() {
            return this.dataSource;
        },
        /**
         * 返回当前选中项对应的对象
         */
        getSelectedModel(value) {
            if(value instanceof Array) {
                return value.map(item => this.valueModelMap[item]);
            }

            return this.valueModelMap[value];
        },

        /**
         * 合并属性
         * @return {Data}
         */
        mergeAttrs() {
            return mergeProps(this.$attrs, {onChange: this.onChangeProxy, onSelect: this.onSelectProxy});
        },

        onChangeProxy(value, e, v) {
            let onChange = this.$props['onChange'];
            if(onChange instanceof Function) {
                let model = this.getSelectedModel(value);
                onChange(value, model, e, v);
            }
        },

        onSelectProxy(value, e, v) {
            let onSelect = this.$props['onSelect'];
            if(onSelect instanceof Function) {
                let model = this.getSelectedModel(value);
                onSelect(value, model, e, v);
            }
        }
    }
})
