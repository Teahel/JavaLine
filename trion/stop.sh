#!/usr/bin/env bash

set -u

SERVER_NAME="trion"
SCRIPT_DIR="$(cd -P "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="${SCRIPT_DIR}/${SERVER_NAME}.pid"
STOP_TIMEOUT="${STOP_TIMEOUT:-30}"

if [ ! -f "${PID_FILE}" ]; then
    echo "${SERVER_NAME} 未运行：PID 文件不存在"
    exit 0
fi

PID="$(cat "${PID_FILE}")"

case "${PID}" in
    ''|*[!0-9]*)
        echo "PID 文件内容无效: ${PID_FILE}"
        exit 1
        ;;
esac

if ! kill -0 "${PID}" 2>/dev/null; then
    echo "${SERVER_NAME} 进程不存在，清理无效 PID 文件"
    rm -f "${PID_FILE}"
    exit 0
fi

echo "正在停止 ${SERVER_NAME}，PID: ${PID}"
kill "${PID}"

WAITED=0
while kill -0 "${PID}" 2>/dev/null && [ "${WAITED}" -lt "${STOP_TIMEOUT}" ]; do
    sleep 1
    WAITED=$((WAITED + 1))
done

if kill -0 "${PID}" 2>/dev/null; then
    echo "等待 ${STOP_TIMEOUT} 秒后进程仍未退出，执行强制停止"
    kill -9 "${PID}"
fi

rm -f "${PID_FILE}"
echo "${SERVER_NAME} 已停止"
