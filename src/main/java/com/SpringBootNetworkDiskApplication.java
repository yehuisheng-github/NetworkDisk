package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *	SpringBoot启动类
 *	引入谷歌验证码配置文件到springboot容器中
 *	开启事务管理
 */
@ImportResource(locations= {"classpath:kaptcha.xml"})
@SpringBootApplication
@EnableTransactionManagement
public class SpringBootNetworkDiskApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootNetworkDiskApplication.class, args);
	}

}
