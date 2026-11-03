import http from 'k6/http';
import exec from 'k6/execution';
import { BASE_URL, COMMODITY_ID, MAX_VUS, rampStages, thresholds } from './config.js';
import { classifyBuy, purchased } from './lib/checks.js';

export const options = {
  scenarios: {
    buy_spike: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 200,
      maxVUs: MAX_VUS,
      stages: rampStages,
    },
  },
  thresholds,
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  // The shared iteration counter gives each request a distinct userId.
  const res = http.get(`${BASE_URL}/commodity/buy/1/${COMMODITY_ID}`, {
    tags: { name: 'buy' },
  });
  classifyBuy(res);
}

export function handleSummary(data) {
  const bought = data.metrics.purchased ? data.metrics.purchased.values.count : 0;
  const stock = parseInt(__ENV.STOCK || '1000');
  console.log(`purchased=${bought} stock=${stock} oversold=${bought > stock}`);
  return { 'results/buy_spike.json': JSON.stringify(data, null, 2) };
}
