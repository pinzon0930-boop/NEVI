import { api } from './api.js'

// ============================================================
// FUNCIÓN: obtenerActividades
// GET /api/actividades?grupoId={grupoId}
// ============================================================
export async function obtenerActividades(grupoId) {
  try {
    const data = await api.get(`/api/actividades?grupoId=${grupoId}`)
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: crearActividad
// POST /api/actividades
// ============================================================
export async function crearActividad(grupoId, titulo, descripcion, fechaEntrega) {
  try {
    const data = await api.post('/api/actividades', {
      grupoId,
      titulo,
      descripcion: descripcion || null,
      fechaEntrega: fechaEntrega
        ? new Date(fechaEntrega).toISOString().slice(0, 19) // "2025-12-31T23:59:00"
        : null,
    })
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: entregarActividad
// POST /api/actividades/{id}/entregar
// ============================================================
export async function entregarActividad(actividadId) {
  try {
    await api.post(`/api/actividades/${actividadId}/entregar`)
    return { data: { activity_id: actividadId }, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: obtenerMisEntregas
// GET /api/actividades/mis-entregas
// Devuelve un array de IDs (UUID string) de actividades ya entregadas.
// ============================================================
export async function obtenerMisEntregas() {
  try {
    const ids = await api.get('/api/actividades/mis-entregas')
    // Convierte al formato que el frontend esperaba: [{ activity_id: "..." }]
    const data = ids.map(id => ({ activity_id: id }))
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}
