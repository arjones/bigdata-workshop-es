#!/usr/bin/env bash

function stop {
  echo "Stopping and removing containers"
  docker-compose --project-name wksp down
}

function cleanup {
  echo "Removing volume"
  docker volume rm wksp_postgres-data
}

function start {
  echo "Starting up"
  docker-compose --project-name wksp up -d
}

function info {
  echo '
  Everything is ready, access http://localhost/ to learn more
  '
}

case $1 in
  start )
  start
  info
    ;;

  stop )
  stop
    ;;

  cleanup )
  stop
  cleanup
    ;;

  logs )
  docker-compose --project-name wksp logs -f
    ;;

  * )
  printf "ERROR: Missing command\n  Usage: `basename $0` (start|stop|cleanup|logs)\n"
  exit 1
    ;;
esac
