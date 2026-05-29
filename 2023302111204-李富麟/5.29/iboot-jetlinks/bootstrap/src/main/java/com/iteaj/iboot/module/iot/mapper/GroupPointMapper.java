package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.iot.entity.GroupPoint;

import java.util.List;

public interface GroupPointMapper extends BaseMapper<GroupPoint> {

    void batchSaveGroupPoint(List<GroupPoint> list);
}
