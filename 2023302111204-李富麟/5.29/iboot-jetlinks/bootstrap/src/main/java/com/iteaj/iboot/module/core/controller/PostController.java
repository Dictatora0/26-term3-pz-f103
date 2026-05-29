package com.iteaj.iboot.module.core.controller;

import java.util.List;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.ListResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.Logical;
import org.springframework.web.bind.annotation.*;
import com.iteaj.framework.security.CheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.iboot.module.core.entity.Post;
import com.iteaj.iboot.module.core.service.IPostService;
import com.iteaj.framework.BaseController;

/**
 * 岗位管理管理
 * @author iteaj
 * @since 2023-09-04
 */
@RestController
@RequestMapping("/core/post")
public class PostController extends BaseController {

    private final IPostService postService;

    public PostController(IPostService postService) {
        this.postService = postService;
    }

    /**
    * 列表查询
    * @param page 分页
    * @param entity 搜索条件
    */
    @GetMapping("/view")
    @Logger("浏览岗位管理功能")
    @CheckPermission({"core:post:view"})
    public Result<IPage<Post>> list(Page<Post> page, Post entity) {
        return this.postService.pageDetail(page, entity);
    }

    /**
     * 获取岗位列表
     * @param entity
     * @return
     */
    @GetMapping("/list")
    public ListResult<Post> list(Post entity) {
        return this.postService.list(entity);
    }

    /**
    * 获取编辑记录
    * @param id 记录id
    */
    @GetMapping("/edit")
    @CheckPermission({"core:post:edit"})
    public Result<Post> getById(Long id) {
        return this.postService.getById(id);
    }

    /**
    * 新增或者更新记录
    * @param entity
    */
    @PostMapping("/saveOrUpdate")
    @Logger("新增或者更新岗位管理记录")
    @CheckPermission(value = {"core:post:edit", "core:post:add"}, logical = Logical.OR)
    public Result<Boolean> saveOrUpdate(@RequestBody Post entity) {
        return this.postService.saveOrUpdate(entity);
    }

    /**
    * 删除指定记录
    * @param idList
    */
    @PostMapping("/del")
    @Logger("删除岗位管理记录")
    @CheckPermission({"core:post:del"})
    public Result<Boolean> remove(@RequestBody List<Long> idList) {
        return this.postService.removeByIds(idList);
    }
}

