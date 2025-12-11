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

    // Si no hay productos, retornar array vacío
    if (!productsData || productsData.length === 0) {
      return [];
    }

    // Recolectamos los IDs para hacer las consultas eficientes
    const productIds = productsData.map((p) => p.id);

    // 2. Obtener las IMÁGENES (solo si hay productos)
    let imagesData = [];
    if (productIds.length > 0) {
      const { data: images, error: imagesError } = await supabase
        .from("producto_imagenes")
        .select("id_im, id_prod, url_imagen")
        .in("id_prod", productIds);

      if (imagesError) throw imagesError;
      imagesData = images || [];
    }

    // Filtrar categoryIds: SOLO números puros, ignorar cualquier string
    const categoryIds = [
      ...new Set(productsData.map((p) => p.categoria)),
    ].filter((cat) => typeof cat === "number"); // SOLO números, nada más

    // 3. Obtener las CATEGORÍAS (solo si hay IDs numéricos)
    let categoriesData = [];
    if (categoryIds.length > 0) {
      const { data: categories, error: categoriesError } = await supabase
        .from(TABLE_CATEGORIA)
        .select("id, nombre")
        .in("id", categoryIds);

      if (categoriesError) throw categoriesError;
      categoriesData = categories || [];
    }

    // 4. Unir todo ("Pegar" imágenes y nombres de categorías)
    const productsComplete = productsData.map((product) => {
      // A. Pegar Imágenes
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({
          id_im: img.id_im,
          url_imagen: img.url_imagen,
        }));

      // B. Pegar Nombre de Categoría
      // Si categoria es un número, buscar en categoriesData
      // Si es un string, usarlo directamente como nombre
      let categoryName = "Desconocido";
      if (typeof product.categoria === "number") {
        const categoryObj = categoriesData.find(
          (c) => c.id === product.categoria
        );
        categoryName = categoryObj ? categoryObj.nombre : "Desconocido";
      } else if (typeof product.categoria === "string") {
        // Si ya es un string (nombre), usarlo directamente
        categoryName = product.categoria;
      }

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
    console.error("Error en getAllProducts:", err);
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
import { getComponentById } from "./componenteRepository.js";

export async function createProduct(productData) {
  const {
    nombre,
    descripcion,
    precio,
    id_usuario,
    stock,
    categoria,
    fecha_publicacion,
    id_componente_maestro,
  } = productData;

  // Validación mínima: si se requiere componente maestro, verificar que exista y pertenezca a la categoría
  if (!id_componente_maestro) {
    throw new Error("Se requiere id_componente_maestro al crear el producto");
  }

  // Obtener componente y validar
  const componente = await getComponentById(id_componente_maestro);
  if (!componente) {
    throw new Error("Componente maestro no encontrado");
  }

  // Validar que la categoría del componente coincida con la categoría enviada
  if (componente.categoria !== categoria) {
    throw new Error(
      "El componente maestro no pertenece a la categoría seleccionada"
    );
  }

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
        id_componente_maestro,
      },
    ])
    .select()
    .single();

  if (error) throw error;

  console.log("Invalidando cachés...");
  const keys = await redisClient.keys("productos*");
  if (keys.length > 0) {
    await redisClient.del(keys);
  }

  // devolver el producto y asegurar producto_imagenes como array vacío
  return {
    ...data,
    producto_imagenes: data.producto_imagenes ?? [],
  };
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
      .from("categoria") // Asegúrate que tu tabla se llama 'categoria'
      .select("id, nombre")
      .order("nombre", { ascending: true });

    if (error) throw error;
    return data || [];
  } catch (error) {
    console.error("Error al obtener categorías:", error);
    throw error;
  }
}

/**
 * Obtiene productos filtrados por ID de categoría
 */
export async function getProductsByCategory(categoryId) {
  try {
    // 1. Obtener productos
    const { data: productsData, error: productsError } = await supabase
      .from("producto") // Asegúrate que tu tabla se llama 'producto'
      .select("*")
      .eq("categoria", categoryId);

    if (productsError) throw productsError;
    if (!productsData || productsData.length === 0) return [];

    // 2. Obtener imágenes para esos productos
    const productIds = productsData.map((p) => p.id);
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen")
      .in("id_prod", productIds);

    if (imagesError) throw imagesError;

    // 3. Unir productos con sus imágenes
    const productsComplete = productsData.map((product) => {
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({
          id_im: img.id_im,
          url_imagen: img.url_imagen,
        }));

      return { ...product, producto_imagenes: imagenes };
    });

    return productsComplete;
  } catch (error) {
    console.error("Error al obtener productos por categoría:", error);
    throw error;
  }
}

export async function deleteProduct(id) {
  try {
    // 1. Borrar de Supabase
    // Nota: Supabase borrará en cascada las imágenes si la FK está configurada así,
    // de lo contrario podrían quedar huérfanas en la tabla 'producto_imagenes'.
    const { error } = await supabase
      .from(TABLE)
      .delete()
      .eq("id", id);

    if (error) throw error;

    // 2. Limpiar Caché (Redis) para que desaparezca de la app inmediatamente
    console.log(`Eliminando caché para producto ${id}`);
    
    // A. Borrar la caché del detalle individual (coincide con getProductById)
    const cacheKeyDetail = `producto_full:${id}`;
    await redisClient.del(cacheKeyDetail);

    // B. Borrar todas las listas cacheadas (paginación, filtros, etc.)
    // Usamos un patrón amplio para asegurar que se limpie todo lo que empiece por "productos"
    const listKeys = await redisClient.keys("productos*");
    if (listKeys.length > 0) {
      await redisClient.del(listKeys);
    }

    return true;
  } catch (error) {
    console.error("Error en deleteProduct:", error);
    return false;
  }
}

export async function getProductsByUserId(userId) {
  try {
    // 1. Obtener productos
    const { data: products, error } = await supabase
      .from('producto')
      .select('*')
      .eq('id_usuario', userId)
      .order('fecha_publicacion', { ascending: false }); // Los más recientes primero

    if (error) throw error;
    if (!products || products.length === 0) return [];

    // 2. Obtener imágenes (para mostrar la foto principal)
    const productIds = products.map(p => p.id);
    const { data: images } = await supabase
      .from('producto_imagenes')
      .select('id_prod, url_imagen')
      .in('id_prod', productIds);

    // 3. Unir imagen principal
    return products.map(p => {
      const img = images.find(i => i.id_prod === p.id);
      return {
        ...p,
        producto_imagenes: img ? [{ url_imagen: img.url_imagen }] : []
      };
    });

  } catch (err) {
    throw new Error(err.message);
  }
}