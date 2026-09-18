/**
 * NOVI (NEVI) — Script de medición baseline
 * Escenario: GET /api/grupos con 10 VUs durante 60 s
 * Relacionado con: ESC-02, QA-02
 *
 * Uso:
 *   export NEVI_TOKEN="<jwt>"
 *   export BASE_URL="http://localhost:8080"
 *   k6 run script-grupos.js --out json=corrida-01.json
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  vus: 10,
  duration: '60s',
  thresholds: {
    // Criterio de éxito de ESC-02
    http_req_duration: ['p(95)<800'],
    errors: ['rate<0.05'],
  },
};

const TOKEN = __ENV.NEVI_TOKEN;
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${BASE_URL}/api/grupos`, {
    headers: {
      Authorization: `Bearer ${TOKEN}`,
      'Content-Type': 'application/json',
    },
  });

  const success = check(res, {
    'HTTP 200': (r) => r.status === 200,
    'respuesta es JSON': (r) => r.headers['Content-Type'] &&
      r.headers['Content-Type'].includes('application/json'),
    'latencia < 800ms': (r) => r.timings.duration < 800,
  });

  errorRate.add(!success);
  sleep(0.5);
}
