# 企业 OA 协同办公系统

前后端分离的学习项目，包含组织与员工管理、GPS 考勤、请假/外出审批、公告与消息、计划进度、会议预约和 AI 助手。后端采用 JWT 登录校验、岗位角色控制及 BCrypt 密码校验；审批状态与流水通过事务更新，考勤通过 Haversine 距离计算校验范围，会议支持预约冲突检查及日历导出。

## 目录

- `backend`：Spring Boot 3、MyBatis-Plus、MySQL、JWT、阿里云 OSS。
- `frontend`：Vue 3、Vite、Element Plus。
- `docs/schema.sql`：数据库表结构与脱敏演示数据。
- `infra/searxng`：可选的本地网页搜索容器配置。

## 环境要求

- Java 17、Maven 3.9。
- Node.js 22.12+ 或 24、npm。
- MySQL 8.0+；SQL 使用 `utf8mb4_0900_ai_ci`。
- 地图和上传分别需要自行申请的高德、OSS 配置；AI 对话需要本机 Ollama 的 `qwen2.5:1.5b` 模型，网页搜索为可选项。

## 本地配置

`backend/.env.example` 仅是变量清单，Spring Boot 不会自动读取 `.env`。在 IDE 的运行配置或启动进程环境中设置 `MYSQL_USERNAME`、`MYSQL_PASSWORD`、`JWT_SECRET`；其他变量按示例填写。JWT 使用本机随机生成、至少 32 字节的独立随机值，不使用数据库密码或示例文字。

OSS 配置读取 `OSS_ENDPOINT`、`OSS_BUCKET_NAME`、`OSS_REGION`；SDK 在上传时直接读取进程环境中的 `OSS_ACCESS_KEY_ID` 和 `OSS_ACCESS_KEY_SECRET`。未配置时不验证上传功能。使用仅允许访问所需 Bucket 的 RAM 凭据，不使用云主账号密钥。

前端在 `frontend` 目录执行：

```powershell
Copy-Item .env.example .env.local
```

仅在本机填写 `.env.local` 的高德配置。**`VITE_*` 变量会进入前端构建结果，不是保密机制**；公开部署前限制可用域名和配额，安全密钥保护按高德官方方案配置。不要公开上传带真实地图配置的 `dist`。如果此前凭据已公开，删除文件不能撤销泄露，应先在服务控制台更换/吊销。

数据库用户名、密码、JWT、OSS 凭据彼此独立，任何真实值均不得写入示例或 README。

## 初始化演示数据库

**`docs/schema.sql` 会 DROP 并重建表，只对独立、可丢弃的演示数据库执行。不要对已有业务数据库执行。** 默认创建禁用的 `admin`，不附带公开通用密码；手机号留空，邮箱为 `example.com`，组织/会议室为演示内容。默认签到坐标仅是上海示例点，不代表真实办公地点，请在演示中调整。

使用项目现有 `BCryptPasswordEncoder` 在本机生成自己选择的演示密码密文（例如 IDE 本地求值，勿将密码/密文写回仓库）。连接 MySQL 时通过 `-p` 交互输入数据库密码；在同一会话先设置会话变量，再导入 SQL：

```sql
SET @DEMO_ADMIN_PASSWORD_HASH = '<本机生成的完整 BCrypt 密文>';
SOURCE docs/schema.sql;
SET @DEMO_ADMIN_PASSWORD_HASH = NULL;
```

从项目根目录启动 MySQL 客户端，`SOURCE` 路径按实际路径调整。SQL 只做 BCrypt 格式检查；真正密码匹配由应用进行。未提供密文时管理员禁用；随后可在本地数据库更新管理员 `password` 和 `status`，不要提交包含实际密文的副本。登录用户名为 `admin`，密码为生成密文时自己选择的值。

## 启动

```powershell
cd backend
mvn spring-boot:run
```

另开终端：

```powershell
cd frontend
npm ci
npm run dev
```

后端默认 `http://localhost:8080/api`，前端默认 `http://localhost:5173`，Vite 把 `/api` 转发到后端。开发服务器默认监听所有网卡，仅在可信本地网络使用；生产环境还需要 HTTPS、访问控制和日志脱敏。

如需网页搜索，可在 `infra/searxng` 执行 `docker compose up -d`，并将 `AI_WEB_SEARCH_ENABLED` 设为 `true`。AI 模型不会由应用自动拉取；未启动 Ollama 或 SearXNG 时不验证相应功能。

## 验证

```powershell
cd backend
mvn test

cd ..\frontend
node --test test/*.test.js
npm run build
```

测试和构建不等于数据库、地图定位、OSS 上传或 AI 服务联调通过，应在自己的本地环境另行验证。

## 发布检查

- 根目录 `.gitignore` 排除构建产物、依赖、缓存、IDE 配置、本地环境文件和常见私钥文件；`.env.example`、SQL、源码及依赖锁文件应保留。
- 检查源码、测试、图片附件及 SQL 是否有个人资料、真实服务地址和凭据，不能只检查 `application.yml`。
- 构建会重新生成 `backend/target` 和 `frontend/dist`，不应提交；忽略规则不影响已被跟踪的文件，也不清除历史。
- 本次整理不初始化 Git、不提交、不推送，也不自动连接云服务或导入数据库。
