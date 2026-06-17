#!/bin/bash
cd "$(dirname "$0")"
mkdir -p D:/data_center/dev/mysql D:/data_center/dev/redis
docker-compose -f docker-compose.dev.yml up -d
echo "Dev MySQL (3306) + Redis (6379) started. Data at D:/data_center/dev/"
