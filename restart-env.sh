#!/usr/bin/env bash

docker-compose --project-name wksp down

docker volume rm postgres-data

docker-compose --project-name wksp up -d

echo 'Use to check logs:
docker-compose --project-name wksp logs -f
'
