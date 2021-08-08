package com.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dao.UserDao;
import com.domain.User;

@Service
public class UserServiceImpl implements UserService{
	
	@Resource(name = "userDao")
	private UserDao userDao;

	/**
	 * 	判断用户名时候存在
	 */
	@Override
	public boolean existUser(String name) {
		User user=userDao.queryByName(name);
		if(user==null) {
			return false;
		}
		return true;
	}

	/*
	 * 	登录
	 */
	@Override
	public User loginByUser(String name, String password) {
		return userDao.queryByNameAndPassword(name, password);
	}

	/**
	 * 	注册
	 */
	@Override
	public int createUser(User user) {
		userDao.insertUser(user);
		int id=userDao.queryIdByName(user.getName());
		return id;
	}

	/*
	 * 	修改
	 */
	@Override
	public int updateMessage(User user) {
		if(user.getCapacity()>20480) {
			user.setCapacity((double) 20480);
		}
		return userDao.updateUser(user);
	}

	/*
	 * 	删除
	 */
	@Override
	public int deleteUser(String id) {
		return userDao.deleteUser(id);
	}
	
	/*
	 * 	根据用户id查找用户
	 */
	@Override
	public User queryUserById(String id) {
		return userDao.queryUserById(id);
	}
	
}
