import {useStore} from "vuex";
import dayjs from "dayjs";
import {computed, defineComponent, inject, provide, reactive, ref, watch} from "vue";
import {initMetaValue, MetaConst} from "@/utils/MetaUtils";
import {EditableTableKey, FuncContextKey, LinkViewContextKey, ViewContextKey} from "@/utils/ProvideKeys";
import {TableContext} from "@/components/view/Context";
import CoreConsts from "@/components/CoreConsts";
import TableFuncHeader from "@/components/table/TableFuncHeader.vue";
import SysUtils from "@/utils/SysUtils";
import {msgWarn} from "@/utils/message";
const onLoaded = (data) => null;

function initSlot(column, slots) {
    let slotName = "";
    if (column.dataIndex instanceof Array) {
        slotName = column.dataIndex.join("_");
    } else if (column.dataIndex instanceof String) {
        slotName = column.dataIndex;
    } else {
        slotName = column.field.split('.').join("_");
    }

    if (slots['c_' + slotName]) {
        if(column['editable'] == true) { // 可编辑行
            column['__editableSlot'] = slots['c_' + slotName]
        } else {
            column['__slot'] = slots['c_' + slotName]
        }
    } else if (slots[slotName]) {
        if(column['editable'] == true) { // 可编辑行
            column['__editableSlot'] = slots[slotName]
        } else {
            column['__slot'] = slots[slotName]
        }
    }
}

function initOptionsLabel(column) {

    let labelField = column.labelField || MetaConst.DefaultLabelField;
    let valueField = column.valueField || MetaConst.DefaultValueField;

    if(column.options instanceof Array) {
        column['__valueLabelMap'] = {}
        column.options.forEach(item => {
            let label = item[labelField];
            let value = item[valueField];

            column['__valueLabelMap'][value] = label;
        })
    } else if(column.dict){
        useStore().getters['sys/getOptionsByDictType'](column.dict, labelField, valueField, column.onLoaded || onLoaded);
        let valueLabelMap = useStore().getters['sys/getValueLabelMap'](column.dict);
        column['__valueLabelMap'] = valueLabelMap
    } else if(column.url) {
        useStore().getters['sys/getOptionsByUrl'](column.url, labelField
            , valueField, MetaConst.DefaultLabelField, MetaConst.DefaultValueField, column.onLoaded || onLoaded);
        let valueLabelMap = useStore().getters['sys/getValueLabelMap'](column.url);
        column['__valueLabelMap'] = valueLabelMap
    }
}

function initColumnFormatterSlot(column) {
    initOptionsLabel(column);

    let formatter = column.formatter;
    if(formatter instanceof Function) {
        // 对formatter创建代理, 新增返回label值
        column.formatter = ({text, record, column}) => {
            let label = column['__valueLabelMap'][text];
            return formatter({text, record, column, label})
        }
    } else {
        column.formatter = ({text, record, column}) => {
            return column['__valueLabelMap'][text] || '';
        }
    }

    let proxySlot = column['__slot']
    if(proxySlot) {
        column['__slot'] = (args) => {
            args['value'] = column.formatter(args);
            return proxySlot(args);
        }
    } else {
        column['__slot'] = column.formatter;
    }

}

const typeFormatMaps = {datetime: 'YYYY-MM-DD HH:mm:ss', date: 'YYYY-MM-DD', month: 'MM', week: 'E', time: 'HH:mm:ss'}
function initDatetimeColumnSlot(column) {
    let formatter = column.formatter;
    if(!(formatter instanceof Function)) {
        column.formatter = ({text, record, column}) => {
            try {
                if (text) {
                    let picker = column.picker || 'datetime';
                    return dayjs(text).format(column.format || typeFormatMaps[picker]);
                } else {
                    return '';
                }
            } catch (e) {
                console.error(e);
                return ''
            }
        }
    }

    let proxySlot = column['__slot']
    if(proxySlot) {
        column['__slot'] = (args) => {
            args['value'] = column.formatter(args);
            return proxySlot(args);
        }
    } else {
        column['__slot'] = column.formatter;
    }
}
function initColumn(column, slots, ellipsis) {
    column.align = column.align || 'center';
    column.dataIndex = column.dataIndex || column.field.split(".");
    if(column['type'] == 'action') { // 编辑表格的动作列也是可以编辑的
        column['editable'] = true;
    }

    if(column.ellipsis == undefined && ellipsis) {
        column.ellipsis = ellipsis;
    }
    initSlot(column, slots); // 初始化插槽

    if(column.dict || column.url || column.options) {
        initColumnFormatterSlot(column);
    } else if(column.type == 'date') {
        initDatetimeColumnSlot(column);
    } else if(column.formatter instanceof Function) {
        column['__slot'] = column.formatter;
    }
}
function initTableColumns(columns, slots, rowKey, editRowData, initRow, ellipsis) {
    slots = {...slots};
    columns.forEach(column => {
        initColumn(column, slots, ellipsis);
        initMetaValue(column['dataIndex'], initRow, null)
        if((column['__slot'] || column['__editableSlot']) && !slots['bodyCell']) {
            slots['bodyCell'] = (args) => {
                let tempColumn = args.column;
                if(tempColumn == null) return null;

                if(tempColumn['editable'] === true) { // 此列可以编辑
                    if(editRowData.editRow) { // 如果设置了要编辑的行
                        if(editRowData.editRow[rowKey] == args.record[rowKey]) { // 当前行等于编辑行
                            args['editing'] = true;
                            return tempColumn['__editableSlot'](args);
                        } else if(tempColumn['type'] == 'action') {
                            args['editing'] = false;
                            return tempColumn['__editableSlot'](args);
                        } else {
                            args['editing'] = false;
                            return args.text;
                        }
                    } else if(tempColumn['type'] == 'action') {
                        args['editing'] = false;
                        return tempColumn['__editableSlot'](args);
                    } else {
                        args['editing'] = false;
                        return tempColumn['__slot'] ? tempColumn['__slot'](args) : args.text;
                    }
                } else {
                    return tempColumn['__slot'] ? tempColumn['__slot'](args) : args.text;
                }
            }
        }
    })
    return slots;
}

/**
 * 可编辑的表格组件
 */
export default defineComponent({
    name: 'UEditableTable',
    props: {
        sticky: {default: false},
        dataSource: {type: Array},
        ellipsis: {type: Boolean, default: true},
        func: {type: Boolean, default: true},
        size: {type: String, default: "middle"},
        bordered: {type: Boolean, default: true},
        columns: {type: Array, default: () => []},
        uid: {type: String, required: true, default: CoreConsts.DefaultTableUid},
        pagination: {
            default: () => {
                return reactive({
                    total: 0,
                    defaultPageSize: 10,
                    showQuickJumper: true,
                    showSizeChanger: true,
                    showTotal: (total, range) => `共 ${total}条`,
                    pageSizeOptions: ['10', '20', '30', '50', '100']
                })
            }
        }, // 是否分页, 不支持使用对象
    },
    components: {TableFuncHeader},
    setup(props, {slots, emit, attrs}) {
        let initRow = ref({});
        let stickyRef = ref(props.sticky);
        let selectedRows = ref([]);
        let selectedRowKeys = ref([]);
        let dataSourceRef = ref(props.dataSource);
        let {columns, expandedRowKeys} = props;
        let unfoldRowKeys = ref(expandedRowKeys);
        let loading = reactive({spinning: false, tip: '数据加载中...'});

        /**
         * @type {LinkContext}
         */
        let linkContext = inject(LinkViewContextKey);
        let tableContext = new TableContext(linkContext);
        // 可编辑的表格
        let editRowData = ref({
            editRow: null, // 在编辑的行
            editRowBack: null // 编辑的行备份
        });
        tableContext.editable = true; // 声明是可编辑组件

        // 默认重置列宽度事件
        let defResizeColumn = (width, column) => column.width = width;

        // 监控展开行数据变化
        watch(() => props['expandedRowKeys'], (newVal) => {
            unfoldRowKeys.value = newVal;
        })

        // 监听数据源变化
        watch(() => props.dataSource, (newVal) => {
            dataSourceRef.value = newVal;
        })

        watch(() => props.sticky, (newVal) => {
            stickyRef.value = newVal;
        })

        // 代理选中改变事件
        if(attrs.rowSelection) {
            let proxyChangeEvent = attrs.rowSelection.onChange;
            attrs.rowSelection.onChange = (selectedKeys, rows) => {
                selectedRows.value = rows;
                selectedRowKeys.value = selectedKeys;
                if(proxyChangeEvent instanceof Function) {
                    proxyChangeEvent(selectedKeys, rows);
                }
            }
        }

        // 分页触发事件
        if(props.pagination instanceof Object) {
            let onChange = props.pagination.onChange;
            props.pagination.onChange = (current, pageSize) => {
                if(onChange instanceof Function) {
                    onChange(current, pageSize);
                }
                tableContext.pageChange(current, pageSize);
            }

            // 每页条数改变事件
            let onShowSizeChange = props.pagination.onShowSizeChange;
            props.pagination.onShowSizeChange = (current, pageSize) => {
                if(onShowSizeChange instanceof Function) {
                    onShowSizeChange(current, pageSize);
                }

                tableContext.sizeChange(current, pageSize);
            }
        }

        // 更新列改变
        let slotsRef = ref(initTableColumns(columns, slots, attrs['rowKey'], editRowData.value, initRow.value, props.ellipsis));
        watch(() => props.columns, () => {
            initRow.value = {};
            slotsRef.value = initTableColumns(props.columns, slots, attrs['rowKey'], editRowData.value, initRow.value, props.ellipsis)
        })
        let isEditRow = (record) => {
            return editRowData.value.editRow && editRowData.value.editRow[attrs['rowKey']] == record[attrs['rowKey']]
        }
        let setLoading = (status, tip) => {
            loading.spinning = status;
            loading.tip = tip || CoreConsts.TableSpinLoadingTip;
        }
        let setSticky = (status) => stickyRef.value = status
        let setDataSource = (ds) => {
            dataSourceRef.value = ds;
            editRowData.value.editRow = editRowData.value.editRowBack = null;
            emit("update:source", ds)
        }
        let setEditRow = (row) => {
            if(!row) {
                editRowData.value.editRow = editRowData.value.editRowBack = row;
            } else if(!editRowData.value.editRow) {
                editRowData.value.editRowBack = SysUtils.clone(row);
                emit("edit", row);
                editRowData.value.editRow = row;
                row['__editing'] = () => true; // 声明此行在编辑中
            } else {
                msgWarn("请先保存当前在编辑的行").then();
            }
        }
        let getEditRow = () => {
            return editRowData.value.editRow;
        }
        // 新增编辑行
        let addEditableRow = () => {
            if(!editRowData.value.editRow) {
                let newRow = SysUtils.clone(initRow.value);
                newRow['__editing'] = () => true;
                editRowData.value.editRow = newRow;
                emit("edit", newRow);
                if(dataSourceRef.value instanceof Array) {
                    dataSourceRef.value.push(newRow);
                } else {
                    dataSourceRef.value = [newRow];
                }

            } else {
                msgWarn("请先保存当前在编辑的行").then();
            }
        }
        // 删除编辑行
        let delEditableRow = () => {
            if(editRowData.value.editRow && !editRowData.value.editRow[attrs['rowKey']]) {
                if(dataSourceRef.value instanceof Array) {
                    for (let i = 0; i < dataSourceRef.value.length; i++) {
                        if(dataSourceRef.value[i] == editRowData.value.editRow) {
                            dataSourceRef.value.splice(i, 1); break;
                        }
                    }
                }

                editRowData.value.editRow = editRowData.value.editRowBack = null;
                return true;
            }

            return false;
        }
        // 取消编辑行
        let cancelEditableRow = () => {
            if(editRowData.value.editRow) {
                // 新增的行
                if(!editRowData.value.editRow[attrs['rowKey']]) {
                    editRowData.value.editRow = editRowData.value.editRowBack = null;
                    if(dataSourceRef.value instanceof Array) {
                        dataSourceRef.value.splice(dataSourceRef.value.length - 1, 1)
                    }
                } else { // 编辑的行
                    delete editRowData.value.editRow['__editing']; // 删除在编辑声明
                    // 恢复原先的数据
                    Object.assign(editRowData.value.editRow, editRowData.value.editRowBack)
                    editRowData.value.editRow = editRowData.value.editRowBack = null;
                }
            }
        }
        // 保存编辑行
        let submitEditableRow = () => {
            return Promise.resolve(editRowData.value.editRow);
        }
        let setTotalRows = (total) => {
            if(props.pagination instanceof Object) {
                props.pagination.total = total;
            }
        }

        if(linkContext) {
            tableContext.uid = props.uid;
            linkContext.addChildrenContext(tableContext);

            tableContext['setSticky'] = setSticky
            tableContext['setLoading'] = setLoading;
            tableContext['setEditRow'] = setEditRow;
            tableContext['getEditRow'] = getEditRow;
            tableContext['setTotalRows'] = setTotalRows;
            tableContext['setDataSource'] = setDataSource;
            tableContext['addEditableRow'] = addEditableRow;
            tableContext['delEditableRow'] = delEditableRow;
            tableContext['submitEditableRow'] = submitEditableRow;
            tableContext['cancelEditableRow'] = cancelEditableRow;

            tableContext['getRowKey'] = () => attrs['rowKey'];
            tableContext['getColumns'] = () => props.columns;

            if(props.pagination instanceof Object) { // 如果需要分页
                tableContext['currentPage'] = 1;
                tableContext['pageSize'] = props.pagination.defaultPageSize
            }
        }

        provide(FuncContextKey, tableContext);
        provide(EditableTableKey, true); // 声明是可编辑表

        // 暴露TableContext
        if(attrs['onUpdate:context'] instanceof Function) {
            attrs['onUpdate:context'](tableContext);
        }
        return {slotsRef, selectedRows, selectedRowKeys, editRowData
            , unfoldRowKeys, loading, dataSourceRef, stickyRef, setSticky, isEditRow
            , setDataSource, setLoading, setTotalRows, defResizeColumn, tableContext}
    },
    created() {
        this.tableContext['expanded'] = this.expanded;
        this.tableContext['getSelectedRows'] = this.getSelectedRows;
        this.tableContext['getSelectedRowKeys'] = this.getSelectedRowKeys;
    },
    render() {
        if(this.stickyRef || this.stickyRef === '') {
            if(typeof this.stickyRef !== 'object') {
                this.stickyRef = {offsetHeader: -16}
            }
        }
        // todo 考虑使用watch监听新增和删除列
        // initTableColumns(this.columns, this.slotsRef, this.$attrs['rowKey'], this.editRowData.editRow);

        let onResizeColumn = this.$attrs['onResizeColumn'] || this.defResizeColumn;
        return (
            <div class="iv-editable-table">
                {this.$slots['func'] ? <TableFuncHeader columns={this.columns} func={this.func} tableContext={this.tableContext} v-slots={this.$slots}/> : ''}
                <ATable {...this.$attrs} columns={this.columns} ref="ATableRef" sticky={this.stickyRef}
                        loading={this.loading} dataSource={this.dataSourceRef} size={this.size} bordered={this.bordered}
                        pagination={this.pagination} v-slots={this.slotsRef} expandedRowKeys={this.unfoldRowKeys}
                        onExpandedRowsChange={this.expandedRowsChange} onResizeColumn={onResizeColumn} customRow={(row) => {
                    return {
                        onClick: (e) => this.$emit('rowClick', {e, row}),       // 点击行
                        onDblclick: (e) => this.$emit('rowDblclick', {e, row}), // 行双击
                    }
                }}>
                </ATable>
            </div>)
    },
    methods: {
        getPagination() {
            return this.$props.pagination ? this.page : false;
        },

        /**
         * @returns {UnwrapRef<TableContext>}
         */
        getTableContext() {
            return this.tableContext;
        },
        /**
         * 展开/折叠
         */
        expanded(expandedRows) {
            if(expandedRows) {
                this.unfoldRowKeys = expandedRows;
                return; // 展开传入的行
            }

            if(this.unfoldRowKeys && this
                .unfoldRowKeys.length > 0) {
                this.unfoldRowKeys = [];
            } else {
                this.unfoldRowKeys = this.getAllKeys();
            }
        },

        /**
         * 展开行改变
         * @param expandedRows
         */
        expandedRowsChange(expandedRows) {
            this.unfoldRowKeys = expandedRows;
        },
        /**
         * 获取所有的可展开keys
         * @return {null|[]}
         */
        getAllKeys() {
            let dataSource = this.dataSourceRef;
            if(dataSource instanceof Array) {
                let keys = [];
                let doGetAllKeys = (children) => {
                    children.forEach(item => {
                        if(item.children instanceof Array) {
                            keys.push(item[this.$attrs.rowKey])
                            doGetAllKeys(item.children)
                        }
                    })

                    return keys;
                }

                return doGetAllKeys(dataSource);
            }

            return null;
        },

        getDataSource() {
            return this.$refs['ATableRef'].dataSource;
        },

        getSelectedRows() {
            return this.selectedRows;
        },

        getSelectedRowKeys() {
            return this.selectedRowKeys;
        },

        setSelectedRowKeys(selectedRowKeys) {
            this.selectedRowKeys = selectedRowKeys;
        }
    }
})

export {initTableColumns}
