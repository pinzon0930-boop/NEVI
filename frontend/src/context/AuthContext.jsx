import React, { createContext, useContext, useEffect, useState } from 'react'
import { obtenerSesionLocal, obtenerPerfil, cerrarSesion as cerrarSesionApi } from '../services/auth.js'

// ============================================================
// CONTEXTO DE AUTENTICACIÓN — Sin Supabase
// ============================================================
// Funciona igual que antes para el resto de la app.
// En lugar de Supabase Auth, lee el JWT del localStorage
// y consulta el perfil al backend propio.
// ============================================================

const AuthContext = createContext(null)

export function AuthProvider({ children }) {

  // "usuario" guarda el objeto con id, email, name, role del usuario autenticado.
  const [usuario, setUsuario] = useState(null)

  // "perfil" es el mismo objeto (en este sistema usuario === perfil, sin Supabase Auth).
  const [perfil, setPerfil] = useState(null)

  // "cargando" evita que las rutas se rendericen antes de saber si hay sesión.
  const [cargando, setCargando] = useState(true)

  useEffect(() => {
    // Al montar la app, revisa si hay un token guardado en localStorage.
    const sesionLocal = obtenerSesionLocal()

    if (sesionLocal) {
      // Si hay sesión local, la restaura de inmediato para evitar parpadeo.
      setUsuario(sesionLocal)
      setPerfil(sesionLocal)

      // Verifica la sesión con el servidor (por si el token expiró).
      obtenerPerfil().then(({ data, error }) => {
        if (error || !data) {
          // El token expiró o es inválido — limpia la sesión.
          setUsuario(null)
          setPerfil(null)
          localStorage.removeItem('nevi_token')
          localStorage.removeItem('nevi_user')
        } else {
          // Actualiza con los datos frescos del servidor.
          setUsuario(data)
          setPerfil(data)
        }
        setCargando(false)
      })
    } else {
      // No hay sesión guardada.
      setCargando(false)
    }
  }, [])

  // Llamado después del login/registro exitoso para actualizar el contexto.
  function establecerUsuario(userData) {
    setUsuario(userData)
    setPerfil(userData)
  }

  // Cierra la sesión y limpia el estado.
  async function cerrarSesion() {
    await cerrarSesionApi()
    setUsuario(null)
    setPerfil(null)
  }

  const valor = {
    usuario,
    perfil,
    cargando,
    establecerUsuario, // Usado en Login.jsx y Register.jsx tras autenticarse.
    cerrarSesion,      // Usado en Dashboard.jsx y la navbar.
  }

  if (cargando) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="text-4xl mb-4">🎓</div>
          <p className="text-gray-500">Cargando...</p>
        </div>
      </div>
    )
  }

  return (
    <AuthContext.Provider value={valor}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
