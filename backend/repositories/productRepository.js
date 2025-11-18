import { supabase } from '../lib/supabaseClient.js';
import redisClient from '../lib/redisClient.js';
const BUCKET_NAME = process.env.SUPABASE_STORAGE_BUCKET;

const TABLE = 'producto';
const CACHE_EXPIRATION_SECONDS = 3600; 

/**
 * Obtiene todos los productos (SIN IMÁGENES)
 */
export async function getAllProducts(page, limit) {
  const CACHE_KEY = `productos_simple:page:${page}:limit:${limit}`;

  try {
    const cachedProducts = await redisClient.get(CACHE_KEY);
    if (cachedProducts) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProducts);
    }

    console.log(`Cache MISS: Pidiendo ${CACHE_KEY} a Supabase (solo tabla 'producto')`);
    
    const from = (page - 1) * limit;
    const to = from + limit - 1;

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // Pedimos SOLO la tabla 'producto', ignorando las imágenes
    const { data, error } = await supabase
      .from(TABLE)
      .select('*')
      .range(from, to);
    // ----------------------------

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
}

/**
 * Obtiene un producto por su ID (SIN IMÁGENES)
 */
export async function getProductById(id) {
  const CACHE_KEY = `producto_simple:${id}`;

  try {
    const cachedProduct = await redisClient.get(CACHE_KEY);
    if (cachedProduct) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProduct);
    }

    console.log(`Cache MISS: Pidiendo ${CACHE_KEY} a Supabase (solo tabla 'producto')`);

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    const { data, error } = await supabase
      .from(TABLE)
      .select('*')
      .eq('id', id)
      .single();
    // ----------------------------

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
}

// --- El resto de tus funciones (createProduct, uploadProductImages) ---
// Las dejamos, aunque 'uploadProductImages' ya no se usa por ahora.
export async function createProduct(productData) {
  const { 
    nombre, 
    descripcion, 
    precio, 
    id_usuario, 
    stock, 
    categoria, 
    fecha_publicacion
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
      fecha_publicacion
    }])
    .select()
    .single();

  if (error) throw error;
  
  console.log('Invalidando cachés...');
  const keys = await redisClient.keys('productos*:'); // Invalida todos los cachés de productos
  if (keys.length > 0) {
    await redisClient.del(keys);
  }
  
  return data;
}

export async function uploadProductImages(id_producto, files) {
  const urls = [];

  for (const file of files) {
    const filePath = `public/${id_producto}/${Date.now()}-${file.originalname}`;

    const { data: uploadData, error: uploadError } = await supabase.storage
      .from(BUCKET_NAME) // Asegúrate de que BUCKET_NAME esté definido arriba
      .upload(filePath, file.buffer, {
        contentType: file.mimetype,
      });

    if (uploadError) throw uploadError;

    const { data: urlData } = supabase.storage
      .from(BUCKET_NAME)
      .getPublicUrl(filePath);
    
    const publicUrl = urlData.publicUrl;
    urls.push(publicUrl);

    const { error: dbError } = await supabase
      .from('producto_imagenes')
      .insert({
        id_prod: id_producto,
        url_imagen: publicUrl
      });
    
    if (dbError) throw dbError;
  }
  
  return urls;
}

export async function getProductImages(id_producto) {
  const CACHE_KEY = `imagenes_producto:${id_producto}`;

  try {
    // 1. Intentar obtener de Redis
    const cachedImages = await redisClient.get(CACHE_KEY);
    if (cachedImages) {
      console.log(`Cache HIT: Imágenes para producto id ${id_producto} desde Redis`);
      return JSON.parse(cachedImages);
    }

    // 2. Si no está en caché, pedir a Supabase
    console.log(`Cache MISS: Pidiendo imágenes de producto id ${id_producto} a Supabase`);

    const { data, error } = await supabase
      .from('producto_imagenes') // Tu tabla de imágenes
      .select('id_im, url_imagen')  // Seleccionamos ID y la URL
      .eq('id_prod', id_producto); // Filtramos por el producto

    if (error) throw error;

    // 3. Guardar en Redis (1 hora)
    await redisClient.setEx(
      CACHE_KEY,
      3600,
      JSON.stringify(data)
    );

    return data;

  } catch (err) {
    throw err;
  }
}