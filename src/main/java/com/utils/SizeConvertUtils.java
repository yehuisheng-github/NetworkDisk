package com.utils;

import java.io.File;
import java.text.DecimalFormat;

import com.domain.FilePojo;
import com.domain.User;
import com.service.UserService;

public class SizeConvertUtils {

	/**
	 * 	获取文件的大小，并将其数据转格式
	 */
	public static String getFileSize(File file) {
		//保留四位小数点
		DecimalFormat decimalFormat=new DecimalFormat("0.000");
		String string = decimalFormat.format((double)1544/1024);
		//获取文件大小
		double len=Double.parseDouble(decimalFormat.format((double)file.length()/1024));
		String size=null;
		//使文件的大小转格式
		if(len<1024) {
			size=len+"KB";
		}else if(len>1024 && len<1024*1024) {
			size=decimalFormat.format((double)len/1024)+"MB";
		}else if(len>1024*1024) {
			size=decimalFormat.format((double)len/(1024*1024))+"GB";
		}
		return size;
	}

	/**
	 * 	重置用户的总存储空间
	 */
	public static User getUserCapacity(String size,String userId,UserService userService,String operator) {
		//保留四位小数点
		DecimalFormat decimalFormat=new DecimalFormat("0.000");
		User user = userService.queryUserById(userId);
		//将文件大小转化格式
		double leng=Double.parseDouble(size.substring(0, size.length()-2));
		if(size.contains("K")) {
			leng=Double.parseDouble(decimalFormat.format((double)leng/1024));
		}else if(size.contains("G")) {
			leng=leng*1024;
		}
		Double capacity = null;
		//判断给用户容量增加还是减少
		if(operator.equals("-")) {
			capacity = Double.parseDouble(decimalFormat.format((double)user.getCapacity()-leng));
		}else if(operator.equals("+")) {
			capacity = Double.parseDouble(decimalFormat.format((double)user.getCapacity()+leng));
		}
		user.setCapacity(capacity);
		return user;
	}
}
