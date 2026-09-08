import { api } from './api.js'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'

// ============================================================
// FUNCIÓN: obtenerMensajes
// GET /api/mensajes?grupoId={grupoId}
// Devuelve los últimos 50 mensajes del grupo.
// ============================================================
export async function obtenerMensajes(grupoId) {
  try {
    const data = await api.get(`/api/mensajes?grupoId=${grupoId}`)
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: enviarMensaje
// POST /api/mensajes
// Envía un mensaje al backend vía REST.
// El backend lo persiste y también lo difunde vía WebSocket.
// ============================================================
export async function enviarMensaje(grupoId, _userId, contenido) {
  // Nota: _userId ya no es necesario — el backend lo extrae del JWT.
  try {
    const data = await api.post('/api/mensajes', { grupoId, contenido })
    return { data, error: null }
  } catch (err) {
    return { data: null, error: { message: err.message } }
  }
}


// ============================================================
// FUNCIÓN: suscribirseAMensajes
// Reemplaza la suscripción de Supabase Realtime.
// Crea un cliente STOMP sobre WebSocket y se suscribe al topic del grupo.
// Cuando llega un mensaje nuevo, llama a onNuevoMensaje(mensaje).
// Devuelve el cliente STOMP (para desconectarse después).
// ============================================================
export function suscribirseAMensajes(grupoId, onNuevoMensaje) {
  const token = localStorage.getItem('nevi_token')

  const client = new Client({
    // SockJS proporciona compatibilidad con navegadores sin WebSocket nativo.
    webSocketFactory: () => new SockJS(WS_URL),

    // Cabeceras de conexión — el token JWT autentica la sesión WebSocket.
    connectHeaders: {
      Authorization: token ? `Bearer ${token}` : '',
    },

    // Cuando la conexión se establece, nos suscribimos al topic del grupo.
    onConnect: () => {
      client.subscribe(`/topic/grupo/${grupoId}`, (frame) => {
        try {
          const mensaje = JSON.parse(frame.body)
          onNuevoMensaje(mensaje)
        } catch (e) {
          console.error('Error parseando mensaje WebSocket:', e)
        }
      })
    },

    // Log de errores de conexión.
    onStompError: (frame) => {
      console.error('Error STOMP:', frame.headers?.message)
    },

    // Reconexión automática si se pierde la conexión.
    reconnectDelay: 5000,
  })

  client.activate() // Inicia la conexión.
  return client     // Devuelve el cliente para poder desconectarse.
}


// ============================================================
// FUNCIÓN: desuscribirse
// Desconecta el cliente STOMP cuando el usuario sale del grupo.
// ============================================================
export function desuscribirse(client) {
  if (client && client.active) {
    client.deactivate()
  }
}
