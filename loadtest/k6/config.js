export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const COMMODITY_ID = __ENV.COMMODITY_ID || '1001';
export const STOCK = parseInt(__ENV.STOCK || '1000');
export const PEAK_RPS = parseInt(__ENV.PEAK_RPS || '500');
export const MAX_VUS = parseInt(__ENV.MAX_VUS || '1000');

export const thresholds = {
  http_req_failed: ['rate<0.01'],
  http_req_duration: ['p(95)<500', 'p(99)<1000'],
  purchased: [`count<=${STOCK}`],
};

export const rampStages = [
  { target: Math.round(PEAK_RPS * 0.1), duration: '30s' },
  { target: Math.round(PEAK_RPS * 0.4), duration: '60s' },
  { target: PEAK_RPS, duration: '60s' },
  { target: PEAK_RPS, duration: '30s' },
  { target: 0, duration: '10s' },
];
