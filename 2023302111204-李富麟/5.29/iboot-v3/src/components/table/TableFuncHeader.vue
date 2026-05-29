<template>
  <a-row class="iv-table-func" align="middle">
    <ACol :xs="24" :sm="24" :md="18" :lg="18">
      <slot name="func"></slot>
    </ACol>
    <a-col style="text-align: right" :xs="0" :sm="0" :md="6" :lg="6">
      <slot name="funcRight" v-if="func">
        <ASpace style="padding-right: 12px" size="middle">
<!--          <ATooltip title="刷新">-->
<!--            <SyncOutlined :style="{fontSize: '15px'}" />-->
<!--          </ATooltip>-->
          <ATooltip placement="top" title="列设置">
            <a-dropdown v-model:open="visible" :trigger="['click']"
                        placement="bottom" :overlayStyle="{minWidth: '160px'}">
              <UnorderedListOutlined :style="{fontSize: '16px'}" />
              <template #overlay>
                <AMenu>
                  <AMenuItem>
                    <ACheckbox v-model:checked="checkedModel.checkedAll" @change="checkedModel.onChange"
                               :indeterminate="checkedModel.indeterminate" />
                    <a style="float: right; color: rgba(30,144,255,0.89)" @click="checkedModel.reset()">重置</a>
                  </AMenuItem>
                  <AMenuDivider />
                  <template v-for="column in columnsWrapper" :key="column['field']">
                    <AMenuItem>
                      <ACheckbox v-model:checked="column['checked']"
                                 @change="()=>checkedModel.childChange(column)" />
                      {{column['title']}}
                    </AMenuItem>
                  </template>
                </AMenu>
              </template>
            </a-dropdown>
          </ATooltip>
          <ATooltip placement="top" title="固定表头">
            <UIcon type="iz-icon-fixed" :style="{fontSize: '15px'}" @click="sticky"/>
          </ATooltip>
          <!--          <a-tooltip placement="top" title="全屏">-->
          <!--            <AButton type="dashed">-->
          <!--              <template #icon>-->
          <!--                <FullscreenOutlined :style="{fontSize: '16px'}" @click="fullScreen"/>-->
          <!--              </template>-->
          <!--            </AButton>-->
          <!--          </a-tooltip>-->
        </ASpace>
      </slot>
    </a-col>
  </a-row>
</template>

<script>
import {reactive, ref} from "vue";
import {TableContext} from "@/components/view/Context";
import {FullscreenOutlined, UnorderedListOutlined, SyncOutlined} from '@ant-design/icons-vue'

export default {
  name: "TableFuncHeader",
  props: {
    func: {type: Boolean, default: true},
    columns: {type: Array, required: true},
    tableContext: {type: TableContext, required: true}
  },
  components: {SyncOutlined, UnorderedListOutlined, FullscreenOutlined},
  setup(props) {
    let sticky = ref(false);
    let visible = ref(false);
    let checkedModel = reactive({
      checkedAll: true,
      indeterminate: false,
      reset() {
        checkedModel.checkedAll = true;
        checkedModel.indeterminate = false;
        columnsWrapper.forEach(columnWrapper => {
          if(!columnWrapper.checked) {
            columnWrapper.checked = true;
            checkedModel.addColumn(columnWrapper);
          }
        })
      },
      onChange: (e) => {
        if(checkedModel.checkedAll) {
          columnsWrapper.forEach(columnWrapper => {
            if(!columnWrapper.checked) {
              columnWrapper.checked = true
              checkedModel.addColumn(columnWrapper);
            }
          });
        } else {
          columnsWrapper.forEach(item => {
            item.checked = false
          });

          props.columns.splice(0, props.columns.length - 1);
        }
        checkedModel.indeterminate = false;
      },
      addColumn: (columnWrapper) => {
        if(columnWrapper.index == 0) {
          props.columns.splice(0, 0, columnWrapper.column); return;
        }

        for(let i= columnWrapper.index - 1; i>=0; i--) {
          let prevColumn = columnsWrapper[i];
          if(prevColumn.checked) {
            for(let index=0; index < props.columns.length; index++) {
              let column = props.columns[index];
              if(column == prevColumn.column) {
                props.columns.splice(index + 1, 0, columnWrapper.column); return;
              }
            }

            break;
          } else if(i == 0) {
            props.columns.splice(0, 0, columnWrapper.column); return;
          }
        }
      },
      childChange: (columnWrapper) => {
        if(!columnWrapper.checked) {
          props.columns.forEach((item, index) => {
            if(columnWrapper.field == item.field) {
              props.columns.splice(index, 1);
            }
          })
        } else {
          checkedModel.addColumn(columnWrapper);
        }
        let checkedList = columnsWrapper.filter(item => item['checked']);
        if(checkedList.length == columnsWrapper.length) {
          checkedModel.checkedAll = true;
          checkedModel.indeterminate = false;
        } else if(checkedList.length == 0) {
          checkedModel.checkedAll = false;
          checkedModel.indeterminate = false;
        } else {
          checkedModel.indeterminate = checkedList.length > 0
              && checkedList.length < columnsWrapper.length;
        }
      }
    });
    let columnsWrapper = reactive([]);

    return {checkedModel, columnsWrapper, sticky, visible}
  },
  mounted() {
    this.columns.filter((item, index) => index < this.columns.length - 1)
        .forEach((column, index) => {
          this.columnsWrapper.push({checked: true, index, field: column.field, title: column.title, column});
        });
  },
  methods: {
    sticky() {
      this.tableContext.setSticky(this.sticky = !this.sticky)
    },
  }
}
</script>

<style scoped>
.iv-table-func {
  padding: 0px 0px 8px;
}
</style>
