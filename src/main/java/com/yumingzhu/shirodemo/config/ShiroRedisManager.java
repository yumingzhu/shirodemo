package com.yumingzhu.shirodemo.config;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.crazycake.shiro.IRedisManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author yumingzhu
 * @since 2020/06/22
 */
@Slf4j
public class ShiroRedisManager implements IRedisManager {

    @Resource(name = "shiroRedisTemplate")
    RedisTemplate shiroRedisTemplate;

    @Override
    public byte[] get(byte[] key) {
        String k = byte2String(key);
        Object value = null;
        byte[] bytes = null;
        if (k != null) {
            value = shiroRedisTemplate.opsForValue().get(k);
        }
        try {
            if (value != null) {
                bytes = shiroRedisTemplate.getValueSerializer().serialize(value);
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return bytes;
    }

    @Override
    public byte[] set(byte[] key, byte[] value, int expire) {
        byte[] bytes = null;
        String k = byte2String(key);
        if (Objects.isNull(k)) {
            return bytes;
        }
        Object v = byte2Object(value);

        if (expire >= 0) {
            shiroRedisTemplate.opsForValue().set(k, v, expire, TimeUnit.SECONDS);
        } else {
            shiroRedisTemplate.opsForValue().set(k, v);
        }
        return value;
    }

    @Override
    public void del(byte[] key) {
        String k = byte2String(key);
        if (k == null) {
            return;
        }
        shiroRedisTemplate.delete(k);
    }

    @Override
    public Long dbSize(byte[] pattern) {
        AtomicLong atomicLong = new AtomicLong();
        String matchKey = byte2String(pattern);
        shiroRedisTemplate.execute((RedisCallback<Void>) connection -> {
            Cursor<byte[]> cursor =
                connection.scan(new ScanOptions.ScanOptionsBuilder().match(matchKey).count(1000).build());
            Long size = 0L;
            while (cursor.hasNext()) {
                atomicLong.getAndAdd(1L);
            }
            return null;
        });
        return atomicLong.longValue();
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        Set<String> keys = shiroRedisTemplate.keys(byte2String(pattern));
        Set<byte[]> bytes = new HashSet<>(keys.size());
        for (String key : keys) {
            bytes.add(key.getBytes());
        }
        return bytes;
    }

    private String byte2String(byte[] bytes) {
        Object deserialize = shiroRedisTemplate.getKeySerializer().deserialize(bytes);
        return (String)deserialize;
    }

    private Object byte2Object(byte[] bytes) {
        return shiroRedisTemplate.getValueSerializer().deserialize(bytes);
    }
}
