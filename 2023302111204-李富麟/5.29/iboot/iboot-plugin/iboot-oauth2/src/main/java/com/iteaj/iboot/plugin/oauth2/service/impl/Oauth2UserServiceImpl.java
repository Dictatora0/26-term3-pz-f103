package com.iteaj.iboot.plugin.oauth2.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.iboot.plugin.oauth2.entity.Oauth2User;
import com.iteaj.iboot.plugin.oauth2.mapper.Oauth2UserMapper;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2UserService;
import org.springframework.stereotype.Service;

@Service
public class Oauth2UserServiceImpl extends BaseServiceImpl<Oauth2UserMapper, Oauth2User> implements Oauth2UserService {

    @Override
    public DetailResult<Oauth2User> getByClientIdAndLoginId(String clientId, Object loginId) {
        return getOne(Wrappers.<Oauth2User>lambdaQuery()
                .eq(Oauth2User::getClientId, clientId)
                .eq(Oauth2User::getLoginId, loginId));
    }
}
