package com.iteaj.framework.spi.iot;

import com.iteaj.framework.IVOption;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.spi.iot.resolver.DefaultResolver;

import java.util.*;

public class DataValueResolverFactory {

    private final static String DEFAULT_KEY = "DEFAULT";
    private final static DataValueResolver defaultResolver = new DefaultResolver();
    private final static Map<String, DataValueResolver> resolverMap = new HashMap<>();

    static {
        resolverMap.put(DEFAULT_KEY, defaultResolver);
    }

    public static DataValueResolver get(String key) {
        return resolverMap.get(key);
    }

    public static DataValueResolver getDefault() {
        return defaultResolver;
    }

    public static boolean contains(String key) {
        return resolverMap.containsKey(key);
    }

    public static void register(String key, DataValueResolver resolver) {
        resolverMap.putIfAbsent(key, resolver);
    }

    public static DataValueResolver update(String key, DataValueResolver resolver) {
        if(Objects.equals(DEFAULT_KEY, key)) {
            throw new ServiceException("不支持替换默认解析器");
        }

        return resolverMap.put(key, resolver);
    }

    public static Object resolver(UpModelAttr attr, Object value) {
        DataValueResolver resolver = defaultResolver;
        if(attr.getResolver() != null) {
            DataValueResolver valueResolver = DataValueResolverFactory.get(attr.getResolver());
            resolver = valueResolver == null ? resolver : valueResolver;
        }

        return resolver.resolver(attr, value);
    }

    public static List<IVOption> options() {
        List<IVOption> options = new ArrayList<>();
        resolverMap.forEach((key, value) -> options.add(new IVOption(value.name(), key)));
        return options;
    }
}
