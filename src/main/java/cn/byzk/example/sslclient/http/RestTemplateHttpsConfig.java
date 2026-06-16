package cn.byzk.example.sslclient.http;

import cn.hutool.core.bean.BeanUtil;
import cn.byzk.example.sslclient.config.KonaProviderRegistrar;
import cn.byzk.example.sslclient.config.KonaSecurityConstants;
import com.tencent.kona.KonaProvider;
import com.tencent.kona.crypto.KonaCryptoProvider;
import com.tencent.kona.pkix.KonaPKIXProvider;
import com.tencent.kona.ssl.KonaSSLProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


/**
 * HttpClient工具类
 * JDK17
 * httpclient5
 *
 * @author xcf
 * @date 2024/7/16 上午9:55
 */
@Configuration
@Slf4j
@Data
public class RestTemplateHttpsConfig {

    public RestTemplateHttpsConfig() {

    }

    public RestTemplateHttpsConfig(int readTimeout) {
        this.readTimeout = readTimeout;
    }


    /**
     * 启用客户端验证服务端
     */
    private static final String TWO_TYPE_ENABLED_FINAL = "enabled";

    /**
     * 停用客户端验证服务端
     */
    private static final String TWO_TYPE_DISABLED_FINAL = "disabled";
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

    /**
     * 一次最多接收MaxTotal次请求
     */
    @Value("${request.max-total:200}")
    private int maxTotal;

    /**
     * 一次最多接收MaxPerRoute次请求
     */
    @Value("${request.max-per-route:200}")
    private int maxPerRoute;

    @Value("${request.two-way.enabled:disabled}")
    private String twoTypeEnabled;

    /**
     * 双向认证
     * 客户端p12路径
     */
    @Value("${request.two-way.client-p12-path}")
    private String clientP12Path;

    /**
     * ca信任库
     */
    @Value("${request.two-way.ca-keystore-path}")
    private String caKeystorePath;

    /**
     * ca信任库密码
     */
    @Value("${request.two-way.key-storepass}")
    private String keyStorePasswd;

    @Value("${request.two-way.trust-store-password}")
    private String trustStorePasswd;


    /**
     * 是否为GM的SSL,默认为false
     */
    @Value("${request.is-gm:false}")
    private boolean isGm;

    /**
     * SSL密钥更新时间（单位：小时），默认为8小时
     */
    @Value("${request.ssl-key-update-hours:8}")
    private int sslKeyUpdateHours;

    private static final String provider = KonaSecurityConstants.PROVIDER_KONA;
    private static final String protocol = KonaSecurityConstants.PROTOCOL_TLCP_V1_1;


    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {

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


    //@Bean(name = "httpsFactory")
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
        try {
            CloseableHttpClient httpClient = acceptsUntrustedCertsHttpClient(maxTotal, maxPerRoute);


            HttpComponentsClientHttpRequestFactory httpsFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);


            return httpsFactory;
        } catch (Exception e) {
            log.error("加载ssl设置错误", e);
            return null;
        }
    }

    /**
     * 从调用层设置读取超时时间
     *
     * @param readTimeOut
     * @return org.springframework.http.client.HttpComponentsClientHttpRequestFactory
     * @author xcf
     * @date 2023/3/16 14:56
     * @Version 1.0.0
     */
    public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory(Integer readTimeOut) {

        if (BeanUtil.isEmpty(readTimeOut)) {
            return httpComponentsClientHttpRequestFactory();
        }

        try {
            CloseableHttpClient httpClient = acceptsUntrustedCertsHttpClient(maxTotal, maxPerRoute);
            return new HttpComponentsClientHttpRequestFactory(httpClient);
        } catch (Exception e) {
            log.error("加载ssl设置错误", e);
            return null;
        }
    }


    /**
     * 判断是否开启客户端验证服务端.
     *
     * @return boolean
     * @author xcf
     * @date 2023/1/6 20:47
     * @Version 1.0.0
     */
    public boolean isTwoTypeEnabled() {
        return TWO_TYPE_ENABLED_FINAL.equals(twoTypeEnabled);
    }


    /**
     * 设置httpclient配置
     * 2个参数的含义：
     * * 服务1要通过Fluent调用服务2的接口。服务1发送了400个请求，
     * * 但由于Fluent默认只支持maxPerRoute=100，MaxTotal=200，
     * * 比如接口执行时间为500ms，由于maxPerRoute=100，所以要分为100,100,100,100分四批来执行，
     * * 全部执行完成需要2000ms。而如果maxPerRoute设置为400，全部执行完需要500ms。
     * * 在这种情况下（提供并发能力时）就要对这两个参数进行设置了。
     *
     * @param maxTotal   一次最多接收MaxTotal次请求
     * @param maxPerRout 每次能并行接收的请求数量
     * @return org.apache.http.impl.client.CloseableHttpClient
     * @author xcf
     * @date 2023/1/6 21:17
     * @Version 1.0.0
     */
    public CloseableHttpClient acceptsUntrustedCertsHttpClient(int maxTotal, int maxPerRout)
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
        HttpClientBuilder b = HttpClientBuilder.create();

        Timeout connectTimeouts = Timeout.ofMilliseconds(connectTimeout);
        Timeout readTimeouts = Timeout.ofMilliseconds(readTimeout);

        //设置请求配置,设置请求配置, 可以进一步设置连接超时、读取超时等属性
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(connectTimeouts)
                .setResponseTimeout(readTimeouts)
                .build();

        PoolingHttpClientConnectionManager connMgr = null;
        SSLContext sslContext = null;
        if (isTwoTypeEnabled()) {

            String[] cipherSuites;
            String[] protocols;
            SSLConnectionSocketFactory sslConnectionSocketFactory;
            if (isGm) {
                log.debug("使用GM-SSL协议");
//                sslContext = customGm(getClientP12Path()
                sslContext = customGmKona(getClientP12Path()
                        , getCaKeystorePath(), getKeyStorePasswd());
                cipherSuites = new String[]{
                        "ECC-SM2-SM4-CBC-SM3",
                        "ECC-SM2-WITH-SM4-SM3",
                        "ECDHE-SM2-WITH-SM4-SM3",
                        // 添加更多你需要的Cipher Suites...
                };

                sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
            } else {
                sslContext = custom(getClientP12Path()
                        , getCaKeystorePath(), getKeyStorePasswd());

                cipherSuites = new String[]{
                        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                        "TLS_CHACHA20_POLY1305_SHA256",
                        // 添加更多你需要的Cipher Suites...
                };
                protocols = new String[]{"TLSv1.2", "TLSv1.3"};

                // 创建SSLConnectionSocketFactory，设置加密套件和协议版本
                sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                        sslContext
                        , protocols
                        , cipherSuites
                        , HttpsSupport.getDefaultHostnameVerifier()
                );
            }


//            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
//                    .register("https", sslConnectionSocketFactory)
//                    .build();

            // 计算连接TTL（Time To Live），将小时转换为毫秒
            TimeValue connectionTimeToLive = TimeValue.ofHours(sslKeyUpdateHours);

            // 使用ConnectionConfig设置连接的生命周期
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setTimeToLive(connectionTimeToLive)
                    .build();

            // 使用Builder模式创建连接管理器，设置连接TTL以实现密钥更新
            connMgr = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .setDefaultConnectionConfig(connectionConfig)
                    .setMaxConnTotal(maxTotal)
                    .setMaxConnPerRoute(maxPerRout)
                    .build();

            log.debug("SSL客户端连接管理器已配置，密钥更新时间: {} 小时", sslKeyUpdateHours);

            return HttpClients.custom().setDefaultRequestConfig(config).setConnectionManager(connMgr).build();

        } else {

            /**
             * setup a Trust Strategy that allows all certificates.
             *
             */
            if (isGm()) {
                sslContext = customGmKona(getClientP12Path()
                        , getCaKeystorePath(), getKeyStorePasswd());
            } else {
                sslContext = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
            }


            // don't check Hostnames, either.
            //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

            // here's the special part:
            //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
            //      -- and create a Registry, to register it.
            //
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            // 计算连接TTL（Time To Live），将小时转换为毫秒
            TimeValue connectionTimeToLive = TimeValue.ofHours(sslKeyUpdateHours);

            // 使用ConnectionConfig设置连接的生命周期
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setTimeToLive(connectionTimeToLive)
                    .build();

            // now, we create connection-manager using our Registry.
            //      -- allows multi-threaded use
            // 使用Builder模式创建连接管理器，设置连接TTL以实现密钥更新
            connMgr = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .setDefaultConnectionConfig(connectionConfig)
                    .setMaxConnTotal(maxTotal)
                    .setMaxConnPerRoute(maxPerRout)
                    .build();

            log.debug("SSL客户端连接管理器已配置（非双向认证），密钥更新时间: {} 小时", sslKeyUpdateHours);

            b.setConnectionManager(connMgr).setDefaultRequestConfig(config);

            // finally, build the HttpClient;
            //      -- done!
            CloseableHttpClient client = b.build();

            return client;

        }

    }

    /**
     * 设置信任自签名证书
     *
     * @param clientP12  客户端证书
     * @param caTrustLib ca信任库
     * @param password   库密码
     * @return
     */
    public SSLContext custom(String clientP12, String caTrustLib, String password) {
        SSLContext sc = null;
        FileInputStream instream = null;
        KeyStore trustStore = null;

        try {
            trustStore = KeyStore.getInstance("PKCS12");
            instream = new FileInputStream(clientP12);
            trustStore.load(instream, keyStorePasswd.toCharArray());


            // 相信自己的CA和所有自签名的证书
            sc = SSLContexts.custom()
                    .loadTrustMaterial(new File(caTrustLib), trustStorePasswd.toCharArray())
                    .loadKeyMaterial(trustStore, trustStorePasswd.toCharArray())
                    .build();

        } catch (KeyStoreException | NoSuchAlgorithmException
                 | CertificateException | IOException | KeyManagementException
                 | UnrecoverableKeyException e) {

            throw new RuntimeException("ssl certificate load failed", e);
        } finally {
            try {
                instream.close();
            } catch (IOException e) {
            }
        }
        return sc;
    }

//    public SSLContext customGm(String clientP12, String caTrustLib, String password) {
//        SSLContext sc = null;
//        FileInputStream instream = null;
//        KeyStore trustStore = null;
//
////        Security.addProvider(new BouncyCastleProvider());
//
//        try {
//
//            String provider = "Tongsuo_Security_Provider";
//            String protocol = "TLSv1.1";
//            String txProtocol = "KonaCrypto";
//            String bcProtocol = "BC";
//
//            // 创建自定义的密钥管理器
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            KeyStore keyStore = KeyStore.getInstance("PKCS12", bcProtocol);
//            char[] keyStorePassword = keyStorePasswd.toCharArray();
//            keyStore.load(new FileInputStream(clientP12Path), keyStorePassword);
//            kmf.init(keyStore, keyStorePassword);
//
//            // 创建自定义的信任管理器
//            trustStore = KeyStore.getInstance("PKCS12", bcProtocol);
//            instream = new FileInputStream(clientP12);
//            trustStore.load(instream, trustStorePasswd.toCharArray());
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            tmf.init(trustStore);
//
//            // 创建 SSLContext
////            SSLContext context = SSLContext.getInstance(protocol, new TongsuoProvider());
//            // 初始化 SSLContext
//            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
//
////            context.init(null, null, null);
//
//            return context;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }

    public SSLContext customGmKona(String clientP12, String caTrustLib, String password) {

        FileInputStream instream = null;
        System.setProperty("java.security.debug", "true");
        System.setProperty("java.security.auth.debug", "true");
        System.setProperty("com.tencent.kona.ssl.debug", "all");

        Security.insertProviderAt(new KonaProvider(), 1);
        Security.insertProviderAt(new KonaCryptoProvider(), 2);
        Security.insertProviderAt(new KonaPKIXProvider(), 3);
        Security.insertProviderAt(new KonaSSLProvider(), 4);

        try {

            // 创建自定义的密钥管理器
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KonaSecurityConstants.KEY_MANAGER_ALGORITHM, provider);
            KeyStore keyStore = KeyStore.getInstance("PKCS12", provider);
            char[] keyStorePassword = keyStorePasswd.toCharArray();
            keyStore.load(new FileInputStream(clientP12), keyStorePassword);
            kmf.init(keyStore, keyStorePassword);

            // 创建自定义的信任管理器
            KeyStore trustStore = KeyStore.getInstance("PKCS12", provider);
            instream = new FileInputStream(caTrustLib);
            trustStore.load(instream, trustStorePasswd.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KonaSecurityConstants.TRUST_MANAGER_ALGORITHM, provider);
            tmf.init(trustStore);

            // 创建 SSLContext
            SSLContext context = SSLContext.getInstance(protocol, provider);
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

//            context.init(null, null, null);

            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (instream != null) {
                try {
                    instream.close();
                } catch (Exception e) {

                }
            }
        }

    }


}
