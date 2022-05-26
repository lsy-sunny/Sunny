package com.lsy.sunny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author sunny
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class SunnyApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(SunnyApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  Sunny健身启动成功   ლ(´ڡ`ლ)ﾞ  \n" );
    }
}
