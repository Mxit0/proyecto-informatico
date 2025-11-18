import productRepository2 from '../repositories/productRepository2.js';

class CompatibilityService {
  async checkCompatibility(cartItems, userId = null) {
    try {
      // 1. Enriquecer datos de productos
      const enrichedItems = await this.enrichProductData(cartItems);
      
      // 2. Llamar a DeepSeek API
      const compatibilityResult = await this.callDeepSeekAPI(enrichedItems);
      
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
    
    // TODO: Implementar llamada real a DeepSeek API
    // Por ahora simulamos una respuesta
    
    console.log('📝 Prompt para DeepSeek:', prompt);
    
    // Simulación de respuesta
    return {
      compatible: true,
      issues: [],
      recommendations: ["Todos los componentes son compatibles"],
      explanation: "Los componentes seleccionados son técnicamente compatibles entre sí.",
      compatibility_score: 95
    };
    
    /*
    // Código real para cuando tengas la API key:
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
    */
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
}

export default new CompatibilityService();