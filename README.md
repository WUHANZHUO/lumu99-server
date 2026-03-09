# lumu99-server

最小可运行的 Java Spring Boot 后端服务（Java 17 + Spring Boot 3）。

## 功能

- 提供 `GET /hello`
- 返回纯文本：`Hello from Spring Boot`
- 服务端口：`8080`

## 本地运行

### 环境要求

- Java 17
- Maven 3.9+

### 方式一：直接运行

```bash
mvn spring-boot:run
```

### 方式二：打包后运行

```bash
mvn clean package
java -jar target/lumu99-server-0.0.1-SNAPSHOT.jar
```

### 接口验证

```bash
curl http://localhost:8080/hello
```

预期输出：

```text
Hello from Spring Boot
```

## 使用 Docker 运行

### 构建镜像

```bash
docker build -t lumu99-server:latest .
```

### 本地测试启动容器

```bash
docker run --rm -p 8080:8080 lumu99-server:latest
```

### VPS 推荐启动命令（仅绑定本机回环，供 Nginx 反向代理）

```bash
docker run -d \
  --name lumu99-server \
  --restart unless-stopped \
  -p 127.0.0.1:8080:8080 \
  lumu99-server:latest
```

## 部署目录

本仓库可部署到：

`/root/apps/lumu99/lumu99-server`

后端不直接暴露到公网，仅通过 Nginx 反向代理访问。
