package com.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.smartcardio.ATR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.domain.FilePojo;
import com.domain.User;
import com.google.code.kaptcha.Producer;
import com.service.FileService;
import com.service.TableService;
import com.service.UserService;
import com.utils.FileUtils;
import com.utils.ParamUtils;

/**
 * 	处理用户模块请求
 */
@SuppressWarnings("unused")
@Controller
@RequestMapping("/user")
public class UserController {
	
	/*
	 * 	将容器中配置好的验证码对象取出来使用
	 */
	@Autowired
	private Producer kaptchaProducer;
	@Autowired
	private UserService userService;
	@Autowired
	private FileService fileService;
	@Autowired
	private TableService tableService;
	
	/**
	 * 	响应Ajax异步请求更换头像
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	/*
	 * 	源码位置：org\springframework\boot\web\servlet\server\DocumentRoot.java
	 * 	SpringBoot的内嵌式tomcat启动后，默认会把commonDocRoot设置成默认的三个目录
	 * 	然后读取commonDocRoot的文件，导致使用getServletContext().getRealPath()得到的是临时文件的路径。
	 * 	解决：配置启动工作区到项目根目录下，根目录必须有public或static目录
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/upload")
	public @ResponseBody String saveImg(MultipartFile file,HttpServletRequest request) 
			throws IllegalStateException, IOException {
		String path=request.getServletContext().getRealPath("/upload/");
		//fileName：随机数+上传的文件名
		String fileName=UUID.randomUUID()+
				String.valueOf(System.currentTimeMillis())+file.getOriginalFilename();
		//projectPath：当前工程访问路径
		String projectPath= request.getScheme()+
				"://"+
				request.getServerName()+   
				":"+
				request.getServerPort()+
				request.getContextPath()+
				"/";
		//transferTo：将上传的文件写入到服务器中
		file.transferTo(new File(path+fileName));
		return projectPath+"upload/"+fileName;
	}
	
	/*
	 * 	处理谷歌验证码请求
	 */
	@GetMapping("/getVerifyCode")
	//使用多例模式，保证线程安全
	@Scope("prototype")
    public void handleRequest(HttpServletRequest request, HttpServletResponse response){
		//对服务器响应头做写设置
        response.setDateHeader("Expires",0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
      
        String capText = kaptchaProducer.createText();      
        BufferedImage bi = kaptchaProducer.createImage(capText);
        //将验证码放入session对象中，并设置存活时间
        request.getSession().setAttribute("capText", capText);
//        request.getSession().setMaxInactiveInterval(60*3);
        
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            ImageIO.write(bi, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	/**
	 * 	实现用户登录功能
	 */
	@SuppressWarnings("unused")
	@PostMapping("/login")
	public ModelAndView doLogin(HttpServletRequest request) {
		
		ModelAndView mv=new ModelAndView();	
		
		//获取客户端请求的参数,并将请求的参数存放在User对象中
		User user=ParamUtils.getValueUtils(request.getParameterMap(), new User());			
		//用户名和密码查询用户信息，假如查询失败，用户信息为0
		User loginUser=userService.loginByUser(user.getName(), user.getPassword());
		
		//检查用户名和密码是否正确
		if(loginUser==null) {					
			//假如，登录信息错误，将错误信息保存到request域中
			mv.addObject("msg", "用户名或密码错误！登录失败！");
			//将回显的表单项信息保存到session中
			request.getSession().setAttribute("name", user.getName());
			//跳转到登录页面
			mv.setViewName("user/login");
		}else{
			String serverPath= request.getScheme()+
				"://"+
				request.getServerName()+   
				":"+
				request.getServerPort()+
				request.getContextPath()+
				"/";
			request.getSession().setAttribute("user",loginUser);
			request.getSession().setAttribute("serverPath",serverPath);
			mv.setViewName("index");
		}
		
		return mv;
	}
	
	/**
	 * 	实现用户注册功能和修改功能
	 */
	@PostMapping("/registOrUpdate")
	@Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String doRegist(Model model,HttpServletRequest request) {
		//获取客户端请求的参数,并将请求的参数存放在User对象中
		User user=ParamUtils.getValueUtils(request.getParameterMap(), new User());
		//获取插件生成的验证码
		String token = (String) request.getSession().getAttribute("capText");
		//获取后删除session对象的验证码，保证每一次从session取的验证码都是最新的
		request.getSession().removeAttribute("capText");
		
		//检查验证码是否正确
		if(token==null || !token.equalsIgnoreCase(user.getCode())) {
			//自定义错误信息，并保存到域对象当中
			model.addAttribute("msg", "验证码错误");
			//保存表单项的值到域对象
			model.addAttribute("name", user.getName());
			model.addAttribute("email", user.getEmail());
			//转发到注册/修改页面	
			return "user/regist";
		}

		//projectPath：当前工程访问路径
		String projectPath= request.getScheme()+
				"://"+
				request.getServerName()+   
				":"+
				request.getServerPort()+
				request.getContextPath()+
				"/";
		if(user.getImg().equals("picture/background.jpg")) {
			user.setImg(projectPath+"picture/background.jpg");
		}
		
		User attribute = (User) request.getSession().getAttribute("user");
		//修改功能
		if(attribute!=null) {
			//调用userService修改用户信息
			userService.updateMessage(user);
			request.getSession().setAttribute("user",user);
			//重定向到首页
			return "redirect:/link-Index";
		}
		//注册，检查用户名是否存在
		if(userService.existUser(user.getName())) {
			//自定义错误信息，并保存到域对象当中
			model.addAttribute("msg", "用户名已存在");
			//保存表单项的值到域对象
			model.addAttribute("name", user.getName());
			model.addAttribute("email", user.getEmail());
			//转发到注册页面	
			return "user/regist";			
		}
		//调用userService将注册信息保存到数据库
		user.setCapacity((double) 20480);
		int userId=userService.createUser(user);
		
		//创建用户的文件表
		tableService.createTable(userId+"");
		
		//注册成功就创建该用户的家目录
		String serverPath=request.getServletContext().getRealPath("/file/"+userId+"/");
		File clientFile=new File(serverPath);
		if(!clientFile.exists()) {
			clientFile.mkdir();
		}
		//将创建的用户家目录信息添加到数据库
		FilePojo filePojo = new FilePojo(null,0,"文件夹","myfile",new Date(),"/myfile","/type/file.png","0KB");
		fileService.saveFile(filePojo, userId+"");
		//重定向到登录页面
		return "redirect:/link-Login";
	} 
	
	/**
	 * 	退出登录
	 */
	@RequestMapping("/loginout")
	public ModelAndView doLogout(HttpServletRequest request) {
		ModelAndView mv=new ModelAndView();
		request.getSession().removeAttribute("user");
		request.getSession().removeAttribute("end");
		mv.setViewName("user/login");
		return mv;
	}

	/**
	 * 	删除用户
	 */
	@DeleteMapping("/delete/{id}")
	@Transactional
	//使用多例模式，保证线程安全
	@Scope("prototype")
	public String deleteUser(@PathVariable("id")String id,HttpServletRequest request) {
		tableService.dropFromUser(id);
		userService.deleteUser(id);
		String path = request.getServletContext().getRealPath("/file/"+id);
		FileUtils.deleteReal(new File(path));
		request.getSession().removeAttribute("user");
		request.getSession().removeAttribute("end");
		return "user/login";
	}
	
}
