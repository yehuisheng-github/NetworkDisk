package com;

import java.io.File;
import java.text.DecimalFormat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.utils.FileUtils;

@SpringBootTest
class SpringBootNetworkDiskApplicationTests {
	
	@Test
	void test() throws Exception {
		File file = new File("C:\\神州驱动\\01_Chipset.zip");
		System.out.println(file.length());
	}
	
}
