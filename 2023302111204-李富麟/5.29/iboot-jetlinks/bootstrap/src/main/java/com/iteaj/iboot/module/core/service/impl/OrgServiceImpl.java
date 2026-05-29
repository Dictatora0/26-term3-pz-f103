package com.iteaj.iboot.module.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iteaj.iboot.common.CacheKeys;
import com.iteaj.iboot.module.core.entity.Admin;
import com.iteaj.iboot.module.core.mapper.IOrgDao;
import com.iteaj.iboot.module.core.entity.Org;
import com.iteaj.iboot.module.core.service.IAdminService;
import com.iteaj.iboot.module.core.service.IOrgService;
import com.iteaj.framework.utils.TreeUtils;
import com.iteaj.framework.BaseServiceImpl;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.BooleanResult;
import com.iteaj.framework.result.DetailResult;
import com.iteaj.framework.result.ListResult;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * create time: 2019/11/26
 *
 * @author iteaj
 * @since 1.0
 */
@Service
@AllArgsConstructor
@CacheConfig(cacheNames = {CacheKeys.IBOOT_CACHE_ORG})
public class OrgServiceImpl extends BaseServiceImpl<IOrgDao, Org> implements IOrgService {

    private final IAdminService adminService;

    @Override
    @CacheEvict(allEntries = true)
    public BooleanResult save(Org entity) {
        this.getOne(Wrappers.<Org>lambdaQuery().eq(Org::getName, entity.getName()))
                .ifPresentThrow("部门["+entity.getName()+"]已存在");

        super.save(entity);
        if(entity.getPid() == 0l) {
            entity.setLevel(1);
            entity.setPath(entity.getId().toString());
        } else if(entity.getPid() > 0l) {
            DetailResult<Org> parent = getById(entity.getPid());
            parent.ofNullable().orElseThrow(() -> new ServiceException("父部门不存在"));

            entity.setLevel(parent.getData().getLevel() + 1);
            entity.setPath(parent.getData().getPath()+","+entity.getId());
        }

        return updateById(entity); // 更新最新路径path;
    }

    @Override
    @CacheEvict(allEntries = true)
    public BooleanResult updateById(Org entity) {
        if(entity.getPid() > 0) {
            DetailResult<Org> parent = getById(entity.getPid());
            parent.ofNullable().orElseThrow(() -> new ServiceException("父部门不存在"));

            entity.setLevel(parent.getData().getLevel() + 1);
            entity.setPath(parent.getData().getPath()+","+entity.getId());

            if(parent.getData().getId().equals(entity.getId())) {
                throw new ServiceException("父部门不能是自己");
            }
        } else {
            entity.setLevel(1);
            entity.setPath(entity.getId().toString());
        }

        return super.updateById(entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public BooleanResult removeByIds(Collection<? extends Serializable> idList) {
        if(idList.size() == 0) {
            throw new ServiceException("未选择要删除的数据");
        }
        if(idList.size() > 1) {
            throw new ServiceException("不支持批量删除");
        }
        final Serializable id = idList.stream().findFirst().get();

        // 如果存在子部门, 则要先删除子部门
        list(Wrappers.<Org>query().eq("pid", id))
                .ofNullable().ifPresent(item -> {
                    if(!item.isEmpty()) {
                        throw new ServiceException("请先删除子部门");
                    }
                });

        // 此部门已经在使用
        adminService.getOne(Wrappers.<Admin>query().eq("org_id", id))
                .ofNullable().ifPresent(item -> {
            throw new ServiceException("此部门已被关联使用");
        });

        return super.removeByIds(idList);
    }

    @Override
    public ListResult<Org> selectTrees(Org org) {
        ListResult<Org> selectList = this.list(org);
        return new ListResult((List<Org>) TreeUtils.toTrees(selectList.getData()));
    }

    @Override
    @Cacheable(key = "'list'")
    public ListResult<Org> list() {
        return super.list();
    }
}
