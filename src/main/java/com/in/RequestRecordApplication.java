package com.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@ServletComponentScan
@EnableSwagger2
public class RequestRecordApplication {

    private static final Logger logger = LoggerFactory.getLogger(RequestRecordApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RequestRecordApplication.class, args);
    }
}
