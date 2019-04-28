package com.edu.scnu.web.shiro;

import com.edu.scnu.util.RedisUtil;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Collection;
import java.util.Set;

/**
 * @ClassName RedisCache
 * @Description TODO
 * @Author Administrator
 * @Date 2019-04-28 22:25
 * @Version 1.0
 **/
@Component
public class RedisCache<K, V> implements Cache<K, V> {

    @Autowired
    private RedisUtil redisUtil;

    private final String PERSSION_PREFIX = "permission:";

    private byte[] getKey(K k) {
        return (PERSSION_PREFIX + k.toString()).getBytes();
    }

    @Override
    public V get(K k) throws CacheException {
        System.out.println("从缓存中获取数据");
        byte[] value = redisUtil.get(getKey(k));
        if (value != null) {
            return (V) SerializationUtils.deserialize(value);
        }
        return null;
    }

    @Override
    public V put(K k, V v) throws CacheException {
        System.out.println("设置缓存数据...");
        byte[] key = getKey(k);
        byte[] value = SerializationUtils.serialize(v);
        redisUtil.set(key, value);
        redisUtil.expire(key, 600);
        return null;
    }

    @Override
    public V remove(K k) throws CacheException {
        byte[] key = getKey(k);
        byte[] value = redisUtil.get(key);
        redisUtil.del(key);
        if (value != null) {
            return (V) SerializationUtils.deserialize(value);
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<K> keys() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }
}
