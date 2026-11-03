#!/usr/bin/env bash
# Resets the hot item's stock in MySQL and Redis and clears orders from earlier runs.
# Run from a host inside the VPC (bastion, EC2 load generator, or SSM session).
# Required env: DB_HOST DB_USER DB_PASSWORD REDIS_HOST. Optional: COMMODITY_ID STOCK
set -euo pipefail
: "${DB_HOST:?}" "${DB_USER:?}" "${DB_PASSWORD:?}" "${REDIS_HOST:?}"
COMMODITY_ID="${COMMODITY_ID:-1001}"
STOCK="${STOCK:-1000}"
CLI="redis-cli"
if ! command -v redis-cli >/dev/null 2>&1 && command -v valkey-cli >/dev/null 2>&1; then
  CLI="valkey-cli"
fi
mysql_cmd() { mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$@"; }

mysql_cmd online_shopping -e "
  INSERT INTO online_shopping_commodity
    (commodity_id, commodity_name, commodity_desc, price, available_stock, creator_user_id, total_stock, lock_stock)
  VALUES ($COMMODITY_ID, 'loadtest-item', 'loadtest', 100, $STOCK, 1, $STOCK, 0)
  ON DUPLICATE KEY UPDATE available_stock=$STOCK, total_stock=$STOCK, lock_stock=0;"

for db in online_shopping_1 online_shopping_2; do
  for t in 1 2; do
    mysql_cmd "$db" -e "DELETE FROM online_shopping_order_$t WHERE commodity_id=$COMMODITY_ID;"
  done
done

$CLI -h "$REDIS_HOST" SET "commodity_stock_$COMMODITY_ID" "$STOCK" >/dev/null
echo "Seeded commodity $COMMODITY_ID with stock $STOCK"
