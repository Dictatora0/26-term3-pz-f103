package com.iteaj.iboot.module.iot.collect.store;

import com.iteaj.iboot.module.iot.consts.IotConsts;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iot.tools.db.rdbms.DefaultRdbmsSqlManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 关系型数据库存储
 */
public class RDBMStoreAction extends StoreAction {

    @Autowired
    private DefaultRdbmsSqlManager rdbmsSqlManager;

    @Override
    public String getName() {
        return IotConsts.STORE_ACTION_RDBMS;
    }

    @Override
    public String getDesc() {
        return "关系型数据库";
    }

    @Override
    public void configValidate(Map<String, Object> jsonConfig) {

    }

    @Override
    public void store(CollectDetail detail, List<CollectData> data) {
        rdbmsSqlManager.batchInsert(CollectData.class, Arrays.asList(data.toArray()));
    }
}
