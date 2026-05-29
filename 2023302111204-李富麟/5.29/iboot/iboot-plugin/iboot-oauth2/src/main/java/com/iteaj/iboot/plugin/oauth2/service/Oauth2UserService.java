package com.iteaj.iboot.plugin.oauth2.service;

import com.iteaj.framework.IBaseService;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.iboot.plugin.oauth2.entity.Oauth2User;

public interface Oauth2UserService extends IBaseService<Oauth2User> {

    DetailResult<Oauth2User> getByClientIdAndLoginId(String clientId, Object loginId);
}
