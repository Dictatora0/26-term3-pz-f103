package com.iteaj.iboot.plugin.message.service.impl;

import com.iteaj.framework.result.BooleanResult;
import com.iteaj.iboot.plugin.message.entity.MessageSource;
import com.iteaj.iboot.plugin.message.mapper.MessageSourceMapper;
import com.iteaj.iboot.plugin.message.service.IMessageSourceService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息源 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-07-30
 */
@Service
public class MessageSourceServiceImpl extends BaseServiceImpl<MessageSourceMapper, MessageSource> implements IMessageSourceService {

    @Override
    public BooleanResult updateById(MessageSource entity) {
        return super.updateById(entity);
    }
}
