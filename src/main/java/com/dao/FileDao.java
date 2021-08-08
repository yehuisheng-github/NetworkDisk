package com.dao;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.domain.FilePojo;

@Mapper
public interface FileDao {

	/*
	 * 	保存文件信息
	 * 	增加了自动建表功能，使用$获取表名，导致JavaBean和SQL语句的映射出问题，所以在service层手动配置映射关系
	 */
	@Insert("insert into user_${tableName}_file values(#{id},#{parentId},#{type},#{name},#{createTime},#{virtualPath},#{typeIco},#{size})")
	public int insertFile(Integer id,Integer parentId,String type,String name,Date createTime,String virtualPath,String typeIco,String size,String tableName);
	
	/*
	 * 	在用户全局范围下查找文件
	 * 	like关键字会导致索引失效，建立覆盖索引
	 */ 
	@Select("select id,name,type,createTime,virtualPath from user_${tableName}_file where name like #{name}")
	public List<FilePojo> querySomeFile(String name,String tableName);
	
	/*
	 * 	在用户当前目录下查找该文件
	 */
	@Select("select id,parentId,type,name,createTime,virtualPath,typeIco,size from user_${tableName}_file where virtualPath=#{virtualPath}")
	public FilePojo queryExistFile(String tableName,String virtualPath);
	
	/*
	 * 	在用户当前目录下查找所有文件
	 */
	@Select("select id,parentId,type,name,createTime,virtualPath,typeIco,size from user_${tableName}_file where parentId=#{parentId}")
	public List<FilePojo> queryAllFile(String tableName,String parentId);
	
	/*
	 * 	删除文件或目录
	 */
	@Delete("delete from user_${tableName}_file where virtualPath=#{virtualPath}")
	public int deleteFile(String tableName,String virtualPath);
	
	/*
	 * 	修改文件信息，id、用户id、创建时间是固定的，不用修改
	 * 	增加了自动建表功能，使用$获取表名，导致JavaBean和SQL语句的映射出问题，所以在service层手动配置映射关系
	 */
	@Update("update user_${tableName}_file set parentId=#{parentId},type=#{type},name=#{name},virtualPath=#{virtualPath},typeIco=#{typeIco},size=#{size} where id=#{id}")
	public int updateFile(Integer id,Integer parentId,String type,String name,String virtualPath,String typeIco,String size,String tableName);
	
	/*
	 * 	查询用户下的某个类型的文件
	 */
	@Select("select id,parentId,type,name,createTime,virtualPath,typeIco,size from user_${tableName}_file where type=#{type}")
	public List<FilePojo> queryClassifyFile(String tableName,String type);
	
}
