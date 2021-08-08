package com.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.domain.FilePojo;
import com.domain.ShareFilePojo;
import com.domain.User;
import com.service.FileService;
import com.service.UserService;
import com.utils.ClassifyTypeUtils;
import com.utils.FileUtils;
import com.utils.SizeConvertUtils;

/**
 *	文件共享模块
 */
@Controller
@RequestMapping("/share")
public class ShareController {
	
	@Autowired
	private FileService fileService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private UserService userService;
	
	/*
	 * 	转发到获取文件共享链接页面
	 */
	@GetMapping("/sharePage")
	public String sharePage(HttpServletRequest request,String clientPath) {
		request.setAttribute("clientPath", clientPath);
		return "backstage/share-getlink";
	}
	
	/*
	 * 	转发到获取文件信息页面
	 */
	@GetMapping("/getShare")
	public String getShare(HttpServletRequest request,String clientPath) {
		return "backstage/share-getfile";
	}
	
	/**
	 * 	自动生成文件共享链接
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	@RequestMapping("/getlink")
	@ResponseBody
	public String getLink(HttpServletRequest request,String clientPath,String userId) {

		//获取用户分配服务器的存储位置
		String[] arr=FileUtils.getRealPath(clientPath, userId, request);
		String serverPath=arr[0];
		clientPath=arr[1];
		
		//获取文件信息
		ShareFilePojo filePojo = fileService.queryShareFile(userId, clientPath);
		//将创建时间改为分享时间
		filePojo.setCreateTime(new Date());
		//生成分享链接
		String link="DATANETWORKDISK"+UUID.randomUUID()+System.currentTimeMillis();
		//设置该链接7天过期
		redisTemplate.opsForValue().setIfAbsent(link, filePojo, 7, TimeUnit.DAYS);
		return link;
	}
	
	/**
	 * 	获取共享文件
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/getfile")
	@ResponseBody
	public Object getFile(HttpServletRequest request,String link) {
		FilePojo filePojo = (FilePojo) redisTemplate.opsForValue().get(link);
		if(StringUtils.isEmpty(filePojo)) {
			return "当前链接无效！";
		}
		return filePojo;
	}
	
	/**
	 * 	保存共享文件
	 */
	@Transactional
	@RequestMapping("/save")
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String saveFile(HttpServletRequest request,String clientPath,String myUserId,String shareUserId){
		
		String fileName=clientPath.split("/")[clientPath.split("/").length-1];
		//获取该用户服务器的存储位置
		String myServerPath=FileUtils.getRealPath("", myUserId, request)[0]+"/share/"+fileName;

		//获取用户分配服务器的存储位置
		String[] arr=FileUtils.getRealPath(clientPath, shareUserId, request);
		String shareServerPath=arr[0];
		clientPath=arr[1];

		File file = new File(FileUtils.getRealPath("", myUserId, request)[0]+"/share/");
		//在指定的服务器地址创建文件
		try {
			//假如固定的获取分享文件的目录不存在，就创建
			if(!file.exists()) {
				
				file.mkdir();
				//获取文件大小
				String size=SizeConvertUtils.getFileSize(file);

				//修改用户总容量，使用存储在数据库中的值计算，容量数据保持一致
				User user = SizeConvertUtils.getUserCapacity(size, myUserId, userService, "-");
				if(user.getCapacity()<0) {
					request.setAttribute("exception", "操作失败！存储空间已满！");
					file.delete();
					return "error/remind";
				}
				
				//将创建的用户分享目录信息添加到数据库
				Integer parentId=fileService.queryExistFile(myUserId, "/myfile").getId();
				FilePojo pojo = new FilePojo(null,
						parentId, "文件夹", "share", new Date(), "/myfile/share", "/type/file.png", size);
				
				userService.updateMessage(user);
				fileService.saveFile(pojo,myUserId);
			}
			
			File myFile=new File(myServerPath);
			File shareFile=new File(shareServerPath);
			if(!myFile.exists()) {
				//将共享的文件复制过来
				Files.copy(shareFile.toPath(), myFile.toPath());
				
				//获取创建文件的时间
				Date date=new Date();
				//获取文件在服务器中的虚拟地址
				String path="/myfile/share/"+fileName;
				//获取父文件的id
				Integer parentId=fileService.queryExistFile(myUserId, "/myfile/share").getId();
				//分类
				String type=ClassifyTypeUtils.classify(myFile);
				
				//获取文件大小
				String size = SizeConvertUtils.getFileSize(myFile);
				
				//获取各种类型的图标
				String typeIco=ClassifyTypeUtils.convertPath(type, "/file/"+myUserId+"/"+path.substring(8));
				FilePojo filePojo = new FilePojo(null, parentId, type, fileName, date, path,typeIco, size);
				fileService.saveFile(filePojo,myUserId);		
				
				//修改用户总容量，使用存储在数据库中的值计算，容量数据保持一致
				User user = SizeConvertUtils.getUserCapacity(size, myUserId, userService, "-");
				if(user.getCapacity()<0) {
					request.setAttribute("exception", "操作失败！存储空间已满！");
					file.delete();
					return "error/remind";
				}
				userService.updateMessage(user);
			}else {
				request.setAttribute("exception", "文件已存在!");
				return "error/remind";
			}
		} catch (IOException e) {
			file.delete();
			//手动事务回滚：捕获异常后，AOP无法捕获异常，就无法进行事务回滚
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			request.setAttribute("exception", "该文件保存失败!");
			return "error/remind";
		}
		
		return "redirect:/index/look?userId="+myUserId+"&clientPath=/myfile/share";
	}
}
