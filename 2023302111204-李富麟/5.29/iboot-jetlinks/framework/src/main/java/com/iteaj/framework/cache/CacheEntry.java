package com.iteaj.framework.cache;

/**
 * create time: 2021/3/21
 *
 * @author iteaj
 * @since 1.0
 */
public class CacheEntry {

    /**
     * 缓存过期时间(毫秒)
     */
    private long expire;

    /**
     * 缓存名称
     */
    private String name;

    /**
     * 反序列类型对象
     */
    private Class<?> clazz;

    /**
     * 序列化类型
     */
    private SerializerType type;

    public CacheEntry(String name, long expire, SerializerType type) {
        this(name, expire, Object.class, type);
    }

    public CacheEntry( String name, long expire, Class<?> clazz, SerializerType type) {
        this.expire = expire;
        this.name = name;
        this.clazz = clazz;
        this.type = type;
    }

    public long getExpire() {
        return expire;
    }

    public CacheEntry setExpire(long expire) {
        this.expire = expire;
        return this;
    }

    public String getName() {
        return name;
    }

    public CacheEntry setName(String name) {
        this.name = name;
        return this;
    }

    public SerializerType getType() {
        return type;
    }

    public CacheEntry setType(SerializerType type) {
        this.type = type;
        return this;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public CacheEntry setClazz(Class<?> clazz) {
        this.clazz = clazz;
        return this;
    }
}
