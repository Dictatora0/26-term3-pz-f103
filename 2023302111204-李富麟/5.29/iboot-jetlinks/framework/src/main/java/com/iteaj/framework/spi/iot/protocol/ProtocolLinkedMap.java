package com.iteaj.framework.spi.iot.protocol;

import java.util.LinkedHashMap;

public class ProtocolLinkedMap<V> extends LinkedHashMap<String, V> {

    public ProtocolLinkedMap add(String key, V value) {
        this.put(key, value); return this;
    }
}
