// test_full_integration.js

import 'dotenv/config'; // Carga las variables de entorno (.env)

// Importa el servicio (Ajusta la ruta si es necesario)
import compatibilityService from './services/compatibilityService.js';

// ----------------------------------------------------
// --- FUNCIÓN DE PRUEBA DE INTEGRACIÓN COMPLETA ---
// ----------------------------------------------------

async function runFullIntegrationTest() {
    console.log("==================================================");
    console.log("🚀 Iniciando prueba de INTEGRACIÓN COMPLETA (DB + Gemini)...");
    console.log("==================================================");
    
    if (!process.env.GEMINI_API_KEY) {
        console.error("❌ ERROR: La variable de entorno GEMINI_API_KEY no está configurada.");
        return;
    }
    
    // 🛑 ATENCIÓN: REEMPLAZA estos strings con IDs UUID REALES de tu tabla producto_compatibilidad.
    // Los IDs deben ser cadenas de texto.
    const REAL_CPU_ID = "1489e02e-8ae7-4b33-b05c-cf3320242455"; 
    const REAL_MB_ID = "0d00d8b4-4cb7-45b8-918f-0d14d5dbb524";

    const cartItems = [{ product_id: REAL_CPU_ID }, { product_id: REAL_MB_ID }];
    
    try {
        console.log(`🔍 1. Buscando productos con IDs ${REAL_CPU_ID} y ${REAL_MB_ID} en Supabase...`);
        console.log("🔍 2. Luego, llamando a la API de Gemini...");
        
        const startTime = Date.now();
        const result = await compatibilityService.checkCompatibility(cartItems);
        const endTime = Date.now();
        
        console.log("\n==================================================");
        console.log("✅ PRUEBA DE INTEGRACIÓN EXITOSA (Supabase y Gemini OK).");
        console.log(`⏱️ Tiempo total de respuesta: ${((endTime - startTime) / 1000).toFixed(2)} segundos`);
        console.log("==================================================");
        
        console.log(JSON.stringify(result, null, 2));

    } catch (error) {
        console.error("\n❌ PRUEBA CRÍTICA FALLIDA.");
        console.error("Detalles del error (Verifica si los IDs UUID son correctos o si la DB está accesible):", error.message);
    }
}

runFullIntegrationTest();