package com.iteaj.framework.spi.excel;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;

public class ExcelExportParams extends ExportParams {

    public ExcelExportParams() {
        this(null, null);
    }

    public ExcelExportParams(String title, String sheetName) {
        this(title, sheetName, ExcelType.HSSF);
    }

    public ExcelExportParams(String title, String sheetName, ExcelType type) {
        this(title, null, sheetName);
        this.setType(type);
    }

    public ExcelExportParams(String title, String secondTitle, String sheetName) {
        super(title, secondTitle, sheetName);
        this.setDictHandler(ExcelDictHandle.getInstance());
    }
}
