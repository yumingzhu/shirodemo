package com.yumingzhu.shirodemo.config.redis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;

import com.yumingzhu.shirodemo.pojo.User;

/**
 * @Description 基于shiro 实现账号登陆人数控制
 * @Author yumigzhu
 * @Date 2020/6/23 16:26
 */
public class KickoutSessionControlFilter extends AccessControlFilter {

	private String prefix = "shiro:session";
	/**
	 * 允许登陆的session 数量
	 */
	private Integer maxSessionCount;

	private RedisTemplate redisTemplate;

	private SessionManager sessionManager;

	@Override
	protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o)
			throws Exception {
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
		Subject subject = getSubject(servletRequest, servletResponse);
		//如果没有登录，不进行多出登录判断
		if (!subject.isAuthenticated() && !subject.isRemembered()) {
			return true;
		}
		Session session = subject.getSession();
		User user = (User) subject.getPrincipal();
		Serializable sessionId = session.getId();
		ArrayList<Serializable> deque = (ArrayList<Serializable>) redisTemplate.opsForList()
				.range(prefix + user.getName(), 0, -1);
		//如果队列里没有此sessionId，且用户没有被踢出,当前session放入队列
		if (!deque.contains(sessionId) && session.getAttribute("logOut") == null) {
			deque.add(sessionId);
			redisTemplate.opsForList().leftPush(prefix + user.getName(), sessionId);
		}
		while (deque.size() > getMaxSessionCount()) {
			Serializable kickoutSessionId = (Serializable) new LinkedList(deque).removeFirst();
			deque.remove(kickoutSessionId);
			redisTemplate.opsForList().remove(prefix + user.getName(), 1, kickoutSessionId);

			try {
				Session logOutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
				if (logOutSession != null) {
					logOutSession.setAttribute("logOut", true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (session.getAttribute("logOut") != null) {
			subject.logout();
			saveRequest(servletRequest);
			//返回401
			HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
			httpResponse.setStatus(HttpStatus.OK.value());
			httpResponse.setContentType("application/json;charset=utf-8");
			httpResponse.getWriter().write("{\"code\":" + "401" + ", \"msg\":\"" + "当前帐号在其他地方登录，您已被强制下载！" + "\"}");

			return false;
		}

		return true;
	}

	public Integer getMaxSessionCount() {
		return maxSessionCount;
	}

	public void setMaxSessionCount(Integer maxSessionCount) {
		this.maxSessionCount = maxSessionCount;
	}

	public RedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
}
