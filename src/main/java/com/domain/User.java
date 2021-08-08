package com.domain;

import lombok.Data;

/*
 * 	用户实体类
 */
@Data
public class User {
	
	private Integer id;
	private String name;
	private String password;
	private String email;
	private String img;
	private String code;
	//每个用户的存储空间，单位 1024*20 MB
	private Double capacity;
	
	public User() {
		super();
	}
	public User(Integer id, String name, String password, String email, String img, String code, Double capacity) {
		super();
		this.id = id;
		this.name = name;
		this.password = password;
		this.email = email;
		this.img = img;
		this.code = code;
		this.capacity = capacity;
	}
	
}
