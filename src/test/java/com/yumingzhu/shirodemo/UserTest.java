package com.yumingzhu.shirodemo;

import javax.annotation.Resource;

import com.google.common.collect.Lists;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yumingzhu.shirodemo.dao.UserMapper;
import com.yumingzhu.shirodemo.pojo.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author yumigzhu
 * @Date 2020/6/22 14:12
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTest {

	@Resource
	private UserMapper userMapper;

	@Test
	public void addUser() {

		// 加密方式
		String hashAlgorithmName = "MD5";
		// 加密次数
		int hashIterations = 2;
		SimpleHash result = new SimpleHash(hashAlgorithmName, "123456", null, hashIterations);
		String pwd = result.toString();

		User user = new User();
		user.setName("root");
		user.setPwd(pwd);
		userMapper.insert(user);

	}


}
