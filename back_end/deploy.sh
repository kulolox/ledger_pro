#!/bin/bash
set -e

if [ -z "$JAVA_HOME" ]; then
    JDK=$(ls -d "/c/Program Files/Java"/jdk-*/ 2>/dev/null | head -1)
    [ -n "$JDK" ] && export JAVA_HOME="${JDK%/}"
fi

if [ -f target/ledger-pro.jar ]; then
    echo "=== JAR 已存在，跳过构建 ==="
else
    echo "=== 打包 JAR ==="
    mvn clean package -DskipTests -q
fi

echo "=== 启动服务 ==="
docker compose up -d --build

echo "=== 日志 ==="
docker compose logs --tail=20

echo "=== 完成 ==="
