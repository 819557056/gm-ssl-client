package cn.byzk.example.sslclient.http;

import cn.hutool.core.bean.BeanUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author xcf
 * @version 1.0
 * @ClassName: AAA
 * @description: TODO
 * @date 2023/1/6 15:01
 */
@Component
@Slf4j
public class HttpClientUtil {

    @Resource
    private RestTemplate restTemplate;

    /**
     * https配置类
     */
    @Resource
    private RestTemplateHttpsConfig restTemplateHttpsConfig;

    /**
     * http配置类
     */
    @Resource
    private RestTemplateHttpConfig restTemplateHttpConfig;


    @Value("${request.type:http}")
    private String requestType;

    private static final String HTTP = "http";
    private static final String HTTPS = "https";


    public HttpClientUtil() {
        //this.restTemplate = new RestTemplate();
    }

    /**
     * 设置RestTemplate的请求协议是http，https
     *
     * @return org.springframework.web.client.RestTemplate
     * @author xcf
     * @date 2023/1/6 16:38
     * @Version 1.0.0
     */
    private RestTemplate getRestTemplate() {
        if (HTTP.equals(requestType)) {
            return restTemplateHttpConfig
                    .restTemplate(restTemplateHttpConfig.simpleClientHttpRequestFactory());
        } else if (HTTPS.equals(requestType)) {
            return restTemplateHttpsConfig
                    .restTemplate(restTemplateHttpsConfig.httpComponentsClientHttpRequestFactory());
        }
        return restTemplateHttpConfig
                .restTemplate(restTemplateHttpConfig.simpleClientHttpRequestFactory());
    }

    private RestTemplate getRestTemplate(Integer readTimeout) {

        //设置读取超时时间
        if (BeanUtil.isEmpty(readTimeout) || readTimeout == 0) {
            return getRestTemplate();
        }


        if (HTTP.equals(requestType)) {

            RestTemplateHttpConfig restTemplateHttpConfigBean = new RestTemplateHttpConfig(readTimeout);

            return restTemplateHttpConfigBean.restTemplate(this.restTemplateHttpConfig.simpleClientHttpRequestFactory());

        } else if (HTTPS.equals(requestType)) {

            RestTemplateHttpsConfig restTemplateHttpsConfigBean = new RestTemplateHttpsConfig(readTimeout);

            return restTemplateHttpsConfigBean
                    .restTemplate(this.restTemplateHttpsConfig.httpComponentsClientHttpRequestFactory());
        }

        RestTemplateHttpConfig restTemplateHttpConfigBean = new RestTemplateHttpConfig(readTimeout);

        return restTemplateHttpConfigBean.restTemplate(this.restTemplateHttpConfig.simpleClientHttpRequestFactory());

    }

    //==========================GET======================================

    /**
     * 一般的GET请求，封装getForEntity接口
     */
    public <T> ResponseEntity<T> getEntity(String url, Class<T> responseType, Object... uriVariables) {
        return restTemplate.getForEntity(url, responseType, uriVariables);
    }

    public String getObj(String url) {
        log.debug("get-> url = {}", url);
        String s = restTemplate.getForObject(url, String.class);
        log.debug("res-> {}", s);
        return s;
    }

    /**
     * 一般的GET请求
     */
    public String getObj(String url, Map<String, ?> paramMap) {
        log.debug("get-> url = {}, params: {}", url, paramMap.toString());
        String s = restTemplate.getForObject(url, String.class, paramMap);
        log.debug("res-> {}", s);
        return s;
    }

    /**
     * 一般的GET请求，并返回header
     */
    public String getWithHeader(String url, Map<String, ?> paramMap, HttpHeaders headers) {
        log.debug("get-> url = {}, params: {}", url, paramMap.toString());
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<String> results = restTemplate
                .exchange(url, HttpMethod.GET, entity, String.class, paramMap);
        String s = results.getBody();
        log.debug("res-> {}", s);
        return s;
    }

    public ResponseEntity<String> getE2(String url, Map<String, ?> paramMap) {
        log.debug("get-> url = {}, params: {}", url, paramMap.toString());
        HttpEntity<String> entity = new HttpEntity<String>("parameters");
        ResponseEntity<String> results = restTemplate
                .exchange(url, HttpMethod.GET, entity, String.class, paramMap);
        String s = results.getBody();
        log.debug("res-> {}", s);
        return results;
    }

    /**
     * 一般的GET请求，请求信息附带cookies
     */
    public String getObjCookie(String url, Map<String, ?> paramMap, List<String> cookies) {
        log.debug("get-> url = {}, params: {}, cookies: {}", url, paramMap.toString(), cookies.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, cookies);
        //headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<String> results = restTemplate
                .exchange(url, HttpMethod.GET, entity, String.class, paramMap);

        String s = results.getBody();
        //String s = restTemplate.getForObject(url, String.class, paramMap);
        log.debug("res-> {}", s);
        return s;
    }

    //========================POST========================================

    /**
     * 一般的POST请求
     *
     * @param url
     * @param paramMap
     * @return java.lang.String
     * @author xcf
     * @date 2023/1/6 15:11
     * @Version 1.0.0
     */
    public String postObj(String url, MultiValueMap paramMap) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);

        log.debug("post-> url = {}, params: {}", url, paramMap.toString());

        String s = getRestTemplate().postForObject(url, httpEntity, String.class);

        log.debug("res-> {}", s);
        return s;
    }



    /**
     * 发送json字符串数据
     * 默认进行签名
     * @param url
     * @param msg
     * @return java.lang.String
     * @author xcf
     * @date 2023/1/6 16:54
     * @Version 1.0.0
     */
    public String postJson(String url, String msg) {
        //请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //请求体
        //封装成一个请求对象
        HttpEntity entity = new HttpEntity(msg, headers);

        String result = getRestTemplate().postForObject(url, entity, String.class);

        log.debug("res-> {}", result);
        return result;
    }

    /**
     * 发送json字符串数据
     * 不签名请求
     * @param url
     * @param msg
     * @return java.lang.String
     * @author xcf
     * @date 2023/1/6 16:54
     * @Version 1.0.0
     */
    public String postJsonUnSign(String url, String msg) {
        //请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //请求体
        //封装成一个请求对象
        HttpEntity entity = new HttpEntity(msg, headers);

        String result = getRestTemplate().postForObject(url, entity, String.class);

        log.debug("res-> {}", result);
        return result;
    }

    /**
     * POST请求，支持设置读取超时时间
     * 默认进行签名
     * @param url
     * @param msg
     * @param readTimeout
     * @return java.lang.String
     * @author xcf
     * @date 2023/3/16 15:59
     * @Version 1.0.0
     */
    public String postJson(String url, String msg, Integer readTimeout) {
        //请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //请求体
        //封装成一个请求对象
        HttpEntity entity = new HttpEntity(msg, headers);

        String result = getRestTemplate(readTimeout).postForObject(url, entity, String.class);

        log.debug("res-> {}", result);
        return result;
    }

    /**
     * POST请求，支持设置读取超时时间
     *
     * @param url
     * @param msg
     * @param readTimeout
     * @return java.lang.String
     * @author xcf
     * @date 2023/3/16 15:59
     * @Version 1.0.0
     */
    public String postJsonUnSign(String url, String msg, Integer readTimeout) {
        //请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //请求体
        //封装成一个请求对象
        HttpEntity entity = new HttpEntity(msg, headers);

        String result = getRestTemplate(readTimeout).postForObject(url, entity, String.class);

        log.debug("res-> {}", result);
        return result;
    }


    /**
     * 一般的POST请求，请求信息附带cookies
     *
     * @param url
     * @param paramMap
     * @param cookies
     * @return java.lang.String
     * @author xcf
     * @date 2023/1/6 15:11
     * @Version 1.0.0
     */
    public String postObjCookie(String url, MultiValueMap paramMap, List<String> cookies) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put(HttpHeaders.COOKIE, cookies);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(paramMap, headers);

        log.debug("post-> url = {}, params: {}, cookies: {}", url, paramMap.toString(), cookies);

        String s = getRestTemplate().postForObject(url, httpEntity, String.class);

        log.debug("res-> {}", s);
        return s;
    }



}
