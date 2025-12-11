// test_full_integration.js

import "dotenv/config"; // Carga las variables de entorno (.env)

// Importa el servicio (Ajusta la ruta si es necesario)
import compatibilityService from "./services/compatibilityService.js";
import fetch from "node-fetch";

// ----------------------------------------------------
// --- FUNCIÓN DE PRUEBA DE INTEGRACIÓN COMPLETA ---
// ----------------------------------------------------

async function runFullIntegrationTest() {
  console.log("==================================================");
  console.log("🚀 Iniciando prueba de INTEGRACIÓN COMPLETA (DB + Gemini)...");
  console.log("==================================================");

  if (!process.env.GEMINI_API_KEY) {
    console.error(
      "❌ ERROR: La variable de entorno GEMINI_API_KEY no está configurada."
    );
    return;
  }

  // Obtener varios productos reales desde la API para prueba (filtramos
  // aquellos que tengan componente maestro asociado). Puedes ajustar
  // TEST_ITEMS_COUNT en las env vars (por defecto 3).
  const BASE = process.env.BASE_URL || "http://localhost:3000";
  const ITEMS_COUNT = parseInt(process.env.TEST_ITEMS_COUNT || "3");

  console.log(
    `🔍 Obteniendo hasta ${ITEMS_COUNT} productos desde ${BASE}/api/productos`
  );
  const productsRes = await fetch(`${BASE}/api/productos?page=1&limit=100`);
  if (!productsRes.ok) {
    const txt = await productsRes.text();
    throw new Error(
      `Fallo al listar productos: ${productsRes.status} - ${txt}`
    );
  }

  const productsList = await productsRes.json();
  // productsList podría venir como un objeto con 'data' o como un array directo
  const productsArray = Array.isArray(productsList)
    ? productsList
    : productsList.data || [];

  // Filtrar productos que tengan componente maestro
  const productsWithComponent = productsArray.filter(
    (p) => p.id_componente_maestro || p.id_componente
  );
  if (productsWithComponent.length < 2) {
    throw new Error(
      "No se encontraron suficientes productos con componente maestro en la base de datos."
    );
  }

  const selected = productsWithComponent.slice(0, ITEMS_COUNT);
  const cartItems = selected.map((p) => ({
    product_id: String(p.id_producto ?? p.id),
  }));

  try {
    console.log(
      `🔍 1. Buscando componentes maestros para productos ${cartItems
        .map((c) => c.product_id)
        .join(", ")} via API...`
    );

    // Obtener componente maestro para cada producto y construir items enriquecidos
    const enrichedItems = [];
    for (const it of cartItems) {
      const pid = it.product_id;
      const res = await fetch(`${BASE}/api/productos/${pid}/componente`);
      if (!res.ok)
        throw new Error(
          `Fallo al obtener componente para producto ${pid}: ${res.status}`
        );
      const comp = await res.json();

      enrichedItems.push({
        name: comp.nombre_componente || comp.nombre || `producto_${pid}`,
        category: comp.categoria || "Desconocido",
        specifications: comp.especificaciones || comp.specifications || {},
      });
    }

    // 2A) PRUEBA 1: Llamar al endpoint del servidor primero
    console.log("🔍 2A. Llamando al endpoint /api/compatibility/check...");
    const token = process.env.TEST_TOKEN || null;
    const headers = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const apiRes = await fetch(`${BASE}/api/compatibility/check`, {
      method: "POST",
      headers,
      body: JSON.stringify({ items: cartItems }),
    });

    if (!apiRes.ok) {
      const text = await apiRes.text();
      throw new Error(
        `Endpoint /api/compatibility/check fallo: ${apiRes.status} - ${text}`
      );
    }

    const apiJson = await apiRes.json();
    console.log("\n--- Resultado desde el endpoint (server) ---");
    console.log(JSON.stringify(apiJson, null, 2));

    // 2B) PRUEBA 2: Llamar al servicio directamente (Gemini)
    console.log(
      "🔍 2B. Llamando directamente a Gemini via compatibilityService.callGeminiAPI..."
    );
    const startTime = Date.now();
    const geminiResult = await compatibilityService.callGeminiAPI(
      enrichedItems
    );
    const endTime = Date.now();

    console.log("\n--- Resultado directo desde Gemini ---");
    console.log(JSON.stringify(geminiResult, null, 2));

    console.log("\n==================================================");
    console.log("✅ PRUEBA DE INTEGRACIÓN COMPLETA REALIZADA.");
    console.log(
      `⏱️ Tiempo total de la llamada directa a Gemini: ${(
        (endTime - startTime) /
        1000
      ).toFixed(2)} segundos`
    );
    console.log("==================================================");
  } catch (error) {
    console.error("\n❌ PRUEBA CRÍTICA FALLIDA.");
    console.error(
      "Detalles del error (Verifica si los IDs UUID son correctos o si la DB está accesible):",
      error.message
    );
  }
}

runFullIntegrationTest();
