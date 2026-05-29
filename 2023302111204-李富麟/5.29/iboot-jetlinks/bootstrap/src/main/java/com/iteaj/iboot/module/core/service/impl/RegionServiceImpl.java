package com.iteaj.iboot.module.core.service.impl;

import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.iboot.module.core.entity.Region;
import com.iteaj.iboot.module.core.mapper.IRegionDao;
import com.iteaj.iboot.module.core.service.IRegionService;
import org.springframework.stereotype.Service;

@Service("regionServiceImpl")
public class RegionServiceImpl extends BaseServiceImpl<IRegionDao, Region> implements IRegionService {

}
