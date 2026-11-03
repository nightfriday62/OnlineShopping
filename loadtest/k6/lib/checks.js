import { Counter } from 'k6/metrics';
import { check } from 'k6';

export const purchased = new Counter('purchased');
export const soldOut = new Counter('sold_out');
export const unexpected = new Counter('unexpected');

// Sold-out responses are still HTTP 200; only the page body differs.
export function classifyBuy(res) {
  const ok = check(res, { 'status is 200': (r) => r.status === 200 });
  if (!ok) {
    unexpected.add(1);
    return 'error';
  }
  if (res.body.includes('Order created successfully')) {
    purchased.add(1);
    return 'purchased';
  }
  if (res.body.includes('Order creation failed')) {
    soldOut.add(1);
    return 'sold_out';
  }
  unexpected.add(1);
  return 'unexpected';
}
