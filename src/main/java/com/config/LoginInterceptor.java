package com.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *	登录检查的拦截器
 */
public class LoginInterceptor implements HandlerInterceptor{
	
	/*
	 *	预处理方法 
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Object user=request.getSession().getAttribute("user");
		if(user==null) {
			request.setAttribute("error", "权限不足！请登陆后再进入后台！");
			request.getRequestDispatcher("/link-Login").forward(request, response);
			return false;
		}else {
			return true;
		}
	}
}
