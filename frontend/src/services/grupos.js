import { api } from './api.js'

// ============================================================
// FUNCIÓN: crearGrupo
// POST /api/grupos
// Solo profesores. El backend genera el código de acceso.
// ============================================================
export async function crearGrupo(nombre, descripcion) {
  try {
    const data = await api.post('/api/grupos', { nombre, descripcion })
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: obtenerMisGrupos
// GET /api/grupos
// El backend sabe el usuario por el JWT y devuelve sus grupos según el rol.
// ============================================================
export async function obtenerMisGrupos() {
  try {
    const data = await api.get('/api/grupos')
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: unirseAGrupo
// POST /api/grupos/unirse
// El estudiante se une con el código de 6 caracteres.
// ============================================================
export async function unirseAGrupo(codigo) {
  try {
    const grupo = await api.post('/api/grupos/unirse', { codigo })
    return { data: {}, grupo, error: null }
  } catch (err) {
    return { data: null, grupo: null, error: { message: err.message } }
  }
}
