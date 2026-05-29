package com.iteaj.iboot.module.core.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.logger.Logger;
import com.iteaj.iboot.module.core.entity.DictData;
import com.iteaj.iboot.module.core.service.IDictDataService;
import com.iteaj.framework.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据管理
 * @author iteaj
 */
@RestController
@RequestMapping("/core/dictData")
public class DictDataController extends BaseController {

    private final IDictDataService dictDataService;

    public DictDataController(IDictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    /**
     * 获取字典数据列表
     * @param dictData
     * @return
     */
    @Logger("查询字典数据")
    @GetMapping("/view")
    public Result<List<DictData>> list(DictData dictData) {
        return dictDataService.list(Wrappers.lambdaQuery(dictData).orderByAsc(DictData::getSort));
    }

    /**
     * 获取指定类型的字典数据
     * @param type 字典类型
     * @return
     */
    @GetMapping("/listByType")
    public Result<List<DictData>> listByDictType(String type) {
        return dictDataService.selectByType(type);
    }

    /**
     * 新增一条字典数据记录
     * @param data
     * @return
     */
    @Logger("新增字典数据")
    @PostMapping("/add")
    public Result add(@RequestBody DictData data) {
        return dictDataService.save(data);
    }

    /**
     * 删除字典数据
     * @param list
     * @return
     */
    @Logger("删除字典数据记录")
    @PostMapping("/del")
    public Result del(@RequestBody List<Long> list) {
        return dictDataService.removeByIds(list);
    }

    /**
     * 获取字典数据详情
     * @param id
     * @return
     */
    @GetMapping("/edit")
    public Result<DictData> edit(Long id) {
        return dictDataService.getById(id);
    }

    /**
     * 修改字典数据记录
     * @param data
     * @return
     */
    @Logger("修改字典数据记录")
    @PostMapping("/edit")
    public Result edit(@RequestBody DictData data) {
        return dictDataService.updateById(data);
    }
}
