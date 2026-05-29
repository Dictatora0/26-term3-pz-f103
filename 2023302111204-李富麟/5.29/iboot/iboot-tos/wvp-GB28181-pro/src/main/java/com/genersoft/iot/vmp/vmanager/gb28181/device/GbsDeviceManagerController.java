package com.genersoft.iot.vmp.vmanager.gb28181.device;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.conf.DynamicTask;
import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.gb28181.bean.Device;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.bean.SyncStatus;
import com.genersoft.iot.vmp.gb28181.task.ISubscribeTask;
import com.genersoft.iot.vmp.gb28181.task.impl.CatalogSubscribeTask;
import com.genersoft.iot.vmp.gb28181.task.impl.MobilePositionSubscribeTask;
import com.genersoft.iot.vmp.gb28181.transmit.callback.DeferredResultHolder;
import com.genersoft.iot.vmp.gb28181.transmit.callback.RequestMessage;
import com.genersoft.iot.vmp.gb28181.transmit.cmd.impl.SIPCommander;
import com.genersoft.iot.vmp.service.IDeviceChannelService;
import com.genersoft.iot.vmp.service.IDeviceService;
import com.genersoft.iot.vmp.service.IInviteStreamService;
import com.genersoft.iot.vmp.storager.IVideoManagerStorage;
import com.genersoft.iot.vmp.vmanager.bean.BaseTree;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import com.iteaj.framework.spi.gbs.GbsRealtimeTree;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import com.iteaj.framework.BaseController;
import com.iteaj.framework.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 国标(GB28181)设备管理
 */
@RestController
//@RequestMapping("/gbs/device")
public class GbsDeviceManagerController extends BaseController {

	private final static Logger logger = LoggerFactory.getLogger(GbsDeviceManagerController.class);

	@Autowired
	private IVideoManagerStorage storager;

	@Autowired
	private IDeviceChannelService deviceChannelService;

	@Autowired
	private IInviteStreamService inviteStreamService;

	@Autowired
	private SIPCommander cmder;

	@Autowired
	private DeferredResultHolder resultHolder;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private DynamicTask dynamicTask;

	/**
	 * 使用ID查询国标设备
	 * @param deviceId 国标ID
	 * @return 国标设备
	 */
	@GetMapping("/edit")
	public Result<Device> edit(String deviceId){
		Device device = deviceService.getDevice(deviceId);
		return success(device);
	}

	/**
	 * 分页查询国标设备
	 * @param page 当前页
	 * @param device 设备参数
	 * @return 分页国标列表
	 */
	@GetMapping("/view")
	public Result<Page<Device>> view(Page page, Device device){
		deviceService.detailByPage(page, device);
		return success(page);
	}

	/**
	 * 树形结构的设备-通道列表
	 * @param deviceId 设备id 用于获取下面的通道列表
	 * @return
	 */
	@GetMapping("/trees")
	public Result<List<GbsRealtimeTree>> trees(String deviceId) {
		if(StrUtil.isBlank(deviceId)) {
			List<Device> devices = deviceService.getAll();
			if(!CollectionUtils.isEmpty(devices)) {
				List<GbsRealtimeTree> realtimeTrees = devices.stream()
						.map(item -> GbsRealtimeTree.buildDevice(item.getDeviceId(), item.getName(), item.isOnLine(), item.getChannelCount()))
						.collect(Collectors.toList());
				return success(realtimeTrees);
			}
		} else {
			List<DeviceChannel> deviceChannels = deviceService.queryVideoDeviceInTreeNode(deviceId, null);
			if(!CollectionUtils.isEmpty(deviceChannels)) {
				List<GbsRealtimeTree> realtimeTrees = deviceChannels.stream()
						.map(item -> GbsRealtimeTree.buildChannel(item.getDeviceId(), item.getChannelId(), item.getName(), item.isStatus()))
						.collect(Collectors.toList());
				return success(realtimeTrees);
			}
		}

		return success();
	}

	/**
	 * 分页查询通道数
	 *
	 * @param deviceId 设备id
	 * @param page 当前页
	 * @param query 查询内容
	 * @param status 是否在线  在线 true / 离线 false
	 * @param channelType 设备 false/子目录 true
	 * @param catalogUnderDevice 是否直属与设备的目录
	 * @return 通道列表
	 */
	@GetMapping("/channels")
	public Result<Page<DeviceChannel>> channels(Page page, String deviceId, String query, Boolean status, Boolean channelType, Boolean catalogUnderDevice) {
		if (ObjectUtils.isEmpty(query)) {
			query = null;
		}

		Page<DeviceChannel> channelPage = storager.queryChannelsByDeviceId(page, deviceId, query, channelType, status, catalogUnderDevice);
		return success(channelPage);
	}

	/**
	 * 同步设备通道
	 * @param deviceId 设备id
	 * @return
	 */
	@GetMapping("/sync")
	public WVPResult<SyncStatus> devicesSync(String deviceId){

		if (logger.isDebugEnabled()) {
			logger.debug("设备通道信息同步API调用，deviceId：" + deviceId);
		}
		Device device = storager.queryVideoDevice(deviceId);
		boolean status = deviceService.isSyncRunning(deviceId);
		// 已存在则返回进度
		if (status) {
			SyncStatus channelSyncStatus = deviceService.getChannelSyncStatus(deviceId);
			return WVPResult.success(channelSyncStatus);
		}
		deviceService.sync(device);

		WVPResult<SyncStatus> wvpResult = new WVPResult<>();
		wvpResult.setCode(0);
		wvpResult.setMsg("开始同步");
		return wvpResult;
	}

	/**
	 * 移除设备
	 * @param deviceId 设备id
	 * @return
	 */
	@Operation(summary = "移除设备")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@DeleteMapping("/devices/{deviceId}/delete")
	public String delete(@PathVariable String deviceId){

		if (logger.isDebugEnabled()) {
			logger.debug("设备信息删除API调用，deviceId：" + deviceId);
		}

		// 清除redis记录
		boolean isSuccess = deviceService.delete(deviceId);
		if (isSuccess) {
			inviteStreamService.clearInviteInfo(deviceId);
			// 停止此设备的订阅更新
			Set<String> allKeys = dynamicTask.getAllKeys();
			for (String key : allKeys) {
				if (key.startsWith(deviceId)) {
					Runnable runnable = dynamicTask.get(key);
					if (runnable instanceof ISubscribeTask) {
						ISubscribeTask subscribeTask = (ISubscribeTask) runnable;
						subscribeTask.stop();
					}
					dynamicTask.stop(key);
				}
			}
			JSONObject json = new JSONObject();
			json.put("deviceId", deviceId);
			return json.toString();
		} else {
			logger.warn("设备信息删除API调用失败！");
			throw new ControllerException(ErrorCode.ERROR100.getCode(), "设备信息删除API调用失败！");
		}
	}

	/**
	 * 分页查询子目录通道
	 * @param deviceId 通道id
	 * @param channelId 通道id
	 * @param page 当前页
	 * @param query 查询内容
	 * @param online 是否在线
	 * @param channelType 通道类型
	 * @return 子通道列表
	 */
	@Operation(summary = "分页查询子目录通道")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "channelId", description = "通道国标编号", required = true)
	@Parameter(name = "page", description = "当前页", required = true)
	@Parameter(name = "count", description = "每页查询数量", required = true)
	@Parameter(name = "query", description = "查询内容")
	@Parameter(name = "online", description = "是否在线")
	@Parameter(name = "channelType", description = "设备/子目录-> false/true")
	@GetMapping("/sub_channels/{deviceId}/{channelId}/channels")
	public Page<DeviceChannel> subChannels(Page page, @PathVariable String deviceId, @PathVariable String channelId
			, @RequestParam(required = false) String query, @RequestParam(required = false) Boolean online
			, @RequestParam(required = false) Boolean channelType){

		DeviceChannel deviceChannel = storager.queryChannel(deviceId,channelId);
		if (deviceChannel == null) {
			return page;
		}

		return storager.querySubChannels(page, deviceId, channelId, query, channelType, online);
	}

	/**
	 * 更新通道信息
	 * @param deviceId 设备id
	 * @param channel 通道
	 * @return
	 */
	@Operation(summary = "更新通道信息")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "channel", description = "通道信息", required = true)
	@PostMapping("/channel/update/{deviceId}")
	public void updateChannel(@PathVariable String deviceId,DeviceChannel channel){
		deviceChannelService.updateChannel(deviceId, channel);
	}

	/**
	 * 修改数据流传输模式
	 * @param deviceId 设备id
	 * @param streamMode 数据流传输模式
	 * @return
	 */
	@Operation(summary = "修改数据流传输模式")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "streamMode", description = "数据流传输模式, 取值：" +
			"UDP（udp传输），TCP-ACTIVE（tcp主动模式,暂不支持），TCP-PASSIVE（tcp被动模式）", required = true)
	@PostMapping("/transport/{deviceId}/{streamMode}")
	public void updateTransport(@PathVariable String deviceId, @PathVariable String streamMode){
		Device device = deviceService.getDevice(deviceId);
		device.setStreamMode(streamMode);
		deviceService.updateCustomDevice(device);
	}

	/**
	 * 添加设备信息
	 * @param device 设备信息
	 * @return
	 */
	@Operation(summary = "添加设备信息")
	@Parameter(name = "device", description = "设备", required = true)
	@PostMapping("/device/add/")
	public void addDevice(Device device){

		if (device == null || device.getDeviceId() == null) {
			throw new ControllerException(ErrorCode.ERROR400);
		}

		// 查看deviceId是否存在
		boolean exist = deviceService.isExist(device.getDeviceId());
		if (exist) {
			throw new ControllerException(ErrorCode.ERROR100.getCode(), "设备编号已存在");
		}
		deviceService.addDevice(device);
	}

	/**
	 * 新增或更新设备信息
	 * @param device 设备信息
	 * @return
	 */
	@PostMapping("/saveOrUpdate")
	public Result<Boolean> saveOrUpdate(@Validated @RequestBody Device device){
		if(device.getId() == null) {
			deviceService.addDevice(device);
		} else {
			deviceService.updateDevice(device);
		}

		return success();
	}

	/**
	 * 设备状态查询请求API接口
	 *
	 * @param deviceId 设备id
	 */
	@Operation(summary = "设备状态查询")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@GetMapping("/devices/{deviceId}/status")
	public DeferredResult<ResponseEntity<String>> deviceStatusApi(@PathVariable String deviceId) {
		if (logger.isDebugEnabled()) {
			logger.debug("设备状态查询API调用");
		}
		Device device = storager.queryVideoDevice(deviceId);
		String uuid = UUID.randomUUID().toString();
		String key = DeferredResultHolder.CALLBACK_CMD_DEVICESTATUS + deviceId;
		DeferredResult<ResponseEntity<String>> result = new DeferredResult<ResponseEntity<String>>(2*1000L);
		if(device == null) {
			result.setResult(new ResponseEntity(String.format("设备%s不存在", deviceId),HttpStatus.OK));
			return result;
		}
		try {
			cmder.deviceStatusQuery(device, event -> {
				RequestMessage msg = new RequestMessage();
				msg.setId(uuid);
				msg.setKey(key);
				msg.setData(String.format("获取设备状态失败，错误码： %s, %s", event.statusCode, event.msg));
				resultHolder.invokeResult(msg);
			});
		} catch (InvalidArgumentException | SipException | ParseException e) {
			logger.error("[命令发送失败] 获取设备状态: {}", e.getMessage());
			throw new ControllerException(ErrorCode.ERROR100.getCode(), "命令发送失败: " + e.getMessage());
		}
		result.onTimeout(()->{
			logger.warn(String.format("获取设备状态超时"));
			// 释放rtpserver
			RequestMessage msg = new RequestMessage();
			msg.setId(uuid);
			msg.setKey(key);
			msg.setData("Timeout. Device did not response to this command.");
			resultHolder.invokeResult(msg);
		});
		resultHolder.put(DeferredResultHolder.CALLBACK_CMD_DEVICESTATUS + deviceId, uuid, result);
		return result;
	}

	/**
	 * 设备报警查询请求API接口
	 * @param deviceId 设备id
	 * @param startPriority	报警起始级别（可选）
	 * @param endPriority	报警终止级别（可选）
	 * @param alarmMethod	报警方式条件（可选）
	 * @param alarmType		报警类型
	 * @param startTime		报警发生起始时间（可选）
	 * @param endTime		报警发生终止时间（可选）
	 * @return				true = 命令发送成功
	 */
	@Operation(summary = "设备状态查询")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "startPriority", description = "报警起始级别")
	@Parameter(name = "endPriority", description = "报警终止级别")
	@Parameter(name = "alarmMethod", description = "报警方式条件")
	@Parameter(name = "alarmType", description = "报警类型")
	@Parameter(name = "startTime", description = "报警发生起始时间")
	@Parameter(name = "endTime", description = "报警发生终止时间")
	@GetMapping("/alarm/{deviceId}")
	public DeferredResult<ResponseEntity<String>> alarmApi(@PathVariable String deviceId,
														@RequestParam(required = false) String startPriority,
														@RequestParam(required = false) String endPriority,
														@RequestParam(required = false) String alarmMethod,
														@RequestParam(required = false) String alarmType,
														@RequestParam(required = false) String startTime,
														@RequestParam(required = false) String endTime) {
		if (logger.isDebugEnabled()) {
			logger.debug("设备报警查询API调用");
		}
		Device device = storager.queryVideoDevice(deviceId);
		String key = DeferredResultHolder.CALLBACK_CMD_ALARM + deviceId;
		String uuid = UUID.randomUUID().toString();
		try {
			cmder.alarmInfoQuery(device, startPriority, endPriority, alarmMethod, alarmType, startTime, endTime, event -> {
				RequestMessage msg = new RequestMessage();
				msg.setId(uuid);
				msg.setKey(key);
				msg.setData(String.format("设备报警查询失败，错误码： %s, %s",event.statusCode, event.msg));
				resultHolder.invokeResult(msg);
			});
		} catch (InvalidArgumentException | SipException | ParseException e) {
			logger.error("[命令发送失败] 设备报警查询: {}", e.getMessage());
			throw new ControllerException(ErrorCode.ERROR100.getCode(), "命令发送失败: " + e.getMessage());
		}
		DeferredResult<ResponseEntity<String>> result = new DeferredResult<ResponseEntity<String >> (3 * 1000L);
		result.onTimeout(()->{
			logger.warn(String.format("设备报警查询超时"));
			// 释放rtpserver
			RequestMessage msg = new RequestMessage();
			msg.setId(uuid);
			msg.setKey(key);
			msg.setData("设备报警查询超时");
			resultHolder.invokeResult(msg);
		});
		resultHolder.put(DeferredResultHolder.CALLBACK_CMD_ALARM + deviceId, uuid, result);
		return result;
	}


	@GetMapping("/{deviceId}/sync_status")
	@Operation(summary = "获取通道同步进度")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	public WVPResult<SyncStatus> getSyncStatus(@PathVariable String deviceId) {
		SyncStatus channelSyncStatus = deviceService.getChannelSyncStatus(deviceId);
		WVPResult<SyncStatus> wvpResult = new WVPResult<>();
		if (channelSyncStatus == null) {
			wvpResult.setCode(-1);
			wvpResult.setMsg("同步尚未开始");
		}else {
			wvpResult.setCode(ErrorCode.SUCCESS.getCode());
			wvpResult.setMsg(ErrorCode.SUCCESS.getMsg());
			wvpResult.setData(channelSyncStatus);
			if (channelSyncStatus.getErrorMsg() != null) {
				wvpResult.setMsg(channelSyncStatus.getErrorMsg());
			}
		}
		return wvpResult;
	}

	@GetMapping("/{deviceId}/subscribe_info")
	@Operation(summary = "获取设备的订阅状态")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	public WVPResult<Map<String, Integer>> getSubscribeInfo(@PathVariable String deviceId) {
		Set<String> allKeys = dynamicTask.getAllKeys();
		Map<String, Integer> dialogStateMap = new HashMap<>();
		for (String key : allKeys) {
			if (key.startsWith(deviceId)) {
				ISubscribeTask subscribeTask = (ISubscribeTask)dynamicTask.get(key);
				if (subscribeTask instanceof CatalogSubscribeTask) {
					dialogStateMap.put("catalog", 1);
				}else if (subscribeTask instanceof MobilePositionSubscribeTask) {
					dialogStateMap.put("mobilePosition", 1);
				}
			}
		}
		WVPResult<Map<String, Integer>> wvpResult = new WVPResult<>();
		wvpResult.setCode(0);
		wvpResult.setData(dialogStateMap);
		return wvpResult;
	}

	@GetMapping("/snap/{deviceId}/{channelId}")
	@Operation(summary = "请求截图")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "channelId", description = "通道国标编号", required = true)
	@Parameter(name = "mark", description = "标识", required = false)
	public void getSnap(HttpServletResponse resp, @PathVariable String deviceId, @PathVariable String channelId, @RequestParam(required = false) String mark) {

		try {
			final InputStream in = Files.newInputStream(new File("snap" + File.separator + deviceId + "_" + channelId + (mark == null? ".jpg": ("_" + mark + ".jpg"))).toPath());
			resp.setContentType(MediaType.IMAGE_PNG_VALUE);
			ServletOutputStream outputStream = resp.getOutputStream();
			IOUtils.copy(in, resp.getOutputStream());
			in.close();
			outputStream.close();
		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	/**
	 * 查询国标树
	 * @param deviceId 设备ID
	 * @param parentId 父ID
	 * @return 国标设备
	 */
	@Operation(summary = "查询国标树")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "parentId", description = "父级国标编号")
	@Parameter(name = "onlyCatalog", description = "只获取目录")
	@Parameter(name = "page", description = "当前页", required = true)
	@Parameter(name = "count", description = "每页条数", required = true)
	@GetMapping("/tree/{deviceId}")
	public ResponseEntity<List<BaseTree<DeviceChannel>>> getTree(@PathVariable String deviceId,
											@RequestParam(required = false) String parentId,
											@RequestParam(required = false) Boolean onlyCatalog){

		List<BaseTree<DeviceChannel>> treeData = deviceService.queryVideoDeviceTree(deviceId, parentId, onlyCatalog);

		return new ResponseEntity<>(treeData, HttpStatus.OK);
	}

}
