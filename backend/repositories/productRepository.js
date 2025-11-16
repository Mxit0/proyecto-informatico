import { supabase } from '../lib/supabaseClient.js';
import redisClient from '../lib/redisClient.js';
const BUCKET_NAME = process.env.SUPABASE_STORAGE_BUCKET;

const TABLE = 'producto';
const CACHE_EXPIRATION_SECONDS = 3600; 

export async function getAllProducts(page, limit) {
  const CACHE_KEY = `productos:page:${page}:limit:${limit}`;

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
      .range(from, to);

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
  
  console.log('Invalidando TODOS los cachés de productos...');
  const keys = await redisClient.keys('productos:page:*');
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
      .from(BUCKET_NAME)
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