import { supabase } from "../lib/supabaseClient.js";
import redisClient from "../lib/redisClient.js";
const BUCKET_NAME = process.env.SUPABASE_STORAGE_BUCKET;

const TABLE = "producto";
// Asumo que la tabla se llama 'categoria' (singular).
// Si se llama 'categorias', cambia esta línea:
const TABLE_CATEGORIA = "categoria";

const CACHE_EXPIRATION_SECONDS = 3600;

/**
 * Obtiene productos, une imágenes y AHORA une categorías.
 */
export async function getAllProducts(page, limit) {
  const CACHE_KEY = `productos_full:page:${page}:limit:${limit}`;

  try {
    const cachedProducts = await redisClient.get(CACHE_KEY);
    if (cachedProducts) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProducts);
    }

    console.log(`Cache MISS: Pidiendo ${CACHE_KEY} a Supabase`);

    const from = (page - 1) * limit;
    const to = from + limit - 1;

    // 1. Obtener los productos
    const { data: productsData, error: productsError } = await supabase
      .from(TABLE)
      .select("*")
      .range(from, to);

    if (productsError) throw productsError;

    // Recolectamos los IDs para hacer las consultas eficientes
    const productIds = productsData.map((p) => p.id);
    const categoryIds = [...new Set(productsData.map((p) => p.categoria))]; // IDs únicos de categorías

    // 2. Obtener las IMÁGENES
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen")
      .in("id_prod", productIds);

    if (imagesError) throw imagesError;

    // 3. Obtener las CATEGORÍAS (¡NUEVO!)
    // Buscamos los nombres de las categorías que aparecen en estos productos
    const { data: categoriesData, error: categoriesError } = await supabase
      .from(TABLE_CATEGORIA)
      .select("id, nombre")
      .in("id", categoryIds);

    if (categoriesError) throw categoriesError;

    // 4. Unir todo ("Pegar" imágenes y nombres de categorías)
    const productsComplete = productsData.map((product) => {
      // A. Pegar Imágenes
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({
          id_im: img.id_im,
          url_imagen: img.url_imagen,
        }));

      // B. Pegar Nombre de Categoría (¡NUEVO!)
      // Buscamos la categoría cuyo ID coincida con product.categoria
      const categoryObj = categoriesData.find(
        (c) => c.id === product.categoria
      );
      const categoryName = categoryObj ? categoryObj.nombre : "Desconocido";

      return {
        ...product,
        producto_imagenes: imagenes,
        categoria: categoryName, // <-- Aquí reemplazamos el '1' por 'Tarjetas Gráficas'
      };
    });

    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(productsComplete)
    );

    return productsComplete;
  } catch (err) {
    throw err;
  }
}

/**
 * Obtiene un producto por ID, con imágenes y categoría.
 */
export async function getProductById(id) {
  const CACHE_KEY = `producto_full:${id}`;

  try {
    const cachedProduct = await redisClient.get(CACHE_KEY);
    if (cachedProduct) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProduct);
    }

    console.log(`Cache MISS: Pidiendo ${CACHE_KEY} a Supabase`);

    // 1. Obtener producto
    const { data: productData, error: productError } = await supabase
      .from(TABLE)
      .select("*")
      .eq("id", id)
      .single();

    if (productError) throw productError;
    if (!productData) return null;

    // 2. Obtener imágenes
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen")
      .eq("id_prod", productData.id);

    if (imagesError) throw imagesError;

    // 3. Obtener categoría (¡NUEVO!)
    const { data: categoryData, error: categoryError } = await supabase
      .from(TABLE_CATEGORIA)
      .select("nombre")
      .eq("id", productData.categoria)
      .single();

    // No lanzamos error si falta la categoría, solo ponemos "Desconocido"
    const categoryName = categoryData ? categoryData.nombre : "Desconocido";

    // 4. Unir todo
    const imagenes = imagesData.map((img) => ({
      id_im: img.id_im,
      url_imagen: img.url_imagen,
    }));

    const productComplete = {
      ...productData,
      producto_imagenes: imagenes,
      categoria: categoryName, // <-- Reemplazo del ID por el Nombre
    };

    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(productComplete)
    );

    return productComplete;
  } catch (err) {
    throw err;
  }
}

// --- (El resto de funciones: createProduct, uploadProductImages... se quedan igual) ---
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
  const keys = await redisClient.keys("productos*:");
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
    if (cachedImages) return JSON.parse(cachedImages);

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

export const updateProduct = async (id, updates) => {
  const { data, error } = await supabase
    .from(TABLE)
    .update(updates)
    .eq("id", id)
    .select();

  if (error) throw new Error(error.message);

  const cacheKeyIndividual = `producto_con_imagenes:${id}`;
  await redisClient.del(cacheKeyIndividual);
  const listKeys = await redisClient.keys("productos_con_imagenes*");
  if (listKeys.length > 0) {
    await redisClient.del(listKeys);
  }
  return data[0];
};

export async function getAllCategories() {
  try {
    const { data, error } = await supabase
      .from(TABLE_CATEGORIA)
      .select("id, nombre")
      .order("nombre", { ascending: true });

    if (error) throw error;
    return data || [];
  } catch (error) {
    console.error("Error al obtener categorías:", error);
    throw error;
  }
}

export async function getProductsByCategory(categoryId, page = 1, limit = 100) {
  const CACHE_KEY = `productos_categoria_${categoryId}:page:${page}:limit:${limit}`;

  try {
    const cachedProducts = await redisClient.get(CACHE_KEY);
    if (cachedProducts) {
      console.log(`Cache HIT: Sirviendo ${CACHE_KEY} desde Redis`);
      return JSON.parse(cachedProducts);
    }

    console.log(
      `Cache MISS: Pidiendo productos de categoría ${categoryId} a Supabase`
    );

    const from = (page - 1) * limit;
    const to = from + limit - 1;

    // 1. Obtener productos filtrados por categoría
    const { data: productsData, error: productsError } = await supabase
      .from(TABLE)
      .select("*")
      .eq("categoria", categoryId)
      .range(from, to);

    if (productsError) throw productsError;

    if (!productsData || productsData.length === 0) {
      return [];
    }

    // 2. Obtener imágenes
    const productIds = productsData.map((p) => p.id);
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen")
      .in("id_prod", productIds);

    if (imagesError) throw imagesError;

    // 3. Obtener nombre de categoría
    const { data: categoryData, error: categoryError } = await supabase
      .from(TABLE_CATEGORIA)
      .select("nombre")
      .eq("id", categoryId)
      .single();

    const categoryName = categoryData ? categoryData.nombre : "Desconocido";

    // 4. Unir todo
    const productsComplete = productsData.map((product) => {
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({
          id_im: img.id_im,
          url_imagen: img.url_imagen,
        }));

      return {
        ...product,
        producto_imagenes: imagenes,
        categoria: categoryName,
      };
    });

    await redisClient.setEx(
      CACHE_KEY,
      CACHE_EXPIRATION_SECONDS,
      JSON.stringify(productsComplete)
    );

    return productsComplete;
  } catch (err) {
    throw err;
  }
}
