package cn.byzk.example.sslclient.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @author xcf
 * @version 1.0
 * @ClassName: AAA
 * @description: TODO
 * @date 2023/1/6 14:37
 */
@Scope("prototype")
@Configuration
@Slf4j
public class RestTemplateHttpConfig {

    /**
     * 读超时时间，单位为ms
     */
    @Value("${request.read-timeout:15000}")
    private int readTimeout;

    /**
     * 连接超时时间，单位为ms
     */
    @Value("${request.connect-timeout:10000}")
    private int connectTimeout;

    public RestTemplateHttpConfig() {

    }

    public RestTemplateHttpConfig(int readTimeout) {
        this.readTimeout = readTimeout;
    }



    @Bean
    public RestTemplate restTemplate(@Qualifier("httpFactory") ClientHttpRequestFactory factory) {
        //public RestTemplate restTemplate(ClientHttpRequestFactory factory) {

        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {

            }
        });
        return restTemplate;
    }

    @Bean("httpFactory")
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();


        /**
         * 读超时时间，单位为ms
         */
        factory.setReadTimeout(readTimeout);

        /**
         * 连接超时时间，单位为ms
         */
        factory.setConnectTimeout(connectTimeout);
        return factory;
    }


}
