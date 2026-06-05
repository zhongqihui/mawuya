#!/usr/bin/env bash
# ==============================================================================
# setup-ssh-key.sh —— 一键配置 SSH 免密登录
#
# 干两件事：
#   1. 如果本机 ~/.ssh/id_ed25519(.pub) 不存在 → 自动生成（无 passphrase）
#   2. 把公钥追加到远端 ~/.ssh/authorized_keys（不重复追加）
#      - 如果安装了 ssh-copy-id：直接用之
#      - 否则用 sshpass + cat | ssh 兜底
#
# 输入：交互式收集远端 SSH 信息（密码用一次后即可丢弃）
# 配置后：所有后续脚本都不再需要密码，可在 deploy.env 把 SSH_PASSWORD 留空
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

KEY_FILE="${HOME}/.ssh/id_ed25519"
PUB_FILE="${KEY_FILE}.pub"

# ---- 1. 生成密钥 ----
if [[ ! -f "${PUB_FILE}" ]]; then
    log_step "本机未发现 SSH 密钥，自动生成 ${KEY_FILE}"
    mkdir -p "${HOME}/.ssh"
    chmod 700 "${HOME}/.ssh"
    ssh-keygen -t ed25519 -C "mawuya-deploy@$(hostname)" -f "${KEY_FILE}" -N "" -q
    log_ok "已生成 ${PUB_FILE}"
else
    log_ok "已存在 SSH 公钥：${PUB_FILE}"
fi

# ---- 2. 收集远端信息（强制密码） ----
log_step "请输入远程服务器密码（仅这一次，配置完成后永久免密）"
read_with_default "服务器 IP 地址"  "${SSH_HOST:-}" SSH_HOST
[[ -z "${SSH_HOST}" ]] && die "IP 不能为空"
read_with_default "SSH 端口"        "${SSH_PORT:-22}" SSH_PORT
read_with_default "登录用户名"      "${SSH_USER:-root}" SSH_USER
read_secret       "登录密码"        SSH_PASSWORD
[[ -z "${SSH_PASSWORD}" ]] && die "密码不能为空"
export SSH_HOST SSH_PORT SSH_USER SSH_PASSWORD

# ---- 3. 下发公钥 ----
log_step "下发公钥到远端 ${SSH_USER}@${SSH_HOST}"
PUB_CONTENT="$(cat "${PUB_FILE}")"

if have_cmd ssh-copy-id && have_cmd sshpass; then
    # 标准方案：ssh-copy-id 自带去重
    sshpass -p "${SSH_PASSWORD}" \
        ssh-copy-id -i "${PUB_FILE}" \
            -o StrictHostKeyChecking=no \
            -o UserKnownHostsFile=/dev/null \
            -p "${SSH_PORT}" \
            "${SSH_USER}@${SSH_HOST}"
elif have_cmd sshpass; then
    # 兜底：手动追加，自带 grep 去重
    sshpass -p "${SSH_PASSWORD}" \
        ssh -o StrictHostKeyChecking=no \
            -o UserKnownHostsFile=/dev/null \
            -p "${SSH_PORT}" \
            "${SSH_USER}@${SSH_HOST}" \
            "mkdir -p ~/.ssh && chmod 700 ~/.ssh && \
             touch ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys && \
             grep -qxF $(printf '%q' "${PUB_CONTENT}") ~/.ssh/authorized_keys || \
             echo $(printf '%q' "${PUB_CONTENT}") >> ~/.ssh/authorized_keys"
else
    log_warn "未安装 sshpass，将进入交互模式，请手动输入密码完成下面 ssh-copy-id"
    ssh-copy-id -i "${PUB_FILE}" \
        -o StrictHostKeyChecking=no \
        -o UserKnownHostsFile=/dev/null \
        -p "${SSH_PORT}" \
        "${SSH_USER}@${SSH_HOST}"
fi

# ---- 4. 验证免密 ----
log_step "验证免密登录..."
unset SSH_PASSWORD
if ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
       -o PasswordAuthentication=no -o BatchMode=yes \
       -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" 'echo __SSH_KEY_OK__' 2>/dev/null | grep -q __SSH_KEY_OK__; then
    log_ok "免密登录配置成功 ✅"
else
    die "免密验证失败，请检查远端 ~/.ssh/authorized_keys 权限（应为 600）和家目录权限"
fi

# ---- 5. 持久化 SSH 信息（不含密码） ----
persist_ssh_info

log_step "完成"
echo "  从此之后所有部署命令都无需再输密码："
echo "    bash deploy/deploy.sh release"
echo "    bash deploy/deploy.sh update"
echo "  公钥已下发到：${SSH_USER}@${SSH_HOST}:~/.ssh/authorized_keys"
echo "  如需撤销，删除上述文件中对应公钥行即可。"
