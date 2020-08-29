package com.yumingzhu.shirodemo.config;

/**
 * @Description TODO
 * @Author yumigzhu
 * @Date 2020/6/23 10:23
 */

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.crazycake.shiro.exception.SerializationException;
import org.crazycake.shiro.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 因为shiro自定义了一套序列化的规则，spring自带的Sring序列化器无法满足shiro内部要求 所以 我们需要一个适配器，把spring的string序列化器适配成能够满足Shiro要求的序列化器
 */
public class SpringStringRedisSerializerAdapter implements RedisSerializer<String> {

	public SpringStringRedisSerializerAdapter() {
		stringRedisSerializer = new StringRedisSerializer(getCharset());
	}

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private Charset charset = null;

	/**
	 * 默认就使用的是UF-8编码，因此我们无需改变
	 */
	private StringRedisSerializer stringRedisSerializer = null;

	@Override
	public byte[] serialize(String s) throws SerializationException {
		return stringRedisSerializer.serialize(s);
	}

	@Override
	public String deserialize(byte[] bytes) throws SerializationException {
		return stringRedisSerializer.deserialize(bytes);
	}

	public Charset getCharset() {
		if (charset != null) {
			return charset;
		}
		return DEFAULT_CHARSET;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}
}
