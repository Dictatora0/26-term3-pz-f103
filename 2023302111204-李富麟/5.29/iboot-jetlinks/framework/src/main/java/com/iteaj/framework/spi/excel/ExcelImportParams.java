package com.iteaj.framework.spi.excel;

import cn.afterturn.easypoi.excel.entity.ImportParams;

public class ExcelImportParams extends ImportParams {

    public ExcelImportParams() {
        setDictHandler(ExcelDictHandle.getInstance());
    }
}
