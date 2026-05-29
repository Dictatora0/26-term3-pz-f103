package com.iteaj.iboot.module.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.core.entity.Post;
import com.iteaj.iboot.module.core.mapper.PostMapper;
import com.iteaj.iboot.module.core.service.IPostService;
import com.iteaj.framework.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 岗位管理 服务实现类
 * </p>
 *
 * @author iteaj
 * @since 2023-09-04
 */
@Service
public class PostServiceImpl extends BaseServiceImpl<PostMapper, Post> implements IPostService {

    @Override
    public BooleanResult save(Post entity) {
        // 同一个机构下面不能存在同一个岗位
        this.getOne(Wrappers.<Post>lambdaQuery()
            .eq(Post::getOrgId, entity.getOrgId())
            .eq(Post::getName, entity.getName())).ifPresent(item -> {
                throw new ServiceException("岗位名称["+entity.getName()+"]已存在");
            });
        return super.save(entity);
    }

    @Override
    public BooleanResult updateById(Post entity) {
        this.getOne(Wrappers.<Post>lambdaQuery()
                .ne(Post::getId, entity.getId())
                .eq(Post::getOrgId, entity.getOrgId())
                .eq(Post::getName, entity.getName())).ifPresent(item -> {
            throw new ServiceException("岗位名称["+entity.getName()+"]已存在");
        });

        return super.updateById(entity);
    }

    @Override
    public PageResult<IPage<Post>> pageDetail(Page<Post> page, Post entity) {
        return new PageResult<>(getBaseMapper().pageDetail(page, entity));
    }
}
