// Importa la función defineConfig de Vite para configurar el proyecto.
import { defineConfig } from 'vite'

// Importa el plugin oficial de React para que Vite pueda procesar archivos .jsx.
import react from '@vitejs/plugin-react'

// Exporta la configuración de Vite.
export default defineConfig({
  plugins: [react()], // Activa el plugin de React.
  server: {
    port: 3000, // El servidor de desarrollo correrá en el puerto 3000.
  },
  // Polyfill para 'global' — sockjs-client fue diseñado para Node.js y
  // usa la variable global que no existe en el navegador con ESModules.
  define: {
    global: 'globalThis',
  },
})
