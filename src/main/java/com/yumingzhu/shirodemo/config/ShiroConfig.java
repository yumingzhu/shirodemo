package com.yumingzhu.shirodemo.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.IRedisManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.crazycake.shiro.serializer.ObjectSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
public class ShiroConfig {

	static  int globalSessionTimeout = 60 * 60 * 1000;

	//shiroFilterFactoryBean
	@Bean
	public ShiroFilterFactoryBean getShiroFilterFactoryBean(
			@Qualifier("securityManager") DefaultWebSecurityManager defaultWebSecurityManager) {
		ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
		bean.setSecurityManager(defaultWebSecurityManager);

		Map<String, String> mapFilter = new LinkedHashMap<>();
		mapFilter.put("/user/add", "authc");
		mapFilter.put("/user/update", "perms[user:update]");

		bean.setFilterChainDefinitionMap(mapFilter);

		//设置登录请求
		bean.setLoginUrl("/toLogin");
		//设置未授权页面
		bean.setUnauthorizedUrl("/noauth");
		return bean;

	}

	//DafaultWebSecurituManager
	@Bean(name = "securityManager")
	public DefaultWebSecurityManager getDefaultWebSecurityManager(@Qualifier("userRealm") UserRealm userRealm) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		//关联UserRealm
		securityManager.setRealm(userRealm);
		/**
		 * 设置session 管理器  用于存储subject 的认证信息
		 */
		securityManager.setSessionManager(sessionManager());
		/**
		 * 设置shiro缓存管理器,  可以存储授权信息
		 */
		securityManager.setCacheManager(cacheManager());
		return securityManager;
	}

	@Bean
	public MemoryConstrainedCacheManager memoryCacheManger(){
		return new MemoryConstrainedCacheManager();
	}

	@Bean
	public RedisCacheManager cacheManager() {
		RedisCacheManager redisCacheManager = new RedisCacheManager();
		redisCacheManager.setRedisManager(redisManager());
		redisCacheManager.setPrincipalIdFieldName("name");
		redisCacheManager.setExpire(30);
		return redisCacheManager;
	}

	@Bean
	public IRedisManager redisManager() {
		return new ShiroRedisManager();
	}

	@Bean
	public RedisSessionDAO redisSessionDAO() {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(redisManager());
		redisSessionDAO.setKeyPrefix("shiro:session");
		// redis作为session缓存中间层，不该擅自删除，session的过期操作应该交给shiro调度器管理
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		serializer.setObjectMapper(objectMapper);
		redisSessionDAO.setKeySerializer(new SpringStringRedisSerializerAdapter());
		redisSessionDAO.setValueSerializer(new ObjectSerializer());
		return redisSessionDAO;
	}

	/***
	 * 配置shiro session 管理器， 可以管理subject 的生命周期
	 * @return
	 */
	@Bean
	public SessionManager sessionManager() {
		//使用默认的web session管理器
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		//设置默认 session  操作，存储的环境
//		sessionManager.setSessionDAO(new MemorySessionDAO());
		sessionManager.setSessionDAO(redisSessionDAO());
		sessionManager.setGlobalSessionTimeout(globalSessionTimeout);
		sessionManager.setSessionIdCookie(sessionIdCookie());
		return sessionManager;
	}




	@Bean("sessionIdCookie")
	public SimpleCookie sessionIdCookie() {
		//这个参数是cookie的名称
		SimpleCookie simpleCookie = new SimpleCookie("sid");
		//setcookie的httponly属性如果设为true的话，会增加对xss防护的安全系数。它有以下特点：

		//setcookie()的第七个参数
		//设为true后，只能通过http访问，javascript无法访问
		//防止xss读取cookie
		simpleCookie.setHttpOnly(true);
		simpleCookie.setPath("/");
		//maxAge=-1表示浏览器关闭时失效此Cookie
		simpleCookie.setMaxAge(7 * 24 * 60 * 60);
		return simpleCookie;
	}

	/**
	 * 会话id生成器
	 * @return
	 */
	@Bean
	public SessionIdGenerator sessionIdGenerator() {
		return new JavaUuidSessionIdGenerator();
	}

	//创建realm对象 ，需要自定义
	@Bean(name = "userRealm")
	public UserRealm userRealm() {
		UserRealm userRealm = new UserRealm();
		//设置凭证匹配器，用于做密码匹配用的
		userRealm.setCredentialsMatcher(hashedCredentialsMatcher());
		return userRealm;
	}

	@Bean(name = "credentialsMatcher")
	public HashedCredentialsMatcher hashedCredentialsMatcher() {
		CustomerMatcher hashedCredentialsMatcher = new CustomerMatcher();
		hashedCredentialsMatcher.setHashAlgorithmName("md5");
		hashedCredentialsMatcher.setHashIterations(2);
		hashedCredentialsMatcher.setStoredCredentialsHexEncoded(true);



		return hashedCredentialsMatcher;
	}
}
