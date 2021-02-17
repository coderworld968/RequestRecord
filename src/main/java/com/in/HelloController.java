package com.in;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @PostMapping(value = "/post/test",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String post(@RequestBody Dto dto){
        // curl http://127.0.0. -X POST -H "Content-Type:application/json" -d ' {"str":"23"}'
        return dto.getStr() +"-"+ System.currentTimeMillis();
    }
}
