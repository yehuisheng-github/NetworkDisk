package com.service;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.dao.FileDao;
import com.dao.UserDao;
import com.domain.FilePojo;
import com.domain.ShareFilePojo;
import com.domain.User;

@Service
public class FileServiceImpl implements FileService{
	
	@Resource(name = "fileDao")
	private FileDao fileDao;
	@Resource(name = "userDao")
	private UserDao userDao;

	/**
	 * 	保存文件信息
	 */
	@Override
	public int saveFile(FilePojo filePojo,String userId) {
		return fileDao.insertFile(filePojo.getId(), filePojo.getParentId(), filePojo.getType(),
				filePojo.getName(), filePojo.getCreateTime(), filePojo.getVirtualPath(),
				filePojo.getTypeIco(), filePojo.getSize(), userId);
	}

	/*
	 * 	在用户全局范围下查找文件
	 */
	@Override
	public List<FilePojo> querySomeFile(String filename, String userId) {
		filename="%"+filename+"%";
		return fileDao.querySomeFile(filename, userId);
	}
	
	/*
	 * 	在用户当前目录下查找该文件
	 */
	@Override
	public FilePojo queryExistFile(String userId,String virtualPath) {
		return fileDao.queryExistFile(userId, virtualPath);
	}


	/*
	 * 	在用户当前目录下查找所有文件
	 */
	@Override
	public List<FilePojo> queryAllFile(String userId,String parentId) {
		return fileDao.queryAllFile(userId, parentId);
	}

	/*
	 * 	删除文件或目录
	 */
	@Override
	public int deleteFile(String userId, String virtualPath) {
		return fileDao.deleteFile(userId, virtualPath);
	}

	/*
	 * 	修改文件信息，id、创建时间是固定的，不用修改
	 */
	@Override
	public FilePojo updateFile(FilePojo filePojo,String userId) {
		fileDao.updateFile(filePojo.getId(), filePojo.getParentId(), filePojo.getType(),
				filePojo.getName(), filePojo.getVirtualPath(),
				filePojo.getTypeIco(), filePojo.getSize(), userId);
		FilePojo pojo = fileDao.queryExistFile(userId, filePojo.getVirtualPath());
		return pojo;
	}

	/*
	 * 	查询用户下的某个类型的文件
	 */
	@Override
	public List<FilePojo> queryClassifyFile(String userId, String type) {
		return fileDao.queryClassifyFile(userId, type);
	}
	
	/**
	 * 	获取分享文件的信息
	 * 	user表和file表没有对应的字段，无法进行一对多、一对一查询
	 * 	所以直接扩展了实体类，在service层两次查询数据库进行赋值
	 */
	@Override
	public ShareFilePojo queryShareFile(String userId, String virtualPath) {
		
		FilePojo filePojo = fileDao.queryExistFile(userId, virtualPath);
		User user = userDao.queryUserById(userId);
		
		ShareFilePojo shareFilePojo = new ShareFilePojo();
		shareFilePojo.setId(filePojo.getId());
		shareFilePojo.setCreateTime(filePojo.getCreateTime());
		shareFilePojo.setName(filePojo.getName());
		shareFilePojo.setParentId(filePojo.getParentId());
		shareFilePojo.setSize(filePojo.getSize());
		shareFilePojo.setType(filePojo.getType());
		shareFilePojo.setTypeIco(filePojo.getTypeIco());
		shareFilePojo.setVirtualPath(filePojo.getVirtualPath());
		shareFilePojo.setUserId(user.getId());
		shareFilePojo.setUserName(user.getName());
		
		return shareFilePojo;
	}

}
