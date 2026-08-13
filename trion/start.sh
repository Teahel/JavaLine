#!/usr/bin/env bash

set -u

SERVER_NAME="trion"
SCRIPT_DIR="$(cd -P "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="${SCRIPT_DIR}/${SERVER_NAME}.jar"
PID_FILE="${SCRIPT_DIR}/${SERVER_NAME}.pid"

LOG_HOME="${LOG_HOME:-/home/logs/${SERVER_NAME}}"
LOG_PATH="${LOG_HOME}/${SERVER_NAME}-info.log"
ACTIVE="${SPRING_PROFILES_ACTIVE:-pro}"
JVM_OPTS="${JVM_OPTS:--Xms1024m -Xmx2048m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8}"

if [ ! -f "${JAR_PATH}" ]; then
    echo "JAR 不存在: ${JAR_PATH}"
    echo "请先执行 mvn clean package -Ppro -DskipTests，并将 target/${SERVER_NAME}.jar 放到脚本所在目录。"
    exit 1
fi

if [ -f "${PID_FILE}" ]; then
    PID="$(cat "${PID_FILE}")"
    if kill -0 "${PID}" 2>/dev/null; then
        echo "${SERVER_NAME} 已经运行，PID: ${PID}"
        exit 1
    fi
    rm -f "${PID_FILE}"
fi

if ! mkdir -p "${LOG_HOME}"; then
    echo "无法创建日志目录: ${LOG_HOME}"
    exit 1
fi

echo "正在启动 ${SERVER_NAME}..."
nohup java ${JVM_OPTS} \
    -jar "${JAR_PATH}" \
    --spring.profiles.active="${ACTIVE}" \
    >>"${LOG_PATH}" 2>&1 &

PID=$!
echo "${PID}" > "${PID_FILE}"

sleep 2

if kill -0 "${PID}" 2>/dev/null; then
    echo "${SERVER_NAME} 启动成功"
    echo "PID: ${PID}"
    echo "运行环境: ${ACTIVE}"
    echo "日志文件: ${LOG_PATH}"
else
    echo "${SERVER_NAME} 启动失败，请查看日志: ${LOG_PATH}"
    rm -f "${PID_FILE}"
    exit 1
fi
