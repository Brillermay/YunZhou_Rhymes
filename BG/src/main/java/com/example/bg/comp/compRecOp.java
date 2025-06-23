package com.example.bg.comp;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(
        origins = {
                "http://localhost:8080",
                "http://localhost:8081",
                "http://127.0.0.1:8081",  // 显式添加此项
                "http://117.72.88.23:8081",
        },
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}, // 添加 OPTIONS
        maxAge = 3600,
        allowCredentials = "true"
)
@RestController
@RequestMapping(value = "/compRec")
public class compRecOp {
    //实现逻辑：先通过getHistory判断里面是否有，如果有那么就使用update，没有就用add
}
