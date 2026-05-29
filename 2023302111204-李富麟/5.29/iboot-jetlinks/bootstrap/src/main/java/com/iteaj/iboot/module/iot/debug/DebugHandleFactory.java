package com.iteaj.iboot.module.iot.debug;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DebugHandleFactory implements InitializingBean {

    private List<DebugHandle> debugHandles;

    private Map<String, DebugHandle> handleMap = new HashMap<>(8);

    public DebugHandleFactory(List<DebugHandle> debugHandles) {
        this.debugHandles = debugHandles;
    }

    public Optional<DebugHandle> getHandle(String type) {
        return Optional.ofNullable(handleMap.get(type));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(!CollectionUtils.isEmpty(debugHandles)) {
            this.handleMap = this.debugHandles.stream()
                    .collect(Collectors.toMap(item -> item.type(), item -> item));
        }
    }
}
