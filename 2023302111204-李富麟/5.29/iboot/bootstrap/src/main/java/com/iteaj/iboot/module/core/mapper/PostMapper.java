package com.iteaj.iboot.module.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.core.entity.Post;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 岗位管理 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2023-09-04
 */
public interface PostMapper extends BaseMapper<Post> {

    IPage<Post> pageDetail(Page<Post> page, Post entity);
}
