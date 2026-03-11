# lumu99-server

Lumu99 论坛/社区平台后端服务。

技术栈：
- Java 17
- Spring Boot 3
- MySQL 8
- Flyway
- Spring Security + JWT
- springdoc-openapi

## 本地环境要求

- JDK 17
- Maven 3.9+
- MySQL 8（本地）

## 数据库初始化

本地示例账号：`root / 123456`。

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS lumu99_forum
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS lumu99_forum_test
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

可选环境变量：

```bash
DB_USERNAME=root
DB_PASSWORD=123456
DB_URL=jdbc:mysql://localhost:3306/lumu99_forum?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai

TEST_DB_USERNAME=root
TEST_DB_PASSWORD=123456
TEST_DB_URL=jdbc:mysql://localhost:3306/lumu99_forum_test?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

## 启动说明

## 本地启动

方式一（开发期推荐，dev profile）：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

方式二（先打包再运行）：

```bash
mvn clean package -DskipTests
java -jar target/lumu99-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

默认访问地址：
- `http://localhost:8080`

说明：
- 接口前缀不包含 `/api`。

## 服务器启动（Linux）

建议先设置环境变量：

```bash
export DB_USERNAME=root
export DB_PASSWORD=123456
export DB_URL='jdbc:mysql://127.0.0.1:3306/lumu99_forum?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
```

方式一（快速启动：jar + nohup）：

```bash
mvn clean package -DskipTests
nohup java -jar target/lumu99-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev > app.log 2>&1 &
```

方式二（推荐：systemd 托管）：

1. 构建并复制 jar 到固定路径（示例：`/opt/lumu99/lumu99-server.jar`）。
2. 创建文件 `/etc/systemd/system/lumu99-server.service`：

```ini
[Unit]
Description=Lumu99 Forum Backend
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/lumu99
Environment=SPRING_PROFILES_ACTIVE=dev
Environment=DB_USERNAME=root
Environment=DB_PASSWORD=123456
Environment=DB_URL=jdbc:mysql://127.0.0.1:3306/lumu99_forum?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
ExecStart=/usr/bin/java -jar /opt/lumu99/lumu99-server.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

3. 启用并启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable lumu99-server
sudo systemctl restart lumu99-server
sudo systemctl status lumu99-server
```

4. 查看日志：

```bash
sudo journalctl -u lumu99-server -f
```

方式三（Docker）：

```bash
docker build -t lumu99-server:latest .
docker run -d \
  --name lumu99-server \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=123456 \
  -e DB_URL='jdbc:mysql://host.docker.internal:3306/lumu99_forum?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  lumu99-server:latest
```

公网部署建议：
- 应用保持监听 `8080`
- 使用 Nginx/Caddy 反向代理到 `80/443`

## Flyway 迁移

`dev` 环境启动时会自动执行 Flyway 迁移。

迁移脚本目录：
- `src/main/resources/db/migration`

当前基线脚本：
- `V1__init_forum_schema.sql`

## 测试与校验

运行完整测试：

```bash
mvn clean test
```

运行完整校验（含打包）：

```bash
mvn verify
```

## API 文档

OpenAPI JSON：
- `http://localhost:8080/v3/api-docs`

Swagger UI：
- `http://localhost:8080/swagger-ui/index.html`

分组 OpenAPI：
- `http://localhost:8080/v3/api-docs/auth`
- `http://localhost:8080/v3/api-docs/admin`
- `http://localhost:8080/v3/api-docs/forum`
- `http://localhost:8080/v3/api-docs/review`
- `http://localhost:8080/v3/api-docs/content`
- `http://localhost:8080/v3/api-docs/message`
