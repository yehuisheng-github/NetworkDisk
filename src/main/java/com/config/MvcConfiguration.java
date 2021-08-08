package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/*
 * 	自定义配置类
 */
@SuppressWarnings("deprecation")
@Configuration
public class MvcConfiguration extends WebMvcConfigurerAdapter{
	
	/**
	 * 	配置视图解析器，如果有引入数据库连接池
	 * 	注意！连接池的拦截器对请求进行拦截！！！
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("user/login");
		registry.addViewController("/link-Login").setViewName("user/login");
		registry.addViewController("/link-Regist").setViewName("user/regist");
		registry.addViewController("/link-Index").setViewName("index");
		registry.addViewController("/link-Search").setViewName("backstage/search");
		registry.addViewController("/link-Help").setViewName("help");
		registry.addViewController("/link-Capacity").setViewName("capacity");
	}
	
	/**
	 * 	注册拦截器	
	 */
	@Bean
	public WebMvcConfigurerAdapter webMvcConfigurerAdapter() {
		WebMvcConfigurerAdapter adapter=new WebMvcConfigurerAdapter() {
			/*
			 * 	注册自定义的登录权限拦截器：
			 * 		addPathPatterns("/**")：拦截所有的请求
			 * 		excludePathPatterns(xxx)：
			 * 			排除掉登录页面、注册页面、所有静态资源、用户模块的所有请求等
			 * 			如果静态资源在static目录下的一级资源，拦截器可以放行
			 * 			如果静态资源在static目录下的文件夹下，需要排除拦截
			 * 
			 * 		SpringBoot1.xx版本已经对静态资源做好映射了，所以我们可以不用理会静态资源的拦截和请求
			 * 		SpringBoot2.xx版本就需要开发人员对静态资源的拦截进行处理
			 */
			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**")
						.excludePathPatterns("/link-Login","/","/link-Regist","/picture/**","/user/**"
								,"/type/**","/favicon.ico/**","/js/**","/layui/**","/file/**","/upload/**");
			}
		};
		return adapter;
	}
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //前端浏览器访问路径带有/file/**就转到对应磁盘下读取图片
        registry.addResourceHandler("/preview/**").addResourceLocations
        	("file:F://software//64bitJava//eclipse program//SpringBoot-NetworkDisk//src//main//resources//public/");
        super.addResourceHandlers(registry);
    }
	
}
