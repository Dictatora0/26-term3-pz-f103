package com.iteaj.framework.spi.iot;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.JarClassLoader;
import cn.hutool.core.util.ServiceLoaderUtil;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.exception.FrameworkException;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.consts.ProtocolImplMode;
import com.iteaj.framework.spi.iot.protocol.AbstractProtocolModelApi;
import com.iteaj.framework.spi.iot.protocol.ProtocolModel;
import com.iteaj.framework.spi.iot.protocol.ProtocolSupplierException;
import com.iteaj.iot.FrameworkComponent;
import com.iteaj.iot.Message;
import com.iteaj.iot.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 协议提供管理
 */
public class ProtocolSupplierManager {

    private static FrameworkProperties properties;
    public static final String PROTOCOL_JAR_DIR = "protocol";
    public static final String PROTOCOL_JAR_TEMP_DIR = "protocol_temp";
    private static ProtocolSupplierManager instance = new ProtocolSupplierManager();
    private static Logger logger = LoggerFactory.getLogger(ProtocolSupplierManager.class);
    private static Map<Class, DeviceProtocolSupplier> messageMap = new HashMap<>(8);
    private static Map<String, DeviceProtocolSupplier> supplierMap = new HashMap<>(8);

    private ProtocolSupplierManager() { }

    public static ProtocolSupplierManager build(FrameworkProperties properties) {
        ProtocolSupplierManager.properties = properties; return instance;
    }

    /**
     * 协议提供实例
     * @return
     */
    public static ProtocolSupplierManager getInstance() {
        return instance;
    }

    /**
     * 注册一个协议
     * @param supplier
     * @return
     */
    public static ProtocolSupplierManager register(DeviceProtocolSupplier supplier) {
        ProtocolModel protocol = supplier.getProtocol();
        if(supplier == null || protocol == null) {
            throw new FrameworkException("参数错误");
        }

        if(contain(protocol.getCode())) {
            throw new FrameworkException("协议已存在["+protocol.getCode()+"]");
        }

        logger.info("<<<注册协议提供成功 {} - 版本: {} - 实现: {} - 说明: {}", protocol.getCode()
                , supplier.getVersion(), supplier.getImplMode().getDesc(), supplier.getDesc());
        supplierMap.put(protocol.getCode(), supplier);
        return instance;
    }

    /**
     * 注册一个协议
     * @param supplier
     * @param messageClass 报文类
     * @return
     */
    public static ProtocolSupplierManager register(Class<? extends Message> messageClass, DeviceProtocolSupplier supplier) {
        ProtocolModel protocol = supplier.getProtocol();
        DeviceProtocolSupplier protocolSupplier = messageMap.get(messageClass);

        if(protocolSupplier != null) {
            throw new FrameworkException("协议已存在["+messageClass.getSimpleName()+"]");
        }

        if(supplier == null || protocol == null) {
            throw new FrameworkException("参数错误");
        }

        messageMap.put(messageClass, supplier);
        return instance;
    }

    /**
     * 从jar文件读取, 使用spi机制
     * @param jarFileOrDir jar文件或者jar文件目录
     * @return
     */
    public static List<DeviceProtocolSupplier> load(File jarFileOrDir) {
        return load(jarFileOrDir, false);
    }

    /**
     * 从jar文件读取, 使用spi机制
     * @param isClose 是否关闭类加载器
     * @param jarFileOrDir jar文件或者jar文件目录
     * @return
     */
    public static List<DeviceProtocolSupplier> load(File jarFileOrDir, boolean isClose) {
        List<DeviceProtocolSupplier> loads = new ArrayList<>();
        if(!jarFileOrDir.exists()) { // 文件不存在
            return Collections.emptyList();
        }

        JarClassLoader jarClassLoader = null;
        try {
            if(jarFileOrDir.isDirectory()) {
                jarClassLoader = JarClassLoader.load(jarFileOrDir);
            } else {
                jarClassLoader = JarClassLoader.loadJar(jarFileOrDir);
            }

            ServiceLoaderUtil.load(DeviceProtocolSupplier.class, jarClassLoader).forEach(item -> {
                // 只加载上传的jar包, 即实现类型为Jar
                if(item.getImplMode() == ProtocolImplMode.Jar) {
                    loads.add(item);
                }
            });
        } catch (FrameworkException se) {
            throw new ServiceException(se.getMessage(), se);
        } catch (Exception e) {
            throw new ServiceException("导入Jar失败", e);
        } finally {
            if(jarClassLoader != null && isClose) {
                try {
                    jarClassLoader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return loads;
    }

    /**
     * 从指定的jar目录下面加载jar包 并解析出里面的协议提供
     * @param jarDir
     * @return
     */
    public static List<DeviceProtocolSupplier> load(String jarDir) {
        return load(new File(jarDir));
    }

    /**
     * 加载协议目录下的所有jar
     * @return
     */
    public static List<DeviceProtocolSupplier> loadFromProtocol() {
        String location = properties.getWeb().getUpload().getLocationPath();
        return load(location + File.separator + PROTOCOL_JAR_DIR);
    }

    /**
     * 从协议目录加载指定jar文件
     * @param fileName
     * @return
     */
    public static List<DeviceProtocolSupplier> loadFromProtocol(String fileName) {
        String location = properties.getWeb().getUpload().getLocationPath();
        File file = new File(location + File.separator + PROTOCOL_JAR_DIR + File.separator + fileName);
        return load(file);
    }

    /**
     * 从协议临时目录加载指定jar文件
     * @param fileName
     * @return
     */
    public static List<DeviceProtocolSupplier> loadFromTempProtocol(String fileName) {
        String location = properties.getWeb().getUpload().getLocationPath();
        File file = new File(location + File.separator + PROTOCOL_JAR_TEMP_DIR + File.separator + fileName);
        return load(file, true);
    }

    /**
     * 获取指定协议提供
     * @param code
     * @return
     */
    public static DeviceProtocolSupplier get(String code) {
        return supplierMap.get(code);
    }

    /**
     * 获取指定协议提供
     * @param messageClass
     * @return
     */
    public static DeviceProtocolSupplier get(Class<? extends Message> messageClass) {
        return messageMap.get(messageClass);
    }

    /**
     * 获取协议提供的数据
     * @param protocol
     * @return
     */
    public static Optional<DataSupplier> getDataSupplier(Protocol protocol) {
        Message message = protocol.requestMessage();
        DeviceProtocolSupplier supplier = ProtocolSupplierManager.get(message.getClass());
        if(supplier != null) {
            // 先从缓存中获取数据
            Object dataCache = protocol.getParam(AbstractProtocolModelApi.INVOKE_API_DATA_SUPPLIER);
            if(dataCache instanceof DataSupplier) {
                return Optional.of((DataSupplier) dataCache);
            }

            // 缓存中没有则从协议提供方获取数据
            Object param = protocol.getParam(AbstractProtocolModelApi.INVOKE_API);
            Object value = protocol.getParam(AbstractProtocolModelApi.INVOKE_API_VALUE);
            DataSupplier dataSupplier = supplier.getDataSupplier(protocol, (ProtocolModelApiInvokeParam) param, value);
            if(dataSupplier != null) { // 将数据加入缓存
                protocol.addParam(AbstractProtocolModelApi.INVOKE_API_DATA_SUPPLIER, dataSupplier);
            }
            return Optional.ofNullable(dataSupplier);
        } else {
            return Optional.empty();
        }
    }

    /**
     * 协议提供列表
     * @return
     */
    public static Collection<DeviceProtocolSupplier> suppliers() {
        return Collections.unmodifiableCollection(supplierMap.values());
    }

    /**
     * 内置的协议提供列表
     * @return
     */
    public static Collection<DeviceProtocolSupplier> internals() {
        List<DeviceProtocolSupplier> suppliers = supplierMap.values().stream()
                .filter(item -> item.getImplMode() == ProtocolImplMode.Internal)
                .collect(Collectors.toList());
        return Collections.unmodifiableCollection(suppliers);
    }

    /**
     * 获取指定协议码
     * @param messageClass
     * @return
     */
    public static String getCode(Class<? extends Message> messageClass) {
        DeviceProtocolSupplier supplier = messageMap.get(messageClass);
        return supplier == null ? null : supplier.getProtocol().getCode();
    }

    /**
     * 是否包含某个协议
     * @param code
     * @return
     */
    public static boolean contain(String code) {
        return supplierMap.containsKey(code);
    }

    public static DeviceProtocolSupplier copyTempToProtocol(String fileName) {
        String location = properties.getWeb().getUpload().getLocationPath();
        File target = new File(location + File.separator + PROTOCOL_JAR_DIR + File.separator + fileName);
        File temp = new File(location + File.separator + PROTOCOL_JAR_TEMP_DIR + File.separator + fileName);
        if(!temp.exists()) {
            throw new ProtocolSupplierException("Jar文件不存在["+fileName+"]");
        }

        FileUtil.copy(temp, target, true);
        return load(target).get(0);
    }

    /**
     * 移除指定协议
     * @param code
     * @return
     */
    public static DeviceProtocolSupplier remove(String code) {
        DeviceProtocolSupplier supplier = supplierMap.get(code);
        if(supplier != null) {
            // 系统内置不允许删除
            if(supplier.getImplMode() == ProtocolImplMode.Internal) {
                throw new ProtocolSupplierException("内置协议["+code+"]不允许删除");
            }

            ClassLoader classLoader = supplier.getClass().getClassLoader();
            if(classLoader instanceof JarClassLoader) {
                try {
                    ((JarClassLoader) classLoader).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            FrameworkComponent component = supplier.getComponent();
            if(component != null) {
                messageMap.remove(component.getMessageClass());
            }

            return supplierMap.remove(code);
        }

        return null;
    }
}
