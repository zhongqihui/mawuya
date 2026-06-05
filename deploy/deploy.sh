#!/usr/bin/env bash
# ==============================================================================
# deploy.sh —— Mawuya 部署一键入口
#
# 子命令：
#   precheck   只做环境检查（本地+远端）
#   build      只做编译打包（不上传）
#   upload     只做上传（需要先 build）
#   release    本地：编译 → 打包 → 上传 → 远端 Docker 部署   全流程
#   docker     远端 Docker 部署 / 维护（up|down|logs|status|rollback）
#
# 用法：
#   bash deploy/deploy.sh release
#   bash deploy/deploy.sh docker logs
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

usage() { sed -n '2,18p' "$0"; exit "${1:-0}"; }

CMD="${1:-}"; shift || true
case "${CMD}" in
    precheck) bash "${SCRIPT_DIR}/01-precheck.sh"            "$@" ;;
    build)    bash "${SCRIPT_DIR}/02-build-and-upload.sh"   --skip-upload ;;
    upload)   bash "${SCRIPT_DIR}/02-build-and-upload.sh"   --skip-build  "$@" ;;
    release)
        bash "${SCRIPT_DIR}/01-precheck.sh"
        bash "${SCRIPT_DIR}/02-build-and-upload.sh"
        bash "${SCRIPT_DIR}/03-docker-deploy.sh" up
        ;;
    docker)   bash "${SCRIPT_DIR}/03-docker-deploy.sh"      "$@" ;;
    -h|--help|"") usage 0 ;;
    *) log_error "未知命令：${CMD}"; usage 1 ;;
esac
