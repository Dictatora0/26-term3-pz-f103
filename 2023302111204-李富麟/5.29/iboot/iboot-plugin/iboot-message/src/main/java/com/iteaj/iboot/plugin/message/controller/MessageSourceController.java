package com.iteaj.iboot.plugin.message.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.IVOption;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.framework.spi.message.MessageManager;
import com.iteaj.framework.spi.message.MessageService;
import com.iteaj.framework.spi.message.SendModel;
import com.iteaj.iboot.plugin.message.entity.MessageSource;
import com.iteaj.iboot.plugin.message.service.IMessageSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息源管理
 *
 * @author iteaj
 * @since 2023-07-30
 */
@RestController
@RequestMapping("/core/messageSource")
public class MessageSourceController extends BaseController {

    private final MessageManager messageManager;
    private final IMessageSourceService messageSourceService;

    public MessageSourceController(@Autowired(required = false) MessageManager messageManager
            , IMessageSourceService messageSourceService) {
        this.messageManager = messageManager;
        this.messageSourceService = messageSourceService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @Logger("浏览消息源功能")
    @GetMapping("/view")
    @CheckPermission({"core:messageSource:view"})
    public Result<IPage<MessageSource>> list(Page<MessageSource> page, MessageSource entity) {
        return this.messageSourceService.page(page, entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"core:messageSource:edit"})
    public Result<MessageSource> getById(Long id) {
        return this.messageSourceService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @Logger("新增或者更新消息源记录")
    @PostMapping("/saveOrUpdate")
    @CheckPermission(value = {"core:messageSource:edit", "core:messageSource:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody MessageSource entity) {
        if(entity.getId() != null) {
            this.messageSourceService.getById(entity.getId()).ifPresent(record -> {
                // 移除已经创建的通道配置
                MessageService service = messageManager.getService(record.getType(), record.getChannel());
                if(service != null) {
                    service.build(entity.getConfig());
                    messageManager.registerOrUpdateDefault(service);
                } else {
                    throw new ServiceException("没有找到对应的通道服务["+record.getChannel()+"]");
                }
            });
        }

        return this.messageSourceService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @Logger("删除消息源记录")
    @PostMapping("/del")
    @CheckPermission({"core:messageSource:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        this.messageSourceService.listByIds(idList).ifPresent(item -> {
            item.forEach(record -> {
                // 移除已经创建的通道配置
                messageManager.remove(record.getType(), record.getChannel());
            });
        });

        return this.messageSourceService.removeByIds(idList);
    }

    /**
     * 消息调试
     * @param id
     */
    @Logger("消息源调试")
    @PostMapping("/debug/{id}")
    @CheckPermission({"core:messageSource:debug"})
    public Result<Boolean> debug(@PathVariable Long id, @RequestBody SendModel model) {
        this.messageSourceService.getById(id).ifPresent(source -> {
            MessageService service = this.messageManager.getDefault(source.getType());
            if(service != null) {
                model.setAccept(Arrays.asList("18059222381", "15959971652"));
                LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
                hashMap.put("device", "ADC3623547A");
                hashMap.put("value", "36");
                model.setTemplateVars(hashMap);
                service.send(model);
            }
        });

        return success();
    }

    /**
     * 获取平台支持的消息类型
     * @return
     */
    @GetMapping("/types")
    public Result<List<IVOption>> types() {
        if(messageManager == null) {
            return success(Collections.emptyList());
        } else {
            List<IVOption> options = this.messageManager.getTypes().stream().map(type -> {
                Set<MessageService> services = this.messageManager.getServices(type);
                MessageService messageService = services.stream().findFirst().get();
                IVOption ivOption = new IVOption(messageService.getName(), messageService.getType());
                services.forEach(channel -> ivOption.addChildren(channel.getChannelName()
                        , channel.getChannelId(), channel.getConfigParams()));

                return ivOption;
            }).distinct().collect(Collectors.toList());

            return success(options);
        }
    }
}

