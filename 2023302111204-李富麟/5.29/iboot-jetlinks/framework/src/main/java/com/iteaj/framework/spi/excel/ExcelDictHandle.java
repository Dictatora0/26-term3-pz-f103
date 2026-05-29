package com.iteaj.framework.spi.excel;

import cn.afterturn.easypoi.handler.inter.IExcelDictHandler;

import java.io.Serializable;

public class ExcelDictHandle implements IExcelDictHandler {

    private final IExcelService excelService;

    private static ExcelDictHandle instance;

    public ExcelDictHandle(IExcelService excelService) {
        ExcelDictHandle.instance = this;
        this.excelService = excelService;
    }

    public static ExcelDictHandle getInstance() {
        return instance;
    }

    @Override
    public String toName(String dict, Object obj, String name, Object value) {
        if(dict.equals("org_id")) {
            return excelService.toOrgName((Serializable) value);
        }
        return excelService.toDictName(dict, (Serializable) value);
    }

    @Override
    public String toValue(String dict, Object obj, String name, Object value) {
        if(dict.equals("org_id")) {
            Serializable orgValue = excelService.toOrgValue((String) value);
            return orgValue == null ? null : orgValue.toString();
        }
        return (String) excelService.toDictValue(dict, (String) value);
    }
}
