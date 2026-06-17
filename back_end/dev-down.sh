#!/bin/bash
cd "$(dirname "$0")"
docker-compose -f docker-compose.dev.yml down
echo "Dev MySQL + Redis stopped."
