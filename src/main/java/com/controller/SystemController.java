package com.controller;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.domain.User;
import com.service.UserService;

/*
 * 	系统模块：查询用户的存储空间
 */
@Controller
@RequestMapping("/system")
public class SystemController {

	@Autowired
	private UserService userService;
	
	/**
	 * 	查询存储空间
	 */
	@RequestMapping("/capacity")
	public String getCapacity(HttpServletRequest request,String userId) {
		
		//获取用户和Map集合
		User user = userService.queryUserById(userId);
		Map<String, Object> map=new HashMap<String,Object>();
		
		//保留四位小数点
		DecimalFormat decimalFormat=new DecimalFormat("0.000");
		double usable=Double.parseDouble(decimalFormat.format((double)user.getCapacity()/1024));
		double used=Double.parseDouble(decimalFormat.format((double)20-usable));
		double percentage=Double.parseDouble(decimalFormat.format((double)usable/20*100));
		
		//存放计算的数据
		map.put("usable", usable);
		map.put("used", used);
		map.put("percentage", percentage);
		
		//返回数据和视图
		request.setAttribute("map", map);
		return "capacity";
	}
}
