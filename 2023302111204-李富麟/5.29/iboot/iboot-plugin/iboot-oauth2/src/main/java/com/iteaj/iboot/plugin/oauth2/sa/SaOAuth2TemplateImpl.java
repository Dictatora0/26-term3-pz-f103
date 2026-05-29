package com.iteaj.iboot.plugin.oauth2.sa;

import cn.dev33.satoken.oauth2.logic.SaOAuth2Template;
import cn.dev33.satoken.oauth2.model.SaClientModel;
import cn.hutool.core.bean.BeanUtil;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2AppService;
import com.iteaj.iboot.plugin.oauth2.service.Oauth2UserService;

public class SaOAuth2TemplateImpl extends SaOAuth2Template {

    private final Oauth2AppService oauth2AppService;
    private final Oauth2UserService oauth2UserService;

    public SaOAuth2TemplateImpl(Oauth2AppService oauth2AppService
            , Oauth2UserService oauth2UserService) {
        this.oauth2AppService = oauth2AppService;
        this.oauth2UserService = oauth2UserService;
    }

    // 根据 id 获取 Client 信息
    @Override
    public SaClientModel getClientModel(String clientId) {
        return oauth2AppService.getByClientId(clientId).ofNullable().map(item -> {
            SaClientModel clientModel = new SaClientModel();
            BeanUtil.copyProperties(item, clientModel);
            return clientModel.setIsAutoMode(true);
        }).orElse(null);
    }

    // 根据ClientId 和 LoginId 获取openid
    @Override
    public String getOpenid(String clientId, Object loginId) {
        return oauth2UserService.getByClientIdAndLoginId(clientId, loginId)
                .ofNullable()
                .map(item -> item.getOpenid())
                .orElse(null);
    }
}
