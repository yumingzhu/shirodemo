package com.yumingzhu.shirodemo.config;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import com.yumingzhu.shirodemo.pojo.User;
import com.yumingzhu.shirodemo.service.UserService;

public class UserRealm extends AuthorizingRealm {

	@Resource
	UserService userService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
		System.out.println(">>授权");
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		User user = (User) principalCollection.getPrimaryPrincipal();
		String permission = user.getPermission();
		info.addStringPermission(permission);
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {
		System.out.println(">>认证");

		UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
		String username = token.getUsername();
		User user = userService.getUserByName(username);
		if (!username.equals(user.getName())) {
			return null;
		}
		SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(user, user.getPwd(),
				getName());

		return simpleAuthenticationInfo;
	}
}
