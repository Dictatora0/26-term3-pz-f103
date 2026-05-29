package com.iteaj.iboot.module.iot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.iot.collect.CollectException;
import com.iteaj.iboot.module.iot.collect.store.StoreAction;
import com.iteaj.iboot.module.iot.collect.store.StoreActionFactory;
import com.iteaj.iboot.module.iot.entity.CollectDetail;
import com.iteaj.iboot.module.iot.service.ICollectDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 采集详情管理
 */
@RestController
@RequestMapping("/iot/collectDetail")
public class CollectDetailController extends BaseController {

    @Autowired
    private ICollectDetailService collectDetailService;

    /**
     * 获取采集详情
     * @param page
     * @param entity
     * @return
     */
    @GetMapping("/details")
    public Result<Page<CollectDetail>> details(Page page, CollectDetail entity) {
        return collectDetailService.detailPage(page, entity);
    }

    /**
     * 新增或者更新记录
     * @param id
     */
    @GetMapping("/edit")
    public Result<CollectDetail> edit(Long id) {
        return this.collectDetailService.detailById(id);
    }

    /**
     * 新增或者更新记录
     * @param entity
     */
    @PostMapping("/saveOrUpdate")
    public Result<Boolean> save(@RequestBody CollectDetail entity) {
        StoreAction storeAction = StoreActionFactory.getInstance().get(entity.getStoreAction());
        if(storeAction != null) {
            try {
                // 做配置校验
                storeAction.configValidate(entity.resolveConfig());
            } catch (CollectException e) {
                return fail(e.getMessage());
            }
        }

        return this.collectDetailService.saveOrUpdate(entity);
    }

    /**
     * 删除指定记录
     * @param idList
     */
    @PostMapping("/del")
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.collectDetailService.removeByIds(idList);
    }
}
