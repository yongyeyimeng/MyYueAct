# 悦活动（MyYueAct）

前后端一体的活动管理小程序项目，包含前端小程序和后端接口服务。

## 项目结构

```text
.
├── activity-management   # Spring Boot 后端
│   ├── application.example.yml
│   └── src/main/resources/sql/  # 数据库脚本
└── ShowActivity          # uni-app 前端小程序
```

## 环境要求

- JDK 17
- Maven 3.8 及以上
- MySQL 8.0
- HBuilderX（打开前端项目）
- 微信开发者工具

## 数据库初始化

1. 启动 MySQL。
2. 执行完整建库脚本：

```sql
SOURCE D:/YueActTry/activity-management/src/main/resources/sql/schema.sql;
```

`schema.sql` 会创建 `huoji` 数据库以及全部业务表。

## 后端启动

1. 复制配置文件：

```text
activity-management/application.example.yml
  -> activity-management/src/main/resources/application.yml
```

2. 在 `application.yml` 或环境变量中配置以下值：

```text
DB_PASSWORD   数据库密码
WECHAT_APPID  微信小程序 AppID
WECHAT_SECRET 微信小程序 AppSecret
JWT_SECRET    JWT 签名密钥，至少 32 个字符
```

3. 启动后端：

```bash
cd activity-management
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

## 前端启动

1. 使用 HBuilderX 打开 `ShowActivity` 目录。
2. 在 `manifest.json` 的 `mp-weixin -> appid` 中填写你自己的微信小程序 AppID。
3. 后端地址默认配置在 `ShowActivity/static/js/api.js` 的 `BASE_URL`，默认是 `http://localhost:8080`。
4. 运行到微信开发者工具。
5. 本地联调时，在微信开发者工具中勾选“不校验合法域名”；正式发布时必须使用 HTTPS 域名并开启校验。

## 安全说明

- `application.yml` 包含本机真实配置，已被 Git 忽略，不会上传。
- 公开分享前，请确认微信 AppSecret 没有泄露；如果曾上传到公开仓库，必须去微信公众平台重置。
- `JWT_SECRET` 从配置读取，不要再写死到代码里。
- 数据库密码不要使用弱密码，正式环境建议使用独立账号并限制权限。
