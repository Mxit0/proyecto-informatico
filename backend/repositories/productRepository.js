import { supabase } from "../lib/supabaseClient.js";
import redisClient from "../lib/redisClient.js";
const BUCKET_NAME = process.env.SUPABASE_STORAGE_BUCKET;

const TABLE = "producto";
const CACHE_EXPIRATION_SECONDS = 3600;

/**
 * Obtiene todos los productos, y "manualmente" une sus imágenes.
 * Esto evita los errores de join de Supabase.
 */
export async function getAllProducts(page, limit) {
  const CACHE_KEY = `productos_con_imagenes:page:${page}:limit:${limit}`;

  try {
    const cachedProducts = await redisClient.get(CACHE_KEY);
    if (cachedProducts) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProducts);
    }

    console.log(
      `Cache MISS: Pidiendo ${CACHE_KEY} a Supabase (enfoque manual)`
    );

    const from = (page - 1) * limit;
    const to = from + limit - 1;

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // 1. Obtener los productos (de la tabla 'producto')
    // Ordenar por fecha_publicacion descendente para que los productos nuevos
    // aparezcan primero (recomendados). También paginamos con range.
    const { data: productsData, error: productsError } = await supabase
      .from(TABLE)
      .select("*")
      .order("fecha_publicacion", { ascending: false })
      .range(from, to);

    if (productsError) throw productsError;

    const productIds = productsData.map((p) => p.id);

    // 2. Obtener TODAS las imágenes para esos productos en UNA sola consulta
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen") // <-- Usamos id_im
      .in("id_prod", productIds);

    if (imagesError) throw imagesError;

    // 3. "Pegar" las imágenes a sus productos
    const productsWithImages = productsData.map((product) => {
      // El frontend espera "id_im" (o "id_img") y "url_imagen"
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({
          id_im: img.id_im, // <-- Mapeo correcto
          url_imagen: img.url_imagen,
        }));

      return {
        ...product,
        producto_imagenes: imagenes, // El frontend espera este nombre de campo
      };
    });
    // --- FIN DEL CAMBIO ---

    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(productsWithImages)
    );

    return productsWithImages;
  } catch (err) {
    throw err;
  }
}

/**
 * Obtiene un producto por su ID, y "manualmente" une sus imágenes.
 */
export async function getProductById(id) {
  const CACHE_KEY = `producto_con_imagenes:${id}`;

  try {
    const cachedProduct = await redisClient.get(CACHE_KEY);
    if (cachedProduct) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProduct);
    }

    console.log(
      `Cache MISS: Pidiendo ${CACHE_KEY} a Supabase (enfoque manual)`
    );

    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
    // 1. Obtener el producto (sin join)
    const { data: productData, error: productError } = await supabase
      .from(TABLE)
      .select("*")
      .eq("id", id)
      .single();

    if (productError) throw productError;
    if (!productData) return null; // Producto no encontrado

    // 2. Obtener las imágenes para ESE producto
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen") // <-- Usamos id_im
      .eq("id_prod", productData.id);

    if (imagesError) throw imagesError;

    // 3. "Pegar" las imágenes
    const imagenes = imagesData.map((img) => ({
      id_im: img.id_im, // <-- Mapeo correcto
      url_imagen: img.url_imagen,
    }));

    const productWithImages = {
      ...productData,
      producto_imagenes: imagenes,
    };
    // --- FIN DEL CAMBIO ---

    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(productWithImages)
    );

    return productWithImages;
  } catch (err) {
    throw err;
  }
}

// --- El resto de tus funciones (createProduct, uploadProductImages, getProductImages) ---
// (No cambian, las dejamos como están)
export async function createProduct(productData) {
  const {
    nombre,
    descripcion,
    precio,
    id_usuario,
    stock,
    categoria,
    fecha_publicacion,
  } = productData;

  const { data, error } = await supabase
    .from(TABLE)
    .insert([
      {
        nombre,
        descripcion,
        precio,
        id_usuario,
        stock,
        categoria,
        fecha_publicacion,
      },
    ])
    .select()
    .single();

  if (error) throw error;

  console.log("Invalidando cachés...");
  // Intentar invalidar las cachés relacionadas con listados de productos.
  // El patrón anterior "productos*:" no coincidía con las claves generadas
  // (ej: "productos_con_imagenes:page:1:limit:100"), por eso no se borraba nada.
  // Usamos patrones más amplios para eliminar listas y por categoría.
  const listKeys = await redisClient.keys("productos*");
  if (listKeys.length > 0) {
    await redisClient.del(listKeys);
  }
  const catKeys = await redisClient.keys("productos_categoria*");
  if (catKeys.length > 0) {
    await redisClient.del(catKeys);
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

    const { error: dbError } = await supabase.from("producto_imagenes").insert({
      id_prod: id_producto,
      url_imagen: publicUrl,
    });

    if (dbError) throw dbError;
  }

  return urls;
}

export async function getProductImages(id_producto) {
  const CACHE_KEY = `imagenes_producto:${id_producto}`;

  try {
    const cachedImages = await redisClient.get(CACHE_KEY);
    if (cachedImages) {
      console.log(
        `Cache HIT: Imágenes para producto id ${id_producto} desde Redis`
      );
      return JSON.parse(cachedImages);
    }

    console.log(
      `Cache MISS: Pidiendo imágenes de producto id ${id_producto} a Supabase`
    );

    const { data, error } = await supabase
      .from("producto_imagenes")
      .select("id_im, url_imagen")
      .eq("id_prod", id_producto);

    if (error) throw error;

    await redisClient.setEx(CACHE_KEY, 3600, JSON.stringify(data));

    return data;
  } catch (err) {
    throw err;
  }
}

/**
 * Obtiene productos por categoria (categoria es FK entero hacia la tabla 'categoria')
 */
export async function getProductsByCategory(categoryId, page = 1, limit = 100) {
  const CACHE_KEY = `productos_categoria:${categoryId}:page:${page}:limit:${limit}`;

  try {
    const cachedProducts = await redisClient.get(CACHE_KEY);
    if (cachedProducts) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProducts);
    }

    console.log(
      `Cache MISS: Pidiendo ${CACHE_KEY} a Supabase (enfoque manual)`
    );

    const from = (page - 1) * limit;
    const to = from + limit - 1;

    // 1. Obtener productos filtrando por categoria (campo FK 'categoria')
    // Ordenar por fecha_publicacion descendente dentro de la categoría
    const { data: productsData, error: productsError } = await supabase
      .from(TABLE)
      .select("*")
      .eq("categoria", categoryId)
      .order("fecha_publicacion", { ascending: false })
      .range(from, to);

    if (productsError) throw productsError;

    const productIds = productsData.map((p) => p.id);

    // 2. Obtener TODAS las imágenes para esos productos en UNA sola consulta
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen")
      .in("id_prod", productIds);

    if (imagesError) throw imagesError;

    // 3. "Pegar" las imágenes a sus productos
    const productsWithImages = productsData.map((product) => {
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({ id_im: img.id_im, url_imagen: img.url_imagen }));

      return {
        ...product,
        producto_imagenes: imagenes,
      };
    });

    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(productsWithImages)
    );

    return productsWithImages;
  } catch (err) {
    throw err;
  }
}

/**
 * Obtiene la lista de categorías únicas de la tabla 'producto'.
 */
export async function getAllCategories() {
  const CACHE_KEY = `categorias_unicas`;
  try {
    const cached = await redisClient.get(CACHE_KEY);
    if (cached) {
      console.log("Cache HIT: Sirviendo categorías desde Redis");
      return JSON.parse(cached);
    }

    console.log("Cache MISS: Solicitando categorías a Supabase");
    // Ahora la columna 'categoria' en 'producto' es un FK (entero) hacia la tabla 'categoria'
    // Obtenemos todas las categorías desde la tabla 'categoria' (id, nombre) y devolvemos los nombres
    const { data, error } = await supabase
      .from("categoria")
      .select("id, nombre")
      .order("nombre", { ascending: true });

    if (error) throw error;

    // data será un array de objetos { id: <int>, nombre: '<nombre>' }
    const categories = (data || []).map((c) => ({
      id: c.id,
      nombre: c.nombre,
    }));

    await redisClient.setEx(CACHE_KEY, 3600, JSON.stringify(categories));
    return categories;
  } catch (err) {
    throw err;
  }
}
