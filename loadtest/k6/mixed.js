import http from 'k6/http';
import exec from 'k6/execution';
import { BASE_URL, COMMODITY_ID, MAX_VUS, rampStages, thresholds } from './config.js';
import { classifyBuy } from './lib/checks.js';

export const options = {
  scenarios: {
    mixed: {
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

const KEYWORDS = ['phone', 'laptop', 'shirt', 'book'];

export default function () {
  const roll = Math.random();
  if (roll < 0.6) {
    const userId = 1000 + exec.scenario.iterationInTest;
    classifyBuy(http.get(`${BASE_URL}/commodity/buy/${userId}/${COMMODITY_ID}`, { tags: { name: 'buy' } }));
  } else if (roll < 0.85) {
    http.get(`${BASE_URL}/item/${COMMODITY_ID}`, { tags: { name: 'item_detail' } });
  } else {
    const kw = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
    http.get(`${BASE_URL}/search?desc=${kw}`, { tags: { name: 'search' } });
  }
}

export function handleSummary(data) {
  return { 'results/mixed.json': JSON.stringify(data, null, 2) };
}
