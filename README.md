# 码无涯 Mawuya · 个人博客系统

> 基于 Spring Boot 2.2 + MyBatis + Thymeleaf 的双端独立部署博客方案：**前台访客站 (AMS) + 后台管理系统 (BMS)**，共享同一份数据。模块化、可换肤、带评论审批与访客分析，全量遵循《阿里巴巴 Java 开发手册（嵩山版）》。

[![Java](https://img.shields.io/badge/Java-1.8+-orange)](https://www.oracle.com/java/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.5-brightgreen)](https://spring.io/projects/spring-boot) [![MyBatis](https://img.shields.io/badge/MyBatis-2.1-blue)](https://mybatis.org/) [![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-blue)](https://www.mysql.com/) [![License](https://img.shields.io/badge/license-MIT-lightgrey)](#license)

---

## 目录

- [项目简介](#项目简介)
- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [架构设计](#架构设计)
- [本地运行](#本地运行)
- [部署](#部署)
- [目录结构](#目录结构)
- [使用示例](#使用示例)
- [开发规范](#开发规范)
- [常见问题](#常见问题)

---

## 项目简介

**码无涯（Mawuya）** 是一个**双端独立部署、共享同一数据**的个人博客系统：

| 端 | 模块 | 默认端口 | 受众 | 用途 |
|---|---|---|---|---|
| 前台 | `mawuya-ams` | `8080` | 访客 | 文章浏览、分类/标签/归档/搜索、评论提交 |
| 后台 | `mawuya-bms` | `8081` | 博主 | 文章 CRUD、评论审批、图片库、主题切换、访客日志、用户管理、数据看板 |

两端是**两个独立的 Spring Boot 进程**，分别打成可执行 jar；通过共享 MySQL `blog` 库实现数据一致性，BMS 的所有改动对 AMS **下次访问立即可见**，无需双端发布。

---

## 核心特性

### 内容管理
- 📝 **Markdown 编辑器**：基于 [editor.md](https://github.com/pandao/editor.md)，支持代码高亮、TeX 公式、Mermaid 流程图、emoji、TOC
- 🏷️ **分类 + 标签** 双维度组织内容；标签云按文章数加权
- 🖼️ **图片库**：上传后自动 SHA256 去重落到 MySQL `image_blob` 表（避免文件系统依赖），支持模糊搜索 / 分页 / 缩略图选择器；写文章和设置博客封面图都可“从图片库选择”

### 互动 & 审批
- 💬 **评论审批工作流**：访客提交 → 默认“待审核” → BMS 审批通过 → 前台展示
- ✅ **状态机**：`PENDING(0) / APPROVED(1) / REJECTED(2)` ，支持 4 个 tab 视图与 `article_info.review_num` 自动同步
- 👍 点赞 / 踩 计数（数据看板算好评率）

### 数据看板（BMS 首页）
- 🎯 **3 大数据分组 12 张数据卡**：内容数据 / 互动数据 / 流量数据，每卡含图标渐变 + 趋势 sparkline
- 📊 **14 天 PV/UV 折线图**（Chart.js 渐变填充 + 联动 tooltip）
- 🩺 **博客健康度评分**（0-100）：4 个维度 + 减分规则 + 等级
- 📋 **TOP 排行**：阅读 TOP 5 文章 / 今日 TOP 5 访客 IP（带地理信息）
- ⚡ **待办速览**：待审核评论 / 未分类文章直达跳转

### 访客分析
- 🌍 **真实地理解析**：`ip-api.com` 主源（中文）+ `ipapi.co` 兜底，自动写入 `country / province / city / isp`
- 🔎 **多条件查询**：IP / URL / 方法 / 状态 / 省份 / 时间范围，支持表头排序、分页、详情弹窗
- 🛡️ **SQL 注入防御**：所有过滤参数化绑定；排序字段/方向走白名单收敛

### 鉴权（Spring Security + JWT）
- 🔐 **BMS 启用 Spring Security**：登录走 `JwtAuthFilter`；用户/角色/权限基于 `sys_user / sys_role / sys_user_role` 三张 RBAC 表
- 🪪 **JWT 无状态**：HMAC-SHA256，token 默认 8 小时；登录返回 `BaseResponse(00000)` + token
- 👥 **用户管理**：超级管理员可在 `/bms/user/list.do` 增删改查、重置密码、启停账号

### 视觉与交互
- 🎨 **一键换皮**：`default(原版) / tech-dark(深色科技) / gradient-vivid(活泼渐变) / minimal-business(极简商务)` 四套主题
- 📱 **响应式适配**：BMS 后台与 AMS 前台均覆盖 ≥1024 桌面 / 平板 / 手机三档断点
- 🌗 主题切换持久化在 `blog_info.theme_code`，跨进程实时同步

### 工程化
- 🚦 **三方依赖最小化**：拒用 lombok、fastjson；仅 Jackson 一种 JSON 库
- ⚡ **异步入库**：访客日志走内存队列 + 后台线程批量写入，不阻塞主请求；线程池统一 `ThreadPoolExecutor` + `NamedThreadFactory`
- 🔌 **可配置**：`mawuya.async.enabled` 控制异步线程开关；`server.port`、数据源、上传目录、JWT 密钥等全部走 yml
- 📐 **阿里规约全面落地**：阿里巴巴 Java 开发手册（嵩山版）强制规约项 100% 通过（详见 [开发规范](#开发规范)）

---

## 技术栈

| 类别 | 选型 | 版本 | 说明 |
|---|---|---|---|
| 语言 | Java | 1.8 | 兼容 LTS |
| 应用框架 | Spring Boot | 2.2.5.RELEASE | 单文件可执行 jar |
| 安全 | Spring Security | 5.2.x | 仅 BMS 启用；JWT + BCrypt |
| Web 模板 | Thymeleaf | 3.0.x | 服务端渲染，对 SEO 友好 |
| ORM | MyBatis Spring Boot | 2.1.3 | xml + 注解混合 |
| 连接池 | Druid | 1.1.22 | 监控/慢 SQL/防注入 |
| 数据库 | MySQL | 5.7+ / 8.x | utf8mb4，支持 emoji |
| HTTP 客户端 | OkHttp | 3.14.9 | 调用第三方 IP 地理 API |
| JSON | Jackson | 跟随 Spring Boot | 项目内**唯一** JSON 库 |
| 通用工具 | commons-lang3 | 3.9 | StringUtils / DateUtils |
| 前端 UI（BMS） | Bootstrap 3 + DataTables + layer | 内置 | 后台管理 |
| 前端 UI（AMS） | NexT 主题 + 自研 blog-extra.css | 内置 | 博客访客页 |
| 编辑器 | editor.md | 内置 | Markdown 全功能 |
| 图表 | Chart.js | 3.9.1（CDN） | 看板趋势图 |
| 构建 | Maven | 3.6+ | 多模块聚合 |

---

## 架构设计

### 模块依赖

```
              ┌────────────────────────────────────┐
              │   mawuya-all (parent pom)          │
              └──────────────────┬─────────────────┘
                                 │
             ┌───────────────────┼───────────────────┐
             │                   │                   │
             ▼                   ▼                   ▼
     mawuya-common         mawuya-core          (子模块)
     ├ utils 工具集     ├ dataobject (DO)         │
     │ (DateUtil/      ├ mapper / service        │
     │  OkHttpUtil/    ├ dto / vo                │
     │  StrUtil/...)   ├ cache / thread          │
                       ├ interceptor / listener  │
                       ├ exception / enums       │
                       └─ 共享 Spring 配置        │
                                 ▲               │
                                 │ 同时被依赖    │
                ┌────────────────┴───────────────┐
                │                                │
                ▼                                ▼
          mawuya-ams                       mawuya-bms
     (访客前台 :8080)                  (后台管理 :8081)
     - IndexController                 - DashboardController
     - ReviewController                - ArticleController
     - SearchController                - ReviewController
     - TagController                   - LogController
     - AboutController                 - ImageController
     - SeoController                   - ThemeController
     - LogToAPIThread (异步)           - CategoryController
                                       - UserController
                                       - SecurityConfig + JwtAuthFilter
```

- **`mawuya-common`**：纯工具类（`DateUtil` / `StrUtil` / `OkHttpUtil` / `FileUtil` / `PageUtil` / `CommonUtil`），无 Spring 依赖
- **`mawuya-core`**：所有持久化层、领域模型与跨端通用 DTO；BMS / AMS 共用 dataobject、mapper、service
- **`mawuya-ams`**：访客前台（无登录，匿名可访问），路径如 `/` `/{sn}` `/categories/{cid}` `/tags/{tid}` `/archive` `/search` `/about` `/comments/submit`
- **`mawuya-bms`**：后台管理（Spring Security + JWT），路径全部以 `/bms/**` 开头，登录入口 `/bms/login.do`

### 命名规约（与代码一致）

| 类别 | 包 | 后缀 | 示例 |
|---|---|---|---|
| 数据对象（与表 1:1） | `core.dataobject` | `xxxDO` | `ArticleDO` / `ReviewDO` / `UserDO` |
| 数据传输（接口出入参） | `core.dto` / `bms.dto` | `xxxDTO` | `LoginRequestDTO` / `ArticleSaveRequestDTO` |
| 视图层对象（页面/查询条件） | `core.vo` | `xxxVO` / `xxxQuery` | `LogInfoQuery` / `Page` |
| 数据访问层 | `core.mapper` | `xxxMapper` | `ArticleInfoMapper` |
| 业务服务层 | `core.service` | `xxxService` | `ArticleService` / `SysUserService` |
| 通用响应包装 | `core.common` | `BaseResponse<T>` | `ResultCodeEnum` 5 位 A/B/C 错误码 |

### 数据流（评论审批为例）

```
[访客在 AMS]                   [BMS 审批员]                  [AMS 下次访问]
   │                                │                                │
   │ POST /comments/submit          │                                │
   │ ───────────────────►           │                                │
   │                                │                                │
   ▼                                │                                │
ReviewController                    │                                │
   │ 持久化为 ReviewDO              │                                │
   │ status = PENDING(0)            │                                │
   ▼                                │                                │
[review_info ◄── MySQL ──► review_info]                              │
   │                                │                                │
   │ 前台过滤 status=1，看不到      │ /bms/review/list.do            │
   │                                │ 看到待审核 tab                 │
   │                                │                                │
   │                  POST approve.do (sn)                           │
   │                  ─────────────►                                 │
   │                                │ updateStatus → APPROVED(1)     │
   │                                │ refreshArticleReviewNum        │
   │                                │ 同步 article_info.review_num   │
   │                                │                                │
   │                                │                ◄───────────────┤
   │                                │                       ▲        │
   │                                │       GET /{sn}       │        │
   │                                │                                │
   │ ◄───────── ReviewService.listByArticle (status=1) ─────         │
   │   评论已可见                                                    │
```

### 异步入库（AMS 访问日志）

```
[请求拦截器]                                 [LogToDBThread]
LogInterceptor                              批量持久化（每 N 条 / N 秒）
  │ 记录 LogDO                                    ▲
  │ enqueue(API queue)                            │
  ▼                                               │
[LogToAPIThread]                                  │
  │ 调 ip-api.com 取地理信息                      │
  │ 失败则切 ipapi.co；都失败重试 ≤3 次            │
  ▼                                               │
enqueue(DB queue) ────────────────────────────────┘
                                      │
                                      ▼
                              MySQL log_info 表
```

主请求线程**不会**等待地理解析，访问体验始终流畅；异步线程统一由 `ThreadPoolExecutor + NamedThreadFactory` 管理，进程优雅关闭时通过 `DisposableBean` 等待任务完成。

### 一键换皮

```
              blog_info.theme_code  (单例表，跨进程持久化)
                       ▲
                       │  BMS 写
                       │
   GET /bms/theme/list.do  ◄── 4 张主题卡片，点【应用】写 theme_code
                       │
                       │ AMS 每次请求读取（GlobalModelAttributes）
                       ▼
   <html data-theme="${currentTheme}">  ──┐
                                          │
                                          ▼
   按 html[data-theme="xxx"] 属性选择器命中
   default 主题不出文件，默认即原版 NexT 风格
```

---

## 本地运行

### 前置依赖

| 工具 | 最低版本 | 推荐 |
|---|---|---|
| JDK | 1.8 | 1.8 / 11 |
| Maven | 3.6 | 3.8+ |
| MySQL | 5.7 | 8.0 |

### 1. 初始化数据库

```bash
# 一键建库 + 全部表 + 必要的种子数据（默认账号 root/123456，按需修改）
mysql -uroot -p123456 -h127.0.0.1 < data/schema.sql

# 可选：导入 25 篇演示文章 + 15 个种子标签，便于本地验证前台展示
mysql -uroot -p123456 -h127.0.0.1 -D blog < data/data.sql
```

> 数据源默认配置在 `mawuya-ams` / `mawuya-bms` 各自的 `src/main/resources/application.yml`：
> ```yaml
> spring.datasource.url: jdbc:mysql://127.0.0.1:3306/blog?...
> spring.datasource.username: root
> spring.datasource.password: 123456
> ```
> 自定义请同步修改两个文件。

### 2. 编译

```bash
# 在工程根目录
mvn -DskipTests clean package
```

构建产物：

```
mawuya-ams/target/mawuya-ams-1.0.0.jar
mawuya-bms/target/mawuya-bms-1.0.0.jar
```

### 3. 启动两个进程

```bash
# 终端 1：启动前台访客站
java -jar mawuya-ams/target/mawuya-ams-1.0.0.jar

# 终端 2：启动后台管理
java -jar mawuya-bms/target/mawuya-bms-1.0.0.jar
```

### 4. 访问

| 入口 | URL | 账号 |
|---|---|---|
| 前台访客站 | http://localhost:8080/ | — |
| 后台登录 | http://localhost:8081/bms/login.do | `admin` / `123456` |

> 默认密码 `123456` 仅用于本地验证，部署到任何非本机环境前**务必先重置**（BMS 用户管理 → 重置密码，或直接更新 `sys_user.password_hash`）。

### 5. （可选）IDE 启动

直接运行两个 `Application` 主类即可：
- `mawuya-ams/src/main/java/.../MawuyaAmsApplication.java`
- `mawuya-bms/src/main/java/.../MawuyaBmsApplication.java`

---

## 部署

### 单机部署（推荐起步）

1. **安装 MySQL 5.7+ / 8.x**，并执行 `data/schema.sql` 初始化库表
2. **打包**：`mvn -DskipTests clean package`
3. **拷贝产物到服务器**：
   ```
   /opt/mawuya/
   ├── mawuya-ams-1.0.0.jar
   ├── mawuya-bms-1.0.0.jar
   ├── application-ams.yml      （从 mawuya-ams/src/main/resources/ 拷贝并改）
   └── application-bms.yml
   ```
4. **后台守护进程**（systemd 或 supervisord 任选；下面给 systemd 示例）：

```ini
# /etc/systemd/system/mawuya-ams.service
[Unit]
Description=Mawuya AMS
After=network.target mysql.service

[Service]
Type=simple
User=mawuya
WorkingDirectory=/opt/mawuya
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar mawuya-ams-1.0.0.jar \
    --spring.config.location=file:/opt/mawuya/application-ams.yml
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

`mawuya-bms.service` 同理；启停：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now mawuya-ams mawuya-bms
sudo systemctl status mawuya-ams
```

### 反向代理（Nginx 示例）

把 AMS 暴露到公网 80/443，BMS 仅开放给内网：

```nginx
# 前台公网
server {
    listen 80;
    server_name www.example.com;
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

# 后台仅内网
server {
    listen 8082;
    allow 192.168.0.0/16;
    deny all;
    location / {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
    }
}
```

### 部署 Checklist

- [ ] **修改默认密码**：BMS 登录后立即重置 `admin` 账号
- [ ] **修改 JWT 密钥**：BMS 的 `mawuya.security.jwt.secret`
- [ ] **关闭 Druid 监控页或加白名单**：避免暴露慢 SQL 信息
- [ ] **检查日志目录权限**：保证应用用户可写
- [ ] **数据库备份策略**：至少 `mysqldump` 定时备份 `article_info / image_blob`

---

## 目录结构

```
mawuya/
├── pom.xml                          顶层 parent，统一管理依赖版本
├── data/                            数据库脚本（仅 2 个文件）
│   ├── schema.sql                   建库 + 全部表 + 必要的种子数据（admin 账号、默认分类）
│   └── data.sql                     可选：演示文章 25 篇 + 标签 15 个，便于本地验证
│
├── mawuya-common/                   公共工具模块（无 Spring 依赖）
│   └── src/main/java/.../common/utils/
│       ├── DateUtil.java            日期格式化（已切换 java.time）
│       ├── StrUtil.java             字符串工具
│       ├── OkHttpUtil.java          OkHttp 异步封装
│       ├── FileUtil.java            上传文件处理
│       ├── PageUtil.java            分页工具
│       └── CommonUtil.java          常量
│
├── mawuya-core/                     领域模型 + 持久化（被 ams/bms 共享）
│   └── src/main/
│       ├── java/.../core/
│       │   ├── dataobject/          数据对象 DO（与表一一对应）
│       │   │   ├── ArticleDO / CategoryDO / TagDO / ReviewDO
│       │   │   ├── ImageDO / LogDO / BlogDO
│       │   │   └── UserDO / RoleDO
│       │   ├── mapper/              MyBatis Mapper 接口（11 个）
│       │   ├── service/             业务服务层（10 个，方法名遵循阿里 get/list/count/save/update/remove）
│       │   ├── vo/                  分页/查询 VO（Page / LogInfoQuery）
│       │   ├── dto/                 跨端通用 DTO
│       │   ├── enums/               ResultCodeEnum（A/B/C 5 位错误码）
│       │   ├── exception/           BusinessException + GlobalExceptionHandler
│       │   ├── common/              BaseResponse<T> + Web 通用骨架
│       │   ├── cache/               DataCenter（异步队列）
│       │   ├── thread/              LogToAPIThread / LogToDBThread / ReadNumToDBThread
│       │   ├── interceptor/         LogInterceptor（访问日志记录）
│       │   ├── controller/          DbImageController（/image/db/{sn} 公共图片输出）
│       │   └── listener/            HTTP Session 监听
│       └── resources/mapper/        所有 *Mapper.xml（与 dataobject 包对齐）
│
├── mawuya-ams/                      前台访客站（端口 8080）
│   └── src/main/
│       ├── java/.../ams/
│       │   ├── MawuyaAmsApplication.java
│       │   ├── controller/          IndexController / ReviewController / SearchController / TagController / AboutController / SeoController
│       │   ├── dto/                 AMS 端响应 DTO
│       │   ├── seo/                 SEO 元信息组装
│       │   ├── thread/              InitializingServer（启动器，统一线程池管理）
│       │   └── config/              WebMvcConfig / GlobalModelAttributes（注入 currentTheme）
│       └── resources/
│           ├── application.yml      端口 8080，async.enabled=true
│           ├── templates/fts/       Thymeleaf 模板（NexT 主题）
│           │   ├── index.html
│           │   ├── show_article.html
│           │   ├── archive.html
│           │   ├── category.html / category_list.html
│           │   ├── tag.html / tag_list.html
│           │   ├── search.html / about.html
│           │   └── common/          css-head / footer / sidebar / page / seo-meta
│           └── static/statics/css/
│               ├── main5174.css     基础（NexT 风格，第三方）
│               ├── blog-extra.css   响应式增强 + 评论提示等（项目自有）
│               └── themes/          一键换皮主题文件（项目自有）
│                   ├── tech-dark.css
│                   ├── gradient-vivid.css
│                   └── minimal-business.css
│
└── mawuya-bms/                      后台管理（端口 8081）
    └── src/main/
        ├── java/.../bms/
        │   ├── MawuyaBmsApplication.java
        │   ├── controller/          18 个 controller（含 api/* 子目录的 REST API）
        │   │   ├── MainController          登录入口 + 主框架
        │   │   ├── DashboardController     数据看板
        │   │   ├── ArticleController       博客 CRUD
        │   │   ├── CategoryController      分类管理
        │   │   ├── ReviewController        评论审批
        │   │   ├── ImageController         图片库
        │   │   ├── ThemeController         一键换皮
        │   │   ├── LogController           访客日志查询
        │   │   ├── UserController          用户管理
        │   │   └── api/*ApiController      REST 数据接口
        │   ├── dto/                 BMS 端 DTO（23 个 request/response）
        │   ├── security/            SecurityConfig / JwtAuthFilter / JwtUtil / CustomUserDetailsService
        │   └── config/              WebMvcConfig
        └── resources/
            ├── application.yml      端口 8081，async.enabled=false
            └── templates/bms/
                ├── adminLogin.html
                ├── index.html       后台主框架
                ├── common/          css-head / head / left / js-footer
                ├── dashboard/       数据看板 fragment
                ├── article/         博客列表 + 写/改文章
                ├── category/        分类列表
                ├── image/           图片库 + 选择器
                ├── review/          评论审批
                ├── theme/           主题切换
                ├── user/            用户管理
                └── log/             访客日志
```

---

## 使用示例

### A. 写博客 + 配封面

1. 登录 BMS → 左侧【博客管理 → 博客列表】
2. 顶部“写博客”（青蓝渐变按钮）→ 填标题 / 分类 / 摘要 / 正文（Markdown）→【立即发布】
3. 在博客列表行尾点 ☁️“上传背景图”
4. 弹窗顶部【从图片库选择】→ 弹出图片选择器（支持搜索 / 分页 / 空态上传引导）
5. 双击图片或选中后点【选择此图片】→ 数据库 `picture_url` 自动更新

### B. 评论审批

```
访客在 AMS 文章页底部提交评论
   │ 表单 POST /comments/submit
   ▼
评论入库 status=0，提交成功后回到详情页，显示绿色提示
"评论已提交，待管理员审核通过后将公开展示"
   │
博主在 BMS【留言管理 → 留言审批】看到 hero 顶部「N 条待审核」徽标
   │
   ▼
点击【通过】→ POST /bms/review/approve.do
   │
AMS 下次访问立即可见；article_info.review_num 自动同步
```

### C. 一键换皮

1. BMS【博客管理 → 主题切换】→ 4 张主题卡片
2. 点击非当前主题的【应用此主题】→ layer 二次确认 → POST /bms/theme/apply.do
3. AMS 端无需重启；下次访问 `<html data-theme="tech-dark">` 即生效

### D. 数据看板

登录 BMS 默认进入首页（content0 tab）即看到：

```
┌───────────────────────────────────────────────────────┐
│  欢迎回来，钟启辉 👋    [健康度: 78 优秀]              │
│  当前共 18 篇文章；今日 414 次访问 · 5 个独立访客      │
│  [⚠ 3 条评论待审核 →]  [2 篇未分类 →]                 │
├───────────────────────────────────────────────────────┤
│  内容数据  │ 文章 18 │ 总字数 14.9k │ 分类 5 │ 标签 21 │
│  互动数据  │ 评论 20 │ 待审核 0    │ 赞 3488│ 人气..  │
│  流量数据  │ PV 414  │ UV 5         │ 累计 PV│ 累计 UV│
├───────────────────────────────────────────────────────┤
│  ╭─────────────  14 天 PV/UV 趋势 ─────────────╮      │
│  │  /\        /\           /\                  │      │
│  │ /  \  /\  /  \    /\   /  \    /\           │      │
│  ╰────────────────────────────────────────────╯      │
├───────────────────────────────────────────────────────┤
│  阅读 TOP 5 文章          │  今日 TOP 5 访客 IP        │
│  🥇 ...                    │  🥇 8.8.8.8 美国/Ashburn   │
└───────────────────────────────────────────────────────┘
```

### E. 访客日志查询

BMS【日志管理 → 访客记录】

- 顶部筛选条：IP / URL / 方法 / 状态 / 省份 / 时间范围（含“今日 / 近 7 天 / 近 30 天”快捷按钮）
- 表头点击排序（白名单字段：sn / ip_addr / req_method / resp_status / consume_time / req_time）
- 行尾【详情】弹窗看完整字段（IP/地理/UA/请求参数/异常信息等 15 项）

### F. 命令行查询数据接口

```bash
# 1. 登录拿 JWT token
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"123456"}' \
    http://localhost:8081/bms/api/auth/login | jq -r .data.token)

# 2. 数据看板原始 JSON（携带 token）
curl -s -H "Authorization: Bearer $TOKEN" \
    http://localhost:8081/bms/dashboard/data.do | jq

# 3. 评论审批
curl -s -H "Authorization: Bearer $TOKEN" \
    -X POST -d "sn=123" http://localhost:8081/bms/review/approve.do
# {"code":"00000","message":"操作成功","data":{"success":1}}

# 4. 切换主题
curl -s -H "Authorization: Bearer $TOKEN" \
    -X POST -d "code=tech-dark" http://localhost:8081/bms/theme/apply.do
```

---

## 开发规范

本项目全量遵循《阿里巴巴 Java 开发手册（嵩山版）》强制规约（PDF 见 `data/阿里巴巴Java开发规范（嵩山版）.pdf`）。

### 命名

- **DO（与表 1:1）**：`xxxDO`，放在 `core.dataobject` 包；类名采用业务化简，如 `ArticleDO`（表 `article_info`）、`UserDO`（表 `sys_user`）
- **DTO（接口出入参）**：`xxxDTO`，请求体加 `Request` 中缀（如 `LoginRequestDTO`），响应体加 `Response` 中缀
- **VO（视图 / 查询条件）**：`xxxVO` / `xxxQuery`
- **Service 方法前缀**：`get`（单个） / `list`（多个） / `count`（计数） / `save`（新增） / `update`（修改） / `remove`（删除） / `search`（搜索）
- **包名**：全部小写、单数（`controller / service / mapper / dataobject`，禁止 `controllers`）

### 错误码

`ResultCodeEnum` 采用阿里 5 位字符串体系：

| 段 | 含义 | 示例 |
|---|---|---|
| `00000` | 成功 | `SUCCESS` |
| `A0xxx` | 用户端错误 | `A0230` 未登录 / `A0400` 参数非法 / `A0411` 必填参数为空 |
| `B0xxx` | 系统执行错误 | `B0001` 系统执行失败 / `B0100` 系统资源不足 |
| `C0xxx` | 第三方服务错误 | `C0001` 第三方服务调用失败 |

### 强制项（CR 阻塞）

- **POJO 全部用包装类型**：`Integer / Long / Boolean`（禁止 `int / long / boolean` 字段）
- **POJO 必须有 toString**：但敏感字段（如 `passwordHash` / `data: byte[]`）不得输出
- **集合必须显式声明初始容量**：`new HashMap<>(N)` / `new ArrayList<>(N)`
- **catch 不得静默吞异常**：必须 `log.warn(..., e.getMessage())` 或抛出
- **禁止使用 `Executors.*` / 裸 `new Thread`**：统一用 `ThreadPoolExecutor + NamedThreadFactory + 拒绝策略 + DisposableBean`
- **禁用过时类**：`SimpleDateFormat / Calendar / java.sql.Date|Time|Timestamp / Apache BeanUtils`，统一 `java.time` API
- **禁止 `throw new RuntimeException`**：业务异常一律 `throw new BusinessException(ResultCodeEnum.XXX, ...)`
- **Mapper 不得 SELECT \***：必须显式列字段
- **魔法值必须常量化**：尤其状态码、阈值、长度限制

### 推荐实践

- **Mapper 层**：动态 SQL 用 `<where>` + `<if>`，所有过滤条件走 `#{}` 参数化绑定，**严禁字符串拼接**
- **排序字段**：必须在 service 层用白名单过滤后再传给 xml 的 `${}`（参考 `LogInfoService.sanitize`）
- **类型一致**：xml 内 `LIKE` 用 `CONCAT('%', #{xx}, '%')`，不要在 controller 拼通配符
- **响应式**：BMS 列表页统一栅格 `col-md-3 col-sm-6 col-xs-12` + `table-responsive` 横滚
- **Thymeleaf inline JS 陷阱**：`th:inline="javascript"` 模式下连**注释里**的 `[[ ]]` 也会被当成内联表达式；遇到 DataTables 的 `[[0,'asc']]` 配置务必用变量分两步赋值（参考 `review_list.html`）

### 来自 README 的硬性规则

1. **禁止使用 Lombok**：所有 entity / DTO / VO 必须手动实现 getter/setter（也是项目当前现状）
2. **JSON 库唯一**：项目内只使用 **Jackson**（Spring Boot 默认）；**禁止引入 fastjson** 或第二种 JSON 库

---

## 常见问题

### Q1：BMS 首页看板全是 0？
依赖加载顺序问题：dashboard fragment 嵌在 `left.html` 中，比 `js-footer` 中的 jQuery 更早渲染。当前实现已用 `bootDashboard()` 轮询启动器解决；若仍异常，强刷一次（Cmd+Shift+R）清缓存即可。

### Q2：访客日志地理信息是 NULL？
- 本地访问时 IP 是 `127.0.0.1`（保留地址），`ip-api.com` 会返回 `status:fail`，是预期行为
- 公网 IP 测试可用 `curl -H "X-Forwarded-For: 8.8.8.8" ...`
- 该接口免费版限速 45 次/分钟（按出口 IP）；超限会自动走兜底 `ipapi.co`

### Q3：评论提交后前台不显示？
评论默认进入“待审核”，需 BMS 审批通过。审批入口：左侧【留言管理 → 留言审批】。

### Q4：图片上传后再删除，引用是否会失效？
当前实现：BMS【图片库】删除按钮**不强制清理已被 picture_url 引用的图片**；建议先在文章列表把对应博客的封面替换后再删。

### Q5：能否单独运行某一端？
可以。两端共享 `blog` 数据库即可独立部署：BMS 仅做管理时关掉 AMS 不影响数据；AMS 可独立对外公网，BMS 走内网。

### Q6：怎么改默认登录密码？
两种方式：
- **推荐**：登录 BMS → 用户管理 → admin 行点【重置密码】
- **直改 DB**：用 BCrypt 算出新密码哈希（`new BCryptPasswordEncoder(10).encode("yourpwd")`），UPDATE `sys_user.password_hash`

### Q7：如何修改 JWT 密钥 / 过期时间？
改 `mawuya-bms/src/main/resources/application.yml` 中：
```yaml
mawuya:
  security:
    jwt:
      secret: <随机 ≥32 字符串>
      expires-seconds: 28800   # 默认 8 小时
```

---

## License

MIT © 2026 钟启辉
