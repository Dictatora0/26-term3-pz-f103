package com.iteaj.iboot.module.iot.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.spi.iot.data.BaseEchartsCount;
import com.iteaj.framework.spi.iot.data.RealtimeCollectDataService;
import com.iteaj.iboot.module.iot.consts.NameValueDto;
import com.iteaj.iboot.module.iot.dto.*;
import com.iteaj.iboot.module.iot.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统功能统计
 */
@RestController
@RequestMapping("/iot/index")
public class IotIndexController extends BaseController {

    private final IDeviceService deviceService;
    private final IProductService productService;
    private final IProtocolService protocolService;
    private final IEventSourceService eventSourceService;
    private final RealtimeCollectDataService collectDataService;

    public IotIndexController(IDeviceService deviceService, IProductService productService
            , IProtocolService protocolService, RealtimeCollectDataService collectDataService
            , IEventSourceService eventSourceService) {
        this.deviceService = deviceService;
        this.productService = productService;
        this.protocolService = protocolService;
        this.collectDataService = collectDataService;
        this.eventSourceService = eventSourceService;
    }

    /**
     * 各组件的产品统计
     * @return
     */
    @GetMapping("numCount")
    public Result<IndexCountDto> indexNumCount() {
        IndexCountDto indexCountDto = new IndexCountDto();
        // 设备数量
        CurrentDeviceDto deviceDto = deviceService.countCurrentDevice();
        indexCountDto.setDeviceNum(deviceDto.getToday());
        indexCountDto.setOnlineNum(deviceDto.getOnline());

        // 协议数量
        protocolService.count().ifPresent(item -> indexCountDto.setProtocolNum(item));
        // 产品数量
        FuncStatusProfileDto profileDto = productService.countStatusProfile();
        indexCountDto.setProductNum(profileDto.getTotalNum());
        indexCountDto.setEnabledProductNum(profileDto.getEnabledNum());
        // 事件源数量
        FuncStatusProfileDto eventSouceCount = eventSourceService.countStatusProfile(new GroupAndProductParamDto());
        indexCountDto.setEventSourceNum(eventSouceCount.getTotalNum());
        indexCountDto.setRunningEventSourceNum(eventSouceCount.getEnabledNum());

        return success(indexCountDto);
    }

    /**
     * 各个产品下面的设备数量统计
     * @return
     */
    @GetMapping("productDeviceCount")
    public Result<List<NameValueDto>> productDeviceCount() {
        List<NameValueDto> dtos = productService.countOfDevice();
        return success(dtos);
    }

    /**
     * 各个设备类型下面的设备数量统计
     * @return
     */
    @GetMapping("deviceTypeCount")
    public Result<List<DeviceTypeCountDto>> deviceTypeCount() {
        List<DeviceTypeCountDto> dtos = productService.countOfDeviceType();
        return success(dtos);
    }

    /**
     * 统计最近一个月采集的数据
     * @return
     */
    @GetMapping("countDataWithLastMonth")
    public Result<List<BaseEchartsCount>> countDataWithLastMonth() {
        List<BaseEchartsCount> countLastTimeDtos = collectDataService.countLastMonth();
        return success(countLastTimeDtos);
    }
}
