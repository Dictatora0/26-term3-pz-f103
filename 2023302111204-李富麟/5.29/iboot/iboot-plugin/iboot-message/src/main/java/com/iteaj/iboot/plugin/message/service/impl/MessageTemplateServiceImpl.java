package com.iteaj.iboot.plugin.message.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.message.SendTemplate;
import com.iteaj.iboot.plugin.message.entity.MessageTemplate;
import com.iteaj.iboot.plugin.message.mapper.MessageTemplateMapper;
import com.iteaj.iboot.plugin.message.service.IMessageTemplateService;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * <p>
 * 消息模板 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2024-07-09
 */
@Service
public class MessageTemplateServiceImpl extends BaseServiceImpl<MessageTemplateMapper, MessageTemplate> implements IMessageTemplateService {

    @Override
    public SendTemplate getTemplateById(Serializable key) {
        return this.getBaseMapper().getTemplateById(key);
    }

    @Override
    public ListResult<IVOption> listAccepts() {
        return new ListResult<>(this.getBaseMapper().listAccepts());
    }

    @Override
    public PageResult<Page<MessageTemplate>> detailPage(Page page, MessageTemplate entity) {
        this.getBaseMapper().detailPage(page, entity);
        return new PageResult<>(page);
    }
}
