const redisClient = require('../lib/redisClient');
const productRepository = require('../repositories/productRepository');
import productRepository2 from '../repositories/productRepository2.js';

class CompatibilityService {
  async checkCompatibility(cartItems, userId = null) {
    try {
      // 1. Verificar cache primero
      const cacheKey = `compatibility:${JSON.stringify(cartItems.sort())}`;
      const cached = await redisClient.get(cacheKey);
      
      if (cached) {
        return JSON.parse(cached);
      }

      // 2. Enriquecer datos de productos
      const enrichedItems = await this.enrichProductData(cartItems);
      
      // 3. Llamar a DeepSeek API
      const compatibilityResult = await this.callDeepSeekAPI(enrichedItems);
      
      // 4. Guardar en cache
      await redisClient.setex(cacheKey, 3600, JSON.stringify(compatibilityResult)); // 1 hora
      
      // 5. Guardar en historial si hay userId
      if (userId) {
        await this.saveCompatibilityHistory(userId, cartItems, compatibilityResult);
      }
      
      return compatibilityResult;
    } catch (error) {
      console.error('Error in compatibility service:', error);
      throw new Error('Error verificando compatibilidad');
    }
  }

  async enrichProductData(items) {
  const enriched = [];
  for (const item of items) {
    const productDetails = await productRepository2.getProductById(item.product_id);
    enriched.push({
      name: productDetails.nombre,
      category: productDetails.categoria,
      specifications: productDetails.especificaciones
    });
  }
  return enriched;
}

  async callDeepSeekAPI(items) {
    const prompt = this.buildCompatibilityPrompt(items);
    
    const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${process.env.DEEPSEEK_API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: "deepseek-chat",
        messages: [
          {
            role: "system",
            content: "Eres un experto en compatibilidad de componentes electrónicos. Analiza si los componentes son compatibles técnicamente."
          },
          {
            role: "user",
            content: prompt
          }
        ],
        temperature: 0.3,
        response_format: { type: "json_object" }
      })
    });

    const data = await response.json();
    return JSON.parse(data.choices[0].message.content);
  }

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

  async saveCompatibilityHistory(userId, items, result) {
    // Implementar según tu base de datos
    const historyEntry = {
      user_id: userId,
      items: items,
      result: result,
      checked_at: new Date()
    };
    // Guardar en Supabase o tu BD
  }
}

module.exports = new CompatibilityService();