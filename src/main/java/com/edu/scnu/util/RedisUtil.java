package com.edu.scnu.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * @ClassName redisUtil
 * @Description TODO
 * @Author Administrator
 * @Date 2019-04-28 14:22
 * @Version 1.0
 **/
@Component
public class RedisUtil {

    @Autowired
    private JedisPool jedisPool;

    private Jedis getResource() {
        return jedisPool.getResource();
    }

    public byte[] set(byte[] key, byte[] value) {
        Jedis jedis = getResource();
        try {
            jedis.set(key, value);
        } finally {
            jedis.close();
        }
        return value;
    }

    public void expire(byte[] key, int time) {
        Jedis jedis = getResource();
        try {
            jedis.expire(key, time);
        } finally {
            jedis.close();
        }
    }

    public byte[] get(byte[] key) {
        Jedis jedis = getResource();
        try {
            return jedis.get(key);
        } finally {
            jedis.close();
        }
    }

    public void del(byte[] key) {
        Jedis jedis = getResource();
        try {
            jedis.del(key);
        } finally {
            jedis.close();
        }
    }

    public Set<byte[]> keys(String prefix) {
        Jedis jedis = getResource();
        try {
            return jedis.keys((prefix + "*").getBytes());
        } finally {
            jedis.close();
        }
    }
}
