#!/usr/bin/env bash
# ==============================================================================
# common.sh —— Mawuya 部署脚本公共库
#
# 提供：颜色输出、日志、确认提示、严格模式、SSH/SCP 工具函数。
# 仅作为 source 引入，不要单独执行。
# ==============================================================================

# 防止重复 source
if [[ -n "${__MAWUYA_COMMON_LOADED:-}" ]]; then
    return 0
fi
__MAWUYA_COMMON_LOADED=1

# -------- 严格模式 --------
set -o errexit
set -o nounset
set -o pipefail

# -------- 路径常量 --------
# DEPLOY_DIR 为 deploy/ 目录绝对路径
COMMON_SH_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export DEPLOY_DIR="$(cd "${COMMON_SH_DIR}/.." && pwd)"
export PROJECT_ROOT="$(cd "${DEPLOY_DIR}/.." && pwd)"
export DEPLOY_LOG_DIR="${DEPLOY_DIR}/logs"
export DEPLOY_DIST_DIR="${DEPLOY_DIR}/dist"
export DEPLOY_CONF_DIR="${DEPLOY_DIR}/conf"
mkdir -p "${DEPLOY_LOG_DIR}" "${DEPLOY_DIST_DIR}" "${DEPLOY_CONF_DIR}"

# 默认部署配置（可被 deploy/conf/deploy.env 覆盖）
export REMOTE_DEPLOY_DIR="${REMOTE_DEPLOY_DIR:-/usr/local/services/mawuya}"
export REMOTE_BASE_DIR="${REMOTE_BASE_DIR:-/usr/local/services}"
export AMS_PORT="${AMS_PORT:-8080}"
export BMS_PORT="${BMS_PORT:-8081}"
export MYSQL_PORT="${MYSQL_PORT:-3306}"
export AMS_JAR_NAME="mawuya-ams-1.0.0.jar"
export BMS_JAR_NAME="mawuya-bms-1.0.0.jar"

# 加载用户自定义环境
if [[ -f "${DEPLOY_CONF_DIR}/deploy.env" ]]; then
    # shellcheck disable=SC1091
    source "${DEPLOY_CONF_DIR}/deploy.env"
fi

# -------- 颜色 --------
if [[ -t 1 ]]; then
    C_RESET="\033[0m"
    C_RED="\033[31m"
    C_GREEN="\033[32m"
    C_YELLOW="\033[33m"
    C_BLUE="\033[34m"
    C_CYAN="\033[36m"
    C_BOLD="\033[1m"
else
    C_RESET=""; C_RED=""; C_GREEN=""; C_YELLOW=""; C_BLUE=""; C_CYAN=""; C_BOLD=""
fi

# -------- 日志 --------
__ts() { date '+%Y-%m-%d %H:%M:%S'; }

log_info()  { printf "%b[INFO ]%b %s %s\n"  "${C_BLUE}"   "${C_RESET}" "$(__ts)" "$*"; }
log_ok()    { printf "%b[ OK  ]%b %s %s\n"  "${C_GREEN}"  "${C_RESET}" "$(__ts)" "$*"; }
log_warn()  { printf "%b[WARN ]%b %s %s\n"  "${C_YELLOW}" "${C_RESET}" "$(__ts)" "$*" >&2; }
log_error() { printf "%b[ERROR]%b %s %s\n"  "${C_RED}"    "${C_RESET}" "$(__ts)" "$*" >&2; }
log_step()  { printf "\n%b==> %s%b\n"       "${C_BOLD}${C_CYAN}" "$*" "${C_RESET}"; }

die() {
    log_error "$*"
    exit 1
}

# -------- 交互工具 --------
# 用法：confirm "请确认是否继续" [默认值 y/n]
confirm() {
    local prompt="${1:-是否继续？}"
    local default="${2:-n}"
    local hint
    [[ "${default}" == "y" ]] && hint="[Y/n]" || hint="[y/N]"

    local ans
    read -r -p "$(printf '%b%s %s %b' "${C_YELLOW}" "${prompt}" "${hint}" "${C_RESET}")" ans
    ans="${ans:-${default}}"
    case "${ans}" in
        y|Y|yes|YES) return 0 ;;
        *) return 1 ;;
    esac
}

# 输入字符串，可指定默认值，空则用默认值
# 用法：read_with_default "提示语" 默认值 变量名
read_with_default() {
    local prompt="$1"
    local default="$2"
    local __var_name="$3"
    local val
    if [[ -n "${default}" ]]; then
        read -r -p "${prompt} [${default}]: " val
        val="${val:-${default}}"
    else
        read -r -p "${prompt}: " val
    fi
    printf -v "${__var_name}" '%s' "${val}"
}

# 读取密码（不回显）
# 用法：read_secret "提示语" 变量名
read_secret() {
    local prompt="$1"
    local __var_name="$2"
    local val
    read -r -s -p "${prompt}: " val
    echo
    printf -v "${__var_name}" '%s' "${val}"
}

# -------- 命令探测 --------
have_cmd() { command -v "$1" >/dev/null 2>&1; }

require_cmd() {
    local missing=()
    for c in "$@"; do
        have_cmd "$c" || missing+=("$c")
    done
    if (( ${#missing[@]} > 0 )); then
        die "缺少以下命令，请先安装：${missing[*]}"
    fi
}

# -------- SSH/SCP 封装 --------
# 使用密码登录时优先 sshpass；建议生产用密钥免密。
# 全局变量：SSH_HOST / SSH_PORT / SSH_USER / SSH_PASSWORD（可空表示用免密）
ssh_opts=(
    -o StrictHostKeyChecking=no
    -o UserKnownHostsFile=/dev/null
    -o LogLevel=ERROR
    -o ConnectTimeout=10
    -o ServerAliveInterval=30
)

# 远端执行命令；stdin 透传，支持 here-doc
# 用法：remote_exec "command string"
remote_exec() {
    local cmd="$1"
    if [[ -n "${SSH_PASSWORD:-}" ]]; then
        require_cmd sshpass
        sshpass -p "${SSH_PASSWORD}" ssh "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" "bash -lc $(printf '%q' "${cmd}")"
    else
        ssh "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" "bash -lc $(printf '%q' "${cmd}")"
    fi
}

# 上传本地文件到远端，带进度（依赖 scp -v 或 rsync）
# 用法：upload_file <本地路径> <远端路径>
upload_file() {
    local src="$1"
    local dst="$2"
    [[ -e "${src}" ]] || die "本地文件不存在：${src}"

    log_info "上传 ${src} -> ${SSH_USER}@${SSH_HOST}:${dst}"
    if have_cmd rsync && [[ -z "${SSH_PASSWORD:-}" ]]; then
        # 仅在使用密钥免密时优先 rsync（rsync over sshpass 体验差）
        rsync -avz --progress -e "ssh ${ssh_opts[*]} -p ${SSH_PORT}" "${src}" "${SSH_USER}@${SSH_HOST}:${dst}"
    elif [[ -n "${SSH_PASSWORD:-}" ]]; then
        require_cmd sshpass
        sshpass -p "${SSH_PASSWORD}" scp "${ssh_opts[@]}" -P "${SSH_PORT}" -r "${src}" "${SSH_USER}@${SSH_HOST}:${dst}"
    else
        scp "${ssh_opts[@]}" -P "${SSH_PORT}" -r "${src}" "${SSH_USER}@${SSH_HOST}:${dst}"
    fi
}

# 测试 SSH 连通性
test_ssh() {
    log_info "测试 SSH 连接 ${SSH_USER}@${SSH_HOST}:${SSH_PORT} ..."
    if remote_exec "echo __SSH_OK__ && uname -a" >/dev/null 2>&1; then
        log_ok "SSH 连接正常"
        return 0
    else
        log_error "SSH 连接失败：请检查 IP/端口/账号/密码或网络"
        return 1
    fi
}

# 交互式收集 SSH 信息，写入全局变量
prompt_ssh_info() {
    log_step "请输入远程 Linux 服务器信息"
    read_with_default "服务器 IP 地址" "${SSH_HOST:-}" SSH_HOST
    [[ -z "${SSH_HOST}" ]] && die "服务器 IP 不能为空"
    read_with_default "SSH 端口" "${SSH_PORT:-22}" SSH_PORT
    read_with_default "登录用户名" "${SSH_USER:-root}" SSH_USER

    local auth_mode
    read_with_default "认证方式 (1=密码, 2=密钥免密)" "1" auth_mode
    if [[ "${auth_mode}" == "1" ]]; then
        read_secret "登录密码（输入不回显）" SSH_PASSWORD
        [[ -z "${SSH_PASSWORD}" ]] && die "密码不能为空"
        if ! have_cmd sshpass; then
            log_warn "未检测到 sshpass，正在使用密码认证将无法继续。"
            log_warn "macOS:  brew install hudochenkov/sshpass/sshpass"
            log_warn "Ubuntu: sudo apt-get install -y sshpass"
            log_warn "CentOS: sudo yum install -y sshpass"
            die "请安装 sshpass 后重试，或选择密钥免密方式"
        fi
    else
        SSH_PASSWORD=""
        log_info "已选择密钥免密；请确保本机 ssh-key 已配置到远端 ~/.ssh/authorized_keys"
    fi
    export SSH_HOST SSH_PORT SSH_USER SSH_PASSWORD
}

# 持久化 SSH 信息（不写密码）
persist_ssh_info() {
    local f="${DEPLOY_CONF_DIR}/deploy.env"
    {
        echo "# auto-generated by deploy scripts at $(__ts)"
        echo "export SSH_HOST=\"${SSH_HOST}\""
        echo "export SSH_PORT=\"${SSH_PORT}\""
        echo "export SSH_USER=\"${SSH_USER}\""
        echo "export REMOTE_DEPLOY_DIR=\"${REMOTE_DEPLOY_DIR}\""
        echo "export AMS_PORT=\"${AMS_PORT}\""
        echo "export BMS_PORT=\"${BMS_PORT}\""
    } > "${f}"
    chmod 600 "${f}"
    log_info "已写入 ${f}（不含密码）"
}
