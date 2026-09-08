import { api } from './api.js'

// ============================================================
// FUNCIÓN: registrarUsuario
// POST /api/auth/register
// Registra un nuevo usuario y guarda el token en localStorage.
// ============================================================
export async function registrarUsuario(email, password, name, role) {
  try {
    const data = await api.post('/api/auth/register', { email, password, name, role })
    // Guarda el token para que api.js lo adjunte en futuras peticiones.
    localStorage.setItem('nevi_token', data.token)
    localStorage.setItem('nevi_user', JSON.stringify({
      id: data.id, email: data.email, name: data.name, role: data.role
    }))
    return { data }
  } catch (err) {
    return { error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: iniciarSesion
// POST /api/auth/login
// ============================================================
export async function iniciarSesion(email, password) {
  try {
    const data = await api.post('/api/auth/login', { email, password })
    localStorage.setItem('nevi_token', data.token)
    localStorage.setItem('nevi_user', JSON.stringify({
      id: data.id, email: data.email, name: data.name, role: data.role
    }))
    return { data }
  } catch (err) {
    return { error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: cerrarSesion
// Limpia el token y los datos del usuario del localStorage.
// ============================================================
export async function cerrarSesion() {
  localStorage.removeItem('nevi_token')
  localStorage.removeItem('nevi_user')
  return { error: null }
}


// ============================================================
// FUNCIÓN: obtenerPerfil
// GET /api/auth/perfil
// Devuelve los datos del usuario autenticado desde el servidor.
// ============================================================
export async function obtenerPerfil() {
  try {
    const data = await api.get('/api/auth/perfil')
    return { data }
  } catch (err) {
    return { error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: obtenerSesionLocal
// Lee el usuario guardado en localStorage (sin llamar al servidor).
// Usado por AuthContext para restaurar la sesión al recargar la página.
// ============================================================
export function obtenerSesionLocal() {
  try {
    const token = localStorage.getItem('nevi_token')
    const user  = localStorage.getItem('nevi_user')
    if (!token || !user) return null
    return JSON.parse(user)
  } catch (_) {
    return null
  }
}
