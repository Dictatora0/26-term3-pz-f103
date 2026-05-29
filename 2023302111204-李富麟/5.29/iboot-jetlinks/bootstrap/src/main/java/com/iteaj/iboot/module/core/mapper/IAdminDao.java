package com.iteaj.iboot.module.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iteaj.framework.result.PageResult;
import com.iteaj.iboot.module.core.dto.AdminDto;
import com.iteaj.iboot.module.core.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

@Mapper
public interface IAdminDao extends BaseMapper<Admin> {

    void createAdmin(AdminDto adminDto);

    void updateAdminRole(AdminDto adminDto);

    AdminDto getAdminDetailById(Long id);

    void updatePwdById(@Param("id") Long id, @Param("password") String password);

    void deleteAllJoinByIds(List<Long> list);

    List<String> selectPermsById(Serializable id);

    AdminDto getAdminCenter(Serializable id);

    String getAdminPassword(Long id);

    IPage<AdminDto> pageDetail(Page page, Admin entity);
}
