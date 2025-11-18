import { supabase } from '../lib/supabaseClient.js';

const TABLE = 'producto_compatibilidad';

export const productRepository2 = {
  // Obtener todos los productos con paginación
  async getAllProducts(page = 1, limit = 10) {
    try {
      const from = (page - 1) * limit;
      const to = from + limit - 1;

      const { data, error } = await supabase
        .from(TABLE)
        .select(`
          *,
          usuario:id_usuario (nombre_usuario, correo, reputacion)
        `)
        .range(from, to)
        .order('fecha_publicacion', { ascending: false });

      if (error) throw error;
      return data;

    } catch (err) {
      throw err;
    }
  },

  // Obtener producto por ID
  async getProductById(productId) {
    try {
      const { data, error } = await supabase
        .from(TABLE)
        .select(`
          *,
          usuario:id_usuario (nombre_usuario, correo, reputacion)
        `)
        .eq('id', productId)
        .single();

      if (error) throw error;
      return data;

    } catch (err) {
      throw err;
    }
  },

  // Obtener múltiples productos por IDs
  async getProductsByIds(productIds) {
    if (!productIds || productIds.length === 0) {
      return [];
    }

    try {
      const { data, error } = await supabase
        .from(TABLE)
        .select(`
          *,
          usuario:id_usuario (nombre_usuario, correo, reputacion)
        `)
        .in('id', productIds);

      if (error) throw error;
      return data || [];

    } catch (err) {
      throw err;
    }
  },

  // Crear producto
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
    return data;
  }
};

export default productRepository2;