package com.iteaj.framework.logger;

import com.iteaj.framework.IVOption;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LoggerManager {

    private static Map<String, LoggerFilter> filters = new HashMap<>();
    private static List<LoggerPushService> services = new ArrayList<>();

    public static void push(Level level, PushParams params) {
        if(!services.isEmpty()) {
            services.forEach(pushService -> {
                try {
                    pushService.push(level, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void addFilter(final LoggerFilter filter) {
        if(!filters.containsKey(filter.type())) {
            filters.put(filter.type(), filter);
        }
    }

    public static boolean containsFilter(final String type) {
        return filters.containsKey(type);
    }

    public static LoggerFilter getFilter(final String type) {
        return filters.get(type);
    }

    public static List<IVOption> filterOptions() {
        final List<IVOption> options = new ArrayList<>();
        filters.forEach((key, value) -> options.add(new IVOption(value.name(), key).setChildren(value.subOptions())));
        return options;
    }

    public static LoggerFilter removeFilter(final String type) {
        return filters.remove(type);
    }

    public static void addPushService(final LoggerPushService service) {
        services.add(service);
    }
}
