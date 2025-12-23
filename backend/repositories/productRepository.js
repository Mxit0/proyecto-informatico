import { supabase } from "../lib/supabaseClient.js";
import redisClient from "../lib/redisClient.js";
const BUCKET_NAME = process.env.SUPABASE_STORAGE_BUCKET;

const TABLE = "producto";

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

    
    const { data: productsData, error: productsError } = await supabase
      .from(TABLE)
      .select("*")
      .eq('activo', true)
      .range(from, to);

    if (productsError) throw productsError;

    
    if (!productsData || productsData.length === 0) {
      return [];
    }

    
    const productIds = productsData.map((p) => p.id);

    
    let imagesData = [];
    if (productIds.length > 0) {
      const { data: images, error: imagesError } = await supabase
        .from("producto_imagenes")
        .select("id_im, id_prod, url_imagen")
        .in("id_prod", productIds);

      if (imagesError) throw imagesError;
      imagesData = images || [];
    }


    const categoryIds = [
      ...new Set(productsData.map((p) => p.categoria)),
    ].filter((cat) => typeof cat === "number"); // SOLO números, nada más

    
    let categoriesData = [];
    if (categoryIds.length > 0) {
      const { data: categories, error: categoriesError } = await supabase
        .from(TABLE_CATEGORIA)
        .select("id, nombre")
        .in("id", categoryIds);

      if (categoriesError) throw categoriesError;
      categoriesData = categories || [];
    }

    
    const productsComplete = productsData.map((product) => {
      
      const imagenes = imagesData
        .filter((img) => img.id_prod === product.id)
        .map((img) => ({
          id_im: img.id_im,
          url_imagen: img.url_imagen,
        }));

      
      let categoryName = "Desconocido";
      if (typeof product.categoria === "number") {
        const categoryObj = categoriesData.find(
          (c) => c.id === product.categoria
        );
        categoryName = categoryObj ? categoryObj.nombre : "Desconocido";
      } else if (typeof product.categoria === "string") {
        
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

   
    const { data: productData, error: productError } = await supabase
      .from(TABLE)
      .select("*")
      .eq("id", id)
      .single();

    if (productError) throw productError;
    if (!productData) return null;

   
    const { data: imagesData, error: imagesError } = await supabase
      .from("producto_imagenes")
      .select("id_im, id_prod, url_imagen")
      .eq("id_prod", productData.id);

    if (imagesError) throw imagesError;

    
    const { data: categoryData, error: categoryError } = await supabase
      .from(TABLE_CATEGORIA)
      .select("nombre")
      .eq("id", productData.categoria)
      .single();

    
    const categoryName = categoryData ? categoryData.nombre : "Desconocido";

    
    const imagenes = imagesData.map((img) => ({
      id_im: img.id_im,
      url_imagen: img.url_imagen,
    }));

    const productComplete = {
      ...productData,
      producto_imagenes: imagenes,
      categoria: categoryName,
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

 
  if (!id_componente_maestro) {
    throw new Error("Se requiere id_componente_maestro al crear el producto");
  }

  
  const componente = await getComponentById(id_componente_maestro);
  if (!componente) {
    throw new Error("Componente maestro no encontrado");
  }

  
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

 
  return {
    ...data,
    producto_imagenes: data.producto_imagenes ?? [],
  };
}

export async function uploadProductImages(id_producto, files) {
  const { count, error: countError } = await supabase
      .from("producto_imagenes")
      .select("*", { count: "exact", head: true })
      .eq("id_prod", id_producto);
  
  if (countError) throw countError;

  // VALIDACIÓN: Máximo 10 imágenes
  if ((count + files.length) > 10) {
      throw new Error(`Límite excedido. Tienes ${count} imágenes, solo puedes agregar ${10 - count} más.`);
  }

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

    const cacheKey = `producto_full:${id_producto}`;
    await redisClient.del(cacheKey);
  
    const listKeys = await redisClient.keys("productos*");
    if (listKeys.length > 0) await redisClient.del(listKeys);

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
      .from("categoria") 
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
      .from("producto") 
      .select("*")
      .eq("categoria", categoryId)
      .eq('activo', true);

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
    
    const { error } = await supabase
      .from(TABLE)
      .update({ activo: false }) 
      .eq("id", id);

    if (error) throw error;

    console.log(`Eliminando caché para producto ${id}`);
    const cacheKeyDetail = `producto_full:${id}`;
    await redisClient.del(cacheKeyDetail);
    
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
    
    const { data: products, error } = await supabase
      .from('producto')
      .select('*')
      .eq('id_usuario', userId)
      .eq('activo', true)
      .order('fecha_publicacion', { ascending: false }); 

    if (error) throw error;
    if (!products || products.length === 0) return [];

   
    const productIds = products.map(p => p.id);
    const { data: images } = await supabase
      .from('producto_imagenes')
      .select('id_prod, url_imagen')
      .in('id_prod', productIds);

    
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

export async function deleteProductImage(id_im) {
  try {
   
    const { data: imgData, error: fetchError } = await supabase
      .from("producto_imagenes")
      .select("id_prod, url_imagen")
      .eq("id_im", id_im)
      .single();

    if (fetchError || !imgData) throw new Error("Imagen no encontrada");

    const productId = imgData.id_prod;


    const { count, error: countError } = await supabase
      .from("producto_imagenes")
      .select("*", { count: "exact", head: true })
      .eq("id_prod", productId);

    if (countError) throw countError;

   
    if (count <= 3) {
      throw new Error("No se puede eliminar. El producto debe tener al menos 3 imágenes.");
    }

  
    const fullUrl = imgData.url_imagen;

    const path = fullUrl.split(`${BUCKET_NAME}/`)[1]; 

    if (path) {
      const { error: storageError } = await supabase.storage
        .from(BUCKET_NAME)
        .remove([path]);
      if (storageError) console.error("Error borrando de Storage:", storageError);
    }


    const { error: dbError } = await supabase
      .from("producto_imagenes")
      .delete()
      .eq("id_im", id_im);

    if (dbError) throw dbError;


    const cacheKey = `producto_full:${productId}`;
    await redisClient.del(cacheKey);
    

    const listKeys = await redisClient.keys("productos*");
    if (listKeys.length > 0) await redisClient.del(listKeys);

    return true;
  } catch (error) {
    throw error; 
  }
}
