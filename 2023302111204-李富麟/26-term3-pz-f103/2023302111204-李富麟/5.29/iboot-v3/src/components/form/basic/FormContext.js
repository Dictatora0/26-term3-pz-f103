function unmount() {

}
export function FormContext() {
    this.validate = unmount;
    this.getRules = unmount;
    this.resetFields = unmount;
    this.getEditModel = unmount;
    this.setEditModel = unmount;
    this.getInitModel = unmount;
    this.scrollToField = unmount;
    this.clearValidate = unmount;
    this.validateFields = unmount;

    /**
     * 移除某个字段
     * @type {Function}
     */
    this.removeField = unmount;
    this.initFieldValue = unmount;
    this.getFieldValue = unmount;
    this.setFieldValue = unmount;
}
