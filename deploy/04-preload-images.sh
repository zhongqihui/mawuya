#!/usr/bin/env bash
# ==============================================================================
# 04-preload-images.sh —— 本地拉镜像 → 打 tar → SCP 上传 → 远端 docker load
#
# 适用场景：
#   远端公网到 docker.io / 国内加速器都很慢（如腾讯云海外轻量、IDC 出墙慢），
#   而本机/办公网拉 docker.io 不卡。本脚本用本机当中转，减少远端拉镜像耗时。
#
# 流程：
#   1. 本机 docker pull <BASE_IMAGE> <MYSQL_IMAGE>（已存在则秒过）
#   2. docker save -o deploy/dist/mawuya-images-<ts>.tar
#   3. 用 pigz/gzip 压缩为 .tar.gz（可选，体积可压 30-40%）
#   4. SCP 上传到远端 /tmp 并 docker load
#   5. 自动清理远端 tar
#
# 用法：
#   bash deploy/04-preload-images.sh
#   BASE_IMAGE=eclipse-temurin:8-jre-jammy MYSQL_IMAGE=mysql:5.7 bash deploy/04-preload-images.sh
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

: "${BASE_IMAGE:=eclipse-temurin:8-jre-jammy}"
: "${MYSQL_IMAGE:=mysql:5.7}"

require_cmd docker

TS="$(date '+%Y%m%d_%H%M%S')"
TAR_FILE="${DEPLOY_DIST_DIR}/mawuya-images-${TS}.tar"
TGZ_FILE="${TAR_FILE}.gz"

# 1. 本机预拉
log_step "本机预拉镜像"
for img in "${BASE_IMAGE}" "${MYSQL_IMAGE}"; do
    if docker image inspect "${img}" >/dev/null 2>&1; then
        log_ok "已存在：${img}"
    else
        log_info "docker pull ${img}"
        docker pull "${img}" || die "本机拉取失败：${img}（先解决本机网络）"
    fi
done

# 2. 打 tar 包
log_step "导出镜像到 ${TAR_FILE}"
docker save -o "${TAR_FILE}" "${BASE_IMAGE}" "${MYSQL_IMAGE}"
log_ok "已导出：$(du -h "${TAR_FILE}" | awk '{print $1}')"

# 3. 压缩（pigz 多核更快；fallback gzip）
COMPRESSED="${TAR_FILE}"
if have_cmd pigz; then
    log_info "pigz 压缩中（多核加速）..."
    pigz -f "${TAR_FILE}"
    COMPRESSED="${TGZ_FILE}"
elif have_cmd gzip; then
    log_info "gzip 压缩中..."
    gzip -f "${TAR_FILE}"
    COMPRESSED="${TGZ_FILE}"
fi
log_ok "压缩完成：${COMPRESSED} ($(du -h "${COMPRESSED}" | awk '{print $1}'))"

# 4. 上传 + 远端 load
log_step "上传镜像到远端"
[[ -z "${SSH_HOST:-}" ]] && prompt_ssh_info
test_ssh || die "SSH 连通失败"

REMOTE_TAR="/tmp/$(basename "${COMPRESSED}")"
upload_file "${COMPRESSED}" "${REMOTE_TAR}"

log_step "远端 docker load"
if [[ "${COMPRESSED}" == *.gz ]]; then
    remote_exec "set -e
        echo '解压并 load 镜像...'
        gunzip -c '${REMOTE_TAR}' | docker load
        rm -f '${REMOTE_TAR}'
        echo '当前镜像：'
        docker images | grep -E '${BASE_IMAGE%%:*}|${MYSQL_IMAGE%%:*}' || true
    "
else
    remote_exec "set -e
        docker load -i '${REMOTE_TAR}'
        rm -f '${REMOTE_TAR}'
        docker images | grep -E '${BASE_IMAGE%%:*}|${MYSQL_IMAGE%%:*}' || true
    "
fi

log_ok "镜像预加载完成 ✅"
log_step "下一步"
echo "  bash deploy/deploy.sh release        # 远端将直接复用已 load 的镜像，无需再 pull"
