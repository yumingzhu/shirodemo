package com.yumingzhu.shirodemo.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: lijincan
 * @date: 2020年02月26日 12:51
 * @Description: TODO
 */
@Controller
public class MyController {

	//    @Autowired
	//    UserService userService;

	@RequestMapping({ "/", "/index" })
	public String toIndex(Model model) {
		model.addAttribute("msg", "hello shiro");
		return "index";
	}

	@RequiresPermissions("user:add")
	@RequestMapping("/user/add")
	public String add() {
		return "user/add";
	}

	@RequiresPermissions("user:update")
	@RequestMapping("/user/update")
	public String update() {
		return "user/update";
	}

	@RequestMapping("/toLogin")
	public String tologin() {
		return "login";
	}

	@RequestMapping("/login")
	public String login(String username, String password, Model model) {
		//获取当前用户
		Subject subject = SecurityUtils.getSubject();
		//封装用户登录数据
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		//        token.setRememberMe(true);
		try {
			subject.login(token);
			return "index";
		} catch (ExcessiveAttemptsException e) {//用户名不存在
			model.addAttribute("msg", "账号已经被锁定");
			return "login";
		} catch (UnknownAccountException e) {//用户名不存在
			model.addAttribute("msg", "用户名错误");
			return "login";
		} catch (IncorrectCredentialsException e) {
			model.addAttribute("msg", "密码错误");
			return "login";
		}
	}

	@RequestMapping("/noauth")
	public String unauthorized() {
		return "noauth";
	}

	@RequestMapping("/logout")
	public String logout() {
		SecurityUtils.getSubject().logout();

		return "logout";

	}
}
