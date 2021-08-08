package com.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.domain.FilePojo;
import com.domain.User;
import com.service.FileService;
import com.service.UserService;
import com.sun.org.apache.bcel.internal.generic.DLOAD;
import com.utils.ClassifyTypeUtils;
import com.utils.FileUtils;
import com.utils.SizeConvertUtils;

/**
 * 	文件操作模块
 */
@Controller
@RequestMapping("/fileOperate")
public class FileOperateController {
	
	@Autowired
	private FileService fileService;
	@Autowired
	private UserService userService;
	
	/**
	 * 	转发到重命名页面		
	 */
	@GetMapping("/renamePage")
	public String createPag(HttpServletRequest request,String clientPath) {
		//将路径分割为重命名的文件名，和其上级目录
		String lastFileName=clientPath.split("/")[clientPath.split("/").length-1];
		clientPath=clientPath.replace("/"+lastFileName,"");
		request.setAttribute("fileName", lastFileName);
		request.setAttribute("clientPath", clientPath);
		return "backstage/file-create";
	}
	
	/**
	 * 	转发到新建文件页面	
	 */
	@GetMapping("/createPage")
	public String renamePage(HttpServletRequest request,String clientPath) {
		request.setAttribute("clientPath", clientPath);
		return "backstage/file-create";
	}
	
	/**
	 * 	转发到移动文件页面	
	 */
	@GetMapping("/movePage")
	public String movePage(HttpServletRequest request,String clientPath){
		request.setAttribute("clientPath", clientPath);
		return "backstage/move";
	}
	
	/**
	 * 	创建文件
	 * @throws IOException 
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	@RequestMapping("/createFile")
	@Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String createFile(HttpServletRequest request,String clientPath,String userId,String fileName) throws IOException{

		//获取用户分配服务器的存储位置
		String[] arr=FileUtils.getRealPath(clientPath, userId, request);
		String serverPath=arr[0]+"/"+fileName;
		clientPath=arr[1];
		
		if(fileName.contains(".")) {
			request.setAttribute("exception", "只能创建文件夹！不可以创建文件！");
			return "error/remind";
		}
		
		//在指定的服务器地址创建文件
		File serverFile=new File(serverPath);
		if(!serverFile.exists()) {
			//保存文件信息
			serverFile.mkdir();
			
			//获取创建文件的时间
			Date date=new Date();
			//获取文件在服务器中的虚拟地址
			String path=clientPath+"/"+fileName;
			//获取父文件的id
			Integer parentId=fileService.queryExistFile(userId, clientPath).getId();
			
			//获取各种类型的图标
			String typeIco=ClassifyTypeUtils.convertPath("文件夹", "/file/"+userId+"/"+path.substring(8));
			FilePojo filePojo = new FilePojo(null,parentId,"文件夹",fileName,date,path,typeIco,"0KB");
			
			//检索用户的存储空间
			User user = userService.queryUserById(userId);
			Double capacity=user.getCapacity();
			if(capacity<0) {
				request.setAttribute("exception", "操作失败！存储空间已满！");
				serverFile.delete();
				return "error/remind";
			}
			try {
				fileService.saveFile(filePojo,userId);
			} catch (Exception e) {
				e.printStackTrace();
				//手动事务回滚：捕获异常后，AOP无法捕获异常，就无法进行事务回滚
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				request.setAttribute("exception", "数据库SQL错误！");
				serverFile.delete();
				return "error/remind";
			}
		}else {
			request.setAttribute("exception", "该文件已存在!");
			return "error/remind";
		}
		return "redirect:/index/look?userId="+userId+"&clientPath="+clientPath;
	}
	
	/*
	 * 	删除文件
	 */
	@SuppressWarnings({ "deprecation", "unused" })
	@RequestMapping("deleteFile")
	@Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String deleteFile(HttpServletRequest request,String clientPath,String userId){
		
		//获取用户分配服务器的存储位置
		String[] arr=FileUtils.getRealPath(clientPath, userId, request);
		String serverPath=arr[0];
		clientPath=arr[1];

		//获取删除的文件信息
		FilePojo pojo = fileService.queryExistFile(userId, clientPath);
		//数据库删除文件
		fileService.deleteFile(userId, clientPath);

		//修改用户总容量
		User user = SizeConvertUtils.getUserCapacity(pojo.getSize(), userId, userService,"+");
		userService.updateMessage(user);
		
		//文件夹类型就递归删除数据库、服务器文件信息
		if(pojo.getType().equals("文件夹")) {
			FileUtils.deleteAll(fileService,userId,pojo,userService, request);
		}
		
		//服务器删除文件
		new File(serverPath).delete();
		
		//获取要删除文件的父文件路径，刷新页面
		String lastFileName="/"+clientPath.split("/")[clientPath.split("/").length-1];
		clientPath=clientPath.replace(lastFileName,"/");
		
		return "redirect:/index/look?userId="+userId+"&clientPath="+clientPath;
	}
	
	/**
	 * 	重命名	
	 * @throws IOException 
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	@RequestMapping("/rename")
	@Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String rename(HttpServletRequest request,
			String clientPath,String userId,String fileName,String oldFileName) throws IOException{
		
		//获取用户分配服务器的存储位置
		String serverPath=request.getServletContext().getRealPath("/file/"+userId+"/");
		//读取修改的文件名以及路径
		String newPath=serverPath+fileName;
		String oldPath=serverPath+oldFileName;
		if(!StringUtils.isEmpty(clientPath)) {
			clientPath=clientPath.replace("//", "/");
			newPath=serverPath+clientPath.substring(7)+"/"+fileName;
			oldPath=serverPath+clientPath.substring(7)+"/"+oldFileName;
		}
		
		FilePojo pojo = fileService.queryExistFile(userId, clientPath+"/"+fileName);
		if(!StringUtils.isEmpty(pojo)) {
			request.setAttribute("exception", "重命名失败！该目录下有相同的文件名！");
			return "error/remind";
		}
		
		//获取要修改的文件信息
		FilePojo filePojo = fileService.queryExistFile(userId, clientPath+"/"+oldFileName);
		//假如是文件夹，递归把文件夹下的所有文件路径都修改
		if(filePojo.getType().equals("文件夹")) {
			FileUtils.updatePath(filePojo, userId, clientPath+"/"+oldFileName, clientPath+"/"+fileName,fileService);
		}else {
			request.setAttribute("exception", "不可以修改文件名！只能给文件夹重命名！");
			return "error/remind";
		}
		filePojo.setName(fileName);
		filePojo.setVirtualPath(clientPath+"/"+fileName);
		
		File newfile = new File(newPath);
		File oldfile = new File(oldPath);
		
		String path="/file/"+userId+"/";
		if(!clientPath.equals("/myfile")) {
			path=path+clientPath.substring(8);
		}
		
		//需要将文件重命名后才可以使用文件做判断，否则获取的类型是未重命名前的
		boolean flag = oldfile.renameTo(newfile);
		if(!flag) {
			request.setAttribute("exception", "文件重命名失败！请稍后重试！");
			return "error/remind";
		}
		
		filePojo.setType(ClassifyTypeUtils.classify(newfile));
		filePojo.setTypeIco(ClassifyTypeUtils.convertPath(ClassifyTypeUtils.classify(newfile), path));
		FilePojo newFilePojo = fileService.updateFile(filePojo,userId);
		
		//假如是文件夹，递归把文件夹下的所有文件路径都修改
		if(filePojo.getType().equals("文件夹")) {
			FileUtils.updatePath(filePojo, userId, clientPath+"/"+oldFileName, clientPath+"/"+fileName,fileService);
		}
		
		return "redirect:/index/look?userId="+userId+"&clientPath="+clientPath;
	}
	
	/**
	 * 	移动文件
	 */
	@RequestMapping("moveFile")
	@Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String moveFile(HttpServletRequest request,
			String newClientPath,String userId,String oldClientPath){
		
		//获取源地址
		String[] arr=FileUtils.getRealPath(newClientPath, userId, request);
		String newServerPath=arr[0];
		newClientPath=arr[1];
		
		//获取新地址
		String[] oldArr=FileUtils.getRealPath(oldClientPath, userId, request);
		String oldServerPath=oldArr[0];
		oldClientPath=oldArr[1];

		File oldfile = new File(oldServerPath);
		File newfile = new File(newServerPath+"/"+oldfile.getName());
		
		//查找移动位置的父文件
		FilePojo pojo = fileService.queryExistFile(userId, newClientPath);
		newClientPath=newClientPath+"/"+oldfile.getName();
		
		//检查移动位置有没有同名文件
		FilePojo exists = fileService.queryExistFile(userId, newClientPath);
		if(!StringUtils.isEmpty(exists)) {
			request.setAttribute("exception", "移动失败！该目录下有相同的文件名！");
			return "error/remind";
		}
		
		//获取要修改的文件信息
		FilePojo filePojo = fileService.queryExistFile(userId, oldClientPath);
		filePojo.setVirtualPath(newClientPath);
		filePojo.setParentId(pojo.getId());

		//文件移动操作
		try {
			if(oldServerPath.contains(".")) {
				org.apache.commons.io.FileUtils.moveFile(oldfile, newfile);
			}else{
				org.apache.commons.io.FileUtils.moveDirectory(oldfile, newfile);
			}
		} catch (IOException e) {
			newfile.delete();
			e.printStackTrace();
			request.setAttribute("exception", "文件移动失败！请稍后重试！");
			return "error/remind";
		}
		FilePojo newFilePojo = fileService.updateFile(filePojo,userId);
		
		//假如是文件夹，递归把文件夹下的所有文件路径都修改
		if(filePojo.getType().equals("文件夹")) {
			FileUtils.updatePath(filePojo, userId, oldClientPath,newClientPath,fileService);
		}
		
		return "redirect:/index/look?userId="+userId+"&clientPath="+newClientPath;
	}
}
