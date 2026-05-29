import {defineComponent} from "vue";

export default defineComponent({
    created() {
        let editContext = this.getEditContext();
        editContext.setVisible = this.setVisible;
        editContext.setLoading = this.setLoading;
        editContext.asyncVisible = this.openByAsync;
        editContext.setLoadingTip = this.setLoadingTip;
        editContext.getFormContext = this.getFormContext;
    },
    unmounted() {

    },
    mounted() {
        this.formRef = this.$refs['iemFormRef'];
    },
    methods: {
        setVisible(visible) {
            this.visible = visible;
        },

        setLoading(status, tip) {
            this.spinning = status;
            this.spinTip = tip || "";
        },

        setLoadingTip(tip) {
            this.spinTip = tip;
        },

        switchActive(visible) {
            this.visible = visible;
        },

        switchSpinning(spinning) {
            this.spinning = spinning;
        },

        /**
         * 异步打开弹框, 表单初始化完成后会出发编辑事件
         * @param row 编辑的行 非必填
         * @param isResetToInit {Boolean} 是否重置编辑模型 非必填
         * @return {Promise<unknown>}
         */
        openByAsync(row, isResetToInit) {
            this.visible = true;
            // 触发Modal或者Drawer打开事件
            this.$emit("open", row);
            // 暴露编辑的数据
            this.$emit('update:source', row);
            return new Promise((resolve, reject) => {
                return resolve(this);
            })
        },

        getEditModel() {
            return this.$refs['iemFormRef'].getEditModel();
        },

        /**
         * @returns {FormContext|null}
         */
        getFormContext() {
            // 可能出现获取的时候form还未初始化, 自行判断
            return this.formRef.getFormContext();
        },

        /**
         * @return {EditContext}
         */
        getEditContext() {
            return this.editContext;
        },

        /**
         * @return {LinkContext}
         */
        getLinkContext() {
            return this.linkContext;
        }

    }
})
