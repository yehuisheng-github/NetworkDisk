package com.service;

import org.apache.ibatis.annotations.Update;

public interface TableService {

	public int createTable(String tableName);

	public int dropFromUser(String tableName);
}
