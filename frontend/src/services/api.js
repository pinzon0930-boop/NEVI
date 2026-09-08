// ============================================================
// CLIENTE HTTP CENTRALIZADO
// ============================================================
// Reemplaza al cliente de Supabase. Todas las llamadas al backend
// pasan por aquí. Si el token está guardado, se adjunta automáticamente.
// ============================================================

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// Realiza una petición HTTP al backend con JSON.
// Adjunta el token JWT si existe en localStorage.
async function request(path, options = {}) {
  const token = localStorage.getItem('nevi_token')

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  })

  // Si el servidor devuelve error HTTP, extraemos el mensaje del cuerpo JSON.
  if (!res.ok) {
    let mensaje = `Error ${res.status}`
    try {
      const body = await res.json()
      mensaje = body.error || mensaje
    } catch (_) {}
    throw new Error(mensaje)
  }

  // Respuestas 204 No Content no tienen cuerpo.
  if (res.status === 204) return null

  return res.json()
}

export const api = {
  get:    (path)         => request(path, { method: 'GET' }),
  post:   (path, body)   => request(path, { method: 'POST',  body: JSON.stringify(body) }),
  put:    (path, body)   => request(path, { method: 'PUT',   body: JSON.stringify(body) }),
  delete: (path)         => request(path, { method: 'DELETE' }),
}
