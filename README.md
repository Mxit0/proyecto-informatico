# 💻 Plataforma de Compraventa de Electrónicos de Segunda Mano

Este proyecto consiste en el desarrollo de un **Marketplace móvil** especializado en la **compraventa de productos electrónicos usados**, con énfasis en la **seguridad transaccional** y la **compatibilidad de piezas de PC**.

📦 **Repositorio GitHub:** [https://github.com/Mxit0/proyecto-informatico](https://github.com/Mxit0/proyecto-informatico)

---

## 🚨 Prerrequisitos (Entorno de Desarrollo)

Para configurar y ejecutar la aplicación de forma local, asegúrate de tener instalado el siguiente software:

| Herramienta        | Versión Requerida | Enlace de Descarga                                                          |
| ------------------ | ----------------- | --------------------------------------------------------------------------- |
| **Git**            | Última            | [Descargar Git](https://git-scm.com/downloads)                              |
| **Node.js**        | Última LTS        | [Descargar Node.js](https://nodejs.org/)                                    |
| **Android Studio** | Última            | [Descargar Android Studio](https://developer.android.com/studio)            |
| **Java JDK**       | Version 11 o superior (creo) |         |

---

## ⚙️ Arquitectura del Proyecto (Monorepo)

| Módulo             | Tecnología Principal    | Propósito                                                                  |
| ------------------ | ----------------------- | -------------------------------------------------------------------------- |
| **frontend-app/**  | Kotlin (Android Nativo) | Cliente móvil único para Usuarios, Vendedores y Administradores.           |
| **backend/**       | Node.js (Express)       | API REST para lógica de negocio, autenticación y gestión de transacciones. |
| **Base de Datos**  | Supabase (PostgreSQL)   | Servicio en la nube para persistencia y autenticación.                     |

---

## 🛠️ Guía de Configuración Local

### A. Clonar el Repositorio

```bash
git clone https://github.com/Mxit0/proyecto-informatico
cd proyecto-informatico
```

### B. Instalar Dependencias

Instalar dependencias del backend:

```bash
cd backend
npm install
cd ..
```

### C. Configuración de Variables de Entorno

Las claves de seguridad (**Supabase URL**, **JWT Secret**, etc.) **no están incluidas** en el repositorio.

Crea un archivo `.env` dentro de la carpeta `backend/` y añade las variables de entorno proporcionadas por el archivo `.env` original.

Esto construirá y levantará el entorno de desarrollo local.

---

## 🧪 Verificación y Pruebas Iniciales

Una vez que Docker muestre el mensaje **"Servidor de API corriendo..."**, verifica los siguientes puntos en tu navegador:

| Prueba            | URL                                                                              | Función Verificada                                                                |
| ----------------- | -------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| **API Online**    | [http://localhost:3000/](http://localhost:3000/)                                 | Confirma que el contenedor de Node.js está activo.                                |
| **Conexión a DB** | [http://localhost:3000/api/v1/db-status](http://localhost:3000/api/v1/db-status) | **CRÍTICO:** Asegura que la API se conecta correctamente a Supabase (PostgreSQL). |

---
