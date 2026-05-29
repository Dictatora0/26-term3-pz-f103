package com.iteaj.iboot.plugin.message.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.spi.message.SendTemplate;
import com.iteaj.iboot.plugin.message.entity.MessageTemplate;
import com.iteaj.framework.IBaseService;

import java.io.Serializable;

/**
 * <p>
 * 消息模板 服务类
 * </p>
 *
 * @author iteaj
 * @since 2024-07-09
 */
public interface IMessageTemplateService extends IBaseService<MessageTemplate> {

    SendTemplate getTemplateById(Serializable key);

    ListResult<IVOption> listAccepts();

    PageResult<Page<MessageTemplate>> detailPage(Page page, MessageTemplate entity);
}
