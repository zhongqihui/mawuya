#!/usr/bin/env bash
# ==============================================================================
# 05-db-deploy.sh —— DB 一键更新脚本
#
# 子命令：
#   backup            备份远端 MySQL 到 mawuya/db-backups/blog-<ts>.sql.gz
#   schema            执行 data/schema.sql（重建全部表，⚠ 会丢数据）
#   data              执行 data/data.sql（重灌业务数据；保留 schema）
#   reset             schema + data 全量重灌（最干净的一次重置）
#   file <path>       执行任意本地 .sql 文件（增量 / 修复 / 迁移）
#   shell             直接进入远端容器的 mysql 交互终端
#   list-backups      列出远端备份
#   restore <name>    从远端备份恢复（mawuya/db-backups/<name>.sql.gz）
#
# 工作流（每次写操作）：
#   1. 自动备份 → mawuya/db-backups/blog-<ts>.sql.gz
#   2. SCP 本地 SQL → /tmp/<file>
#   3. docker cp 进 mysql 容器
#   4. docker exec mysql -uroot 执行
#   5. 失败保留备份；成功 trim 历史只留 10 份
#
# 用法示例：
#   bash deploy/deploy.sh db backup
#   bash deploy/deploy.sh db data
#   bash deploy/deploy.sh db file ./data/patch-20260606.sql
#   bash deploy/deploy.sh db shell
# ==============================================================================

set -o errexit
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "${SCRIPT_DIR}/lib/common.sh"

ACTION="${1:-}"; shift || true

# 远端固定路径（与 03 部署脚本一致）
REMOTE_HOME="${REMOTE_DEPLOY_DIR}"
REMOTE_BACKUP_DIR="${REMOTE_HOME}/db-backups"
MYSQL_CONTAINER="mawuya-mysql"
MYSQL_DB="${MYSQL_DATABASE:-blog}"

# 远端通过环境变量取密码（不暴露在命令行）
# 容器内 root 密码就是 MYSQL_ROOT_PASSWORD
remote_mysql_env() {
    cat <<EOF
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-ChangeMe_Root_2026}"
MYSQL_DB="${MYSQL_DB}"
EOF
}

# 容器内执行 mysql 命令；通过 stdin 传 SQL，避免命令行密码泄漏
# 用法：remote_mysql_run "SHOW TABLES;"
remote_mysql_run() {
    local sql="$1"
    remote_exec "
set -e
$(remote_mysql_env)
docker exec -i -e MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" ${MYSQL_CONTAINER} \
    mysql -uroot --default-character-set=utf8mb4 \"\$MYSQL_DB\" <<'__SQL_EOF__'
${sql}
__SQL_EOF__
"
}

# 容器内执行整个 SQL 文件（先上传到远端 /tmp）
# 用法：remote_mysql_run_file <本地 .sql 路径>
remote_mysql_run_file() {
    local local_sql="$1"
    [[ -f "${local_sql}" ]] || die "本地 SQL 文件不存在：${local_sql}"

    local fname; fname="$(basename "${local_sql}")"
    local remote_tmp="/tmp/mawuya-sql-$(date +%s)-${fname}"

    log_info "上传 SQL 文件：${local_sql} → ${remote_tmp} ($(du -h "${local_sql}" | awk '{print $1}'))"
    upload_file "${local_sql}" "${remote_tmp}"

    log_info "在容器内执行：${fname}"
    remote_exec "
set -e
$(remote_mysql_env)
# 用 docker exec -i 把 SQL 文件喂进去；MYSQL_PWD 经环境变量传，不进程列表
docker exec -i -e MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" ${MYSQL_CONTAINER} \
    mysql -uroot --default-character-set=utf8mb4 \"\$MYSQL_DB\" < ${remote_tmp}
rm -f ${remote_tmp}
echo \"[OK] ${fname} 执行完成\"
"
}

# 备份远端 MySQL（mysqldump 全量 + gzip）
cmd_backup() {
    log_step "备份远端 MySQL（${MYSQL_DB} 库）"
    local ts; ts="$(date +%Y%m%d_%H%M%S)"
    local remote_file="${REMOTE_BACKUP_DIR}/blog-${ts}.sql.gz"

    remote_exec "
set -e
$(remote_mysql_env)
mkdir -p '${REMOTE_BACKUP_DIR}'
docker exec -e MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" ${MYSQL_CONTAINER} \
    mysqldump -uroot --default-character-set=utf8mb4 \
              --single-transaction --quick --hex-blob --set-gtid-purged=OFF \
              \"\$MYSQL_DB\" | gzip > '${remote_file}'
size=\$(du -h '${remote_file}' | awk '{print \$1}')
echo \"[OK] 备份完成：${remote_file} (\${size})\"

# 保留最近 10 个备份，旧的清理
ls -1t '${REMOTE_BACKUP_DIR}'/blog-*.sql.gz 2>/dev/null | awk 'NR>10' | xargs -r rm -f
echo '当前备份列表（最多保留 10 份）：'
ls -lh '${REMOTE_BACKUP_DIR}'/blog-*.sql.gz 2>/dev/null || true
"
    log_ok "备份完成"
}

# 校验 mysql 容器存活
ensure_container() {
    log_info "检查 ${MYSQL_CONTAINER} 容器状态"
    remote_exec "
if ! docker ps --format '{{.Names}}' | grep -qx '${MYSQL_CONTAINER}'; then
    echo '[ERROR] ${MYSQL_CONTAINER} 未运行，请先 bash deploy/deploy.sh release' >&2
    exit 1
fi
" || die "MySQL 容器未运行"
}

cmd_schema() {
    log_step "执行 schema.sql（⚠ 会重建全部表，删除现有数据）"
    log_warn "此操作将删除 blog 库中的所有现有数据！"
    confirm "确认继续？" "n" || { log_warn "已取消"; exit 0; }
    ensure_container
    cmd_backup
    remote_mysql_run_file "${PROJECT_ROOT}/data/schema.sql"
    log_ok "schema 重建完成"
}

cmd_data() {
    log_step "执行 data.sql（重灌业务数据，schema 不变）"
    log_warn "本脚本会 TRUNCATE 业务表后插入 data.sql 中的数据。"
    confirm "确认继续？" "n" || { log_warn "已取消"; exit 0; }
    ensure_container
    cmd_backup
    remote_mysql_run_file "${PROJECT_ROOT}/data/data.sql"
    log_ok "data 重灌完成"
}

cmd_reset() {
    log_step "完整重置：schema.sql + data.sql"
    log_warn "整个 blog 库会被销毁重建！"
    confirm "确认继续？" "n" || { log_warn "已取消"; exit 0; }
    ensure_container
    cmd_backup
    remote_mysql_run_file "${PROJECT_ROOT}/data/schema.sql"
    remote_mysql_run_file "${PROJECT_ROOT}/data/data.sql"
    log_ok "DB 全量重置完成"
}

cmd_file() {
    local sql="${1:-}"
    [[ -z "${sql}" ]] && die "用法：db file <本地 .sql 路径>"
    [[ -f "${sql}" ]] || die "文件不存在：${sql}"
    log_step "执行自定义 SQL：${sql}"
    confirm "确认在远端 ${MYSQL_DB} 库执行此 SQL？" "n" || { log_warn "已取消"; exit 0; }
    ensure_container
    cmd_backup
    remote_mysql_run_file "${sql}"
    log_ok "SQL 执行完成"
}

cmd_shell() {
    log_step "进入远端 ${MYSQL_CONTAINER} 的 mysql 交互终端"
    log_info "退出请输入 \\q 或按 Ctrl+D"
    # 注意：交互式 mysql 需要 -t（分配 TTY）
    if [[ -n "${SSH_PASSWORD:-}" ]] && ! ssh -O check "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" >/dev/null 2>&1; then
        require_cmd sshpass
        sshpass -p "${SSH_PASSWORD}" ssh -t "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" \
            "docker exec -it -e MYSQL_PWD='${MYSQL_ROOT_PASSWORD:-ChangeMe_Root_2026}' ${MYSQL_CONTAINER} mysql -uroot --default-character-set=utf8mb4 ${MYSQL_DB}"
    else
        ssh -t "${ssh_opts[@]}" -p "${SSH_PORT}" "${SSH_USER}@${SSH_HOST}" \
            "docker exec -it -e MYSQL_PWD='${MYSQL_ROOT_PASSWORD:-ChangeMe_Root_2026}' ${MYSQL_CONTAINER} mysql -uroot --default-character-set=utf8mb4 ${MYSQL_DB}"
    fi
}

cmd_list_backups() {
    log_step "远端备份列表"
    remote_exec "ls -lh '${REMOTE_BACKUP_DIR}'/blog-*.sql.gz 2>/dev/null || echo '(无备份)'"
}

cmd_restore() {
    local name="${1:-}"
    [[ -z "${name}" ]] && die "用法：db restore <备份文件名，如 blog-20260606_010101.sql.gz>"
    log_step "从备份恢复：${name}"
    log_warn "当前 blog 库的数据将被备份内容覆盖！"
    confirm "确认继续？" "n" || { log_warn "已取消"; exit 0; }
    ensure_container

    # 恢复前再做一次"恢复前"备份，便于二次回滚
    log_info "恢复前先做一次保险备份"
    cmd_backup

    remote_exec "
set -e
$(remote_mysql_env)
target='${REMOTE_BACKUP_DIR}/${name}'
[ -f \"\$target\" ] || { echo '[ERROR] 备份文件不存在：'\"\$target\" >&2; exit 1; }
echo '解压并执行恢复...'
gunzip -c \"\$target\" | docker exec -i -e MYSQL_PWD=\"\$MYSQL_ROOT_PASSWORD\" ${MYSQL_CONTAINER} \
    mysql -uroot --default-character-set=utf8mb4 \"\$MYSQL_DB\"
echo '[OK] 恢复完成'
"
    log_ok "已从 ${name} 恢复"
}

case "${ACTION}" in
    backup)         cmd_backup ;;
    schema)         cmd_schema ;;
    data)           cmd_data ;;
    reset)          cmd_reset ;;
    file)           cmd_file "$@" ;;
    shell)          cmd_shell ;;
    list-backups)   cmd_list_backups ;;
    restore)        cmd_restore "$@" ;;
    -h|--help|"")
        sed -n '2,30p' "$0"
        ;;
    *) die "未知子命令：${ACTION}（backup|schema|data|reset|file|shell|list-backups|restore）" ;;
esac
