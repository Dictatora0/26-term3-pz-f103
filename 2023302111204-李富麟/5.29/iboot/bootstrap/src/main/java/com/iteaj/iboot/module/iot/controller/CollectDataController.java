package com.iteaj.iboot.module.iot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.iot.data.RealtimeCollectDataService;
import com.iteaj.iboot.module.iot.dto.CollectDataDto;
import com.iteaj.iboot.module.iot.entity.CollectData;
import com.iteaj.iboot.module.iot.service.ICollectDataService;
import com.iteaj.iboot.module.iot.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 采集数据管理
 */
@RestController
@RequestMapping("/iot/collectData")
public class CollectDataController extends BaseController {

    private final RealtimeCollectDataService collectDataService;

    public CollectDataController(RealtimeCollectDataService collectDataService) {
        this.collectDataService = collectDataService;
    }

    /**
     * 获取点位数据
     * @param page
     * @param entity
     * @return
     */
    @Logger("访问点位数据页面")
    @GetMapping("/signal")
    public Result<Page<CollectDataDto>> signal(Page page, CollectDataDto entity) {
        entity.setCollectMode("signal");
        return collectDataService.detailOfPage(page, entity);
    }

    /**
     * 获取模型数据
     * @param page
     * @param entity
     * @return
     */
    @Logger("访问模型数据页面")
    @GetMapping("/model")
    public Result<Page<CollectDataDto>> model(Page page, CollectDataDto entity) {
        entity.setCollectMode("model");
        return collectDataService.detailOfPage(page, entity);
    }
}
