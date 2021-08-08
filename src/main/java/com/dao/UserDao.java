package com.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.domain.User;

/**
 * 	dao接口，使用注解编写SQL语句
 */
@Mapper
public interface UserDao {

	/*
	 * 	根据用户名查找用户
	 */
	@Select("select id,name,password,email,img,capacity from user where name=#{name}")
	public User queryByName(String name);
	
	/*
	 * 	根据用户名和密码查找用户
	 */
	@Select("select id,name,password,email,img,capacity from user where name=#{name} and password=#{password}")
	public User queryByNameAndPassword(String name,String password);
	
	/*
	 * 	添加用户信息
	 */
	@Insert("insert into user values(#{id},#{name},#{password},#{email},#{img},#{capacity})")
	public int insertUser(User user);
	
	/*
	 * 	修改用户信息
	 */
	@Insert("update user set name=#{name},password=#{password},email=#{email},img=#{img},capacity=#{capacity} where id=#{id}")
	public int updateUser(User user);
	
	/*
	 * 	删除用户
	 */
	@Delete("delete from user where id=#{id}")
	public int deleteUser(String id);
	
	/*
	 * 	根据用户名查找用户id
	 */
	@Select("select id from user where name=#{name}")
	public int queryIdByName(String name);
	
	/*
	 * 	根据用户id查找用户
	 */
	@Select("select id,name,password,email,img,capacity from user where id=#{id}")
	public User queryUserById(String id);
}
