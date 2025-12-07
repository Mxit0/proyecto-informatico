// test_conexion_real.js

// Importa dotenv para cargar las variables de entorno
import 'dotenv/config'; 

// Importa el servicio que quieres probar. 
// Ajusta la ruta si el archivo está en otro lugar (ej: './services/compatibilityService.js')
import compatibilityService from './services/compatibilityService.js';

// Importa el repositorio para poder reemplazar la función de búsqueda de productos con datos de prueba.
import productRepository from './repositories/productRepository.js'; 


// --- 1. DATOS DE PRUEBA EN MEMORIA (Mock del Repositorio) ---

const REAL_PRODUCT_DATA = {
    // Componentes de prueba compatibles (i9 y Placa Z790, ambos LGA 1700)
    cpu: { id: 99, nombre: "Intel Core i9-14900K", categoria: "procesador", especificaciones: { socket: "LGA 1700", tdp: "125W" } },
    placa: { id: 100, nombre: "ASUS ROG STRIX Z790-E GAMING WIFI II", categoria: "placa_madre", especificaciones: { socket: "LGA 1700", chipset: "Z790" } },
};

// Sobreescribimos la función del objeto importado para devolver nuestros datos de prueba en lugar de llamar a la DB real.
productRepository.getProductById = async (productId) => {
    switch (productId) {
        case 99:
            return REAL_PRODUCT_DATA.cpu;
        case 100:
            return REAL_PRODUCT_DATA.placa;
        default:
            // Es importante devolver un valor para evitar fallos si se llama con otro ID
            return { nombre: "Producto Desconocido", categoria: "Otro", especificaciones: {} };
    }
};

// ----------------------------------------------------
// --- 2. FUNCIÓN DE PRUEBA DE INTEGRACIÓN ---
// ----------------------------------------------------

async function runRealIntegrationTest() {
    console.log("==================================================");
    console.log("🚀 Iniciando prueba de conexión real con Gemini API...");
    console.log("==================================================");
    
    if (!process.env.GEMINI_API_KEY) {
        console.error("❌ ERROR: La variable de entorno GEMINI_API_KEY no está configurada.");
        return;
    }

    const cartItems = [{ product_id: 99 }, { product_id: 100 }];
    
    try {
        console.log(`🔍 Llamando a checkCompatibility con IDs de prueba...`);
        
        const startTime = Date.now();
        // Llama a tu servicio, que internamente usará los datos mockeados y luego llamará a la API real.
        const result = await compatibilityService.checkCompatibility(cartItems);
        const endTime = Date.now();
        
        console.log("\n==================================================");
        console.log("✅ PRUEBA EXITOSA: Respuesta REAL de Gemini recibida.");
        console.log(`⏱️ Tiempo de respuesta: ${((endTime - startTime) / 1000).toFixed(2)} segundos`);
        console.log("==================================================");
        
        // Imprimir el resultado para verificar el formato JSON y la autenticación
        console.log(JSON.stringify(result, null, 2));

    } catch (error) {
        console.error("\n❌ PRUEBA CRÍTICA FALLIDA: Falla de Conexión, Autenticación o formato.");
        console.error("Si el error indica un fallo en la API, revisa tu GEMINI_API_KEY.");
        console.error("Detalles del error:", error.message);
    }
}

// Ejecutar la función principal
runRealIntegrationTest();