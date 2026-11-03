# Load test: flash-sale buy path

k6 scripts that hammer `GET /commodity/buy/{userId}/{commodityId}` (Redis stock deduction -> RocketMQ -> async DB insert) and verify there is no overselling.

## Prerequisites
- [k6](https://k6.io/docs/get-started/installation/) on the load generator. Run it from EC2 in the same region as the ALB for credible numbers.
- `mysql` and `redis-cli` on a host inside the VPC for the seed and verify scripts.

## Run
```bash
export DB_HOST=... DB_USER=root DB_PASSWORD=... REDIS_HOST=...
export COMMODITY_ID=1001 STOCK=1000

# 1. Reset stock and old orders
./scripts/seed_stock.sh

# 2. Load test (k6 prints "purchased=N")
k6 run -e BASE_URL=http://<alb-dns> -e STOCK=$STOCK -e COMMODITY_ID=$COMMODITY_ID \
       -e PEAK_RPS=500 k6/buy_spike.js

# 3. Check correctness and measure queue drain lag
PURCHASED=<purchased from k6> ./scripts/verify.sh
```
`k6/mixed.js` runs the same ramp with 60% buy, 25% item detail, 15% search. Re-run `seed_stock.sh` between runs.

## Parameters (env vars)
| Var | Default | Meaning |
|---|---|---|
| `BASE_URL` | `http://localhost:8080` | ALB URL |
| `COMMODITY_ID` | `1001` | Hot item |
| `STOCK` | `1000` | Seeded stock; `purchased` must never exceed it |
| `PEAK_RPS` | `500` | Peak arrival rate (ramps 10% -> 40% -> 100%) |
| `MAX_VUS` | `1000` | VU ceiling for the arrival-rate executor |

Thresholds (`k6/config.js`): failed requests < 1%, p95 < 500 ms, p99 < 1 s, `purchased <= STOCK`.

## What to report
- Sustained req/s and p95/p99 (`http_req_duration`) from the k6 summary.
- `purchased` == `STOCK` and `sold_out` for the rest (a sold-out response is still HTTP 200).
- `verify.sh`: orders in DB == `purchased`, Redis stock 0, and `queue_drain_seconds_after_k6`.

Results are written to `results/` (gitignored).
