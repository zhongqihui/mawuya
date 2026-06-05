#!/usr/bin/env bash
# ==============================================================================
# 02-build-and-upload.sh —— 编译打包 + SCP 上传到远端
#
# 步骤：
#   1. （可选）执行 mvn clean package 生成 AMS / BMS jar
#   2. 把 jar、data/schema.sql、data/data.sql、deploy/03-docker-deploy.sh、
#      docker 模板等打成 mawuya-bundle-<ts>.tar.gz 放在 deploy/dist
#   3. 交互式收集远端 SSH 信息（IP / 端口 / 用户名 / 密码）
#   4. SSH 连通性测试
#   5. SCP 把 bundle 上传到 /usr/local/services/，并展示进度
#   6. 远端解包到 ${REMOTE_DEPLOY_DIR}/release-<ts>，软链接 current 指向它
#
# 用法：
#   bash 02-build-and-upload.sh [--skip-build] [--skip-upload]
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

SKIP_BUILD=0
SKIP_UPLOAD=0
for arg in "$@"; do
    case "$arg" in
        --skip-build)  SKIP_BUILD=1 ;;
        --skip-upload) SKIP_UPLOAD=1 ;;
        -h|--help)
            sed -n '2,18p' "$0"; exit 0 ;;
        *) die "未知参数：$arg" ;;
    esac
done

TS="$(date '+%Y%m%d_%H%M%S')"
BUNDLE_NAME="mawuya-bundle-${TS}.tar.gz"
BUNDLE_PATH="${DEPLOY_DIST_DIR}/${BUNDLE_NAME}"
STAGE_DIR="${DEPLOY_DIST_DIR}/stage-${TS}"

# ------- 1. 编译 -------
build_project() {
    log_step "Maven 编译打包"

    require_cmd mvn java
    cd "${PROJECT_ROOT}"

    log_info "执行：mvn -DskipTests clean package"
    if ! mvn -DskipTests clean package; then
        die "Maven 打包失败，请检查上方编译日志"
    fi

    local ams_jar="${PROJECT_ROOT}/mawuya-ams/target/${AMS_JAR_NAME}"
    local bms_jar="${PROJECT_ROOT}/mawuya-bms/target/${BMS_JAR_NAME}"
    [[ -f "${ams_jar}" ]] || die "未找到 AMS jar：${ams_jar}"
    [[ -f "${bms_jar}" ]] || die "未找到 BMS jar：${bms_jar}"
    log_ok "AMS: ${ams_jar} ($(du -h "${ams_jar}" | awk '{print $1}'))"
    log_ok "BMS: ${bms_jar} ($(du -h "${bms_jar}" | awk '{print $1}'))"
}

# ------- 2. 组装 bundle -------
make_bundle() {
    log_step "组装发布包：${BUNDLE_NAME}"
    rm -rf "${STAGE_DIR}"
    mkdir -p "${STAGE_DIR}/jars" "${STAGE_DIR}/sql" "${STAGE_DIR}/scripts" "${STAGE_DIR}/docker"

    cp "${PROJECT_ROOT}/mawuya-ams/target/${AMS_JAR_NAME}" "${STAGE_DIR}/jars/"
    cp "${PROJECT_ROOT}/mawuya-bms/target/${BMS_JAR_NAME}" "${STAGE_DIR}/jars/"

    if [[ -d "${PROJECT_ROOT}/data" ]]; then
        for f in schema.sql data.sql; do
            [[ -f "${PROJECT_ROOT}/data/${f}" ]] && cp "${PROJECT_ROOT}/data/${f}" "${STAGE_DIR}/sql/" || true
        done
    fi

    # 部署脚本与公共库一并发到远端，便于直接在远端执行 03-docker-deploy.sh
    cp "${SCRIPT_DIR}/03-docker-deploy.sh" "${STAGE_DIR}/scripts/"
    cp -r "${SCRIPT_DIR}/lib"               "${STAGE_DIR}/scripts/"
    [[ -d "${SCRIPT_DIR}/docker" ]] && cp -r "${SCRIPT_DIR}/docker" "${STAGE_DIR}/" || true

    # 写入 manifest，供远端校验
    cat > "${STAGE_DIR}/MANIFEST" <<EOF
build_time=${TS}
ams_jar=${AMS_JAR_NAME}
bms_jar=${BMS_JAR_NAME}
ams_jar_sha256=$(shasum -a 256 "${STAGE_DIR}/jars/${AMS_JAR_NAME}" | awk '{print $1}')
bms_jar_sha256=$(shasum -a 256 "${STAGE_DIR}/jars/${BMS_JAR_NAME}" | awk '{print $1}')
EOF

    ( cd "${DEPLOY_DIST_DIR}" && tar -czf "${BUNDLE_NAME}" -C "${STAGE_DIR}" . )
    log_ok "已生成发布包：${BUNDLE_PATH} ($(du -h "${BUNDLE_PATH}" | awk '{print $1}'))"
    rm -rf "${STAGE_DIR}"
}

# ------- 3. 上传 -------
upload_bundle() {
    log_step "上传发布包到远端"
    [[ -z "${SSH_HOST:-}" ]] && prompt_ssh_info
    test_ssh || die "SSH 连接失败，已中断"

    log_info "在远端创建部署目录：${REMOTE_DEPLOY_DIR}"
    remote_exec "mkdir -p ${REMOTE_DEPLOY_DIR}/releases ${REMOTE_BASE_DIR} && \
                 [ -w ${REMOTE_DEPLOY_DIR} ] || { echo '目录不可写'; exit 2; }"

    # 直接传到 /usr/local/services/ 下（题目要求），再由远端解到 release 目录
    log_info "SCP 传输中（含进度）..."
    upload_file "${BUNDLE_PATH}" "${REMOTE_BASE_DIR}/${BUNDLE_NAME}"

    # 远端解包并切换 current 软链接
    local release_dir="${REMOTE_DEPLOY_DIR}/releases/${TS}"
    log_info "远端解包到 ${release_dir}"
    remote_exec "set -e
        mkdir -p '${release_dir}'
        tar -xzf '${REMOTE_BASE_DIR}/${BUNDLE_NAME}' -C '${release_dir}'
        ln -sfn '${release_dir}' '${REMOTE_DEPLOY_DIR}/current'
        chmod +x '${release_dir}/scripts/03-docker-deploy.sh' 2>/dev/null || true
        echo '远端发布目录：${REMOTE_DEPLOY_DIR}/current'
        ls -lh '${release_dir}/jars/'
    "

    persist_ssh_info
    log_ok "上传完成：${REMOTE_BASE_DIR}/${BUNDLE_NAME}"
    log_ok "已解包至：${release_dir}（current 软链接已切换）"
}

# ------- 主流程 -------
[[ "${SKIP_BUILD}"  == "0" ]] && build_project  || log_warn "跳过编译（--skip-build）"
[[ "${SKIP_BUILD}"  == "0" ]] && make_bundle    || {
    # 跳过编译时若 dist 中有最新 bundle 则复用
    latest=$(ls -1t "${DEPLOY_DIST_DIR}"/mawuya-bundle-*.tar.gz 2>/dev/null | head -n1 || true)
    [[ -z "${latest:-}" ]] && die "--skip-build 模式下未找到任何已构建的 bundle，请先编译"
    BUNDLE_PATH="${latest}"
    BUNDLE_NAME="$(basename "${BUNDLE_PATH}")"
    log_info "复用最近发布包：${BUNDLE_PATH}"
}
[[ "${SKIP_UPLOAD}" == "0" ]] && upload_bundle  || log_warn "跳过上传（--skip-upload）"

log_step "下一步"
echo "在远端执行 Docker 一键部署："
echo "  ssh ${SSH_USER:-<user>}@${SSH_HOST:-<host>} 'cd ${REMOTE_DEPLOY_DIR}/current && bash scripts/03-docker-deploy.sh up'"
echo "或在本机继续执行： bash deploy/03-docker-deploy.sh up"
