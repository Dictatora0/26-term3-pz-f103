package com.iteaj.iboot.module.iot.collect.model;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.logger.LoggerFilter;
import com.iteaj.framework.logger.PushParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.iteaj.iboot.module.iot.utils.IotLogger.REPORT_NAME;
import static com.iteaj.iboot.module.iot.utils.IotLogger.SOURCE_NAME;

/**
 * 数据采集和数据上报过滤
 */
public class DataLoggerFilter implements LoggerFilter {

    private final List<IVOption> subOptions = Arrays.asList(
            new IVOption(SOURCE_NAME, SOURCE_NAME),
            new IVOption(REPORT_NAME, REPORT_NAME)
    );

    @Override
    public String type() {
        return "collect";
    }

    @Override
    public String name() {
        return SOURCE_NAME;
    }

    @Override
    public boolean filter(PushParams params) {
        return params.getType().equals(name()) || params.getType().equals(REPORT_NAME);
    }

    @Override
    public List<IVOption> subOptions() {
        return subOptions;
    }
}
