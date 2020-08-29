package com.yumingzhu.shirodemo.config;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 自定义 凭证匹配器, 实现输入密码错误超过三次抛出异常
 * @Author yumigzhu
 * @Date 2020/6/23 15:50
 */

public class CustomerMatcher extends HashedCredentialsMatcher {

	@Resource
	RedisTemplate redisTemplate;

	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;

		String username = usernamePasswordToken.getUsername();
        AtomicInteger errorNum = new AtomicInteger(0);//初始化错误登录次数

        Integer value = (Integer) redisTemplate.opsForValue().get("login:error:" + username);
        if(value!=null&&value!=0){
            errorNum = new AtomicInteger(value);
        }
        if (errorNum.get() >= 3) { //如果用户错误登录次数超过3次
			throw new ExcessiveAttemptsException(); //抛出账号锁定异常类
		}
		boolean matches = super.doCredentialsMatch(usernamePasswordToken, info); //判断用户是否可用，即是否为正确的账号密码
		if (matches) {
			redisTemplate.delete("login:error:" + username);
		} else {
			redisTemplate.opsForValue().set("login:error:" + username, errorNum.incrementAndGet(), Duration.ofHours(1));
		}

		return matches;

	}
}
