import {defineComponent} from "vue";

export default defineComponent({
    created() {
        let visibleContext = this.getVisibleContext();
        visibleContext.setVisible = this.setVisible;
        visibleContext.setLoading = this.setLoading;
        visibleContext.asyncVisible = this.openByAsync;
        visibleContext.setLoadingTip = this.setLoadingTip;
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

        /**
         * @return {VisibleContext}
         */
        getVisibleContext() {
            return this.visibleContext;
        },

        /**
         * @return {LinkContext}
         */
        getLinkContext() {
            return this.linkContext;
        }

    }
})
