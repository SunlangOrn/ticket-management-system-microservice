#!/bin/bash
set -e

create_service_db() {
  local db=$1 user=$2 pass=$3
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER $user WITH PASSWORD '$pass';
    CREATE DATABASE $db OWNER $user;
    REVOKE ALL ON DATABASE $db FROM PUBLIC;
EOSQL
}

create_service_db event_db  event_user  "${EVENT_DB_PASSWORD:-event_pass}"
create_service_db ticket_db ticket_user "${TICKET_DB_PASSWORD:-ticket_pass}"
create_service_db user_db user_user "${USER_DB_PASSWORD:-user_pass}"
create_service_db order_db order_user "${ORDER_DB_PASSWORD:-order_pass}"
create_service_db payment_db payment_user "${PAYMENT_DB_PASSWORD:-payment_pass}"