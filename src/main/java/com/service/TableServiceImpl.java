package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dao.TableDao;

@Service
public class TableServiceImpl implements TableService{

	@Autowired
	private TableDao tableDao;
	
	/*
	 * 	自动建表
	 */
	@Override
	public int createTable(String tableName) {
		return tableDao.createTable(tableName);
	}

	/*
	 * 	自动删表
	 */
	@Override
	public int dropFromUser(String tableName) {
		return tableDao.dropFromUser(tableName);
	}

}
