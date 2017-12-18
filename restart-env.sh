#!/usr/bin/env bash

docker-compose --project-name wksp down

docker volume rm wksp_postgres-data

docker-compose --project-name wksp up -d

echo 'Use to check logs:
docker-compose --project-name wksp logs -f
'
