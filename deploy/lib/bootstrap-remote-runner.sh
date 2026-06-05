#!/usr/bin/env bash
# ==============================================================================
# bootstrap-remote-runner.sh —— 在远端 Linux 上执行的依赖安装脚本
#
# 由 00-bootstrap-remote.sh 通过 SCP 上传到远端 /tmp 后由 root 执行。
# 也可以独立 scp 到任意 Linux 服务器后 sudo bash 跑。
#
# 支持发行版：
#   - CentOS 7 / 8 (含 EOL 仓库自动切换 vault.centos.org)
#   - Rocky Linux / AlmaLinux 8/9
#   - RHEL 7/8/9
#   - Ubuntu 18.04 / 20.04 / 22.04 / 24.04
#   - Debian 10 / 11 / 12
#
# 安装内容：
#   - 基础工具：curl wget tar unzip ca-certificates 时间同步
#   - Docker CE + docker-compose-plugin（v2）
#   - Docker 国内镜像加速（可关闭）
#   - 可选：firewalld/ufw 放行 8080/8081/3306
#   - 可选：JDK 1.8（默认不装，Docker 模式不需要）
#
# 退出码：
#   0 成功；非 0 失败（失败时打印诊断建议）
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

# ---------- 颜色与日志 ----------
if [ -t 1 ]; then
    R='\033[31m'; G='\033[32m'; Y='\033[33m'; B='\033[34m'; C='\033[36m'; N='\033[0m'; BD='\033[1m'
else
    R=''; G=''; Y=''; B=''; C=''; N=''; BD=''
fi
ts() { date '+%Y-%m-%d %H:%M:%S'; }
info()  { printf "${B}[INFO ]${N} %s %s\n"  "$(ts)" "$*"; }
ok()    { printf "${G}[ OK  ]${N} %s %s\n"  "$(ts)" "$*"; }
warn()  { printf "${Y}[WARN ]${N} %s %s\n"  "$(ts)" "$*" >&2; }
err()   { printf "${R}[ERROR]${N} %s %s\n"  "$(ts)" "$*" >&2; }
step()  { printf "\n${BD}${C}==> %s${N}\n" "$*"; }
die()   { err "$*"; exit 1; }

# ---------- 参数 ----------
INSTALL_DOCKER=1
INSTALL_JDK=0
ENABLE_MIRROR=1
OPEN_FIREWALL=1
AMS_PORT="${AMS_PORT:-8080}"
BMS_PORT="${BMS_PORT:-8081}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

while [ $# -gt 0 ]; do
    # 容错：上游若误传空字符串作为参数，直接跳过
    if [ -z "$1" ]; then shift; continue; fi
    case "$1" in
        --skip-docker)   INSTALL_DOCKER=0 ;;
        --with-jdk)      INSTALL_JDK=1 ;;
        --no-mirror)     ENABLE_MIRROR=0 ;;
        --no-firewall)   OPEN_FIREWALL=0 ;;
        --ams-port)      AMS_PORT="$2"; shift ;;
        --bms-port)      BMS_PORT="$2"; shift ;;
        --mysql-port)    MYSQL_PORT="$2"; shift ;;
        -h|--help)
            sed -n '2,30p' "$0"; exit 0 ;;
        *) die "未知参数：$1" ;;
    esac
    shift
done

# ---------- 必须 root ----------
if [ "$(id -u)" -ne 0 ]; then
    if command -v sudo >/dev/null 2>&1; then
        warn "当前非 root，自动用 sudo 重新执行..."
        exec sudo -E bash "$0" "$@"
    fi
    die "需要 root 权限运行（或安装 sudo 后重试）"
fi

# ---------- 识别发行版 ----------
detect_os() {
    if [ -r /etc/os-release ]; then
        . /etc/os-release
        OS_ID="${ID:-unknown}"
        OS_VER="${VERSION_ID:-unknown}"
        OS_PRETTY="${PRETTY_NAME:-$NAME $VERSION}"
    else
        die "无法识别发行版（缺少 /etc/os-release）"
    fi

    case "${OS_ID}" in
        centos|rhel|rocky|almalinux|ol)
            OS_FAMILY="rhel"
            if command -v dnf >/dev/null 2>&1; then PKG="dnf"; else PKG="yum"; fi
            ;;
        ubuntu|debian|raspbian)
            OS_FAMILY="debian"
            PKG="apt-get"
            export DEBIAN_FRONTEND=noninteractive
            ;;
        *)
            die "暂不支持的发行版：${OS_PRETTY}（仅支持 CentOS/RHEL/Rocky/Alma/Ubuntu/Debian）"
            ;;
    esac

    # 主版本号（取整数）
    OS_VER_MAJOR="$(echo "${OS_VER}" | awk -F. '{print $1}')"
    ARCH="$(uname -m)"

    info "OS    : ${OS_PRETTY}"
    info "Family: ${OS_FAMILY}  (pkg=${PKG})"
    info "Arch  : ${ARCH}"
}

# ---------- CentOS 7/8 EOL 仓库修复 ----------
fix_centos_eol() {
    [ "${OS_FAMILY}" = "rhel" ] || return 0
    [ "${OS_ID}" = "centos" ]   || return 0

    # CentOS 8 已 EOL，官方源 410 Gone；CentOS 7 2024-06-30 EOL
    if [ "${OS_VER_MAJOR}" = "8" ] || [ "${OS_VER_MAJOR}" = "7" ]; then
        # 探测是否还能解析官方源；不能则切到 vault
        if ! curl -fsSL --max-time 5 -o /dev/null \
            "http://mirror.centos.org/centos/${OS_VER_MAJOR}/os/${ARCH}/repodata/repomd.xml" 2>/dev/null; then
            warn "检测到 CentOS ${OS_VER_MAJOR} 官方源不可用，切换到 vault.centos.org"
            sed -i.bak \
                -e 's|^mirrorlist=|#mirrorlist=|g' \
                -e 's|^#baseurl=http://mirror.centos.org|baseurl=https://vault.centos.org|g' \
                -e 's|^baseurl=http://mirror.centos.org|baseurl=https://vault.centos.org|g' \
                /etc/yum.repos.d/CentOS-*.repo 2>/dev/null || true
            ${PKG} clean all >/dev/null 2>&1 || true
            ${PKG} makecache >/dev/null 2>&1 || true
            ok "已切换到 vault.centos.org"
        fi
    fi
}

# ---------- 基础工具 ----------
install_base_tools() {
    step "安装基础工具"
    if [ "${OS_FAMILY}" = "rhel" ]; then
        ${PKG} install -y curl wget tar unzip ca-certificates which net-tools \
                          iproute yum-utils device-mapper-persistent-data lvm2 \
            || die "基础工具安装失败"
    else
        ${PKG} update -y
        ${PKG} install -y curl wget tar unzip ca-certificates apt-transport-https \
                          gnupg lsb-release iproute2 net-tools \
            || die "基础工具安装失败"
    fi
    ok "基础工具就绪"
}

# ---------- 时间同步 ----------
sync_time() {
    if command -v timedatectl >/dev/null 2>&1; then
        timedatectl set-timezone Asia/Shanghai 2>/dev/null || true
        timedatectl set-ntp true 2>/dev/null || true
    fi
    if command -v chronyd >/dev/null 2>&1; then
        systemctl enable --now chronyd 2>/dev/null || true
    fi
    ok "时区/时间同步：$(date '+%F %T %Z')"
}

# ---------- Docker 安装 ----------
install_docker_rhel() {
    # 卸载老版本（如果存在）
    ${PKG} remove -y docker docker-client docker-client-latest docker-common \
                     docker-latest docker-latest-logrotate docker-logrotate \
                     docker-engine podman runc 2>/dev/null || true

    # 加入 docker-ce repo（用阿里云镜像加速国内访问）
    local repo_url
    if [ "${ENABLE_MIRROR}" = "1" ]; then
        repo_url="https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo"
    else
        repo_url="https://download.docker.com/linux/centos/docker-ce.repo"
    fi
    yum-config-manager --add-repo "${repo_url}" \
        || die "添加 docker-ce repo 失败：${repo_url}"

    # CentOS 8 需要 --allowerasing 解决 podman/runc 冲突
    local extra_args=""
    [ "${OS_VER_MAJOR}" = "8" ] && extra_args="--allowerasing"

    ${PKG} install -y ${extra_args} docker-ce docker-ce-cli containerd.io \
                                    docker-buildx-plugin docker-compose-plugin \
        || die "Docker 安装失败"
}

install_docker_debian() {
    ${PKG} remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true

    install -m 0755 -d /etc/apt/keyrings

    local docker_url
    if [ "${ENABLE_MIRROR}" = "1" ]; then
        docker_url="https://mirrors.aliyun.com/docker-ce/linux/${OS_ID}"
    else
        docker_url="https://download.docker.com/linux/${OS_ID}"
    fi

    curl -fsSL "${docker_url}/gpg" | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
        || die "下载 Docker GPG key 失败"
    chmod a+r /etc/apt/keyrings/docker.gpg

    local codename
    codename="$(. /etc/os-release && echo "${VERSION_CODENAME}")"
    [ -z "${codename}" ] && codename="$(lsb_release -cs 2>/dev/null || echo bookworm)"

    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] ${docker_url} ${codename} stable" \
        > /etc/apt/sources.list.d/docker.list

    ${PKG} update -y
    ${PKG} install -y docker-ce docker-ce-cli containerd.io \
                      docker-buildx-plugin docker-compose-plugin \
        || die "Docker 安装失败"
}

config_docker_daemon() {
    mkdir -p /etc/docker

    # 自动探测公网出口，腾讯云机器优先用内网镜像（仅腾讯云内网可达，速度可达 50MB/s+）
    local tencent_mirror=""
    if curl -fsSL --max-time 3 -o /dev/null https://mirror.ccs.tencentyun.com/v2/ 2>/dev/null; then
        tencent_mirror='        "https://mirror.ccs.tencentyun.com",'
        info "检测到腾讯云内网，将优先使用 mirror.ccs.tencentyun.com"
    fi

    local mirrors=""
    if [ "${ENABLE_MIRROR}" = "1" ]; then
        # 2025-2026 实测公开可用且稳定的镜像列表（多源容错；任一可用即可加速）
        mirrors='
    "registry-mirrors": [
'"${tencent_mirror}"'
        "https://docker.1ms.run",
        "https://docker.xuanyuan.me",
        "https://dockerpull.org",
        "https://docker.1panel.live",
        "https://hub.rat.dev",
        "https://docker.m.daocloud.io"
    ],'
    fi
    cat > /etc/docker/daemon.json <<EOF
{${mirrors}
    "log-driver": "json-file",
    "log-opts": {
        "max-size": "100m",
        "max-file": "3"
    },
    "storage-driver": "overlay2",
    "live-restore": true,
    "max-concurrent-downloads": 10,
    "max-concurrent-uploads": 5,
    "features": {
        "buildkit": true
    }
}
EOF
    # 校验 JSON 合法性（避免一个逗号导致 docker 起不来）
    if command -v python3 >/dev/null 2>&1; then
        python3 -c 'import json,sys; json.load(open("/etc/docker/daemon.json"))' \
            || die "/etc/docker/daemon.json 语法错误，请检查"
    elif command -v python >/dev/null 2>&1; then
        python -c 'import json,sys; json.load(open("/etc/docker/daemon.json"))' \
            || die "/etc/docker/daemon.json 语法错误，请检查"
    fi
    info "已写入 /etc/docker/daemon.json"
}

start_docker() {
    systemctl daemon-reload
    systemctl enable docker
    if ! systemctl restart docker; then
        err "Docker 启动失败，最近 30 行日志："
        journalctl -u docker --no-pager -n 30 || true
        die "请按上方日志排查（常见：daemon.json 语法错误、磁盘满、SELinux 拦截）"
    fi
    sleep 2
    docker info >/dev/null 2>&1 || die "docker info 异常，请检查 systemctl status docker"
}

verify_docker() {
    info "Docker  版本：$(docker --version)"
    info "Compose 版本：$(docker compose version | head -n1)"
    # 实测拉取 hello-world 验证（失败仅警告，不阻断）
    if docker run --rm hello-world >/dev/null 2>&1; then
        ok "Docker hello-world 测试通过"
    else
        warn "hello-world 拉取失败（可能镜像加速尚未生效或网络受限），不阻断后续部署"
    fi
}

install_docker() {
    [ "${INSTALL_DOCKER}" = "1" ] || { warn "跳过 Docker 安装（--skip-docker）"; return 0; }
    step "安装 Docker CE + docker compose v2"
    if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
        ok "Docker 与 compose 已存在，跳过安装"
        config_docker_daemon
        start_docker
        verify_docker
        return 0
    fi
    if [ "${OS_FAMILY}" = "rhel" ]; then
        install_docker_rhel
    else
        install_docker_debian
    fi
    config_docker_daemon
    start_docker
    verify_docker
    ok "Docker 安装完成"
}

# ---------- 防火墙放行 ----------
open_firewall() {
    [ "${OPEN_FIREWALL}" = "1" ] || { warn "跳过防火墙放行（--no-firewall）"; return 0; }
    step "防火墙放行 ${AMS_PORT} ${BMS_PORT} ${MYSQL_PORT}"

    if systemctl is-active --quiet firewalld 2>/dev/null; then
        for p in "${AMS_PORT}" "${BMS_PORT}" "${MYSQL_PORT}"; do
            firewall-cmd --permanent --add-port="${p}/tcp" >/dev/null 2>&1 || true
        done
        firewall-cmd --reload >/dev/null 2>&1 || true
        ok "firewalld 已放行（永久）"
    elif command -v ufw >/dev/null 2>&1 && ufw status 2>/dev/null | grep -q "Status: active"; then
        for p in "${AMS_PORT}" "${BMS_PORT}" "${MYSQL_PORT}"; do
            ufw allow "${p}/tcp" >/dev/null 2>&1 || true
        done
        ok "ufw 已放行"
    else
        info "未检测到激活的 firewalld/ufw，跳过（云服务器请到控制台安全组放行）"
    fi
}

# ---------- 可选：JDK ----------
install_jdk() {
    [ "${INSTALL_JDK}" = "1" ] || return 0
    step "安装 OpenJDK 1.8（可选）"
    if command -v java >/dev/null 2>&1; then
        ok "Java 已存在：$(java -version 2>&1 | head -n1)"
        return 0
    fi
    if [ "${OS_FAMILY}" = "rhel" ]; then
        ${PKG} install -y java-1.8.0-openjdk-headless || warn "JDK 安装失败"
    else
        ${PKG} install -y openjdk-8-jre-headless 2>/dev/null \
            || ${PKG} install -y default-jre-headless || warn "JDK 安装失败"
    fi
    java -version 2>&1 | head -n1 || true
}

# ---------- 部署目录 ----------
prepare_dirs() {
    step "准备部署目录"
    mkdir -p /usr/local/services
    chmod 755 /usr/local/services
    ok "目录就绪：/usr/local/services"
}

# ---------- 端口体检 ----------
check_ports() {
    step "端口占用检查（仅提示，不阻断）"
    for p in "${AMS_PORT}" "${BMS_PORT}" "${MYSQL_PORT}"; do
        if ss -ltn 2>/dev/null | awk -v p=":${p}" '$4 ~ p {found=1} END{exit !found}'; then
            warn "端口 ${p} 已被占用：$(ss -ltnp 2>/dev/null | awk -v p=":${p}" '$4 ~ p')"
        else
            ok "端口 ${p} 空闲"
        fi
    done
}

# ---------- 报告 ----------
print_summary() {
    step "完成"
    cat <<EOF

  发行版    : ${OS_PRETTY}
  Docker    : $(docker --version 2>/dev/null || echo 未安装)
  Compose   : $(docker compose version 2>/dev/null | head -n1 || echo 未安装)
  部署目录  : /usr/local/services
  放行端口  : ${AMS_PORT} ${BMS_PORT} ${MYSQL_PORT}
  日志查看  : journalctl -u docker -f

下一步：在编译机执行
  bash deploy/deploy.sh release
EOF
}

# ---------- 主流程 ----------
main() {
    step "Mawuya 远端环境一键安装"
    detect_os
    fix_centos_eol
    install_base_tools
    sync_time
    install_jdk
    install_docker
    open_firewall
    prepare_dirs
    check_ports
    print_summary
}

main "$@"
