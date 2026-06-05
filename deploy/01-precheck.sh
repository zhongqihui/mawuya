#!/usr/bin/env bash
# ==============================================================================
# 01-precheck.sh —— Linux 环境准备与检查
#
# 功能：
#   1. 本地环境检查：Java/Maven/Docker/sshpass 等命令是否就绪（编译机）
#   2. 远端服务器检查（可选）：
#        - 系统版本（/etc/os-release）、内核
#        - 网络连通性（DNS / 公网）
#        - CPU/内存/磁盘资源
#        - 必需依赖：Docker / docker compose / curl / java(可选) / mysql client(可选)
#        - 端口占用：AMS_PORT / BMS_PORT / MYSQL_PORT
#   3. 输出彩色检查报告，写入 deploy/logs/precheck-<host>-<ts>.log
#   4. 任一关键项不满足 → 中断并提示修复建议（exit 1）
#
# 用法：
#   bash 01-precheck.sh [--local-only] [--remote-only]
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

MODE="all"
for arg in "$@"; do
    case "$arg" in
        --local-only)  MODE="local"  ;;
        --remote-only) MODE="remote" ;;
        -h|--help)
            sed -n '2,20p' "$0"; exit 0 ;;
        *) die "未知参数：$arg" ;;
    esac
done

REPORT_TS="$(date '+%Y%m%d_%H%M%S')"
REPORT_FILE="${DEPLOY_LOG_DIR}/precheck-${REPORT_TS}.log"
exec > >(tee -a "${REPORT_FILE}") 2>&1

# 用于聚合结果
TOTAL=0
PASSED=0
WARNINGS=0
FAILED=0
declare -a FAIL_HINTS=()

check_pass() { TOTAL=$((TOTAL+1)); PASSED=$((PASSED+1));   log_ok   "$*"; }
check_warn() { TOTAL=$((TOTAL+1)); WARNINGS=$((WARNINGS+1)); log_warn "$*"; }
check_fail() {
    TOTAL=$((TOTAL+1)); FAILED=$((FAILED+1))
    log_error "$1"
    [[ -n "${2:-}" ]] && FAIL_HINTS+=("$2")
}

# ============== 本地检查 ==============
local_checks() {
    log_step "本地编译机环境检查"

    # 操作系统
    log_info "OS: $(uname -srm)"

    # Java
    if have_cmd java; then
        local jv
        jv="$(java -version 2>&1 | head -n1)"
        if echo "$jv" | grep -Eq '"(1\.8|9|10|11|12|13|14|15|16|17|18|19|20|21)'; then
            check_pass "Java 已安装：${jv}"
        else
            check_warn "Java 版本可能不兼容：${jv}（推荐 1.8 / 11）"
        fi
    else
        check_fail "未安装 Java" "请安装 JDK 1.8+：https://adoptium.net/"
    fi

    # Maven
    if have_cmd mvn; then
        check_pass "Maven 已安装：$(mvn -v | head -n1)"
    else
        check_fail "未安装 Maven" "请安装 Maven 3.6+：https://maven.apache.org/"
    fi

    # ssh / scp
    if have_cmd ssh && have_cmd scp; then
        check_pass "ssh / scp 可用"
    else
        check_fail "缺少 ssh / scp" "请安装 openssh-client"
    fi

    # 项目根目录与 pom.xml
    if [[ -f "${PROJECT_ROOT}/pom.xml" ]]; then
        check_pass "Maven 工程已识别：${PROJECT_ROOT}/pom.xml"
    else
        check_fail "未找到 ${PROJECT_ROOT}/pom.xml" "请把 deploy/ 目录放在 mawuya 工程根下"
    fi

    # 网络
    if curl -fsSL --max-time 5 -o /dev/null https://repo.maven.apache.org/maven2/ ; then
        check_pass "Maven 中央仓库可达"
    else
        check_warn "Maven 中央仓库不可达，编译可能失败（可换 settings.xml 镜像）"
    fi
}

# ============== 远端检查 ==============
remote_checks() {
    log_step "远端 Linux 服务器检查"

    if [[ -z "${SSH_HOST:-}" ]]; then
        prompt_ssh_info
    fi

    test_ssh || die "SSH 连通性检查失败，已中断"

    # 把检查脚本一次性发到远端执行，避免多次握手
    local remote_script
    remote_script=$(cat <<'REMOTE'
set -u
ok()    { echo "PASS|$*"; }
warn()  { echo "WARN|$*"; }
fail()  { echo "FAIL|$*"; }

# 1. OS
if [ -r /etc/os-release ]; then
    . /etc/os-release
    ok "OS: ${PRETTY_NAME:-$NAME $VERSION} | Kernel: $(uname -r)"
else
    warn "无 /etc/os-release，未识别发行版；Kernel: $(uname -r)"
fi

# 2. 资源
mem_total_mb=$(awk '/MemTotal/ {print int($2/1024)}' /proc/meminfo 2>/dev/null || echo 0)
if [ "${mem_total_mb}" -ge 1500 ]; then
    ok "内存：${mem_total_mb} MB"
else
    warn "内存仅 ${mem_total_mb} MB（建议 ≥2GB，AMS+BMS+MySQL 容器）"
fi

cpu_cores=$(grep -c ^processor /proc/cpuinfo 2>/dev/null || echo 1)
ok "CPU 核心：${cpu_cores}"

disk_free=$(df -BG /usr/local 2>/dev/null | awk 'NR==2 {print $4}' | tr -d 'G')
disk_free=${disk_free:-0}
if [ "${disk_free}" -ge 5 ]; then
    ok "磁盘 /usr/local 可用：${disk_free} GB"
else
    warn "磁盘 /usr/local 可用仅 ${disk_free} GB（建议 ≥5GB）"
fi

# 3. 网络
if command -v curl >/dev/null 2>&1; then
    if curl -fsSL --max-time 5 -o /dev/null https://registry-1.docker.io/v2/; then
        ok "Docker Hub 可达"
    else
        warn "Docker Hub 不可达，镜像拉取可能失败（可配置 daemon.json registry-mirrors）"
    fi
else
    warn "未安装 curl，已跳过网络检查"
fi

# 4. 必需依赖
if command -v docker >/dev/null 2>&1; then
    dv=$(docker --version 2>/dev/null || echo unknown)
    if docker info >/dev/null 2>&1; then
        ok "Docker：${dv}（守护进程运行中）"
    else
        fail "Docker 已安装但守护进程不可用：${dv} | 请执行：systemctl start docker"
    fi
else
    fail "未安装 Docker | 安装：curl -fsSL https://get.docker.com | sh && systemctl enable --now docker"
fi

if docker compose version >/dev/null 2>&1; then
    ok "docker compose（v2 插件）：$(docker compose version | head -n1)"
elif command -v docker-compose >/dev/null 2>&1; then
    ok "docker-compose（v1）：$(docker-compose --version)"
else
    fail "未安装 docker compose | yum install -y docker-compose-plugin 或 apt install -y docker-compose-plugin"
fi

# 5. 端口占用（AMS/BMS/MySQL）
check_port() {
    local p="$1" name="$2"
    if command -v ss >/dev/null 2>&1; then
        line=$(ss -ltnp 2>/dev/null | awk -v p=":$p" '$4 ~ p {print; exit}')
    else
        line=$(netstat -ltnp 2>/dev/null | awk -v p=":$p" '$4 ~ p {print; exit}')
    fi
    if [ -n "${line:-}" ]; then
        fail "端口 ${p}（${name}）已被占用：${line} | 请释放或修改部署端口"
    else
        ok "端口 ${p}（${name}）空闲"
    fi
}
check_port "__AMS_PORT__"   "AMS"
check_port "__BMS_PORT__"   "BMS"
check_port "__MYSQL_PORT__" "MySQL"

# 6. 部署目录可写
mkdir -p "__REMOTE_DEPLOY_DIR__" 2>/dev/null && \
    [ -w "__REMOTE_DEPLOY_DIR__" ] \
    && ok "部署目录可写：__REMOTE_DEPLOY_DIR__" \
    || fail "部署目录不可写：__REMOTE_DEPLOY_DIR__ | 请确认当前用户对 /usr/local/services 有写权限或使用 sudo"
REMOTE
)
    # 模板替换
    remote_script="${remote_script//__AMS_PORT__/${AMS_PORT}}"
    remote_script="${remote_script//__BMS_PORT__/${BMS_PORT}}"
    remote_script="${remote_script//__MYSQL_PORT__/${MYSQL_PORT}}"
    remote_script="${remote_script//__REMOTE_DEPLOY_DIR__/${REMOTE_DEPLOY_DIR}}"

    local out
    out=$(remote_exec "${remote_script}") || die "远端检查脚本执行失败"

    # 解析结果
    while IFS= read -r line; do
        case "${line%%|*}" in
            PASS) check_pass "${line#PASS|}" ;;
            WARN) check_warn "${line#WARN|}" ;;
            FAIL)
                # FAIL|消息 | 提示
                msg="${line#FAIL|}"
                hint=""
                if [[ "${msg}" == *"|"* ]]; then
                    hint="${msg#*|}"
                    msg="${msg%%|*}"
                fi
                check_fail "${msg}" "${hint}"
                ;;
            *) echo "${line}" ;;
        esac
    done <<< "${out}"
}

# ============== 报告 ==============
print_report() {
    log_step "检查报告汇总"
    printf "总计：%d  通过：%b%d%b  警告：%b%d%b  失败：%b%d%b\n" \
        "${TOTAL}" \
        "${C_GREEN}" "${PASSED}"   "${C_RESET}" \
        "${C_YELLOW}" "${WARNINGS}" "${C_RESET}" \
        "${C_RED}"   "${FAILED}"   "${C_RESET}"
    log_info "完整报告：${REPORT_FILE}"

    if (( FAILED > 0 )); then
        log_error "存在关键项未通过，请按下列建议修复后重试："
        local i=1
        for h in "${FAIL_HINTS[@]}"; do
            printf "  %d) %s\n" "$i" "$h"
            i=$((i+1))
        done
        exit 1
    fi
    log_ok "环境检查通过，可继续后续部署步骤"
}

case "${MODE}" in
    local)  local_checks ;;
    remote) remote_checks ;;
    all)    local_checks; remote_checks ;;
esac
print_report
