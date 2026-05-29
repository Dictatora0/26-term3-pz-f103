package com.iteaj.iboot.module.iot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.spi.iot.consts.CollectStatus;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class EMapDetailDto {

    /**
     * 控制api
     */
    private ModelApi ctrlApi;

    /**
     * 控制api的属性值
     */
    private Object ctrlAttrValue;

    /**
     * 控制api的属性选项
     */
    private List<IVOption> ctrlAttrs = new ArrayList<>();

    /**
     * 物模型功能接口
     */
    private List<ModelApi> funcApis;

    /**
     * 物模型事件接口
     */
    private List<ModelApi> eventApis;

    /**
     * 模型属性列表
     */
    private List<ModelAttr> attrs = new ArrayList<>();

    /**
     * 告警记录
     */
    private List<Warn> warns = new ArrayList<>();

    /**
     * 接口调试参数
     */
    private Map<String, List<IVOption>> apiCodeDebugMap;

    public EMapDetailDto addAttr(String field, String name, String unit) {
        return addAttr(field, name, unit, null, null, CollectStatus.Success);
    }

    public EMapDetailDto addAttr(String field, String name, String unit, Object value, Date collectTime, CollectStatus status) {
        attrs.add(new ModelAttr(field, name, unit, value, collectTime, status));
        return this;
    }

    public EMapDetailDto addCtrlAttr(String name, String value) {
        ctrlAttrs.add(new IVOption(name, value));
        return this;
    }

    @Getter
    @Setter
    public class ModelAttr {

        /**
         * 属性代码
         */
        private String field;

        /**
         * 属性名称
         */
        private String name;

        /**
         * 单位
         */
        private String unit;

        /**
         * 实时值
         */
        private Object value;

        /**
         * 采集时间
         */
        @JsonFormat(pattern = "MM-dd HH:mm:ss")
        private Date collectTime;

        private CollectStatus status;

        public ModelAttr(String field, String name, String unit, Object value
                , Date collectTime, CollectStatus status) {
            this.field = field;
            this.name = name;
            this.unit = unit;
            this.value = value;
            this.status = status;
            this.collectTime = collectTime;
        }
    }

    public void addApiDebugParam(String apiCode, List<IVOption> params) {
        if(apiCodeDebugMap == null) {
            apiCodeDebugMap = new HashMap<>();
        }

        apiCodeDebugMap.put(apiCode, params);
    }

    @Getter
    @Setter
    public class Warn {

        /**
         * 告警等级
         */
        private String grade;

        /**
         * 告警内容
         */
        private String content;

        @JsonFormat(pattern = "yy-MM-dd HH:mm:ss")
        private Date warnTime;

        public Warn(String grade, String content, Date warnTime) {
            this.grade = grade;
            this.content = content;
            this.warnTime = warnTime;
        }
    }
}
