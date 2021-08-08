package com.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tika.Tika;

public class ClassifyTypeUtils {
	/*
	 *
	 *	使用Tika工具查询文件类型
	 *
	 * 	常见文件类型:
	 *		MimeType    文件类型
	 *		application/msword    word(.doc)
	 *		application/vnd.ms-powerpoint    powerpoint(.ppt)
	 *		application/vnd.ms-excel    excel(.xls)
	 *		application/vnd.openxmlformats-officedocument.wordprocessingml.document    word(.docx)
	 *		application/vnd.openxmlformats-officedocument.presentationml.presentation    powerpoint(.pptx)
	 *		application/vnd.openxmlformats-officedocument.spreadsheetml.sheet    excel(.xlsx)
	 *		application/x-rar-compressed    rar
	 *		application/zip    zip
	 *		application/pdf    pdf
	 *		video/*    视频文件
	 *		image/*    图片文件
	 *		audio/*	   	音频文件
	 *		text/plain    纯文本
	 *		text/css    css文件
	 *		text/html    html文件
	 *		text/x-java-source    java源代码
	 *		text/x-csrc    c源代码
	 *		text/x-c++src    c++源代码
	 *
	 */
	public static String classify(File file) throws IOException {
		
		//文件夹或者目录
		if(file.isDirectory()) {
			return "文件夹";
		}
		
		//使用tika工具判断文件类型
       Tika tika = new Tika();
       String type = tika.detect(file);
       
       //图片类型
       Pattern image = Pattern.compile("image/.*");
       Matcher imageMatcher = image.matcher(type);
       if(imageMatcher.matches()) {
       	return "图片";
       }
       
       //视频类型
       Pattern video = Pattern.compile("video/.*");
       Matcher videoMatcher = video.matcher(type);
       if(videoMatcher.matches()) {
       	return "视频";
       }
       
       //音频类型
       Pattern audio = Pattern.compile("audio/.*");
       Matcher audioMatcher = audio.matcher(type);
       if(audioMatcher.matches()) {
       	return "音频";
       }
       
       //文本类型
       Pattern text = Pattern.compile("text/.*");
       Matcher textMatcher = text.matcher(type);
       if(textMatcher.matches()) {
       	return "文本";
       }
       
       //office类型
       Pattern office = Pattern.compile("application/vnd.openxmlformats-officedocument.*");
       Matcher oM = office.matcher(type);
       Pattern office2 = Pattern.compile("application/vnd.ms-.*");
       Matcher o2M = office2.matcher(type);
       if(oM.matches() || o2M.matches() 
       		|| type.equals("application/msword") ||type.equals("application/x-tika-msoffice")) {
       	return "office";
       }
       
       //其他类型
       return "other";
	}
	
	/**
	 * 	将文件类型转换为路径
	 */
	public static String convertPath(String type,String path) {
		String typeIco=null;
		switch (type) {
	        case "文件夹":
	        	typeIco="/type/file.png";
				break;
			case "图片":
				typeIco=path;
				break;
			case "视频":
				typeIco="/type/video.png";
				break;
			case "音频":
				typeIco="/type/audio.png";
				break;
			case "文本":
				typeIco="/type/text.png";
				break;
			case "office":
				typeIco="/type/office.png";
				break;
			default:
				typeIco="/type/other.png";
		}
		return typeIco;
	}
	
}
