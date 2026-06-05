#!/usr/bin/env bash
# ==============================================================================
# deploy.sh —— Mawuya 部署一键入口
#
# 子命令：
#   setup-ssh  一键配置 SSH 免密登录（从此完全不用输密码，强烈推荐先跑这步）
#   bootstrap  一键安装远端 Linux 依赖（Docker/compose/工具，支持 CentOS/Rocky/Ubuntu/Debian）
#   preload    本机拉镜像→打 tar→上传→远端 docker load（解决远端拉镜像太慢）
#   precheck   只做环境检查（本地+远端）
#   build      只做编译打包（不上传）
#   upload     只做上传（需要先 build）
#   release    全流程：① precheck ② 编译+上传 ③ Docker up   适合首次部署
#     选项：--fast        跳过 precheck（已部署过，仅迭代时推荐）
#           --no-mysql    跳过 MySQL 容器重启，仅替换 AMS/BMS（最快的常规迭代）
#   update     等价于 release --fast --no-mysql（快速迭代的语义糖）
#   docker     远端 Docker 部署 / 维护（up|update|down|logs|status|rollback）
#   db         DB 一键维护（backup|schema|data|reset|file|shell|list-backups|restore）
#
# 密码体验优化：
#   - 已配置免密 → 全程零密码
#   - 未配置免密 → 整次 deploy.sh 执行中**只需输一次密码**（ControlMaster 复用连接）
#
# 推荐流程：
#   bash deploy/deploy.sh setup-ssh        # ① 一次性：配置免密
#   bash deploy/deploy.sh bootstrap        # ② 首次：远端装 Docker
#   bash deploy/deploy.sh release          # ③ 首次：完整发布
#   bash deploy/deploy.sh update           # 日常：快速迭代（仅 AMS/BMS）
#   bash deploy/deploy.sh db data          # 仅更新业务数据（自动备份）
#   bash deploy/deploy.sh db file foo.sql  # 执行任意自定义 SQL（增量补丁）
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

usage() { sed -n '2,33p' "$0"; exit "${1:-0}"; }

CMD="${1:-}"; shift || true

# 解析 release 选项
FAST=0; NO_MYSQL=0
parse_release_flags() {
    for a in "$@"; do
        case "$a" in
            --fast)     FAST=1 ;;
            --no-mysql) NO_MYSQL=1 ;;
            *) log_warn "release 忽略未知参数：$a" ;;
        esac
    done
}

do_release() {
    parse_release_flags "$@"
    if [[ "${FAST}" -eq 0 ]]; then
        bash "${SCRIPT_DIR}/01-precheck.sh"
    else
        log_warn "跳过 precheck（--fast）"
    fi
    bash "${SCRIPT_DIR}/02-build-and-upload.sh"
    if [[ "${NO_MYSQL}" -eq 1 ]]; then
        bash "${SCRIPT_DIR}/03-docker-deploy.sh" update
    else
        bash "${SCRIPT_DIR}/03-docker-deploy.sh" up
    fi
}

# 需要复用 SSH master 连接的子命令：在入口处先建好 master，
# 后续所有 ssh/scp 自动走 master socket，密码只需输入一次
needs_ssh() {
    case "$1" in
        bootstrap|preload|precheck|upload|release|update|docker|db) return 0 ;;
        *) return 1 ;;
    esac
}

if needs_ssh "${CMD}"; then
    if [[ -z "${SSH_HOST:-}" ]]; then
        prompt_ssh_info
    fi
    ssh_master_start || true
    trap ssh_master_stop EXIT INT TERM
fi

case "${CMD}" in
    setup-ssh) bash "${SCRIPT_DIR}/setup-ssh-key.sh"        "$@" ;;
    bootstrap) bash "${SCRIPT_DIR}/00-bootstrap-remote.sh"   "$@" ;;
    preload)   bash "${SCRIPT_DIR}/04-preload-images.sh"     "$@" ;;
    precheck)  bash "${SCRIPT_DIR}/01-precheck.sh"           "$@" ;;
    build)     bash "${SCRIPT_DIR}/02-build-and-upload.sh"  --skip-upload ;;
    upload)    bash "${SCRIPT_DIR}/02-build-and-upload.sh"  --skip-build  "$@" ;;
    release)   do_release "$@" ;;
    update)    do_release --fast --no-mysql ;;
    docker)    bash "${SCRIPT_DIR}/03-docker-deploy.sh"     "$@" ;;
    db)        bash "${SCRIPT_DIR}/05-db-deploy.sh"          "$@" ;;
    -h|--help|"") usage 0 ;;
    *) log_error "未知命令：${CMD}"; usage 1 ;;
esac
