package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.iboot.module.core.entity.Region;
import com.iteaj.iboot.module.core.service.IRegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行政区管理
 * @author iteaj
 * @since 1.0
 */
@RestController
@RequestMapping("/core/region")
public class RegionController extends BaseController {

    private final IRegionService regionService;

    public RegionController(IRegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * 获取列表
     * @param region
     * @return
     */
    @Logger("查询行政区列表")
    @GetMapping("/view")
    @CheckPermission("core:region:view")
    public Result<List<Region>> view(Region region) {
        return this.regionService.list(region);
    }

    /**
     * 获取指定记录
     * @param id
     * @return
     */
    @GetMapping("/getById")
    public Result<Region> getById(Long id) {
        return this.regionService.getById(id);
    }
}
