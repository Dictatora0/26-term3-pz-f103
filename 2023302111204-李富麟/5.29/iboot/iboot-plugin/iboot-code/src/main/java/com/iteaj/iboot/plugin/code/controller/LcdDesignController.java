package com.iteaj.iboot.plugin.code.controller;

import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.plugin.code.dto.TableOptionsDto;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.plugin.code.entity.LcdDesign;
import com.iteaj.iboot.plugin.code.service.ILcdDesignService;
import com.iteaj.framework.BaseController;

/**
 * <p>
 * 低代码功能设计管理
 * </p>
 *
 * @author iteaj
 * @since 2021-10-22
 */
@RestController
@RequestMapping("/lcd/design")
public class LcdDesignController extends BaseController {

    private final ILcdDesignService lcdDesignService;

    public LcdDesignController(ILcdDesignService lcdDesignService) {
        this.lcdDesignService = lcdDesignService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    public Result<IPage<LcdDesign>> list(Page<LcdDesign> page, LcdDesign entity) {
        return this.lcdDesignService.page(page, Wrappers.query(entity)
                .select("id", "name", "table_name", "comment", "create_time", "update_time", "table_exists"));
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    public Result<LcdDesign> getEditDetail(Long id) {
        return this.lcdDesignService.getById(id);
    }

    /**
    * 修改记录
    * @param entity
    */
    @PostMapping("/edit")
    public Result<Boolean> edit(@RequestBody LcdDesign entity) {
        return this.lcdDesignService.updateById(entity);
    }

    /**
    * 新增记录
    * @param entity
    */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody LcdDesign entity) {
        return this.lcdDesignService.save(entity).of()
                .map(item -> success(entity.getId())).get();
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.lcdDesignService.removeByIds(idList);
    }

    /**
     * 外键列表
     * @param id 不包含的表
     * @return
     */
    @GetMapping("foreign")
    public Result<List<TableOptionsDto>> foreign(Long id) {
        List<TableOptionsDto> tableOptionsDtos = this.lcdDesignService.listForeign(id);
        return success(tableOptionsDtos);
    }

    /**
     * 外键列
     * @param tableName
     * @return
     */
    @GetMapping("columns")
    public Result<List<TableOptionsDto>> columns(String tableName) {
        List<TableOptionsDto> tableOptionsDtos = this.lcdDesignService.listColumns(tableName);
        return success(tableOptionsDtos);
    }
}

