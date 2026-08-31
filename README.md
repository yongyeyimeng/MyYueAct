# 悦活动（MyYueAct）

前后端一体的活动管理小程序项目，包含 Spring Boot 后端和 uni-app 前端小程序。

## 项目结构

```text
MyYueAct
├── activity-management       # 后端接口服务
│   ├── application.example.yml
│   └── src/main/resources/sql/  # 数据库脚本
└── ShowActivity              # 前端小程序
```

## 环境准备

开始前请准备好以下环境：

- JDK 17
- Maven 3.8 及以上
- MySQL 8.0
- HBuilderX
- 微信开发者工具
- 一个微信小程序 AppID

## 第一步：初始化数据库

1. 启动本机 MySQL。
2. 导入建库脚本：

```bash
mysql -uroot -p < activity-management/src/main/resources/sql/schema.sql
```

也可以使用 Navicat 等工具直接运行该文件。脚本会自动创建 `huoji` 数据库和全部业务表，不包含业务数据。

## 第二步：配置并启动后端

1. 复制配置文件：

```text
activity-management/application.example.yml
  -> activity-management/src/main/resources/application.yml
```

2. 打开复制出来的 `application.yml`，填写自己的配置：

```yaml
datasource:
  password: 你的数据库密码

wechat:
  appid: 你的微信小程序 AppID
  secret: 你的微信小程序 AppSecret

jwt:
  secret: 自己生成一个至少 32 位的随机字符串
```

也可以不填值，改成设置环境变量 `DB_PASSWORD`、`WECHAT_APPID`、`WECHAT_SECRET`、`JWT_SECRET`，配置文件里对应的是 `${...}` 占位符。

3. 启动后端：

```bash
cd activity-management
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

## 第三步：配置并运行前端

1. 使用 HBuilderX 打开 `ShowActivity` 目录。
2. 在 `manifest.json` 的 `mp-weixin -> appid` 中填写你自己的微信小程序 AppID。
3. 后端地址默认是 `http://localhost:8080`，如需修改，编辑 `ShowActivity/static/js/api.js` 中的 `BASE_URL`。
4. 在 HBuilderX 中运行到小程序模拟器，选择微信开发者工具。
5. 本地调试时，在微信开发者工具中勾选“不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书”。
6. 正式发布时，把 `BASE_URL` 改成线上 HTTPS 域名，并取消上面的“不校验”选项。

## 注意事项

- 数据库脚本使用 MySQL 8 语法，请使用 MySQL 8.0 及以上版本。
- 后端默认使用 8080 端口；如果前端以 H5 模式运行且也占用 8080，请修改前端 H5 开发端口。
- 真机预览时，`localhost` 指向手机本身，无法访问电脑上的后端，需要把 `BASE_URL` 改成电脑局域网 IP 或线上域名。
- `application.yml` 属于本地配置，不要提交到 Git；本项目已经在 `.gitignore` 中忽略该文件，公开配置只保留 `application.example.yml`。
