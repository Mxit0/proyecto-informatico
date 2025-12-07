import productRepository from '../repositories/productRepository.js';
import { GoogleGenAI } from '@google/genai'; // 👈 1. Importar el SDK de Gemini

// 2. Inicializar el Cliente
// El SDK busca automáticamente la variable de entorno GEMINI_API_KEY
// Asegúrate de definirla en tu archivo .env: GEMINI_API_KEY="TU_CLAVE_AQUI"
const ai = new GoogleGenAI({}); 
const MODEL = 'gemini-2.5-flash'; // 👈 Modelo rápido y rentable

class CompatibilityService {
  async checkCompatibility(cartItems, userId = null) {
    try {
      // 1. Enriquecer datos de productos
      const enrichedItems = await this.enrichProductData(cartItems);
      
      // 2. Llamar a la API de Gemini
      const compatibilityResult = await this.callGeminiAPI(enrichedItems); // 👈 Cambiado el nombre de la función
      
      return compatibilityResult;
    } catch (error) {
      console.error('Error in compatibility service:', error);
      throw new Error('Error verificando compatibilidad');
    }
  }

  // ... (La función enrichProductData queda igual)
  async enrichProductData(items) {
    const enriched = [];
    for (const item of items) {
      const productDetails = await productRepository2.getProductById(item.product_id);
      
      // 🛑 NECESITAS ESTA LÍNEA AQUÍ
      if (!productDetails) {
        console.warn(`⚠️ Producto con ID ${item.product_id} no pudo ser enriquecido y fue saltado.`);
        continue; // Esto evita que el código llegue a la línea 32 y crashée.
      }
      
      // Si llegamos aquí, productDetails NO es null, y la lectura funciona.
      enriched.push({
        name: productDetails.nombre, // Ahora es seguro leer 'nombre'
        category: productDetails.categoria,
        specifications: productDetails.especificaciones
      });
    }
    return enriched;
  }

  // 👈 Esta es la función clave modificada
  async callGeminiAPI(items) { 
    const prompt = this.buildCompatibilityPrompt(items);
    
    console.log('📝 Prompt para Gemini:', prompt);

    try {
      const response = await ai.models.generateContent({
        model: MODEL,
        contents: prompt,
        config: {
          // Indicamos a Gemini que la respuesta debe ser un objeto JSON válido.
          responseMimeType: "application/json", 
          // Opcional: ajusta la temperatura para respuestas más precisas (cercanas a 0)
          temperature: 0.2
        },
      });

      // El SDK de Gemini retorna la respuesta como una cadena JSON que debemos parsear.
      const jsonText = response.text.trim();
      return JSON.parse(jsonText);
      
    } catch (error) {
      console.error('Error llamando a la API de Gemini:', error);
      // Lanzamos un error más específico si la API falla.
      throw new Error('Fallo al conectar con la API de Gemini. Verifique la clave y el servicio.');
    }
  }

  // ... (La función buildCompatibilityPrompt queda igual)
  buildCompatibilityPrompt(items) {
    let prompt = `Analiza la compatibilidad técnica de estos componentes electrónicos:

Componentes en el carrito:
`;

    items.forEach(item => {
      prompt += `- ${item.name} (${item.category}): ${JSON.stringify(item.specifications)}\n`;
    });

    prompt += `
Responde EXCLUSIVAMENTE en formato JSON con esta estructura:
{
  "compatible": boolean,
  "issues": string[],
  "recommendations": string[],
  "explanation": string,
  "compatibility_score": number
}

Sé técnico y preciso en tu análisis.`;
    
    return prompt;
  }
}

export default new CompatibilityService();