import fetch from "node-fetch";

// Uso: node testGetComponentePorProducto.js [PRODUCT_ID]
const PRODUCT_ID = process.argv[2] || process.env.TEST_PRODUCT_ID || "1";
const BASE = process.env.BASE_URL || "http://localhost:3000";

async function run() {
  try {
    console.log(`Consultando componente maestro del producto ${PRODUCT_ID}...`);
    const res = await fetch(`${BASE}/api/productos/${PRODUCT_ID}/componente`);
    const text = await res.text();
    try {
      const json = JSON.parse(text);
      console.log("Respuesta JSON:", JSON.stringify(json, null, 2));
    } catch (err) {
      console.log("Respuesta (no JSON):", text);
    }
  } catch (err) {
    console.error("Error ejecutando test:", err.message || err);
  }
}

run();
