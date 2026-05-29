package com.genersoft.iot.vmp.storager.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.media.zlm.dto.StreamProxyItem;
import com.genersoft.iot.vmp.vmanager.bean.ResourceBaseInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StreamProxyMapper extends BaseMapper<StreamProxyItem> {

    Page<StreamProxyItem> listDetailByPage(Page page, StreamProxyItem entity);

    List<StreamProxyItem> selectForEnable(boolean enable);

    StreamProxyItem getByAppAndStream(@Param("app") String app, @Param("stream") String stream);

    List<StreamProxyItem> selectForEnableInMediaServer( @Param("id")  String id, @Param("enable") boolean enable);

    List<StreamProxyItem> selectInMediaServer(String id);

    void updateStatusByMediaServerId(@Param("mediaServerId") String mediaServerId, @Param("status") boolean status);

    int updateStatus(@Param("app") String app, @Param("stream") String stream, @Param("status") boolean status);

    void deleteAutoRemoveItemByMediaServerId(String mediaServerId);

    List<StreamProxyItem> selectAutoRemoveItemByMediaServerId(String mediaServerId);

    ResourceBaseInfo getOverview();

}
