package com.genersoft.iot.vmp.storager.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.genersoft.iot.vmp.conf.SipConfig;
import com.genersoft.iot.vmp.conf.UserSetting;
import com.genersoft.iot.vmp.gb28181.bean.*;
import com.genersoft.iot.vmp.gb28181.event.EventPublisher;
import com.genersoft.iot.vmp.gb28181.event.subscribe.catalog.CatalogEvent;
import com.genersoft.iot.vmp.media.zlm.dto.StreamProxyItem;
import com.genersoft.iot.vmp.media.zlm.dto.StreamPushItem;
import com.genersoft.iot.vmp.service.*;
import com.genersoft.iot.vmp.service.bean.GPSMsgInfo;
import com.genersoft.iot.vmp.storager.IRedisCatchStorage;
import com.genersoft.iot.vmp.storager.IVideoManagerStorage;
import com.genersoft.iot.vmp.storager.dao.*;
import com.genersoft.iot.vmp.storager.dao.dto.ChannelSourceInfo;
import com.genersoft.iot.vmp.utils.DateUtil;
import com.genersoft.iot.vmp.vmanager.gb28181.platform.bean.ChannelReduce;
import com.genersoft.iot.vmp.web.gb28181.dto.DeviceChannelExtend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 视频设备数据存储-jdbc实现
 * swwheihei
 * 2020年5月6日 下午2:31:42
 */
@SuppressWarnings("rawtypes")
@Component
public class VideoManagerStorageImpl implements IVideoManagerStorage {

	private final Logger logger = LoggerFactory.getLogger(VideoManagerStorageImpl.class);

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	SipConfig sipConfig;

	@Autowired
	TransactionDefinition transactionDefinition;

	@Autowired
	DataSourceTransactionManager dataSourceTransactionManager;

//	@Autowired
//    private DeviceMapper deviceMapper;

	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private DeviceChannelMapper deviceChannelMapper;

	@Autowired
	private IDeviceChannelService deviceChannelService;

	@Autowired
	private DeviceMobilePositionMapper deviceMobilePositionMapper;

	@Autowired
    private ParentPlatformMapper platformMapper;

	@Autowired
    private IRedisCatchStorage redisCatchStorage;

	@Autowired
    private PlatformChannelMapper platformChannelMapper;

	@Autowired
	private PlatformCatalogMapper platformCatalogMapper;

	@Autowired
    private StreamProxyMapper streamProxyMapper;

	@Autowired
	private IStreamProxyService streamProxyService;

	@Autowired
    private StreamPushMapper streamPushMapper;

	@Autowired
	private IStreamPushService streamPushService;

	@Autowired
    private GbStreamMapper gbStreamMapper;

	@Autowired
    private UserSetting userSetting;

	@Autowired
    private PlatformCatalogMapper catalogMapper;

	@Autowired
    private PlatformGbStreamMapper platformGbStreamMapper;

	@Autowired
    private IGbStreamService gbStreamService;

	@Autowired
    private ParentPlatformMapper parentPlatformMapper;

	/**
	 * 根据设备ID判断设备是否存在
	 *
	 * @param deviceId 设备ID
	 * @return true:存在  false：不存在
	 */
	@Override
	public boolean exists(String deviceId) {
		return deviceService.getDeviceMapper().getDeviceByDeviceId(deviceId) != null;
	}

	@Override
	@Transactional
	public boolean resetChannels(String deviceId, List<DeviceChannel> deviceChannelList) {
		if (CollectionUtils.isEmpty(deviceChannelList)) {
			return false;
		}

		List<DeviceChannel> allChannels = deviceChannelService.selectChannelsByDeviceId(deviceId);
		Map<String,DeviceChannel> allChannelMap = Optional.ofNullable(allChannels).orElse(Collections.emptyList())
				.stream().collect(Collectors.toConcurrentMap(DeviceChannel::getChannelId, Function.identity()));

		// 数据去重
		List<DeviceChannel> channels = new ArrayList<>();

		List<DeviceChannel> updateChannels = new ArrayList<>();
		List<DeviceChannel> addChannels = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder();
		Map<String, Integer> subContMap = new HashMap<>();

		String now = DateUtil.getNow();

		// 数据去重
		Set<String> gbIdSet = new HashSet<>();
		for (DeviceChannel deviceChannel : deviceChannelList) {
			if (gbIdSet.contains(deviceChannel.getChannelId())) {
				stringBuilder.append(deviceChannel.getChannelId()).append(",");
				continue;
			}
			gbIdSet.add(deviceChannel.getChannelId());
			DeviceChannel existsChannel = allChannelMap.get(deviceChannel.getChannelId());
			if (existsChannel != null) {

				deviceChannel.setUpdateTime(now);
				deviceChannel.setId(existsChannel.getId());
				deviceChannel.setName(existsChannel.getName());
				deviceChannel.setHasAudio(existsChannel.isHasAudio());
				deviceChannel.setStreamId(existsChannel.getStreamId());

				if (!existsChannel.isStatus().equals(deviceChannel.isStatus())){
					List<String> strings = platformChannelMapper.queryParentPlatformByChannelId(deviceChannel.getChannelId());
					if (!CollectionUtils.isEmpty(strings)){
						strings.forEach(platformId->{
							eventPublisher.catalogEventPublish(platformId, deviceChannel, deviceChannel.isStatus()?CatalogEvent.ON:CatalogEvent.OFF);
						});
					}
				}

				updateChannels.add(deviceChannel);
			}else {
				deviceChannel.setCreateTime(now);
				deviceChannel.setUpdateTime(now);
				addChannels.add(deviceChannel);
			}
			allChannelMap.remove(deviceChannel.getChannelId());
			channels.add(deviceChannel);
			if (!ObjectUtils.isEmpty(deviceChannel.getParentId())) {
				if (subContMap.get(deviceChannel.getParentId()) == null) {
					subContMap.put(deviceChannel.getParentId(), 1);
				}else {
					Integer count = subContMap.get(deviceChannel.getParentId());
					subContMap.put(deviceChannel.getParentId(), count++);
				}
			}
		}

		if (!channels.isEmpty()) {
			for (DeviceChannel channel : channels) {
				if (subContMap.get(channel.getChannelId()) != null){
					Integer count = subContMap.get(channel.getChannelId());
					if (count > 0) {
						channel.setSubCount(count);
						channel.setParental(1);
					}
				}
			}
		}

		if (stringBuilder.length() > 0) {
			logger.info("[目录查询]收到的数据存在重复： {}" , stringBuilder);
		}
		if(CollectionUtils.isEmpty(channels)){
			logger.info("通道重设，数据为空={}" , deviceChannelList);
			return false;
		}

		if (!addChannels.isEmpty()) {
			deviceChannelService.saveBatch(addChannels);
		}

		if (!updateChannels.isEmpty()) {
			deviceChannelService.saveOrUpdateBatch(updateChannels);
		}

		if (!allChannelMap.isEmpty()) {
			List<Long> collect = allChannelMap.values().stream()
					.map(DeviceChannel::getId).collect(Collectors.toList());
			deviceChannelService.removeByIds(collect);
		}

		return true;
	}


	@Override
	@Transactional
	public boolean updateChannels(String deviceId, List<DeviceChannel> deviceChannelList) {
		if (CollectionUtils.isEmpty(deviceChannelList)) {
			return false;
		}

		String now = DateUtil.getNow();
		List<DeviceChannel> allChannels = deviceChannelService.selectChannelsByDeviceId(deviceId);
		Map<String,DeviceChannel> allChannelMap = new ConcurrentHashMap<>();
		if (allChannels.size() > 0) {
			for (DeviceChannel deviceChannel : allChannels) {
				allChannelMap.put(deviceChannel.getChannelId(), deviceChannel);
			}
		}

		// 数据去重
		List<DeviceChannel> channels = new ArrayList<>();

		List<DeviceChannel> updateChannels = new ArrayList<>();
		List<DeviceChannel> addChannels = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder();
		Map<String, Integer> subContMap = new HashMap<>();
		if (deviceChannelList.size() > 0) {
			// 数据去重
			Set<String> gbIdSet = new HashSet<>();
			for (DeviceChannel deviceChannel : deviceChannelList) {
				if (!gbIdSet.contains(deviceChannel.getChannelId())) {
					gbIdSet.add(deviceChannel.getChannelId());
					deviceChannel.setUpdateTime(now);
					DeviceChannel existsChannel = allChannelMap.get(deviceChannel.getChannelId());
					if (existsChannel != null) {
						deviceChannel.setId(existsChannel.getId());
						deviceChannel.setName(existsChannel.getName());
						deviceChannel.setStreamId(existsChannel.getStreamId());
						deviceChannel.setHasAudio(existsChannel.isHasAudio());
						if (!existsChannel.isStatus().equals(deviceChannel.isStatus())){
							List<String> strings = platformChannelMapper.queryParentPlatformByChannelId(deviceChannel.getChannelId());
							if (!CollectionUtils.isEmpty(strings)){
								strings.forEach(platformId->{
									eventPublisher.catalogEventPublish(platformId, deviceChannel, deviceChannel.isStatus()?CatalogEvent.ON:CatalogEvent.OFF);
								});
							}
						}
						updateChannels.add(deviceChannel);
					}else {
						deviceChannel.setCreateTime(now);
						addChannels.add(deviceChannel);
					}

					channels.add(deviceChannel);
					if (!ObjectUtils.isEmpty(deviceChannel.getParentId())) {
						if (subContMap.get(deviceChannel.getParentId()) == null) {
							subContMap.put(deviceChannel.getParentId(), 1);
						}else {
							Integer count = subContMap.get(deviceChannel.getParentId());
							subContMap.put(deviceChannel.getParentId(), count++);
						}
					}
				}else {
					stringBuilder.append(deviceChannel.getChannelId()).append(",");
				}
			}

			if (channels.size() > 0) {
				for (DeviceChannel channel : channels) {
					if (subContMap.get(channel.getChannelId()) != null){
						channel.setSubCount(subContMap.get(channel.getChannelId()));
					}
				}
			}

		}
		if (stringBuilder.length() > 0) {
			logger.info("[目录查询]收到的数据存在重复： {}" , stringBuilder);
		}
		if(CollectionUtils.isEmpty(channels)){
			logger.info("通道重设，数据为空={}" , deviceChannelList);
			return false;
		}

		if (addChannels.size() > 0) {
			deviceChannelService.saveBatch(addChannels);
		}

		if (updateChannels.size() > 0) {
			deviceChannelService.saveOrUpdateBatch(updateChannels);
		}

		return true;
	}

	@Override
	public void deviceChannelOnline(String deviceId, String channelId) {
		deviceChannelMapper.update(null, Wrappers.<DeviceChannel>lambdaUpdate()
				.set(DeviceChannel::isStatus, true)
				.eq(DeviceChannel::getDeviceId, deviceId)
				.eq(DeviceChannel::getChannelId, channelId));
	}

	@Override
	public void deviceChannelOffline(String deviceId, String channelId) {
		deviceChannelMapper.update(null, Wrappers.<DeviceChannel>lambdaUpdate()
				.set(DeviceChannel::isStatus, false)
				.eq(DeviceChannel::getDeviceId, deviceId)
				.eq(DeviceChannel::getChannelId, channelId));
	}

	@Override
	public void startPlay(String deviceId, String channelId, String streamId) {
		deviceChannelMapper.startPlay(deviceId, channelId, streamId);
	}

	@Override
	public void stopPlay(String deviceId, String channelId) {
		deviceChannelMapper.stopPlay(deviceId, channelId);
	}

	/**
	 * 获取设备
	 *
	 * @param deviceId 设备ID
	 * @return Device 设备对象
	 */
	@Override
	public Device queryVideoDevice(String deviceId) {
		return deviceService.getDeviceMapper().getDeviceByDeviceId(deviceId);
	}

	@Override
	public Page queryChannelsByDeviceId(Page page, String deviceId, String query, Boolean hasSubChannel, Boolean online, Boolean catalogUnderDevice) {
		// 获取到所有正在播放的流
		if (catalogUnderDevice != null && catalogUnderDevice) {
//			all = deviceChannelMapper.queryChannels(deviceId, deviceId, query, hasSubChannel, online,null);
//			// 海康设备的parentId是SIP id
//			List<DeviceChannel> deviceChannels = deviceChannelMapper.queryChannels(deviceId, sipConfig.getId(), query, hasSubChannel, online,null);
//			all.addAll(deviceChannels);
			// 海康设备的parentId是SIP id
			deviceChannelMapper.queryChannelsByPage(page, deviceId, sipConfig.getId(), query, hasSubChannel, online);
		}else {
			deviceChannelMapper.queryChannelsByPage(page, deviceId, null, query, hasSubChannel, online);
		}

		return page;
	}

	@Override
	public List<DeviceChannelExtend> queryChannelsByDeviceIdWithStartAndLimit(String deviceId, List<String> channelIds, String query, Boolean hasSubChannel, Boolean online, int start, int limit) {
		return deviceChannelMapper.queryChannelsByDeviceIdWithStartAndLimit(deviceId, channelIds, null, query, hasSubChannel, online, start, limit);
	}


	@Override
	public List<DeviceChannel> queryChannelsByDeviceId(String deviceId,Boolean online,List<String> channelIds) {
		return deviceChannelMapper.queryChannels(deviceId, null,null, null, online,channelIds);
	}

	@Override
	public List<DeviceChannelExtend> queryChannelsByDeviceId(String deviceId, List<String> channelIds, Boolean online) {
		return deviceChannelMapper.queryChannelsWithDeviceInfo(deviceId, null,null, null, online,channelIds);
	}

	@Override
	public Page<DeviceChannel> querySubChannels(Page page, String deviceId, String parentChannelId, String query, Boolean hasSubChannel, Boolean online) {
		return deviceChannelMapper.queryChannelsByPage(page, deviceId, parentChannelId, query, hasSubChannel, online);
	}

	@Override
	public DeviceChannel queryChannel(String deviceId, String channelId) {
		return deviceChannelMapper.selectOne(Wrappers.<DeviceChannel>lambdaQuery()
				.eq(DeviceChannel::getDeviceId, deviceId)
				.eq(DeviceChannel::getChannelId, channelId));
	}


	@Override
	public int delChannel(String deviceId, String channelId) {
		return deviceChannelMapper.delete(Wrappers.<DeviceChannel>lambdaQuery()
				.eq(DeviceChannel::getDeviceId, deviceId)
				.eq(DeviceChannel::getChannelId, channelId));
	}

	/**
	 * 获取多个设备
	 *
	 * @param page 当前页数
	 * @param online 每页数量
	 * @return Page<Device> 分页设备对象数组
	 */
	@Override
	public Page<Device> queryVideoDeviceList(Page page, Boolean online) {
		return deviceService.getDeviceMapper().getDevices(page, online);
	}

	/**
	 * 清空通道
	 * @param deviceId
	 */
	@Override
	public void cleanChannelsForDevice(String deviceId) {
		deviceChannelMapper.cleanChannelsByDeviceId(deviceId);
	}

	/**
	 * 添加Mobile Position设备移动位置
	 * @param mobilePosition
	 */
	@Override
	public synchronized boolean insertMobilePosition(MobilePosition mobilePosition) {
		return deviceMobilePositionMapper.insert(mobilePosition) > 0;
	}

	/**
	 * 查询移动位置轨迹
	 * @param deviceId
	 * @param startTime
	 * @param endTime
	 */
	@Override
	public synchronized List<MobilePosition> queryMobilePositions(String deviceId, String channelId, String startTime, String endTime) {
		return deviceMobilePositionMapper.selectList(Wrappers.<MobilePosition>lambdaQuery()
				.eq(MobilePosition::getDeviceId, deviceId)
				.eq(StrUtil.isNotBlank(channelId), MobilePosition::getChannelId, channelId)
				.ge(StrUtil.isNotBlank(startTime), MobilePosition::getTime, startTime)
				.le(StrUtil.isNotBlank(endTime), MobilePosition::getTime, endTime)
				.orderByAsc(MobilePosition::getTime));
	}

	@Override
	public boolean addParentPlatform(ParentPlatform parentPlatform) {
		if (parentPlatform.getCatalogId() == null) {
			parentPlatform.setCatalogId(parentPlatform.getServerGBId());
		}

		return platformMapper.insert(parentPlatform) > 0;
	}

	@Override
	public boolean updateParentPlatform(ParentPlatform parentPlatform) {
		int result = 0;
		if (parentPlatform.getCatalogGroup() == 0) {
			parentPlatform.setCatalogGroup(1);
		}
		if (parentPlatform.getAdministrativeDivision() == null) {
			parentPlatform.setAdministrativeDivision(parentPlatform.getAdministrativeDivision());
		}
		ParentPlatformCatch parentPlatformCatch = redisCatchStorage.queryPlatformCatchInfo(parentPlatform.getServerGBId()); // .getDeviceGBId());
		if (parentPlatform.getId() == null ) {
			if (parentPlatform.getCatalogId() == null) {
				parentPlatform.setCatalogId(parentPlatform.getServerGBId());
			}

			result = platformMapper.insert(parentPlatform);
			if (parentPlatformCatch == null) {
				parentPlatformCatch = new ParentPlatformCatch();
				parentPlatformCatch.setParentPlatform(parentPlatform);
				parentPlatformCatch.setId(parentPlatform.getServerGBId());
			}
		}else {
			if (parentPlatformCatch == null) { // serverGBId 已变化
				ParentPlatform parentPlatById = platformMapper.selectById(parentPlatform.getId());
				// 使用旧的查出缓存ID
				parentPlatformCatch = new ParentPlatformCatch();
				parentPlatformCatch.setId(parentPlatform.getServerGBId());
				redisCatchStorage.delPlatformCatchInfo(parentPlatById.getServerGBId());
			}

			result = platformMapper.updateById(parentPlatform);
		}
		// 更新缓存
		parentPlatformCatch.setParentPlatform(parentPlatform);
		redisCatchStorage.updatePlatformCatchInfo(parentPlatformCatch);

		return result > 0;
	}

	@Transactional
	@Override
	public boolean deleteParentPlatform(ParentPlatform parentPlatform) {
		int result = platformMapper.delete(Wrappers.<ParentPlatform>lambdaQuery()
				.eq(ParentPlatform::getServerGBId, parentPlatform.getServerGBId()));

		// 删除关联的通道
		platformChannelMapper.cleanChannelForGB(parentPlatform.getServerGBId());
		return result > 0;
	}

	@Override
	public ParentPlatform queryParentPlatByServerGBId(String platformGbId) {
		return platformMapper.getParentPlatByServerGBId(platformGbId);
	}

	@Override
	public List<ParentPlatform> queryEnableParentPlatformList(boolean enable) {
		return platformMapper.selectList(Wrappers.<ParentPlatform>lambdaQuery().eq(ParentPlatform::isEnable, enable));
	}

	@Override
	public List<ParentPlatform> queryEnablePlatformListWithAsMessageChannel() {
		return platformMapper.selectList(Wrappers.<ParentPlatform>lambdaQuery()
				.eq(ParentPlatform::isEnable, true)
				.eq(ParentPlatform::isAsMessageChannel, true));
	}

	@Override
	public List<Device> queryDeviceWithAsMessageChannel() {
		return deviceService.getDeviceMapper().selectList(Wrappers.<Device>lambdaQuery().eq(Device::isAsMessageChannel, true));
	}

	@Override
	public void outlineForAllParentPlatform() {
		platformMapper.update(null, Wrappers.<ParentPlatform>lambdaUpdate().eq(ParentPlatform::isStatus, false));
	}


	@Override
	public Page<ChannelReduce> queryAllChannelList(Page page, String query, Boolean online, Boolean channelType, String platformId, String catalogId) {
		return deviceChannelMapper.queryChannelListByPage(page, query, online, channelType, platformId, catalogId);
	}

	@Override
	public List<DeviceChannelInPlatform> queryChannelListInParentPlatform(String platformId) {

		return deviceChannelMapper.queryChannelByPlatformId(platformId);
	}


	@Override
	public int delChannelForGB(String platformId, List<ChannelReduce> channelReduces) {

		int result = platformChannelMapper.delChannelForGB(platformId, channelReduces);
		List<DeviceChannel> deviceChannelList = new ArrayList<>();
		for (ChannelReduce channelReduce : channelReduces) {
			DeviceChannel deviceChannel = new DeviceChannel();
			deviceChannel.setChannelId(channelReduce.getChannelId());
			deviceChannelList.add(deviceChannel);
		}
		eventPublisher.catalogEventPublish(platformId, deviceChannelList, CatalogEvent.DEL);
		return result;
	}

	@Override
	public DeviceChannel queryChannelInParentPlatform(String platformId, String channelId) {
		List<DeviceChannel> channels = platformChannelMapper.queryChannelInParentPlatform(platformId, channelId);
		if (channels.size() > 1) {
			// 出现长度大于0的时候肯定是国标通道的ID重复了
			logger.warn("国标ID存在重复：{}", channelId);
		}
		if (channels.size() == 0) {
			return null;
		}else {
			return channels.get(0);
		}
	}

	@Override
	public List<PlatformCatalog> queryChannelInParentPlatformAndCatalog(String platformId, String catalogId) {
		List<PlatformCatalog> catalogs = platformChannelMapper.queryChannelInParentPlatformAndCatalog(platformId, catalogId);
		return catalogs;
	}

	@Override
	public List<PlatformCatalog> queryStreamInParentPlatformAndCatalog(String platformId, String catalogId) {
		List<PlatformCatalog> catalogs = platformGbStreamMapper.queryChannelInParentPlatformAndCatalogForCatalog(platformId, catalogId);
		return catalogs;
	}

	@Override
	public Device queryVideoDeviceByPlatformIdAndChannelId(String platformId, String channelId) {
		List<Device> devices = platformChannelMapper.queryVideoDeviceByPlatformIdAndChannelId(platformId, channelId);
		if (devices.size() > 1) {
			// 出现长度大于0的时候肯定是国标通道的ID重复了
			logger.warn("国标ID存在重复：{}", channelId);
		}
		if (devices.size() == 0) {
			return null;
		}else {
			return devices.get(0);
		}


	}

	@Override
	public Device queryDeviceInfoByPlatformIdAndChannelId(String platformId, String channelId) {
		List<Device> devices = platformChannelMapper.queryDeviceInfoByPlatformIdAndChannelId(platformId, channelId);
		if (devices.size() > 1) {
			// 出现长度大于0的时候肯定是国标通道的ID重复了
			logger.warn("国标ID存在重复：{}", channelId);
		}
		if (devices.size() == 0) {
			return null;
		}else {
			return devices.get(0);
		}
	}

	/**
	 * 查询最新移动位置
	 * @param deviceId
	 */
	@Override
	public MobilePosition queryLatestPosition(String deviceId) {
		return deviceMobilePositionMapper.selectOne(Wrappers.<MobilePosition>lambdaQuery()
				.eq(MobilePosition::getDeviceId, deviceId)
				.orderByDesc(MobilePosition::getTime).last("limit 1"));
	}

	/**
	 * 删除指定设备的所有移动位置
	 * @param deviceId
	 */
	@Override
	public int clearMobilePositionsByDeviceId(String deviceId) {
		return deviceMobilePositionMapper.delete(Wrappers.<MobilePosition>lambdaQuery().eq(MobilePosition::getDeviceId, deviceId));
	}


	/**
	 * 移除代理流
	 * @param app
	 * @param stream
	 * @return
	 */
	@Override
	public int deleteStreamProxy(String app, String stream) {
		return streamProxyService.deleteByAppAndStream(app, stream);
	}

	/**
	 * 根据是否启用获取代理流列表
	 * @param enable
	 * @return
	 */
	@Override
	public List<StreamProxyItem> getStreamProxyListForEnable(boolean enable) {
		return streamProxyMapper.selectForEnable(enable);
	}

	/**
	 * 分页查询代理流列表
	 * @param page
	 * @return
	 */
	@Override
	public Page<StreamProxyItem> queryStreamProxyList(Page page, StreamProxyItem entity) {
		return streamProxyMapper.listDetailByPage(page, entity);
	}

	/**
	 * 根据国标ID获取平台关联的直播流
	 * @param platformId
	 * @param gbId
	 * @return
	 */
	@Override
	public GbStream queryStreamInParentPlatform(String platformId, String gbId) {
		return gbStreamMapper.queryStreamInPlatform(platformId, gbId);
	}

	/**
	 * 获取平台关联的直播流
	 * @param platformId
	 * @return
	 */
	@Override
	public List<DeviceChannel> queryGbStreamListInPlatform(String platformId) {
		return gbStreamMapper.queryGbStreamListInPlatform(platformId, userSetting.isUsePushingAsStatus());
	}

	/**
	 * 按照是app和stream获取代理流
	 * @param app
	 * @param stream
	 * @return
	 */
	@Override
	public StreamProxyItem queryStreamProxy(String app, String stream){
		return streamProxyMapper.getByAppAndStream(app, stream);
	}

	@Override
	public int removeMedia(String app, String stream) {
		return streamPushService.getBaseMapper().delete(Wrappers.<StreamPushItem>lambdaQuery()
				.eq(StreamPushItem::getApp, app)
				.eq(StreamPushItem::getStream, stream));
	}

	@Override
	public int mediaOffline(String app, String stream) {
		GbStream gbStream = gbStreamService.selectByAppAndStream(app, stream);
		int result;
		if ("proxy".equals(gbStream.getStreamType())) {
			result = streamProxyMapper.updateStatus(app, stream, false);
		}else {
			result = streamPushMapper.updatePushStatus(app, stream, false);
		}
		return result;
	}

	@Override
	public int mediaOnline(String app, String stream) {
		GbStream gbStream = gbStreamService.selectByAppAndStream(app, stream);
		int result;
		if ("proxy".equals(gbStream.getStreamType())) {
			result = streamProxyMapper.updateStatus(app, stream, true);
		}else {
			result = streamPushMapper.updatePushStatus(app, stream, true);
		}
		return result;
	}

	@Override
	public void updateParentPlatformStatus(String platformGbID, boolean online) {
		platformMapper.updateParentPlatformStatus(platformGbID, online);
	}

	@Override
	public List<StreamProxyItem> getStreamProxyListForEnableInMediaServer(String id, boolean enable) {
		return streamProxyMapper.selectForEnableInMediaServer(id, enable);
	}


	@Override
	public Device queryVideoDeviceByChannelId( String channelId) {
		Device result = null;
		List<DeviceChannel> channelList = deviceChannelService.list(Wrappers.<DeviceChannel>lambdaQuery()
				.eq(DeviceChannel::getChannelId, channelId)).getData();
		if (channelList.size() == 1) {
			result = deviceService.getDeviceMapper().getDeviceByDeviceId(channelList.get(0).getDeviceId());
		}
		return result;
	}

	@Override
	public StreamProxyItem getStreamProxyByAppAndStream(String app, String streamId) {
		return streamProxyMapper.getByAppAndStream(app, streamId);
	}

	@Override
	public List<PlatformCatalog> getChildrenCatalogByPlatform(String platformId, String parentId) {
		return catalogMapper.selectByParentId(platformId, parentId);
	}

	@Override
	public int addCatalog(PlatformCatalog platformCatalog) {
		ParentPlatform platform = platformMapper.getParentPlatByServerGBId(platformCatalog.getPlatformId());
		if (platform == null) {
			return 0;
		}
		if (platformCatalog.getId().length() <= 8) {
			platformCatalog.setCivilCode(platformCatalog.getParentId());
		}else {
			if (platformCatalog.getId().length() != 20) {
				return 0;
			}
			if (platformCatalog.getParentId() != null) {
				switch (Integer.parseInt(platformCatalog.getId().substring(10, 13))){
					case 200:
					case 215:
						if (platformCatalog.getParentId().length() <= 8) {
							platformCatalog.setCivilCode(platformCatalog.getParentId());
						}else {
							PlatformCatalog catalog = catalogMapper.selectByPlatFormAndCatalogId(platformCatalog.getPlatformId(), platformCatalog.getParentId());
							if (catalog != null) {
								platformCatalog.setCivilCode(catalog.getCivilCode());
							}
						}
						break;
					case 216:
						if (platformCatalog.getParentId().length() <= 8) {
							platformCatalog.setCivilCode(platformCatalog.getParentId());
						}else {
							PlatformCatalog catalog = catalogMapper.selectByPlatFormAndCatalogId(platformCatalog.getPlatformId(),platformCatalog.getParentId());
							if (catalog == null) {
								logger.warn("[添加目录] 无法获取目录{}的CivilCode和BusinessGroupId", platformCatalog.getPlatformId());
								break;
							}
							platformCatalog.setCivilCode(catalog.getCivilCode());
							if (Integer.parseInt(platformCatalog.getParentId().substring(10, 13)) == 215) {
								platformCatalog.setBusinessGroupId(platformCatalog.getParentId());
							}else {
								if (Integer.parseInt(platformCatalog.getParentId().substring(10, 13)) == 216) {
									platformCatalog.setBusinessGroupId(catalog.getBusinessGroupId());
								}
							}
						}
						break;
					default:
						break;
				}
			}
		}
		int result = catalogMapper.insert(platformCatalog);
		if (result > 0) {
			DeviceChannel deviceChannel = getDeviceChannelByCatalog(platformCatalog);
			eventPublisher.catalogEventPublish(platformCatalog.getPlatformId(), deviceChannel, CatalogEvent.ADD);
		}
		return result;
	}

	private PlatformCatalog getTopCatalog(String id, String platformId) {
		PlatformCatalog catalog = catalogMapper.selectByPlatFormAndCatalogId(platformId, id);
		if (catalog.getParentId().equals(platformId)) {
			return catalog;
		}else {
			return getTopCatalog(catalog.getParentId(), platformId);
		}
	}

	@Override
	public PlatformCatalog getCatalog(String platformId, String id) {
		return catalogMapper.selectByPlatFormAndCatalogId(platformId, id);
	}

	@Override
	public int delCatalog(String platformId, String id) {
		return delCatalogExecute(id, platformId);
	}
	private int delCatalogExecute(String id, String platformId) {
		int delresult =  catalogMapper.del(platformId, id);
		DeviceChannel deviceChannelForCatalog = new DeviceChannel();
		if (delresult > 0){
			deviceChannelForCatalog.setChannelId(id);
			eventPublisher.catalogEventPublish(platformId, deviceChannelForCatalog, CatalogEvent.DEL);
		}

		List<GbStream> gbStreams = platformGbStreamMapper.queryChannelInParentPlatformAndCatalog(platformId, id);
		if (gbStreams.size() > 0){
			List<DeviceChannel> deviceChannelList = new ArrayList<>();
			for (GbStream gbStream : gbStreams) {
				DeviceChannel deviceChannel = new DeviceChannel();
				deviceChannel.setChannelId(gbStream.getGbId());
				deviceChannelList.add(deviceChannel);
			}
			eventPublisher.catalogEventPublish(platformId, deviceChannelList, CatalogEvent.DEL);
		}
		int delStreamresult = platformGbStreamMapper.delByPlatformAndCatalogId(platformId,id);
		List<PlatformCatalog> platformCatalogs = platformChannelMapper.queryChannelInParentPlatformAndCatalog(platformId, id);
		if (platformCatalogs.size() > 0){
			List<DeviceChannel> deviceChannelList = new ArrayList<>();
			for (PlatformCatalog platformCatalog : platformCatalogs) {
				DeviceChannel deviceChannel = new DeviceChannel();
				deviceChannel.setChannelId(platformCatalog.getId());
				deviceChannelList.add(deviceChannel);
			}
			eventPublisher.catalogEventPublish(platformId, deviceChannelList, CatalogEvent.DEL);
		}
		int delChannelresult = platformChannelMapper.delByCatalogId(platformId, id);
		// 查看是否存在子目录，如果存在一并删除
		List<String> allChildCatalog = getAllChildCatalog(id, platformId);
		if (!allChildCatalog.isEmpty()) {
			int limitCount = 50;
			if (allChildCatalog.size() > limitCount) {
				for (int i = 0; i < allChildCatalog.size(); i += limitCount) {
					int toIndex = i + limitCount;
					if (i + limitCount > allChildCatalog.size()) {
						toIndex = allChildCatalog.size();
					}
					delChannelresult += platformCatalogMapper.deleteAll(platformId, allChildCatalog.subList(i, toIndex));
				}
			}else {
				delChannelresult += platformCatalogMapper.deleteAll(platformId, allChildCatalog);
			}
		}
		return delresult + delChannelresult + delStreamresult;
	}

	private List<String> getAllChildCatalog(String id, String platformId) {
		List<String> catalogList = platformCatalogMapper.queryCatalogFromParent(id, platformId);
		List<String> catalogListChild = new ArrayList<>();
		if (catalogList != null && !catalogList.isEmpty()) {
			for (String childId : catalogList) {
				List<String> allChildCatalog = getAllChildCatalog(childId, platformId);
				if (allChildCatalog != null && !allChildCatalog.isEmpty()) {
					catalogListChild.addAll(allChildCatalog);
				}

			}
		}
		if (!catalogListChild.isEmpty()) {
			catalogList.addAll(catalogListChild);
		}
		return catalogList;
	}


	@Override
	public int updateCatalog(PlatformCatalog platformCatalog) {
		int result = catalogMapper.update(platformCatalog, Wrappers.<PlatformCatalog>lambdaUpdate()
				.eq(PlatformCatalog::getId, platformCatalog.getId())
				.eq(PlatformCatalog::getPlatformId, platformCatalog.getPlatformId()));

		if (result > 0) {
			DeviceChannel deviceChannel = getDeviceChannelByCatalog(platformCatalog);
			eventPublisher.catalogEventPublish(platformCatalog.getPlatformId(), deviceChannel, CatalogEvent.UPDATE);
		}
		return result;
	}

	@Override
	public int setDefaultCatalog(String platformId, String catalogId) {
		return platformMapper.update(null, Wrappers.<ParentPlatform>lambdaUpdate()
				.set(ParentPlatform::getCatalogId, catalogId)
				.set(ParentPlatform::getUpdateTime, DateUtil.getNow())
				.eq(ParentPlatform::getServerGBId, platformId));
	}

	@Override
	public List<DeviceChannel> queryCatalogInPlatform(String platformId) {
		return catalogMapper.queryCatalogInPlatform(platformId);
	}

	@Override
	public int delRelation(PlatformCatalog platformCatalog) {
		if (platformCatalog.getType() == 1) {
			DeviceChannel deviceChannel = new DeviceChannel();
			deviceChannel.setChannelId(platformCatalog.getId());
			eventPublisher.catalogEventPublish(platformCatalog.getPlatformId(), deviceChannel, CatalogEvent.DEL);
			return platformChannelMapper.delByCatalogIdAndChannelIdAndPlatformId(platformCatalog);
		}else if (platformCatalog.getType() == 2) {
			List<GbStream> gbStreams = platformGbStreamMapper.queryChannelInParentPlatformAndCatalog(platformCatalog.getPlatformId(), platformCatalog.getParentId());
			for (GbStream gbStream : gbStreams) {
				if (gbStream.getGbId().equals(platformCatalog.getId())) {
					DeviceChannel deviceChannel = new DeviceChannel();
					deviceChannel.setChannelId(gbStream.getGbId());
					eventPublisher.catalogEventPublish(platformCatalog.getPlatformId(), deviceChannel, CatalogEvent.DEL);
					return platformGbStreamMapper.delByAppAndStream(gbStream.getApp(), gbStream.getStream());
				}
			}
		}
		return 0;
	}

	@Override
	public int updateStreamGPS(List<GPSMsgInfo> gpsMsgInfos) {
		return gbStreamMapper.updateStreamGPS(gpsMsgInfos);
	}


	private DeviceChannel getDeviceChannelByCatalog(PlatformCatalog catalog) {
		ParentPlatform platform = platformMapper.getParentPlatByServerGBId(catalog.getPlatformId());
		DeviceChannel deviceChannel = new DeviceChannel();
		deviceChannel.setChannelId(catalog.getId());
		deviceChannel.setName(catalog.getName());
		deviceChannel.setDeviceId(platform.getDeviceGBId());
		deviceChannel.setManufacture("wvp-pro");
		deviceChannel.setStatus(true);
		deviceChannel.setParental(1);

		deviceChannel.setRegisterWay(1);
		deviceChannel.setParentId(catalog.getParentId());
		deviceChannel.setBusinessGroupId(catalog.getBusinessGroupId());

		deviceChannel.setModel("live");
		deviceChannel.setOwner("wvp-pro");
		deviceChannel.setSecrecy("0");
		return deviceChannel;
	}

	@Override
	public List<ParentPlatform> queryPlatFormListForGBWithGBId(String channelId, List<String> platforms) {
		return platformChannelMapper.queryPlatFormListForGBWithGBId(channelId, platforms);
	}

	@Override
	public List<ParentPlatform> queryPlatFormListForStreamWithGBId(String app, String stream, List<String> platforms) {
		if (platforms == null || platforms.size() == 0) {
			return new ArrayList<>();
		}
		return platformGbStreamMapper.queryPlatFormListForGBWithGBId(app, stream, platforms);
	}

	@Override
	public GbStream getGbStream(String app, String streamId) {
		return gbStreamService.selectByAppAndStream(app, streamId);
	}

	@Override
	public void delCatalogByPlatformId(String serverGBId) {
		catalogMapper.delByPlatformId(serverGBId);
	}

	@Override
	public void delRelationByPlatformId(String serverGBId) {
		platformGbStreamMapper.delByPlatformId(serverGBId);
		platformChannelMapper.delByPlatformId(serverGBId);
	}

	@Override
	public PlatformCatalog queryDefaultCatalogInPlatform(String platformId) {
		return catalogMapper.selectDefaultByPlatFormId(platformId);
	}

	@Override
	public List<ChannelSourceInfo> getChannelSource(String platformId, String gbId) {
		return platformMapper.getChannelSource(platformId, gbId);
	}

	@Override
	public void updateChannelPosition(DeviceChannel deviceChannel) {
		if (deviceChannel.getChannelId().equals(deviceChannel.getDeviceId())) {
			deviceChannel.setChannelId(null);
		}
		if (deviceChannel.getGpsTime() == null) {
			deviceChannel.setGpsTime(DateUtil.getNow());
		}

		deviceChannelService.updatePosition(deviceChannel);
	}

	@Override
	public void cleanContentForPlatform(String serverGBId) {
//		List<PlatformCatalog> catalogList = catalogMapper.selectByPlatForm(serverGBId);
//		if (catalogList.size() > 0) {
//			int result = catalogMapper.delByPlatformId(serverGBId);
//			if (result > 0) {
//				List<DeviceChannel> deviceChannels = new ArrayList<>();
//				for (PlatformCatalog catalog : catalogList) {
//					deviceChannels.add(getDeviceChannelByCatalog(catalog));
//				}
//				eventPublisher.catalogEventPublish(serverGBId, deviceChannels, CatalogEvent.DEL);
//			}
//		}
		catalogMapper.delByPlatformId(serverGBId);
		platformChannelMapper.delByPlatformId(serverGBId);
		platformGbStreamMapper.delByPlatformId(serverGBId);
	}

	@Override
	public List<DeviceChannel> queryChannelWithCatalog(String serverGBId) {
		return deviceChannelMapper.queryChannelWithCatalog(serverGBId);
	}
}
