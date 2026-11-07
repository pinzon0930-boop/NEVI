# Prueba de Fuga de Datos — VITE_GROQ_API_KEY en Bundle Público

**Proyecto:** NEVI — Plataforma de aprendizaje colaborativo con IA  
**Módulo:** 7 — Seguridad  
**Semana:** 13  
**Versión:** 1.0 — 2026-11-07  
**Relacionado con:** EDAV-03-B, Riesgo R05, Control CRED-02

---

## 1. Descripción del escenario

NEVI usa la API de Groq (modelo `llama3-8b-8192`) para generar sugerencias
de actividades. La clave de API (`VITE_GROQ_API_KEY`) se pasa al proceso de
build de Vite como variable de entorno. Vite sustituye en tiempo de
compilación todas las referencias a `import.meta.env.VITE_*` con sus
valores literales, embebiendo el valor directamente en el bundle JavaScript
que se sirve públicamente.

Esto significa que **cualquier visitante** del sitio puede extraer la clave
de API inspeccionando el código fuente del frontend, sin necesidad de
autenticarse ni vulnerar ningún otro control.

---

## 2. Vector de ataque

```
Atacante                     NEVI Frontend (Nginx)
   │                                 │
   │  GET / (página inicial)         │
   ├────────────────────────────────►│
   │                                 │
   │  200 OK + index.html            │
   │◄────────────────────────────────┤
   │                                 │
   │  GET /assets/index-[hash].js    │
   ├────────────────────────────────►│
   │                                 │
   │  200 OK + bundle JS (público)   │
   │◄────────────────────────────────┤
   │                                 │
   │  grep "gsk_" bundle.js          │
   │  → gsk_Vn5sS...NDpeQDgnII      │
   │  (clave completa visible)       │
   │                                 │
   │  POST https://api.groq.com/...  │
   │  Authorization: Bearer gsk_...  │
   ├────────────────────────────────►│ Groq API
   │  (factura en la cuenta de NEVI) │
```

---

## 3. Pasos para reproducir

### Prerequisito

Tener Docker instalado y el repositorio NEVI clonado localmente.
La variable `VITE_GROQ_API_KEY` debe estar definida (en `.env` o como
variable de entorno del shell).

### Paso 1 — Construir la imagen del frontend

```bash
# Desde la raíz del repositorio NEVI
export VITE_GROQ_API_KEY="<valor-de-tu-archivo-.env>"

docker build \
  --build-arg VITE_GROQ_API_KEY="$VITE_GROQ_API_KEY" \
  -t nevi-frontend-test \
  -f frontend/Dockerfile \
  frontend/
```

Tiempo esperado: 60–120 segundos (descarga dependencias npm).

### Paso 2 — Extraer el bundle generado

```bash
# Crear contenedor temporal sin ejecutarlo
docker create --name nevi-fe-tmp nevi-frontend-test

# Copiar el directorio de assets al host
docker cp nevi-fe-tmp:/app/dist/assets ./nevi-dist-assets

# Eliminar el contenedor temporal
docker rm nevi-fe-tmp
```

### Paso 3 — Buscar la clave en el bundle

```bash
grep -r "gsk_" ./nevi-dist-assets/
```

### Resultado esperado (vulnerable)

```
./nevi-dist-assets/index-BxK9pQmR.js:...Authorization:"Bearer gsk_Vn5sS..."...
```

La clave aparece en texto plano dentro del JavaScript minificado. Cualquier
navegador puede acceder a este archivo sin autenticación.

### Resultado objetivo (corregido)

```
(sin salida)
```

Cuando el control esté implementado (llamadas a Groq desde el backend como
proxy), la clave no debe aparecer en ningún archivo bajo `dist/assets/`.

---

## 4. Impacto medido

| Dimensión | Valor |
|-----------|-------|
| Facilidad de explotación | Alta — requiere solo `curl` o DevTools |
| Acceso que otorga | Acceso completo a la API de Groq con la cuenta de NEVI |
| Impacto económico | Consumo ilimitado de tokens → factura inesperada |
| Impacto reputacional | Compromiso de cuenta de API del proyecto |
| DREAD score (R05) | Daño 3 + Reproducibilidad 3 + Explotabilidad 3 + Usuarios afectados 2 + Detectabilidad 2 = **13/15** |

---

## 5. Causa raíz técnica

**`docker-compose.yml` (líneas relevantes):**

```yaml
frontend:
  build:
    context: ./frontend
    args:
      - VITE_GROQ_API_KEY=${GROQ_API_KEY}   # ← build arg expone la clave
```

**`frontend/Dockerfile`:**

```dockerfile
ARG VITE_GROQ_API_KEY
ENV VITE_GROQ_API_KEY=$VITE_GROQ_API_KEY   # ← Vite la lee y la incrusta
RUN npm run build                           # ← aquí se embebe en el bundle
```

Vite reemplaza `import.meta.env.VITE_GROQ_API_KEY` con el valor literal
durante `npm run build`. Una vez en el bundle, el valor no puede ser
eliminado sin reconstruir la imagen.

---

## 6. Mitigación recomendada (control CRED-02)

**Corto plazo — rotación inmediata de la clave comprometida:**

```bash
# 1. Ir a console.groq.com → API Keys → Revocar clave actual
# 2. Generar nueva clave
# 3. Actualizar .env local (nunca en git)
# 4. Reconstruir imagen sin build arg
```

**Largo plazo — proxy IA en el backend:**

Eliminar `VITE_GROQ_API_KEY` del frontend completamente. Las peticiones
al modelo de IA pasarán por `POST /api/ia/sugerencia` en el backend Spring
Boot, que tiene la clave en variable de entorno del contenedor (no en el
bundle JS).

```
Antes:  Browser → Groq API (clave en bundle JS)
Después: Browser → Spring Boot /api/ia → Groq API (clave solo en servidor)
```

**Verificación post-corrección:**

```bash
# Debe devolver 0 líneas
grep -r "gsk_" frontend/dist/assets/ | wc -l
# Resultado esperado: 0
```

---

## 7. Estado actual del control

| Control | Estado | Fecha objetivo |
|---------|--------|----------------|
| Rotación de clave expuesta | ✅ Realizado (clave en `.env` local, no en git) | Inmediato |
| Eliminar `VITE_GROQ_API_KEY` del build arg | ❌ Pendiente | Sprint siguiente |
| Implementar proxy `/api/ia` en backend | ❌ Pendiente | Sprint siguiente |

La vulnerabilidad está documentada en `10-checklist-seguridad.md` como
control `CRED-02 ❌` y en la matriz de riesgos como R05 (Fuga de API key,
nivel Alto).
