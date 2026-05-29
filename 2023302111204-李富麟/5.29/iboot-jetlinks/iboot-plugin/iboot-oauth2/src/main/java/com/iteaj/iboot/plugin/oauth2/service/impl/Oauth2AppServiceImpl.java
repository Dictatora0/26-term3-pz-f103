package com.iteaj.iboot.plugin.oauth2.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.iboot.plugin.oauth2.entity.Oauth2App;
import com.iteaj.iboot.plugin.oauth2.mapper.Oauth2AppMapper;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2AppService;
import org.springframework.stereotype.Service;

@Service
public class Oauth2AppServiceImpl extends BaseServiceImpl<Oauth2AppMapper, Oauth2App> implements Oauth2AppService {

    @Override
    public DetailResult<Oauth2App> getByClientId(String clientId) {
        return this.getOne(Wrappers.<Oauth2App>lambdaQuery()
                .eq(Oauth2App::getClientId, clientId));
    }
}
