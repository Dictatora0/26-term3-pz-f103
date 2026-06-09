import {computed, defineComponent, inject, mergeProps, reactive} from "vue";
import {RowContextKey} from "@/utils/ProvideKeys";

export default defineComponent({
    props: ['name', 'label', 'labelCol', 'wrapperCol', 'colon', 'hasFeedback'
        , 'help', 'labelAlign', 'validateStatus', 'validateFirst', 'validateTrigger'
        , 'extra', 'autoLink', 'required', 'field', 'class', 'style'],
    data() {
        return {
            attrs: null,
            namePath: [],
            colConfig: inject(RowContextKey),
            /**
             * @type {FormContext}
             */
            formContext: inject('formContext'),
        }
    },
    created() {
        if(this.$attrs.span) { // 以span为主
            this.colConfig = {span: this.$attrs.span};
        }

        if(this.name) {
            if(!(this.name instanceof Array)) {
                this.namePath = [this.name];
                console.warn(`[name]必须是数组或者用[field]替代[name]]`)
            } else {
                this.namePath = this.name;
            }
        } else if(this.field){
            this.namePath = this.field.split('.');
        } else {
            return console.error(`表单组件未设置属性[field or name]`)
        }

        if(this.formContext && this.namePath) {
            let initModel = inject('initModel');
            let defaultValue = this.$attrs.defaultValue;
            initModel(this.namePath, this.getDefaultValue(defaultValue));
        }
    },
    methods: {
        getDefaultValue(defaultValue) {
            return defaultValue != undefined ? defaultValue : null;
        },
        getFormItemProps() {
            return {...this.$props, name: this.namePath, style: null, class: null, onChange: null, onSelect: null}
        },
        getColProps() {
            return {...this.$attrs, ...this.colConfig};
        },
        getFormAttrs(options) {
            if(this.attrs) {
                // let mergeProps1 = mergeProps(this.attrs, this.$attrs, options);
                let assign = Object.assign(this.attrs, this.$attrs, options || {});
                return assign;
            }

            this.attrs = this.$attrs;
            if(this.formContext && !this.attrs['onUpdate:value']) {
                let value = computed(() => {
                    return this.formContext.getFieldValue(this.namePath)
                });
                this.attrs = {...this.$attrs, value: value, 'onUpdate:value': (val) => {
                        this.formContext.setFieldValue(this.namePath, val);
                    }, ...options, class: this.$props.class, style: this.$props.style}
            }
            if("isOption" in this) {
                return this.proxyOptionsEvent(this.attrs);
            } else {
                return this.attrs;
            }
        },
        getCheckedAttrs() {
            if(this.attrs) {
                return this.attrs;
            }

            this.attrs = this.$attrs;
            let value = computed(() => this.formContext.getFieldValue(this.namePath));
            if(this.formContext && !this.attrs['onUpdate:checked']) {
                this.attrs = reactive(mergeProps(this.$attrs, {
                    checked: value, 'onUpdate:checked': (val) => {
                        this.formContext.setFieldValue(this.namePath, val);
                    }
                }));
            }

            return this.proxyOptionsEvent(this.attrs);
        },
        getTransferAttrs(options) {
            if(this.attrs) {
                return mergeProps(this.attrs, this.$attrs, options);
            }
            this.attrs = this.$attrs;
            if(this.formContext && !this.attrs['onUpdate:targetKeys']) {
                let value = computed(() => {
                    return this.formContext.getFieldValue(this.namePath) || []
                });
                this.attrs = {...this.$attrs, targetKeys: value, 'onUpdate:targetKeys': (val) => {
                        this.formContext.setFieldValue(this.namePath, val);
                    }, ...options, class: this.$props.class, style: this.$props.style}
            }

            return this.proxyOptionsEvent(this.attrs);
        },
    }
})
