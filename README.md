# RenewCommunity

基于 Spring Boot 的老旧小区改造项目资料台账管理系统，围绕"一项目一档"实现项目资料归档、台账管理、版本追溯、进度跟踪与权限控制。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3、TypeScript、Vite、Naive UI、Pinia（基于 Soybean Admin） |
| 后端 | Spring Boot 3.5、Java 17、MyBatis-Plus 3.5 |
| 认证授权 | Sa-Token 1.46（会话存储于 Redis） |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| 构建 | Maven、pnpm |

## 工程结构

```text
RenewCommunity/
├── src/                # Spring Boot 后端
│   └── main/java/com/renewcommunity/renewcommunity/
│       ├── common/     # 统一响应、错误码、全局异常
│       ├── config/     # Sa-Token、跨域、业务配置
│       ├── controller/ # RESTful 接口
│       ├── service/    # 业务逻辑
│       └── model/      # DTO / VO
├── admin/              # Vue 3 管理端
├── uniapp/             # uni-app 小程序端（微信小程序 + H5）
└── pom.xml
```

## 快速开始

环境要求：JDK 17、MySQL 8.0、Redis 6+、Node.js 20.19+、pnpm 10.5+。

```bash
# 1. 创建数据库
mysql -uroot -p -e "CREATE DATABASE renew_community DEFAULT CHARSET utf8mb4;"

# 2. 启动后端（默认 dev 环境，端口 8080）
./mvnw spring-boot:run

# 3. 启动管理端（端口 9527）
cd admin
pnpm install
pnpm dev
```

访问 `http://localhost:9527`，开发环境默认账号：`admin / 123456`。

### 小程序端

```bash
cd uniapp
pnpm install

# H5 调试（端口 5173，/api 代理到 8080）
pnpm dev:h5

# 微信小程序
pnpm dev:mp-weixin
# 用微信开发者工具导入 uniapp/dist/dev/mp-weixin
```

小程序端说明：在 `src/manifest.json` 中填写自己的微信 AppID；本地调试需在微信开发者工具中勾选"不校验合法域名"；真机调试或上线时需将后端部署为 HTTPS 域名并在小程序后台配置 request 合法域名。

数据库与 Redis 连接可通过环境变量覆盖：

```bash
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

## 当前进度

- [x] 前后端工程骨架与分层结构
- [x] 统一响应、全局异常、参数校验
- [x] Sa-Token 登录认证、路由拦截与角色权限
- [x] 管理端登录接入（请求拦截器、`satoken` 鉴权头、登录失效处理）
- [x] 小程序端工程（uni-app + Vue3 + TS），支持微信小程序与 H5，已接入登录
- [ ] 用户 / 角色 / 菜单权限与动态路由
- [ ] 小区、楼栋、项目、参建单位管理
- [ ] 资料分类、资料台账、版本管理与文件上传
- [ ] 进度节点、统计报表、操作日志

## License

管理端基于 [Soybean Admin](https://github.com/soybeanjs/soybean-admin)（MIT License）二次开发。
