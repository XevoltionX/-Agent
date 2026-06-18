package com.gaocui;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.gaocui.mapper")
@EnableScheduling
public class GaocuiApplication {
    public static void main(String[] args) {
        SpringApplication.run(GaocuiApplication.class, args);
    }
}
