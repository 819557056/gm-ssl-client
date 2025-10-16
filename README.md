# SSL Client 项目

## 项目简介

这是一个基于 Spring Boot 的 SSL/TLS 客户端示例项目，支持国密（GM SSL）算法，实现了与 SSL 服务端的双向认证通信。项目使用腾讯 Kona 国密支持库，提供安全的 HTTPS 通信能力。

## 技术栈

- **Spring Boot**: 3.3.1
- **Java**: 17
- **Kona 国密库**: 1.0.13（支持国密算法）
- **Apache HttpClient 5**: 5.3.1（HTTP 通信）
- **Hutool**: 5.8.25（工具类库）
- **Lombok**: 简化代码编写

## 主要功能

- ✅ 支持 HTTPS 双向认证
- ✅ 支持国密（GM SSL）算法
- ✅ 提供灵活的 HTTP/HTTPS 客户端配置
- ✅ 封装 HTTP 请求工具类
- ✅ 支持标准 SSL 和国密 SSL 切换

## 项目结构

```
ssl-client/
├── src/
│   └── main/
│       ├── java/
│       │   └── cn/byzk/example/sslclient/
│       │       ├── controller/          # 控制器层
│       │       │   ├── TestController.java
│       │       │   └── TestServController.java
│       │       ├── http/                # HTTP 客户端配置
│       │       │   ├── HttpClientUtil.java
│       │       │   ├── RestTemplateHttpConfig.java
│       │       │   └── RestTemplateHttpsConfig.java
│       │       ├── model/               # 数据模型
│       │       │   └── UserDto.java
│       │       └── SslClientApplication.java
│       └── resources/
│           ├── application.yml          # 应用配置文件
│           ├── static/
│           └── templates/
├── ssl/                                 # SSL 证书目录
│   ├── keystore.p12                    # 客户端证书
│   └── truststore.p12                  # 信任的 CA 证书
└── pom.xml                             # Maven 配置文件
```

## 快速开始

### 1. 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- 已配置好的 SSL 证书文件（keystore.p12 和 truststore.p12）

### 2. 配置说明

在 `application.yml` 中配置以下参数：

```yaml
server:
  port: 7778                    # 客户端服务端口

request:
  read-timeout: 15000           # 读取超时时间（ms）
  connect-timeout: 15000        # 连接超时时间（ms）
  max-total: 200                # 最大连接数
  max-per-route: 200            # 每个路由的最大连接数
  type: https                   # 请求类型：http/https
  is-gm: true                   # 是否启用国密 SSL
  
  two-way:
    enabled: enabled            # 启用双向认证
    client-p12-path: /path/to/keystore.p12      # 客户端证书路径
    ca-keystore-path: /path/to/truststore.p12   # CA 证书路径
    key-storepass: 123456                       # 客户端证书密码
    trust-store-password: 123456                # CA 证书密码
```

### 3. 证书准备

将以下证书文件放置在 `ssl/` 目录下：

- `keystore.p12`: 客户端证书（包含签名证书和加密证书）
- `truststore.p12`: 受信任的 CA 证书

**注意**: 确保证书路径与 `application.yml` 中的配置一致。

### 4. 启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

应用启动后将监听 7778 端口。

### 5. 测试接口

项目提供了多个测试接口：

#### 测试接口 1 - 本地服务端
```bash
curl http://localhost:7778/test/cli/t1
```
连接到 `https://127.0.0.1:8888`

#### 测试接口 2 - 标准 HTTPS
```bash
curl http://localhost:7778/test/cli/t2
```
连接到 `https://localhost:8443`

#### 测试接口 3 - 远程服务端
```bash
curl http://localhost:7778/test/cli/t3
```
连接到 `https://192.168.100.3:8443`

## 核心组件说明

### HttpClientUtil

封装了 HTTP/HTTPS 请求的工具类，提供以下功能：

- POST JSON 请求
- 自动处理 SSL 连接
- 支持国密算法
- 连接池管理

### RestTemplateHttpsConfig

HTTPS 客户端配置类，负责：

- SSL 上下文初始化
- 证书加载和验证
- 国密算法支持配置
- 双向认证设置

### RestTemplateHttpConfig

HTTP 客户端配置类（非 SSL）。

## 国密支持

本项目使用腾讯 Kona 国密库，支持以下国密算法：

- **SM2**: 椭圆曲线公钥密码算法
- **SM3**: 密码杂凑算法
- **SM4**: 分组密码算法
- **TLCP**: 国密 SSL 协议

### 启用/禁用国密

在 `application.yml` 中设置：

```yaml
request:
  is-gm: true   # true: 使用国密算法, false: 使用标准 SSL
```

## 依赖说明

### Kona 国密库

```xml
<dependency>
    <groupId>com.tencent.kona</groupId>
    <artifactId>kona-provider</artifactId>
    <version>1.0.13</version>
</dependency>
<dependency>
    <groupId>com.tencent.kona</groupId>
    <artifactId>kona-ssl</artifactId>
    <version>1.0.13</version>
</dependency>
```

### Apache HttpClient 5

```xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.3.1</version>
</dependency>
```

## 常见问题

### 1. 证书验证失败

**问题**: `javax.net.ssl.SSLHandshakeException`

**解决方案**:
- 检查证书文件路径是否正确
- 确认证书密码是否正确
- 验证证书是否在有效期内
- 确认 truststore 中包含正确的 CA 证书

### 2. 连接超时

**问题**: 连接服务端超时

**解决方案**:
- 检查服务端是否正常运行
- 确认网络连接是否正常
- 调整 `connect-timeout` 和 `read-timeout` 参数

### 3. 国密算法不支持

**问题**: 国密算法初始化失败

**解决方案**:
- 确保 Kona 依赖版本正确
- 检查 JDK 版本是否为 17+
- 验证证书是否为国密算法生成

## 配置示例

### 标准 SSL 配置

```yaml
request:
  type: https
  is-gm: false
  two-way:
    enabled: enabled
    client-p12-path: /path/to/standard-keystore.p12
    ca-keystore-path: /path/to/standard-truststore.p12
    key-storepass: password
    trust-store-password: password
```

### 国密 SSL 配置

```yaml
request:
  type: https
  is-gm: true
  two-way:
    enabled: enabled
    client-p12-path: /path/to/gm-keystore.p12
    ca-keystore-path: /path/to/gm-truststore.p12
    key-storepass: password
    trust-store-password: password
```

## 开发指南

### 添加新的 HTTP 请求方法

在 `HttpClientUtil` 中添加：

```java
public String get(String url) {
    // 实现 GET 请求
}

public String postForm(String url, Map<String, String> params) {
    // 实现 POST 表单请求
}
```

### 自定义 SSL 配置

继承或修改 `RestTemplateHttpsConfig` 类：

```java
@Configuration
public class CustomSslConfig extends RestTemplateHttpsConfig {
    @Override
    protected void customizeSSLContext(SSLContext sslContext) {
        // 自定义 SSL 上下文
    }
}
```

## 打包部署

### 构建 JAR 包

```bash
mvn clean package
```

### 运行 JAR 包

```bash
java -jar target/ssl-client-0.0.1-SNAPSHOT.jar
```

### Docker 部署（可选）

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/ssl-client-0.0.1-SNAPSHOT.jar app.jar
COPY ssl/ /app/ssl/
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 相关项目

- [ssl-server](../ssl-server/) - SSL 服务端项目

## 许可证

该项目仅供学习和参考使用。

## 联系方式

如有问题或建议，请联系项目维护者。

---

**注意**: 请勿在生产环境中使用测试证书，务必使用正规 CA 签发的证书。

