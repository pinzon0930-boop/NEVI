## Descripción del cambio

<!-- ¿Qué documento o funcionalidad modifica este PR? -->

**Documento / componente:**  
**Rama de origen:**  

---

## Decisión arquitectónica aplicada

<!-- ¿Qué driver o riesgo del dossier motiva este cambio? (ej. DRIVER-01 Seguridad, R-02) -->

**Driver / Riesgo relacionado:**  
**Decisión tomada:**  

---

## Evidencia empírica

<!-- Describe la prueba, medición o verificación que respalda esta decisión. -->

| Tipo de evidencia | Resultado |
|---|---|
| Prueba automatizada | ✅ / ❌ |
| Revisión de código | — |
| Prueba manual | — |

---

## Trade-off aceptado

<!-- ¿Qué se sacrifica para obtener el beneficio de este cambio? -->

**Ganancia:**  
**Costo:**  

---

## Checklist

- [ ] El sistema levanta con `docker-compose up --build` sin errores
- [ ] El frontend es accesible en `http://localhost`
- [ ] La API responde en `http://localhost:8080`
- [ ] No hay push directo a `main` — este cambio llega solo por PR
- [ ] El dossier refleja el estado actual del sistema
- [ ] No se suben credenciales ni archivos `.env` reales
- [ ] Si se modifica un endpoint, el frontend sigue funcionando sin cambios
