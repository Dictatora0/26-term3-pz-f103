package com.iteaj.iboot.module.iot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.CheckPermission;
import com.iteaj.framework.security.Logical;
import com.iteaj.iboot.module.iot.entity.ModelApi;
import com.iteaj.iboot.module.iot.service.IModelApiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物模型接口管理
 * @author iteaj
 * @since 2023-09-12
 */
@RestController
@RequestMapping("/iot/modelApi")
public class ModelApiController extends BaseController {

    private final IModelApiService modelApiService;

    public ModelApiController(IModelApiService modelApiService) {
        this.modelApiService = modelApiService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览物模型接口功能")
    public Result<IPage<ModelApi>> list(Page<ModelApi> page, ModelApi entity) {
        return this.modelApiService.page(page.addOrder(OrderItem.desc("as_status"), OrderItem.desc("create_time")), entity);
    }

    /**
     * 获取接口列表
     * @param entity
     * @return
     */
    @GetMapping("list")
    public Result<List<ModelApi>> list(ModelApi entity) {
        return this.modelApiService.list(entity);
    }

    /**
     * 获取指定产品下面的接口列表详情
     * @param productId
     * @return
     */
    @GetMapping("detailsOfProductId")
    public Result<List<ModelApi>> detailsOfProductId(Long productId) {
        return modelApiService.detailsOfProductId(productId);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"iot:modelApi:edit"})
    public Result<ModelApi> getById(Long id) {
        return this.modelApiService.detailById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新物模型接口记录")
    @CheckPermission(value = {"iot:modelApi:edit", "iot:modelApi:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody ModelApi entity) {
        return this.modelApiService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除物模型接口记录")
    @CheckPermission({"iot:modelApi:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.modelApiService.removeByIds(idList);
    }

    /**
     * 修改默认状态位
     * @param productId 产品id
     * @param code 接口代码
     * @return
     */
    @Logger("切换状态接口")
    @PostMapping("asStatus")
    public Result<Boolean> asStatus(Long productId, String code) {
        return this.modelApiService.updateAsStatus(productId, code);
    }
}

