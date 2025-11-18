import { supabase } from '../lib/supabaseClient.js';
import redisClient from '../lib/redisClient.js';

const TABLE = 'producto_compatibilidad';
const CACHE_EXPIRATION_SECONDS = 3600;

export const productRepository2 = {
  // Obtener todos los productos con paginación
  async getAllProducts(page = 1, limit = 10) {
    const CACHE_KEY = `productos_compatibilidad:page:${page}:limit:${limit}`;

    try {
      const cachedProducts = await redisClient.get(CACHE_KEY);
      if (cachedProducts) {
        console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
        return JSON.parse(cachedProducts);
      }

      console.log(`Cache MISS: Pidiendo ${CACHE_KEY} a Supabase`);
      
      const from = (page - 1) * limit;
      const to = from + limit - 1;

      const { data, error } = await supabase
        .from(TABLE)
        .select('*')
        .range(from, to)
        .order('fecha_publicacion', { ascending: false });

      if (error) throw error;

      await redisClient.setEx(
        CACHE_KEY,
        CACHE_EXPIRATION_SECONDS,
        JSON.stringify(data)
      );

      return data;

    } catch (err) {
      throw err;
    }
  },

  // Obtener producto por ID con especificaciones técnicas
  async getProductById(productId) {
    const CACHE_KEY = `producto_compatibilidad:${productId}`;

    try {
      const cachedProduct = await redisClient.get(CACHE_KEY);
      if (cachedProduct) {
        console.log(`Cache HIT: Sirviendo producto ${productId} desde Redis`);
        return JSON.parse(cachedProduct);
      }

      console.log(`Cache MISS: Pidiendo producto ${productId} a Supabase`);

      const { data, error } = await supabase
        .from(TABLE)
        .select('*')
        .eq('id', productId)
        .single();

      if (error) throw error;

      if (data) {
        await redisClient.setEx(
          CACHE_KEY,
          CACHE_EXPIRATION_SECONDS,
          JSON.stringify(data)
        );
      }

      return data;

    } catch (err) {
      throw err;
    }
  },

  // Obtener múltiples productos por IDs (para el carrito)
  async getProductsByIds(productIds) {
    if (!productIds || productIds.length === 0) {
      return [];
    }

    const CACHE_KEY = `productos_compatibilidad:ids:${productIds.sort().join('-')}`;

    try {
      const cachedProducts = await redisClient.get(CACHE_KEY);
      if (cachedProducts) {
        console.log(`Cache HIT: Sirviendo productos por IDs desde Redis`);
        return JSON.parse(cachedProducts);
      }

      console.log(`Cache MISS: Pidiendo productos por IDs a Supabase`);

      const { data, error } = await supabase
        .from(TABLE)
        .select('*')
        .in('id', productIds);

      if (error) throw error;

      if (data && data.length > 0) {
        await redisClient.setEx(
          CACHE_KEY,
          CACHE_EXPIRATION_SECONDS,
          JSON.stringify(data)
        );
      }

      return data || [];

    } catch (err) {
      throw err;
    }
  },

  // Crear producto con especificaciones técnicas
  async createProduct(productData) {
    const { 
      nombre, 
      descripcion, 
      precio, 
      id_usuario, 
      stock, 
      categoria,
      especificaciones 
    } = productData;

    const { data, error } = await supabase
      .from(TABLE)
      .insert([{ 
        nombre, 
        descripcion, 
        precio, 
        id_usuario,
        stock, 
        categoria,
        especificaciones: especificaciones || {}
      }])
      .select()
      .single();

    if (error) throw error;
    
    // Invalidar cachés relevantes
    await this.invalidateProductCaches();
    
    return data;
  },

  // Buscar productos por categoría y especificaciones
  async getProductsByCategoryAndSpecs(category, filters = {}) {
    const filterString = JSON.stringify(filters);
    const CACHE_KEY = `productos_compatibilidad:category:${category}:filters:${filterString}`;

    try {
      const cachedProducts = await redisClient.get(CACHE_KEY);
      if (cachedProducts) {
        console.log(`Cache HIT: Sirviendo productos filtrados desde Redis`);
        return JSON.parse(cachedProducts);
      }

      let query = supabase
        .from(TABLE)
        .select('*')
        .eq('categoria', category);

      // Aplicar filtros a las especificaciones
      Object.keys(filters).forEach(key => {
        query = query.filter('especificaciones', 'cs', JSON.stringify({[key]: filters[key]}));
      });

      const { data, error } = await query;

      if (error) throw error;

      if (data) {
        await redisClient.setEx(
          CACHE_KEY,
          CACHE_EXPIRATION_SECONDS,
          JSON.stringify(data)
        );
      }

      return data || [];

    } catch (err) {
      throw err;
    }
  },

  // Invalidar cachés de productos
  async invalidateProductCaches() {
    console.log('Invalidando cachés de productos de compatibilidad...');
    const keys = await redisClient.keys('productos_compatibilidad:*');
    if (keys.length > 0) {
      await redisClient.del(keys);
    }
  },

  // Obtener categorías disponibles
  async getCategories() {
    const CACHE_KEY = 'productos_compatibilidad:categories';

    try {
      const cachedCategories = await redisClient.get(CACHE_KEY);
      if (cachedCategories) {
        return JSON.parse(cachedCategories);
      }

      const { data, error } = await supabase
        .from(TABLE)
        .select('categoria')
        .not('categoria', 'is', null);

      if (error) throw error;

      const categories = [...new Set(data.map(item => item.categoria))];
      
      await redisClient.setEx(
        CACHE_KEY,
        CACHE_EXPIRATION_SECONDS,
        JSON.stringify(categories)
      );

      return categories;

    } catch (err) {
      throw err;
    }
  }
};

export default productRepository2;