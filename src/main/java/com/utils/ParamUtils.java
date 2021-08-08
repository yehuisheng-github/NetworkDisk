package com.utils;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.BeanUtils;

/*
 * 	代码优化：
 * 		通过BeanUtils类把客户端的所有请求参数注入到JavaBean对象中
 * 		就不需要每个请求参数都创建一个变量去接收
 */
public class ParamUtils {
	
	/**
	 * 	通过JavaBean对象的setXxx()方法，将客户端的请求参数注入到JavaBean当中
	 * 	因为方法使用了httpServelt，只能在web层，耦合度高
	 */
	public static void getParamUtils(HttpServletRequest request,Object bean) {
		try {
			BeanUtils.populate(bean, request.getParameterMap());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 	通过Map的值，将客户端的请求参数注入到JavaBean属性当中
	 * 	方法可以JavaEE三层架构中的任意一层，没有限制，耦合度低
	 * 
	 * 	使用泛型，返回一个对象时不需要类型强制转换
	 */
	public static <T> T getValueUtils(Map value,T bean) {
		try {
			BeanUtils.populate(bean,value);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return bean;
	}
	
}
