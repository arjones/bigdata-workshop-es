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

  To check logs use:
  docker-compose --project-name wksp logs -f


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

  * )
  printf "ERROR: Missing command\n  Usage: `basename $0` (start|stop|cleanup)\n"
  exit 1
    ;;
esac
