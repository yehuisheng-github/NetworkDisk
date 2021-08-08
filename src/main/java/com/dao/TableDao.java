package com.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TableDao {

	/*
	 * 	自动建表
	 */
	@Update("CREATE TABLE IF NOT EXISTS `user_${tableName}_file` (\r\n" + 
			"	  `id` int(11) NOT NULL AUTO_INCREMENT,\r\n" + 
			"	  `parentId` int(11) NOT NULL,\r\n" + 
			"	  `type` varchar(50) NOT NULL,\r\n" + 
			"	  `name` varchar(255) NOT NULL,\r\n" + 
			"	  `createTime` datetime NOT NULL,\r\n" + 
			"	  `virtualPath` varchar(255) NOT NULL,\r\n" + 
			"	  `typeIco` varchar(255) NOT NULL,\r\n" + 
			"	  `size` varchar(255) NOT NULL,\r\n" + 
			"	  PRIMARY KEY (`id`),\r\n" + 
			"	  UNIQUE KEY `virtualPath` (`virtualPath`),\r\n" + 
			"	  KEY `idx_file_ntcv` (`name`,`type`,`createTime`,`virtualPath`),\r\n" + 
			"	  KEY `idx_file_parentId` (`parentId`),\r\n" + 
			"	  KEY `idx_file_type` (`type`)\r\n" + 
			"	) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci")
	public int createTable(String tableName);

	/*
	 * 	自动删表
	 */
	@Update("drop table user_${tableName}_file")
	public int dropFromUser(String tableName);
}
