package com.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import com.domain.FilePojo;
import com.domain.User;
import com.service.FileService;
import com.service.UserService;

/**
 * 	文件工具类
 */
public class FileUtils {

	/*
	 * 	删除该用户所有的服务器文件
	 */
	public static void deleteReal(File file) {
		File[] files = file.listFiles();
		if(StringUtils.isEmpty(files) || files.length==0) {
			file.delete();
			return;
		}
		for(int i=0;i<files.length;i++) {
			if(files[i].isDirectory()) {
				deleteReal(files[i]);
			}
			files[i].delete();
		}
		file.delete();
	}
	
	/*
	 * 	递归删除数据库、服务器文件
	 */
	public static void deleteAll(FileService fileService,String userId,
			FilePojo filePojo,UserService userService,HttpServletRequest request) {
		
		//获取当前文件夹下所有文件
		List<FilePojo> list = fileService.queryAllFile(userId, filePojo.getId()+"");
		for(FilePojo pojo:list) {	
			//如果文件夹下还是文件夹就继续循环删除数据库文件信息
			if(pojo.getType().equals("文件夹")) {
				deleteAll(fileService,userId,pojo, userService,request);
			}
			//获取用户分配服务器的存储位置
			String[] arr=FileUtils.getRealPath(pojo.getVirtualPath(), userId, request);
			//服务器删除文件
			new File(arr[0]).delete();
			
			//数据库删除文件
			fileService.deleteFile(userId, pojo.getVirtualPath());
			
			//修改用户总容量
			User user = userService.queryUserById(userId);
			double leng=Integer.parseInt(pojo.getSize().substring(0, pojo.getSize().length()-2));
			if(pojo.getSize().contains("K")) {
				leng=(double)leng/1024;
			}else if(pojo.getSize().contains("G")) {
				leng=(double)leng*1024;
			}
			Double capacity=user.getCapacity()+leng;
			user.setCapacity(capacity);
			userService.updateMessage(user);
		}
	}
	
	/**
	 * 	递归修改文件路径
	 * @param filePojo	当前目录的文件信息
	 * @param oldpath	重命名前的路径
	 * @param newpath	重命名后的路径
	 * @param fileService
	 */
	public static void updatePath(FilePojo filePojo,
			String userId,String oldpath,String newpath,FileService fileService) {
		//获取当前文件夹下所有文件
		List<FilePojo> list = fileService.queryAllFile(userId, filePojo.getId()+"");
		for(FilePojo pojo:list) {
			//修改文件路径
			String p=pojo.getVirtualPath().replaceFirst(oldpath,newpath);
			pojo.setVirtualPath(p);
			//如果文件是图片类型需要修改typeIco参数
			if(pojo.getType().equals("图片")) {
				String path="/file/"+userId+"/";
				if(!pojo.getVirtualPath().equals("/myfile")) {
					path=path+pojo.getVirtualPath().substring(8);
				}
				pojo.setTypeIco(path);
			}
			fileService.updateFile(pojo,userId);
			
			//如果文件夹下还是文件夹就继续循环修改
			if(pojo.getType().equals("文件夹")) {
				updatePath(pojo, userId, oldpath, newpath, fileService);
			}
		}
	}
	
	
	/**
	 * 	获取文件实际存储路径
	 */
	@SuppressWarnings("deprecation")
	public static String[] getRealPath(String clientPath,String userId,HttpServletRequest request) {
		//拼接文件实际存储的服务器路径
		String serverPath=request.getServletContext().getRealPath("/file/"+userId+"/");
		if(!StringUtils.isEmpty(clientPath)) {
			clientPath=clientPath.replace("//", "/");
			//clientPath如果最后一位是斜杠且非根目录，就把它去掉
			if(clientPath.substring(clientPath.length()-1).equals("/") && clientPath.length()!=1) {
				clientPath=clientPath.substring(0,clientPath.length()-1);
			}
			serverPath=serverPath+clientPath.substring(7);
		}
		String[] arr= {serverPath,clientPath};
		return arr;
	}
	
}



