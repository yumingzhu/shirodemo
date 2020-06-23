package com.yumingzhu.shirodemo.config.redis;

import org.crazycake.shiro.serializer.ObjectSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author
 * @since 2020/5/16
 */
@Slf4j
public class ObjectSerializerAdapter implements RedisSerializer<Object> {

    private ObjectSerializer redisSerializer = new ObjectSerializer();

    @Override
    public byte[] serialize(Object o) throws SerializationException {
        try {
            return redisSerializer.serialize(o);
        } catch (org.crazycake.shiro.exception.SerializationException e) {
            log.error("", e);
        }
        return null;
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {

        try {
            return redisSerializer.deserialize(bytes);
        } catch (org.crazycake.shiro.exception.SerializationException e) {
            log.error("", e);
        }
        return null;
    }
}
