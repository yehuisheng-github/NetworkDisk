package com.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.domain.FilePojo;
import com.domain.User;
import com.service.FileService;
import com.service.UserService;
import com.utils.ClassifyTypeUtils;
import com.utils.FileUtils;
import com.utils.SizeConvertUtils;

import sun.misc.BASE64Encoder;

/**
 * 	上传下载模块
 *
 */
@Controller
public class UploadAndDownload {
	
	@Autowired
	private FileService fileService;
	@Autowired
	private UserService userService;
	
	/**
	 * 	转发到上传页面		
	 */
	@GetMapping("/uploadPage")
	public String createPage(HttpServletRequest request,String clientPath) {
		request.setAttribute("clientPath", clientPath);
		return "backstage/upload";
	}
	
	/**
	 * 	文件上传
	 */
	@PostMapping("/upload")
    @ResponseBody
    @Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
    public Object upload (HttpServletRequest request,
    		@RequestParam("file")MultipartFile multipartFile,String clientPath,String userId){
		
        Map<String, Object> result = new ConcurrentHashMap<String, Object>();
        if (multipartFile != null){

    		//获取用户分配服务器的存储位置
    		String[] arr=FileUtils.getRealPath(clientPath, userId, request);
    		String serverPath=arr[0];
    		clientPath=arr[1];
    		
    		String fileName=multipartFile.getOriginalFilename().trim();
    		
            File file=new File(serverPath+"/"+fileName);
            if(!file.exists()) {
            	try {
                	//把上传的文件写入服务器
                	multipartFile.transferTo(file);
                	
                	//获取创建文件的时间
    				Date date=new Date();
    				//获取文件在服务器中的虚拟地址
    				String path=clientPath+"/"+fileName;
    				//获取父文件的id
    				Integer parentId=fileService.queryExistFile(userId, clientPath).getId();
    				//分类
    				String type=ClassifyTypeUtils.classify(file);
    				
    				//获取文件大小
    				String size=SizeConvertUtils.getFileSize(file);
    				
    				//获取各种类型的图标
    				String typeIco=ClassifyTypeUtils.convertPath(type, "/file/"+userId+"/"+path.substring(8));
    				FilePojo filePojo = new FilePojo(null, parentId, type, fileName, date, path,typeIco, size);
    				
    				//修改用户总容量，使用存储在数据库中的值计算，容量数据保持一致
    				User user = SizeConvertUtils.getUserCapacity(size, userId, userService, "-");
    				if(user.getCapacity()<0) {
    					request.setAttribute("exception", "操作失败！存储空间已满！");
    					file.delete();
    					return "error/remind";
    				}
    				
    				//保存到数据库
    				userService.updateMessage(user);
    				fileService.saveFile(filePojo,userId);
    				
                    result.put("code", 200);
                    result.put("msg", "上传成功");
                } catch (Exception e) {
                	file.delete();
                	//上传失败，返回异常提示
                    result.put("code", -1);
                    result.put("msg", "文件上传出错，请重新上传！");
                    e.printStackTrace();
                }
            }else {
            	//上传的文件名字冲突
            	result.put("code", -1);
                result.put("msg", "文件名冲突：该路径下有相同文件名的文件！");
            }
        } else {
            result.put("code", -1);
            result.put("msg", "未获取到有效的文件信息，请重新上传!");
        }
//        for(Entry<String,Object> e:result.entrySet()) {
//			System.out.println("Key:"+e.getKey()+", value:"+e.getValue());//遍历map集合
//		  }
        return result;
    }
	
	/*
	 * 	文件下载
	 */
	@RequestMapping("/download")
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public void download(HttpServletResponse response,
			HttpServletRequest request,String clientPath,String userId) throws Exception {

		//获取用户分配服务器的存储位置
		String[] arr=FileUtils.getRealPath(clientPath, userId, request);
		String serverPath=arr[0];
		clientPath=arr[1];
		
		String fileName=clientPath.split("/")[clientPath.split("/").length-1];
		File file = new File(serverPath);
		
		//给客户端响应，当前进行文件下载操作及返回的数据类型
		response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
					
		/*可以设置下载的文件名xxx.xxx(如：phono.jpg)*/
		//response.setHeader("Content-Disposition", "attament;filename=xxx.txt");			
		//判断客户端时哪个浏览器再进行编码操作，这样所有类型的浏览器访问服务器也不会出现文件名乱码
		if(request.getHeader("User-Agent").contains("Firefox")) {
			//对浏览器设置BASE64编码
			response.setHeader("Content-Disposition", 
			"attament;filename==?UTF-8?B?"+new BASE64Encoder().encode(fileName.getBytes("UTF-8"))+"?=");
		}else {
			//对浏览器设置URL编码
			response.setHeader("Content-Disposition", 
			"attament;filename="+URLEncoder.encode(fileName,"UTF-8"));
		}
				
		//将下载的文件回写客户端
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i = 0;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
	}
}
