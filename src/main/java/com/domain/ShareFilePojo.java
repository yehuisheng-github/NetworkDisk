package com.domain;

import lombok.Data;

@Data
public class ShareFilePojo extends FilePojo{
	private Integer userId;
	private String userName;
	public ShareFilePojo(Integer userId, String userName) {
		super();
		this.userId = userId;
		this.userName = userName;
	}
	public ShareFilePojo() {
		super();
	}
}
