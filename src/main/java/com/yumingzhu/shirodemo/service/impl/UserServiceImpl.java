package com.yumingzhu.shirodemo.service.impl;

import javax.annotation.Resource;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yumingzhu.shirodemo.dao.UserMapper;
import com.yumingzhu.shirodemo.pojo.User;
import com.yumingzhu.shirodemo.service.UserService;

/**
 * @Description TODO
 * @Author yumigzhu
 * @Date 2020/6/22 14:21
 */
@Service
public class UserServiceImpl implements UserService {

	@Resource
	UserMapper userMapper;

	@Override
	public User getUserByName(String name) {
		return userMapper.selectOne(Wrappers.<User> lambdaQuery().eq(User::getName, name));
	}

}
