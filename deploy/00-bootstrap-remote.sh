#!/usr/bin/env bash
# ==============================================================================
# 00-bootstrap-remote.sh —— 一键安装远端 Linux 服务器依赖
#
# 适用：CentOS 7/8、Rocky/AlmaLinux、RHEL、Ubuntu、Debian
#
# 流程：
#   1. 交互式输入 SSH 信息（与 02 上传脚本共用 prompt）
#   2. 测试连通性
#   3. SCP 推送 lib/bootstrap-remote-runner.sh 到远端 /tmp
#   4. 通过 SSH 在远端以 root 执行（自动 sudo 提权）
#   5. 把所有输出 tee 到 deploy/logs/bootstrap-<host>-<ts>.log
#
# 用法：
#   bash 00-bootstrap-remote.sh                # 默认装 Docker + 加速 + 防火墙
#   bash 00-bootstrap-remote.sh --with-jdk     # 同时安装 JDK 1.8（一般不用）
#   bash 00-bootstrap-remote.sh --no-mirror    # 不使用国内镜像加速
#   bash 00-bootstrap-remote.sh --no-firewall  # 不操作 firewalld/ufw
#   bash 00-bootstrap-remote.sh --skip-docker  # 跳过 Docker（仅装基础工具）
#
# 也可不依赖本机直接把 lib/bootstrap-remote-runner.sh scp 上去手动执行。
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

EXTRA_ARGS=()
for arg in "$@"; do
    case "$arg" in
        -h|--help) sed -n '2,22p' "$0"; exit 0 ;;
        *) EXTRA_ARGS+=("$arg") ;;
    esac
done

REMOTE_RUNNER="${SCRIPT_DIR}/lib/bootstrap-remote-runner.sh"
[[ -f "${REMOTE_RUNNER}" ]] || die "未找到 ${REMOTE_RUNNER}"

TS="$(date '+%Y%m%d_%H%M%S')"

# 收集 SSH 信息（若 deploy.env 里已有则复用，密码仍当场输入）
[[ -z "${SSH_HOST:-}" ]] && prompt_ssh_info || {
    log_info "复用已有 SSH 信息：${SSH_USER}@${SSH_HOST}:${SSH_PORT}"
    if [[ -z "${SSH_PASSWORD:-}" ]]; then
        # 检查是否能免密
        if ! test_ssh >/dev/null 2>&1; then
            log_warn "免密登录失败，请输入密码"
            read_secret "登录密码（输入不回显）" SSH_PASSWORD
            export SSH_PASSWORD
            require_cmd sshpass
        fi
    fi
}

LOG_FILE="${DEPLOY_LOG_DIR}/bootstrap-${SSH_HOST}-${TS}.log"
exec > >(tee -a "${LOG_FILE}") 2>&1

log_step "远端环境一键安装：${SSH_USER}@${SSH_HOST}:${SSH_PORT}"
log_info "完整日志：${LOG_FILE}"

test_ssh || die "SSH 连接失败，请先确认 IP/端口/账号"

# 上传 runner
REMOTE_TMP="/tmp/mawuya-bootstrap-${TS}.sh"
log_info "上传安装脚本到远端 ${REMOTE_TMP}"
upload_file "${REMOTE_RUNNER}" "${REMOTE_TMP}"

# 组装远端执行参数（注意：空数组在 set -u 下不能直接 "${arr[@]:-}" 展开，
# 否则会多出一个空字符串元素，导致远端 runner 收到空参数）
REMOTE_ARGS=""
if [[ ${#EXTRA_ARGS[@]} -gt 0 ]]; then
    for a in "${EXTRA_ARGS[@]}"; do
        REMOTE_ARGS+=" $(printf '%q' "$a")"
    done
fi
# 把端口环境变量也带过去
ENV_PREFIX="AMS_PORT=${AMS_PORT} BMS_PORT=${BMS_PORT} MYSQL_PORT=${MYSQL_PORT}"

log_step "在远端执行安装（自动 sudo 提权）"
remote_exec "chmod +x ${REMOTE_TMP} && ${ENV_PREFIX} bash ${REMOTE_TMP}${REMOTE_ARGS}; rc=\$?; rm -f ${REMOTE_TMP}; exit \$rc" \
    || die "远端安装失败，请查看上方日志"

persist_ssh_info
log_ok "远端环境安装完成 ✅"
log_step "下一步"
echo "  bash deploy/deploy.sh release        # 一键全流程：检查 → 打包 → 上传 → Docker 部署"
echo "  bash deploy/01-precheck.sh           # 或单独再跑一次环境检查"
