# Reservas Frontend

Proyecto frontend minimal para consumir el backend de reservas.

Requisitos:
- Node 18+

Instalación y desarrollo:

1. Entrar en la carpeta del frontend:

   cd frontend

2. Instalar dependencias:

   npm install

3. Iniciar en modo desarrollo (Vite):

   npm run dev

Notas:
- El dev server de Vite está configurado para proxear peticiones a /api hacia http://localhost:8080. Asegúrate de tener tu backend Spring Boot corriendo en ese puerto.
- Variables de entorno: .env.development tiene VITE_API_BASE=/api
- Para producción se puede buildar con `npm run build` y servir los archivos estáticos con cualquier servidor o copiarlos a Spring Boot (src/main/resources/static).
