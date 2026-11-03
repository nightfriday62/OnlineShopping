#!/usr/bin/env bash
# Checks for overselling and measures how long the consumer takes to persist all orders.
# Run after k6 finishes. Required env: DB_HOST DB_USER DB_PASSWORD REDIS_HOST.
# Optional: COMMODITY_ID STOCK PURCHASED (k6's "purchased" count) TIMEOUT_S
set -euo pipefail
: "${DB_HOST:?}" "${DB_USER:?}" "${DB_PASSWORD:?}" "${REDIS_HOST:?}"
COMMODITY_ID="${COMMODITY_ID:-1001}"
STOCK="${STOCK:-1000}"
TIMEOUT_S="${TIMEOUT_S:-60}"

CLI="redis-cli"
if ! command -v redis-cli >/dev/null 2>&1 && command -v valkey-cli >/dev/null 2>&1; then
  CLI="valkey-cli"
fi
mysql_cmd() { mysql -N -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$@"; }

order_count() {
  local total=0 n
  for db in online_shopping_1 online_shopping_2; do
    for t in 1 2; do
      n=$(mysql_cmd "$db" -e "SELECT COUNT(*) FROM online_shopping_order_$t WHERE commodity_id=$COMMODITY_ID;")
      total=$((total + n))
    done
  done
  echo "$total"
}

expected="${PURCHASED:-$STOCK}"
echo "Polling MySQL for orders (expected >= $expected, timeout: ${TIMEOUT_S}s)..."
start=$(date +%s)
orders=0
while :; do
  orders=$(order_count)
  elapsed=$(( $(date +%s) - start ))
  echo "[${elapsed}s] orders in DB: $orders / $expected"
  [ "$orders" -ge "$expected" ] && break
  [ "$elapsed" -ge "$TIMEOUT_S" ] && { echo "Timed out waiting for queue to drain."; break; }
  sleep 3
done
drain=$(( $(date +%s) - start ))

redis_stock=$($CLI -h "$REDIS_HOST" GET "commodity_stock_$COMMODITY_ID" || echo unknown)
db_stock=$(mysql_cmd online_shopping -e "SELECT available_stock FROM online_shopping_commodity WHERE commodity_id=$COMMODITY_ID;")

echo "----------------------------------------"
echo "orders_in_db=$orders expected=$expected stock_limit=$STOCK"
echo "redis_stock=$redis_stock db_available_stock=$db_stock"
echo "queue_drain_seconds_after_k6=$drain"

fail=0
[ "$orders" -le "$STOCK" ] || { echo "FAIL: oversold ($orders > $STOCK)"; fail=1; }
[ "$orders" -eq "$expected" ] || { echo "FAIL: orders in DB in DB ($orders) differ from expected ($expected)"; fail=1; }
[ "$db_stock" -ge 0 ] || { echo "FAIL: negative DB stock"; fail=1; }
[ "$fail" -eq 0 ] && echo "PASS"
exit "$fail"
