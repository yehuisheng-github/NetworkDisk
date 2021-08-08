package com.service;

import com.domain.User;

public interface UserService {

	public boolean existUser(String name);	
	public User loginByUser(String name,String password);
	public int createUser(User user);
	public int updateMessage(User user);
	public int deleteUser(String id);
	public User queryUserById(String id);
}
