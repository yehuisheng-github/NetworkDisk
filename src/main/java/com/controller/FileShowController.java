package com.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.domain.FilePojo;
import com.service.FileService;
import com.utils.FileUtils;

/**
 * 	编写负责文件前台展示的controller
 */
@Controller
@RequestMapping("/index")
public class FileShowController {
	
	@Autowired
	private FileService fileService;
	
	/**
	 * 	搜索文件
	 */
	@SuppressWarnings("unused")
	@RequestMapping("/search")
	public String searchFile(HttpServletRequest request,String fileName,String userId){
		List<FilePojo> list = fileService.querySomeFile(fileName, userId);
		if(list.size()==0) {
			request.setAttribute("warnSearch","整个文件系统下没有查找到"+fileName+"文件");
			return "backstage/search";
		}
		request.setAttribute("listSearch", list);
		return "backstage/search";
	}
	
	/*
	 * 	获取当前目录下所有文件名
	 */
	@SuppressWarnings({ "unused", "deprecation" })
	@RequestMapping("/look")
	public String showFiles (HttpServletRequest request,String clientPath,String userId){
		
		//获取用户分配服务器的存储位置
		String[] arr=FileUtils.getRealPath(clientPath, userId, request);
		String serverPath=arr[0];
		clientPath=arr[1];
		
		//返回文件夹路径，显示在导航框
		request.setAttribute("clientPath", clientPath);
		
		//根目录没有父文件，所以需要特殊处理
		if(clientPath.equals("/")) {
			List<FilePojo> list = fileService.queryAllFile(userId, 0+"");
			request.setAttribute("list", list);
			return "index";
		}
		
		//判断该路径是否存在
		FilePojo filePojo = fileService.queryExistFile(userId, clientPath);
		if(filePojo==null) {
			request.setAttribute("clientPath", "/");
			return "index";
		}
		
		//返回查询的数据
		List<FilePojo> list = fileService.queryAllFile(userId, filePojo.getId()+"");
		request.setAttribute("list", list);
		return "index";
	}
	
	/**
	 * 	获取分类的文件
	 */
	@RequestMapping(value="/getText",method=RequestMethod.GET)
	public String getText(HttpServletRequest request,String type,String userId) { 
		List<FilePojo> list = fileService.queryClassifyFile(userId, type);
		request.setAttribute("list", list);
		return "backstage/classify";
	}

}
