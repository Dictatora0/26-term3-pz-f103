package com.iteaj.iboot.module.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.PageResult;
import com.iteaj.framework.result.Result;
import com.iteaj.iboot.module.core.entity.Post;
import com.iteaj.framework.IBaseService;

/**
 * <p>
 * 岗位管理 服务类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-04
 */
public interface IPostService extends IBaseService<Post> {

    PageResult<IPage<Post>> pageDetail(Page<Post> page, Post entity);
}
