package com.wcy.dubboservice.gmall.impl;

import java.util.List;

import com.wcy.dubbotestinterface.bean.UserAddress;
import com.wcy.dubbotestinterface.service.UserService;
import org.springframework.util.StringUtils;



public class UserServiceStub implements UserService {
	
	private final UserService userService;
	

	/**
	 * 传入的是userService远程的代理对象
	 * @param userService
	 */
	public UserServiceStub(UserService userService) {
		super();
		this.userService = userService;
	}


	@Override
	public List<UserAddress> getUserAddressList(String userId) {
		// TODO Auto-generated method stub
		System.out.println("UserServiceStub.....");
		if(!StringUtils.isEmpty(userId)) {
			return userService.getUserAddressList(userId);
		}
		return null;
	}

}