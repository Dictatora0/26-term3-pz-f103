import {computed, defineAsyncComponent, defineComponent, h, inject, ref, resolveComponent} from "vue";
import MixinsFormItem from "@/components/form/basic/MixinsFormItem";
import MixinsOptionsItem from "@/components/form/basic/MixinsOptionsItem";
import CoreConsts from "@/components/CoreConsts";
import ACol from "ant-design-vue/es/grid/Col";
import AFormItem from "ant-design-vue/es/form/FormItem";
import {useInjectFormItemContext} from "ant-design-vue/es/form";

const UEditor = defineAsyncComponent(() => import('@/components/form/editor/editor.jsx'))
const UMarkdown = defineAsyncComponent(() => import('@/components/form/editor/markdown.jsx'))

export const UInputItem = defineComponent({
    name: 'UInputItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-input {...this.getFormAttrs()} v-slots={this.$slots}></a-input>}
            </AFormItem>
        </a-col>
    }

})

export const UInputNumberItem = defineComponent({
    name: 'UInputNumberItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-input-number {...this.getFormAttrs()} v-slots={this.$slots} style="width: 100%"></a-input-number>}
            </AFormItem>
        </a-col>
    }

})
export const UInputPasswordItem = defineComponent({
    name: 'UInputPasswordItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-input-password {...this.getFormAttrs()} v-slots={this.$slots} />}
            </AFormItem>
        </a-col>
    }
})

const UInputGroupWrapper = defineComponent({
    name: 'UInputGroupWrapper',
    props: ['onUpdate:value', 'value', "onChange", 'onBlur', 'namePath', "valueModelMap", "field"],
    setup({field, namePath, valueModelMap, onChange, onBlur}, {emit}) {
        let formContext = inject('formContext');
        return {formContext};
    },
    render() {
        return <a-input-group {...this.$attrs} compact>
            {this.$slots.default ? this.$slots.default(this.formContext.getEditModel()) : []}
        </a-input-group>
    }
})

export const UInputGroupItem = defineComponent({
    name: 'UInputGroupItem',
    mixins: [MixinsFormItem],
    components: {UInputGroupWrapper},
    props: {
        field: {required: true}
    },
    render() {
        let props = this.getFormItemProps();

        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <UInputGroupWrapper {...this.$attrs} v-slots={this.$slots} field={this.field}/>
            </AFormItem>
        </a-col>
    }
})

export const UTextareaItem = defineComponent({
    name: 'UTextareaItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <a-textarea {...this.getFormAttrs()}></a-textarea>
            </AFormItem>
        </a-col>
    }

})
export const UMarkdownItem = defineComponent({
    name: 'UMarkdownItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <UMarkdown {...this.getFormAttrs()} v-slots={this.$slots}/>
            </AFormItem>
        </a-col>
    }
})
export const UCheckboxItem = defineComponent({
    name: 'UCheckboxItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    setup(props, {slots}) {
        let defaultSlots
        if(props.options instanceof Array || props.dict || props.url) {
            defaultSlots = (attrs) => h(resolveComponent('a-checkbox-group'), attrs, slots.default)
        } else {
            defaultSlots = (attrs) => h(resolveComponent('a-checkbox'), attrs, slots.default)
        }

        return {defaultSlots}
    },
    render() {
        let props = this.getFormItemProps();

        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {this.defaultSlots(this.getFormAttrs({options: this.dataSource}))}
            </AFormItem>
        </a-col>
    }

})

export const USwitchItem = defineComponent({
    name: 'USwitchItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-switch {...this.getCheckedAttrs()} v-slots={this.$slots}></a-switch>}
            </AFormItem>
        </a-col>
    }

})

export const URateItem = defineComponent({
    name: 'URateItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-rate {...this.getFormAttrs()} v-slots={this.$slots}></a-rate>}
            </AFormItem>
        </a-col>
    }

})

export const USelectItem = defineComponent({
    name: 'USelectItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    setup(props, {attrs}) {
        let fieldNames = attrs['fieldNames'];
        if(fieldNames == null) {
            fieldNames = {label: props.labelField, value: props.valueField, children: CoreConsts.Options_ChildrenField};
        }

        return {fieldNames};
    },
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <ASelect {...this.getFormAttrs()} options={this.dataSource} fieldNames={this.fieldNames} v-slots={this.$slots} />
            </AFormItem>
        </a-col>
    }
})

export const USliderItem = defineComponent({
    name: 'USliderItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-slider {...this.getFormAttrs()} v-slots={this.$slots}></a-slider>}
            </AFormItem>
        </a-col>
    }

})
export const UCascaderItem = defineComponent({
    name: 'UCascaderItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    render() {
        let props = this.getFormItemProps();

        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-cascader {...this.getFormAttrs({options: this.dataSource})} v-slots={this.$slots}></a-cascader>}
            </AFormItem>
        </a-col>
    }

})
export const UAutoCompleteItem = defineComponent({
    name: 'UAutoCompleteItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {<a-auto-complete {...this.getFormAttrs({options: this.dataSource})} v-slots={this.$slots}></a-auto-complete>}
            </AFormItem>
        </a-col>
    }
})

export const URadioItem = defineComponent({
    name: 'URadioItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    render() {
        let props = this.getFormItemProps();
        return <ACol {...this.getColProps()}>
            <AFormItem {...props}>
                <ARadioGroup {...this.getFormAttrs({options: this.dataSource})} v-slots={this.$slots} />
            </AFormItem>
        </ACol>
    }

})
export const UMentionsItem = defineComponent({
    name: 'UMentionsItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <AMentions {...this.getFormAttrs()} options={this.dataSource} v-slots={this.$slots} />
            </AFormItem>
        </a-col>
    }

})
const USegmentedWrapper = defineComponent({
    name: 'USegmentedWrapper',
    props: ['onUpdate:value', 'value', "onChange", 'onBlur', 'namePath', "valueModelMap"],
    setup({namePath, valueModelMap, onChange, onBlur}, {emit}) {
        let formItemContext = useInjectFormItemContext();
        let formContext = inject('formContext');
        // let modelValue = ref(null);
        let modelValue = computed(() => {
            return formContext.getFieldValue(namePath);
        })

        let changeHandle = (value) => {
            formContext.setFieldValue(namePath, value);
            if(onChange instanceof Function) {
                let model = valueModelMap[value];
                onChange(value, model);
            }

            formItemContext.onFieldChange();
        }

        let blurHandle = (value) => {
            if(onBlur instanceof Function) {
                onBlur(value);
            }

            formItemContext.onFieldBlur();
        }

        return {formItemContext, changeHandle, blurHandle, modelValue};
    },
    render() {
        return <ASegmented value={this.modelValue} onChange={this.changeHandle} onBlur={this.blurHandle} />
    }
})
export const USegmentedItem = defineComponent({
    name: 'USegmentedItem',
    components: {USegmentedWrapper},
    mixins: [MixinsFormItem, MixinsOptionsItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col>
            <AFormItem {...props}>
                {<USegmentedWrapper options={this.dataSource} class={this.class} onChange={this.onChange}
                        style={this.style} namePath={this.namePath} valueModelMap={this.valueModelMap}/>}
            </AFormItem>
        </a-col>
    }
})
export const UTreeSelectItem = defineComponent({
    name: 'UTreeSelectItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    setup(props, {attrs}) {

        /**
         * 使用方法主要解决TreeSelect组件不能触发同步问题
         * @return {{children: string, title: *, value: *}}
         */
        let getFieldNames = () => {
            let fieldNames = attrs['fieldNames'];
            if(fieldNames == null) {
                fieldNames = {label: props.labelField, value: props.valueField, children: CoreConsts.Options_ChildrenField};
            }

            return fieldNames;
        }

        return {getFieldNames}
    },
    render() {
        let props = this.getFormItemProps();
        return <ACol {...this.getColProps()}>
            <AFormItem {...props}>
                <ATreeSelect {...this.getFormAttrs()} fieldNames={this.getFieldNames()}
                    treeData={this.dataSource} v-slots={this.$slots} />
            </AFormItem>
        </ACol>
    }

})

const typeMaps = {date: 'a-date-picker', month: 'a-month-picker'
    , range: 'a-range-picker', week: 'a-week-picker', time: 'a-time-picker'}
export const UDateTimeItem = defineComponent({
    name: 'UDateTimeItem',
    props: ['picker'],
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        let tag = typeMaps[this.$props['picker']] || 'a-date-picker';

        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                {h(resolveComponent(tag), this.getFormAttrs(), this.$slots)}
            </AFormItem>
        </a-col>
    }
})
export const UDatePickerItem = defineComponent({
    name: 'UDatePickerItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <ADatePicker {...this.getFormAttrs()} v-slots={this.$slots}/>
            </AFormItem>
        </a-col>
    }
})
export const UMonthPickerItem = defineComponent({
    name: 'UMonthPickerItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <AMonthPicker {...this.getFormAttrs()} v-slots={this.$slots}/>
            </AFormItem>
        </a-col>
    }
})
export const UWeekPickerItem = defineComponent({
    name: 'UWeekPickerItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <AWeekPicker {...this.getFormAttrs()} v-slots={this.$slots}/>
            </AFormItem>
        </a-col>
    }
})
export const URangePickerItem = defineComponent({
    name: 'URangePickerItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <ARangePicker {...this.getFormAttrs()} v-slots={this.$slots}/>
            </AFormItem>
        </a-col>
    }
})
export const UTimePickerItem = defineComponent({
    name: 'UTimePickerItem',
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <ATimePicker {...this.getFormAttrs()} v-slots={this.$slots}/>
            </AFormItem>
        </a-col>
    }
})
export const UTransferItem = defineComponent({
    name: 'UTransferItem',
    mixins: [MixinsFormItem, MixinsOptionsItem],
    setup(props, {attrs}) {
        let fieldNames = attrs['fieldNames'];
        if(fieldNames == null) {
            fieldNames = {label: props.labelField, value: props.valueField, children: CoreConsts.Options_ChildrenField};
        }
        let selectedKeys = ref([]);

        let render = attrs.render || (item => item[fieldNames.label]);
        let rowKey = attrs.rowKey || (record => record[fieldNames.value] + '');

        let attrOptions = {rowKey, render}
        return {fieldNames, selectedKeys, attrOptions};
    },
    render() {
        let props = this.getFormItemProps();
        return <ACol {...this.getColProps()}>
            <AFormItem {...props}>
                <ATransfer {...this.getTransferAttrs(this.attrOptions)} v-model={[this.selectedKeys, 'selectedKeys', ["modifier"]]}
                   dataSource={this.dataSource} v-slots={this.$slots}/>
            </AFormItem>
        </ACol>
    }
})
export const UUploadItem = defineComponent({
    name: 'UUploadItem',
    props: ['onChange'],
    mixins: [MixinsFormItem],
    render() {
        let props = this.getFormItemProps();
        return <a-col {...this.getColProps()}>
            <AFormItem {...props}>
                <AUpload {...this.$attrs} class={this.class} style={this.style} v-slots={this.$slots} onChange={this.changeHandle}/>
            </AFormItem>
        </a-col>
    },
    methods: {
        changeHandle(event) {
            let fileList = event['fileList'];
            if(this.onChange instanceof Function) {
                this.onChange(event);
            } else {
                if(fileList instanceof Array && fileList.length > 0) {
                    let result = fileList.filter(item => {
                        return item['status'] == 'done' && item['response'].code == CoreConsts.SuccessCode;
                    }).map(item => {
                        return item['response'].data;
                    });

                    if(this.$attrs['defaultValue'] instanceof Array) {
                        this.formContext.setFieldValue(this.namePath, result);
                    } else if(this.$attrs['maxCount'] == 1) {
                        this.formContext.setFieldValue(this.namePath, result[0]);
                    } else {
                        this.formContext.setFieldValue(this.namePath, result.join(","));
                    }
                }
            }
        }
    }
})
const formComponents = {UInputItem, USelectItem, UCheckboxItem, USwitchItem, UMarkdownItem, UUploadItem
    , URateItem, USliderItem, UInputNumberItem, UCascaderItem, UAutoCompleteItem, UInputPasswordItem
    , URadioItem, UMentionsItem, UDateTimeItem, UTreeSelectItem, UTextareaItem, UInputGroupItem, USegmentedItem
    , UDatePickerItem, UMonthPickerItem, URangePickerItem, UTimePickerItem, UWeekPickerItem, UTransferItem}

export default {
    install(app) {
        Object.values(formComponents).forEach(component => {
            app.component(component.name, component);
        })
    }
}
