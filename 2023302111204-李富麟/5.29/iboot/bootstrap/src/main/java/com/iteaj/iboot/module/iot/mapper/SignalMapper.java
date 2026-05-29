package com.iteaj.iboot.module.iot.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iteaj.iboot.module.iot.entity.Signal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 寄存器点位 Mapper 接口
 * </p>
 *
 * @author iteaj
 * @since 2022-07-22
 */
public interface SignalMapper extends BaseMapper<Signal> {

    IPage<Signal> detailByPage(Page<Signal> page, Signal entity);

    List<Signal> listByProductIds(@Param("list") List<Long> productIds, @Param("name") String name);

    Signal detailById(Long id);
}
