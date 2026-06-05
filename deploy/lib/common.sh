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
export AMS_PORT="${AMS_PORT:-80}"
export BMS_PORT="${BMS_PORT:-8899}"
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
# 通过 SSH ControlMaster 复用单条 TCP 连接，实现：
#   1) 密码只需输入一次（第一次建 master 时输入，后续 ssh/scp 直接复用，无需再输）
#   2) 大幅加速：第二次以后的 ssh 建链时间从 ~1s 降到 <50ms
#   3) 子脚本（如 02→03 调用链）共享同一个 master socket
# 全局变量：SSH_HOST / SSH_PORT / SSH_USER / SSH_PASSWORD（可空表示用免密）
# 由父脚本调用 ssh_master_start 开启；脚本退出时 trap 自动 ssh_master_stop。

# ControlPath 必须放在写入权限受控的目录，且总长度 < 100 字节
SSH_CTRL_DIR="${HOME}/.ssh/cm-mawuya"
mkdir -p "${SSH_CTRL_DIR}" 2>/dev/null || true
chmod 700 "${SSH_CTRL_DIR}" 2>/dev/null || true

ssh_ctrl_path() {
    # 用 % 占位符让 ssh 自己拼，确保唯一
    echo "${SSH_CTRL_DIR}/%C"
}

ssh_opts=(
    -o StrictHostKeyChecking=no
    -o UserKnownHostsFile=/dev/null
    -o LogLevel=ERROR
    -o ConnectTimeout=10
    -o ServerAliveInterval=30
    -o ControlMaster=auto
    -o "ControlPath=${SSH_CTRL_DIR}/%C"
    -o ControlPersist=10m
)

# 启动 SSH master 连接（首次需要密码时输入一次，后续全部复用）
# 在 deploy.sh / 单脚本入口调用一次即可。
ssh_master_start() {
    [[ -z "${SSH_HOST:-}" ]] && return 0
    # 已存在 master 则跳过
    if ssh -O check "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" >/dev/null 2>&1; then
        return 0
    fi
    log_info "建立 SSH master 连接（密码仅需输入此一次，后续操作全部复用）..."
    if [[ -n "${SSH_PASSWORD:-}" ]]; then
        require_cmd sshpass
        # -fN：建好链就 detach 到后台，不执行远端命令
        sshpass -p "${SSH_PASSWORD}" ssh -fN "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" \
            || die "SSH master 连接失败"
    else
        ssh -fN "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" \
            || die "SSH master 连接失败"
    fi
    log_ok "SSH master 已建立，后续无需再输密码"
}

# 关闭 SSH master（脚本退出 trap）
ssh_master_stop() {
    [[ -z "${SSH_HOST:-}" ]] && return 0
    ssh -O exit "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" >/dev/null 2>&1 || true
}

# 远端执行命令
# 用法：remote_exec "command string"
remote_exec() {
    local cmd="$1"
    # 有 master 时直接走 master，连 sshpass 都不再需要
    if ssh -O check "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" >/dev/null 2>&1; then
        ssh "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" "bash -lc $(printf '%q' "${cmd}")"
    elif [[ -n "${SSH_PASSWORD:-}" ]]; then
        require_cmd sshpass
        sshpass -p "${SSH_PASSWORD}" ssh "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" "bash -lc $(printf '%q' "${cmd}")"
    else
        ssh "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" "bash -lc $(printf '%q' "${cmd}")"
    fi
}

# 上传本地文件到远端
upload_file() {
    local src="$1"
    local dst="$2"
    [[ -e "${src}" ]] || die "本地文件不存在：${src}"

    log_info "上传 ${src} -> ${SSH_USER}@${SSH_HOST}:${dst}"
    # 有 master 复用时，rsync 也可以无密码走（rsync over ssh 会复用 master socket）
    local master_ok=0
    ssh -O check "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" >/dev/null 2>&1 && master_ok=1

    if have_cmd rsync && { [[ "${master_ok}" -eq 1 ]] || [[ -z "${SSH_PASSWORD:-}" ]]; }; then
        rsync -avz --progress -e "ssh ${ssh_opts[*]} -p ${SSH_PORT}" "${src}" "${SSH_USER}@${SSH_HOST}:${dst}"
    elif [[ "${master_ok}" -eq 1 ]]; then
        scp "${ssh_opts[@]}" -P "${SSH_PORT}" -r "${src}" "${SSH_USER}@${SSH_HOST}:${dst}"
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

# 是否能通过密钥免密登录（不会触发密码提示）
can_ssh_passwordless() {
    [[ -n "${SSH_HOST:-}" ]] || return 1
    ssh -o BatchMode=yes -o PasswordAuthentication=no \
        -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
        -o ConnectTimeout=5 \
        -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" 'exit 0' >/dev/null 2>&1
}

# 交互式收集 SSH 信息，写入全局变量
# 优化：如果已经可以免密登录（密钥已配 或 master 已建立），就不再询问密码
prompt_ssh_info() {
    log_step "请输入远程 Linux 服务器信息"
    read_with_default "服务器 IP 地址" "${SSH_HOST:-}" SSH_HOST
    [[ -z "${SSH_HOST}" ]] && die "服务器 IP 不能为空"
    read_with_default "SSH 端口" "${SSH_PORT:-22}" SSH_PORT
    read_with_default "登录用户名" "${SSH_USER:-root}" SSH_USER
    export SSH_HOST SSH_PORT SSH_USER

    # 已配置免密 → 直接跳过密码询问
    if can_ssh_passwordless; then
        SSH_PASSWORD=""
        export SSH_PASSWORD
        log_ok "检测到已配置免密登录，跳过密码输入"
        return 0
    fi

    local auth_mode
    read_with_default "认证方式 (1=密码, 2=密钥免密)" "1" auth_mode
    if [[ "${auth_mode}" == "1" ]]; then
        read_secret "登录密码（输入不回显，仅本次会话使用）" SSH_PASSWORD
        [[ -z "${SSH_PASSWORD}" ]] && die "密码不能为空"
        if ! have_cmd sshpass; then
            log_warn "未检测到 sshpass，正在使用密码认证将无法继续。"
            log_warn "macOS:  brew install hudochenkov/sshpass/sshpass"
            log_warn "Ubuntu: sudo apt-get install -y sshpass"
            log_warn "CentOS: sudo yum install -y sshpass"
            log_warn "推荐改用：bash deploy/deploy.sh setup-ssh  一次性配置免密"
            die "请安装 sshpass 后重试，或选择密钥免密方式"
        fi
    else
        SSH_PASSWORD=""
        log_info "已选择密钥免密；请确保本机 ssh-key 已配置到远端 ~/.ssh/authorized_keys"
        log_info "或运行：bash deploy/deploy.sh setup-ssh  自动完成配置"
    fi
    export SSH_PASSWORD
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
