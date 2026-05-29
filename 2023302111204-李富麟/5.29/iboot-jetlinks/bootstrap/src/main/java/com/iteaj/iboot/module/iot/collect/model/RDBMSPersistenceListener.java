package com.iteaj.iboot.module.iot.collect.model;

import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iot.tools.db.rdbms.DefaultRdbmsSqlManager;

import java.util.List;

public class RDBMSPersistenceListener implements DataPersistenceListener {

    private final DefaultRdbmsSqlManager rdbmsSqlManager;

    public RDBMSPersistenceListener(DefaultRdbmsSqlManager rdbmsSqlManager) {
        this.rdbmsSqlManager = rdbmsSqlManager;
    }

    @Override
    public void persistence(List collectData) {
        rdbmsSqlManager.batchInsert(CollectData.class, collectData);
    }
}
