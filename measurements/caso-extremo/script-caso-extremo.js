/**
 * NEVI — Semilla 80/20 con caso extremo + medicion
 * Escenario: GET /api/grupos con 10 VUs durante 60 s
 * Relacionado con: ESC-02, QA-02 (extension del baseline en 01-preregistro.md)
 * Preregistro: docs/experiment/03-preregistro-caso-extremo.md
 *
 * Este script hace DOS cosas en una sola corrida de k6:
 *   1. setup(): siembra los datos (1 profesor, 70 grupos, 20 estudiantes con
 *      una distribucion sesgada 80/20 y un caso extremo) usando la propia API
 *      REST de NEVI. Se ejecuta UNA sola vez, antes de medir.
 *   2. default(): repite GET /api/grupos con el token del estudiante
 *      "extremo" (miembro de 65 de los 70 grupos), igual que el baseline
 *      original pero contra un usuario con muchas membresias.
 *
 * IMPORTANTE: requiere una base de datos limpia (docker compose down -v
 * && docker compose up -d) para evitar chocar con usuarios/codigos de
 * corridas anteriores (los emails de prueba son fijos).
 *
 * Uso:
 *   export BASE_URL="http://localhost:8080"
 *   k6 run script-caso-extremo.js --out json=corrida-01.json
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const PASSWORD = 'Carga2026!';
const TOTAL_GRUPOS = 70;

export const options = {
  vus: 10,
  duration: '60s',
  setupTimeout: '180s',
  thresholds: {
    errors: ['rate<0.05'],
  },
};

function registrar(email, name, role) {
  const res = http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email, password: PASSWORD, name, role,
  }), { headers: { 'Content-Type': 'application/json' } });
  if (res.status !== 200) {
    throw new Error(`No se pudo registrar ${email}: ${res.status} ${res.body}`);
  }
}

function login(email) {
  const res = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email, password: PASSWORD,
  }), { headers: { 'Content-Type': 'application/json' } });
  if (res.status !== 200) {
    throw new Error(`No se pudo iniciar sesion ${email}: ${res.status} ${res.body}`);
  }
  return res.json('token');
}

function crearGrupo(token, nombre) {
  const res = http.post(`${BASE_URL}/api/grupos`, JSON.stringify({
    nombre, descripcion: 'Semilla de carga 80/20 - caso extremo',
  }), { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } });
  if (res.status !== 200) {
    throw new Error(`No se pudo crear el grupo ${nombre}: ${res.status} ${res.body}`);
  }
  return res.json('accessCode');
}

function unirse(token, codigo) {
  const res = http.post(`${BASE_URL}/api/grupos/unirse`, JSON.stringify({ codigo }), {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  });
  if (res.status !== 200) {
    throw new Error(`No se pudo unir con el codigo ${codigo}: ${res.status} ${res.body}`);
  }
}

// Siembra los datos UNA sola vez, antes de que arranquen los VUs.
// Distribucion 80/20 (ver docs/experiment/03-preregistro-caso-extremo.md):
//   - 1 estudiante "extremo":  miembro de 65/70 grupos (65 membresias)
//   - 3 estudiantes "activos": miembros de 5 grupos cada uno (15 membresias)
//   - 16 estudiantes "normales": miembros de 1-2 grupos (20 membresias)
//   Total = 100 membresias. El 20% de los estudiantes (4 de 20) concentra
//   el 80% de las membresias (80/100); el estudiante extremo por si solo
//   concentra el 65% (el "caso extremo" dentro del 20%).
export function setup() {
  console.log('Sembrando datos (profesor + 70 grupos)...');
  registrar('profesor.carga@nevi-test.com', 'Profesor Carga', 'teacher');
  const tokenProfesor = login('profesor.carga@nevi-test.com');

  const accessCodes = [];
  for (let i = 1; i <= TOTAL_GRUPOS; i++) {
    accessCodes.push(crearGrupo(tokenProfesor, `Carga-${String(i).padStart(2, '0')}`));
  }

  console.log('Creando estudiante extremo (65 membresias)...');
  const emailExtremo = 'extremo.carga@nevi-test.com';
  registrar(emailExtremo, 'Estudiante Extremo', 'student');
  const tokenExtremo = login(emailExtremo);
  for (let i = 0; i < 65; i++) {
    unirse(tokenExtremo, accessCodes[i]);
  }

  console.log('Creando 3 estudiantes activos (5 membresias cada uno)...');
  for (let e = 1; e <= 3; e++) {
    const email = `activo${e}.carga@nevi-test.com`;
    registrar(email, `Estudiante Activo ${e}`, 'student');
    const token = login(email);
    for (let i = 65; i < 70; i++) {
      unirse(token, accessCodes[i]);
    }
  }

  console.log('Creando 16 estudiantes normales (1-2 membresias cada uno)...');
  for (let e = 1; e <= 16; e++) {
    const email = `normal${e}.carga@nevi-test.com`;
    registrar(email, `Estudiante Normal ${e}`, 'student');
    const token = login(email);
    const numGrupos = e <= 12 ? 1 : 2;
    for (let i = 0; i < numGrupos; i++) {
      unirse(token, accessCodes[i]);
    }
  }

  console.log('Semilla completa. Iniciando medicion contra el estudiante extremo.');
  return { token: tokenExtremo };
}

export default function (data) {
  const res = http.get(`${BASE_URL}/api/grupos`, {
    headers: {
      Authorization: `Bearer ${data.token}`,
      'Content-Type': 'application/json',
    },
  });

  const success = check(res, {
    'HTTP 200': (r) => r.status === 200,
    'devuelve 65 grupos': (r) => {
      try {
        return JSON.parse(r.body).length === 65;
      } catch (e) {
        return false;
      }
    },
  });

  errorRate.add(!success);
  sleep(0.5);
}
