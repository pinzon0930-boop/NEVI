# ============================================================
# DOCKERFILE — NEVI Frontend
# ============================================================
# Construcción en dos etapas (multi-stage build):
#   Etapa 1 (builder): compila la app React con Node.js.
#   Etapa 2 (nginx):   sirve los archivos estáticos con Nginx.
# ============================================================

# ---- ETAPA 1: Compilar la app React ----
FROM node:18-alpine AS builder

WORKDIR /app

# Declarar argumentos de build — se pasan desde docker-compose.yml.
# Vite reemplaza import.meta.env.VITE_* con estos valores durante el build.
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

ARG VITE_WS_URL
ENV VITE_WS_URL=$VITE_WS_URL

ARG VITE_GROQ_API_KEY
ENV VITE_GROQ_API_KEY=$VITE_GROQ_API_KEY

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# ---- ETAPA 2: Servir los archivos con Nginx ----
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
