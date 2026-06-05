# Mawuya 部署脚本集合

一套面向 **AMS + BMS + MySQL** 三件套的 Shell 自动化部署工具，覆盖 *环境检查 → 编译打包 → 上传 → Docker 一键部署* 全流程，并自带健康检查与失败回滚。

## 目录

```
deploy/
├── deploy.sh                  # 一键入口（推荐使用）
├── 00-bootstrap-remote.sh     # ⓪ 远端 Linux 依赖一键安装（Docker/compose/工具）
├── 01-precheck.sh             # ① 本地+远端环境检查
├── 02-build-and-upload.sh     # ② Maven 打包 + SCP 上传
├── 03-docker-deploy.sh        # ③ 远端 Docker 部署 / 维护
├── lib/
│   ├── common.sh                       # 公共函数（颜色/日志/SSH/SCP）
│   └── bootstrap-remote-runner.sh      # 在远端执行的安装脚本（被 ⓪ 推送）
├── conf/
│   └── deploy.env.example     # 环境变量示例（复制为 deploy.env）
├── dist/                      # 构建产物 mawuya-bundle-*.tar.gz
└── logs/                      # 检查报告与执行日志
```

> 远端服务器目录约定（题目要求）：`/usr/local/services/`
>
> ```
> /usr/local/services/
> ├── mawuya-bundle-<ts>.tar.gz       # SCP 上传到此
> └── mawuya/
>     ├── current -> releases/<ts>    # 软链接到当前版本（便于回滚）
>     ├── releases/
>     │   ├── 20260605_173300/
>     │   └── 20260605_180120/
>     ├── docker/                     # Dockerfile.ams / Dockerfile.bms / jar
>     ├── initdb/                     # MySQL 容器首次启动自动执行
>     ├── docker-compose.yml          # 自动生成
>     └── .last_good_release          # 上一次成功版本路径（回滚用）
> ```

---

## 一、快速开始

```bash
# 1) 复制配置（可选；密码不要写进去）
cp deploy/conf/deploy.env.example deploy/conf/deploy.env
chmod 600 deploy/conf/deploy.env

# 2) 首次：远端一键装 Docker（可省略已装好的服务器）
bash deploy/deploy.sh bootstrap

# 3) 全流程：本地检查 → 打包 → 上传 → 远端 Docker 部署
bash deploy/deploy.sh release
```

按提示输入：远程 IP、SSH 端口、用户名、密码（或选择密钥免密）。

成功后访问：

| 入口 | URL | 默认账号 |
|---|---|---|
| AMS 访客前台 | `http://<远端IP>:8080/` | — |
| BMS 后台管理 | `http://<远端IP>:8081/bms/login.do` | `admin` / `123456` ← **务必立刻修改** |

---

## 二、单步使用

### 0. 远端依赖一键安装 `00-bootstrap-remote.sh`

如果远端是**全新的机器**，直接跑这一步即可装好 Docker。脚本会把
`lib/bootstrap-remote-runner.sh` SCP 到远端 `/tmp` 后用 root（自动 sudo 提权）执行。

| 支持的发行版 | 备注 |
|---|---|
| CentOS 7 / 8 | 8 已 EOL，脚本自动切到 `vault.centos.org` |
| Rocky / AlmaLinux 8/9 | dnf |
| RHEL 7/8/9 | dnf/yum |
| Ubuntu 18.04 / 20.04 / 22.04 / 24.04 | apt |
| Debian 10 / 11 / 12 | apt |

安装内容：
- 基础工具：`curl wget tar unzip ca-certificates iproute net-tools` …
- **Docker CE + docker-compose-plugin（v2）**（默认走阿里云镜像源更稳）
- `/etc/docker/daemon.json`：国内 `registry-mirrors` + 日志大小限制 + `overlay2`
- 时区 `Asia/Shanghai` + NTP 同步
- `firewalld` / `ufw` 自动放行 `8080 / 8081 / 3306`（云服务器另需控制台安全组）
- 创建 `/usr/local/services` 部署目录
- 自动 `docker run --rm hello-world` 验证

```bash
# 默认（推荐）：装 Docker + 镜像加速 + 防火墙放行
bash deploy/deploy.sh bootstrap

# 选项
bash deploy/00-bootstrap-remote.sh --no-mirror      # 不使用国内镜像（海外服务器用）
bash deploy/00-bootstrap-remote.sh --no-firewall    # 不动 firewalld/ufw
bash deploy/00-bootstrap-remote.sh --skip-docker    # 仅装基础工具（已装 Docker 时）
bash deploy/00-bootstrap-remote.sh --with-jdk       # 同时装 OpenJDK 1.8（Docker 模式不需要）
```

> 也可以**纯离线**：把 `deploy/lib/bootstrap-remote-runner.sh` 单独 `scp` 到任意 Linux
> 服务器，`sudo bash bootstrap-remote-runner.sh` 即可，不依赖本机驱动。

### 1. 环境检查 `01-precheck.sh`

| 检查项 | 本地 | 远端 |
|---|---|---|
| OS / 内核 / 资源 |  | ✓ |
| Java、Maven、ssh/scp、Maven 中央仓库可达 | ✓ |  |
| Docker / docker compose 可用 |  | ✓ |
| Docker Hub 可达 |  | ✓ |
| 端口占用（8080 / 8081 / 3306） |  | ✓ |
| `/usr/local/services` 可写 |  | ✓ |

```bash
bash deploy/01-precheck.sh                # 本地+远端
bash deploy/01-precheck.sh --local-only   # 仅本地
bash deploy/01-precheck.sh --remote-only  # 仅远端
```

不通过项会以 `[ERROR]` 红字列出，并附带修复建议；脚本以 `exit 1` 中断。
完整报告写入 `deploy/logs/precheck-<ts>.log`。

### 2. 编译打包 + 上传 `02-build-and-upload.sh`

```bash
bash deploy/02-build-and-upload.sh                  # 全流程
bash deploy/02-build-and-upload.sh --skip-build     # 复用最近的 dist bundle
bash deploy/02-build-and-upload.sh --skip-upload    # 只打包不上传
```

流程：

1. `mvn -DskipTests clean package`
2. 打成 `deploy/dist/mawuya-bundle-<ts>.tar.gz`，包含：
   - `jars/mawuya-ams-1.0.0.jar`、`jars/mawuya-bms-1.0.0.jar`
   - `sql/schema.sql`、`sql/data.sql`
   - `scripts/03-docker-deploy.sh` + `scripts/lib/common.sh`
   - `MANIFEST`（含 SHA256）
3. 交互输入远端 SSH 信息（IP/端口/用户名/密码 或 密钥免密）
4. **SSH 连通性测试**（先 `echo __SSH_OK__`）
5. **SCP 上传到 `/usr/local/services/`**（rsync 优先以获得进度，否则 `scp`）
6. 远端 `tar -xzf` 解到 `releases/<ts>` 并切换 `current` 软链接

> 密码使用 `read -s` 不回显；不会写入 `deploy.env`，仅环境变量内存中传递。

### 3. Docker 一键部署 `03-docker-deploy.sh`

```bash
# 在本机驱动远端（自动 ssh 进去执行）
bash deploy/03-docker-deploy.sh up
bash deploy/03-docker-deploy.sh status
bash deploy/03-docker-deploy.sh logs
bash deploy/03-docker-deploy.sh rollback
bash deploy/03-docker-deploy.sh down

# 也可以直接在远端 /usr/local/services/mawuya/current 执行
ssh user@host
cd /usr/local/services/mawuya/current
bash scripts/03-docker-deploy.sh up
```

`up` 阶段自动产物：
- `docker/Dockerfile.ams`、`docker/Dockerfile.bms`（基于 `eclipse-temurin:8-jre-jammy`，含 HEALTHCHECK）
- `docker-compose.yml`（mysql 5.7 / mawuya-ams / mawuya-bms 三服务，使用 `mawuya-net` 网桥，命名卷 `mawuya-mysql-data`）
- `initdb/01-schema.sql`、`02-data.sql`（MySQL 容器首次启动自动导入）

健康检查：
- 容器级：每 15s 探测一次容器内 HTTP
- 脚本级：宿主机 `curl http://127.0.0.1:8080/` 与 `:8081/bms/login.do`，最多等 180s
- 超时 → 输出最近 200 行容器日志 → **回滚** 到 `.last_good_release` 软链接的版本并重启

---

## 三、依赖

### 编译机（执行脚本的机器）
- bash 4+、JDK 1.8+、Maven 3.6+、ssh / scp
- 使用密码登录时：`sshpass`
  - macOS: `brew install hudochenkov/sshpass/sshpass`
  - Ubuntu: `sudo apt-get install -y sshpass`
  - CentOS: `sudo yum install -y sshpass`
- 推荐：`rsync`（带进度），`curl`

### 远端服务器
- Linux x86_64（CentOS 7+/Ubuntu 18+）
- `docker` + `docker compose v2`（脚本会检测，不达标会提示安装命令）
- 内存 ≥ 2GB、磁盘可用 ≥ 5GB

---

## 四、安全约定

- **密码不落盘**：`deploy.env` 仅写 IP/端口/用户名/部署目录，密码通过 `read -s` 即时传递
- **0600 权限**：自动写入的 `deploy.env` 强制 `chmod 600`
- **SSH 严格设置**：`ConnectTimeout=10`、`ServerAliveInterval=30`，禁用 `known_hosts` 写入避免污染（仅出于自动化便利；生产环境建议保留并使用密钥）
- **MySQL 默认账号**：`MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` 一定要在 `deploy.env` 改成强密码，**严禁继续使用示例值上线**
- **BMS 默认密码**：`admin / 123456` 仅本地验证用，部署后**立刻**通过 BMS 用户管理重置
- **JWT 密钥**：上线前修改 `mawuya.security.jwt.secret`（在 BMS 的 `application.yml`，可通过 `SPRING_APPLICATION_JSON` 容器环境变量覆盖）
- **端口暴露**：脚本默认把 8080/8081/3306 都映射到宿主，若仅供内网请改 `docker-compose.yml` 的 ports 段或加防火墙

---

## 五、故障排查

| 现象 | 排查命令 |
|---|---|
| 容器起不来 | `docker compose -f /usr/local/services/mawuya/docker-compose.yml logs --tail=200` |
| 端口被占 | `ss -ltnp \| grep -E ':(8080\|8081\|3306)'` |
| MySQL 数据想重置 | `docker compose down && docker volume rm mawuya_mawuya-mysql-data` |
| 想回到上一版 | `bash deploy/03-docker-deploy.sh rollback` |
| AMS/BMS 报数据库连接失败 | 检查 compose 中 `SPRING_DATASOURCE_URL` 与 `mysql` 服务名是否一致 |
| 远端 Docker Hub 拉不下来 | 给远端 `/etc/docker/daemon.json` 加 `registry-mirrors` 后 `systemctl restart docker` |

---

## 六、典型使用流程图

```
本地编译机                                 远端服务器(/usr/local/services/)
─────────────                              ────────────────────────────────
deploy.sh release
   │
   ├─ 01-precheck.sh ──── ssh ────►  检查 OS/Docker/端口/磁盘
   │                                       │
   │                                  PASS / FAIL
   │
   ├─ 02-build-and-upload.sh
   │     │ mvn clean package
   │     │ tar -czf bundle
   │     │ ssh test
   │     └─ scp bundle ──────────►  /usr/local/services/mawuya-bundle-*.tar.gz
   │                                       │
   │                                       └─► 解包到 mawuya/releases/<ts>
   │                                            current → releases/<ts>
   │
   └─ 03-docker-deploy.sh up
         │ ssh into remote
         └──────────────────────────►  生成 Dockerfile + compose
                                       docker compose up -d --build
                                       healthcheck (180s)
                                         ├─ OK   → 写 .last_good_release
                                         └─ FAIL → 回滚到 .last_good_release
```
