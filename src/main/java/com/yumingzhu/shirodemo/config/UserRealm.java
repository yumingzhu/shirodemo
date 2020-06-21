package com.yumingzhu.shirodemo.config;

import com.yumingzhu.shirodemo.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

public class UserRealm extends AuthorizingRealm {
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println(">>授权");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println(">>认证");

        String username="root";
        String password="123456";
        UsernamePasswordToken  token= (UsernamePasswordToken) authenticationToken;
         if(!token.getUsername().equals(username)){
             return  null;
         }
        Subject subject = SecurityUtils.getSubject();

        return new SimpleAuthenticationInfo("",password,"");
    }
}
