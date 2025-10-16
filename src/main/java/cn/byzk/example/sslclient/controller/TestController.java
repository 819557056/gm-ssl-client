package cn.byzk.example.sslclient.controller;

import cn.byzk.example.sslclient.http.HttpClientUtil;
import cn.byzk.example.sslclient.model.UserDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/test/cli/")
@RestController
public class TestController {

    @Resource
    private HttpClientUtil httpClientUtil;

    @GetMapping("t1")
    public void testServer() {

        UserDto userDto = new UserDto();
        userDto.setAge(1);
        userDto.setName("client");

        String resp = httpClientUtil.postJson("https://127.0.0.1:8888/test/serv/t1", null);
        log.info("收到服务端的响应信息： {}", resp);

    }

    @GetMapping("t2")
    public void testServer2() {

        UserDto userDto = new UserDto();
        userDto.setAge(1);
        userDto.setName("client");

//        String resp = httpClientUtil.postJson("https://127.0.0.1:7777/test/serv/t1", null);
        String resp = httpClientUtil.postJson("https://localhost:8443/test/serv/t1", null);
//        String resp = httpClientUtil.postJson("https://localhost:7777/tomcat", null);
//        String resp = httpClientUtil.postJson("https://192.168.100.3:7777/test/serv/t1", null);
        log.info("收到服务端的响应信息： {}", resp);

    }

    @GetMapping("t3")
    public void testServer3() {

        UserDto userDto = new UserDto();
        userDto.setAge(1);
        userDto.setName("client");

        String resp = httpClientUtil.postJson("https://192.168.100.3:8443/test/serv/t1", null);
        log.info("收到服务端的响应信息： {}", resp);

    }
}
