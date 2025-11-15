// Archivo: repository/productRepository.js
import { supabase } from '../lib/supabaseClient.js';
import redisClient from '../lib/redisClient.js';

const TABLE = 'producto';
const CACHE_EXPIRATION_SECONDS = 3600; 

export async function getAllProducts(page, limit) {
  
   
  const CACHE_KEY = `productos:page:${page}:limit:${limit}`;

  try {
    // --- 1. INTENTAR OBTENER DE CACHÉ ---
    const cachedProducts = await redisClient.get(CACHE_KEY);
    if (cachedProducts) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProducts);
    }

    // --- 2. CACHE MISS: OBTENER DE SUPABASE (CON PAGINACIÓN) ---
    console.log(`Cache MISS: Pidiendo ${CACHE_KEY} a Supabase`);
    
    // Calcular el rango para Supabase
    const from = (page - 1) * limit;
    const to = from + limit - 1;

    const { data, error } = await supabase
      .from(TABLE)
      .select('*')
      .range(from, to);

    if (error) throw error;

    // --- 3. GUARDAR EN CACHÉ ANTES DE DEVOLVER ---
    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(data)
    );

    return data;

  } catch (err) {
    throw err;
  }
}

export async function createProduct(productData) {
 
  const { 
    nombre, 
    descripcion, 
    precio, 
    id_usuario, 
    stock, 
    categoria, 
    imagen, // 'imagen' puede ir como 'null' si no se envía
    fecha_publicacion // <-- Campo de fecha añadido
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
      imagen,
      fecha_publicacion
    }])
    .select()
    .single();

  if (error) throw error;
  
  // --- INVALIDAR EL CACHÉ ---
  console.log('Invalidando TODOS los cachés de productos...');
  const keys = await redisClient.keys('productos:page:*');
  if (keys.length > 0) {
    await redisClient.del(keys);
  }
  
  return data;
}
// Nota: También deberías invalidar (borrar) el caché si actualizas o eliminas un producto.