package com.li;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.li.mapper")
public class JwtTestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtTestBackendApplication.class, args);
    }

}
