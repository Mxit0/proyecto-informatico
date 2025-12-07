import { supabase } from '../lib/supabaseClient.js';

// --- FUNCIÓN AUXILIAR (Privada) ---
// Sirve para: Buscar los datos "extra" (nombres, fotos) y dar formato para que el Frontend (Kotlin) no falle.
async function enrichReviews(reviews) {
  if (!reviews || reviews.length === 0) return [];

  // Recolectamos IDs únicos para no consultar lo mismo 20 veces
  const userIds = [...new Set(reviews.map(r => r.id_usuario))];
  const productIds = [...new Set(reviews.map(r => r.id_producto))];

  // 1. Obtener AUTORES (Usuarios que escribieron)
  // CORRECCIÓN AQUÍ: Usamos 'nombre_usuario' y 'foto' según tu tabla
  const { data: usersData, error: usersError } = await supabase
    .from('usuario') // Tabla 'usuario'
    .select('id_usuario, nombre_usuario, foto') 
    .in('id_usuario', userIds);
  
  if (usersError) throw usersError;

  // 2. Obtener PRODUCTOS
  // Sirve para: Saber el nombre del producto y quién lo vendió
  const { data: productsData, error: productsError } = await supabase
    .from('producto') // Tabla 'producto'
    .select('id, nombre, id_usuario') 
    .in('id', productIds);
  
  if (productsError) throw productsError;

  // 3. Obtener IMAGENES DE PRODUCTOS
  // Sirve para: Mostrar la fotito del producto al lado de la reseña
  const { data: imagesData, error: imagesError } = await supabase
    .from('producto_imagenes')
    .select('id_prod, url_imagen')
    .in('id_prod', productIds);
    
  if (imagesError) throw imagesError;

  // 4. Mapeo final (Unir todo en el formato que pide Kotlin)
  return reviews.map(r => {
    // Buscamos los datos cruzados
    const user = usersData.find(u => u.id_usuario === r.id_usuario);
    const product = productsData.find(p => p.id === r.id_producto);
    const prodImg = imagesData.find(img => img.id_prod === r.id_producto);

    return {
      id: String(r.id), // ID de la reseña como String
      
      // Datos del Producto
      productId: String(r.id_producto),
      productName: product ? product.nombre : 'Producto Desconocido',
      productImageUrl: prodImg ? prodImg.url_imagen : '', 
      
      // Datos del Autor (CORREGIDO con tus columnas)
      author: user ? user.nombre_usuario : `Usuario #${r.id_usuario}`, 
      authorId: String(r.id_usuario),
      authorImageUrl: user ? user.foto : null, 
      
      // Datos de la Reseña
      date: r.fecha, 
      rating: Number(r.calificacion), 
      comment: r.comentario || '',
      
      // Campo extra que pide el frontend (lo enviamos vacío por ahora)
      likedByUserIds: [] 
    };
  });
}

// --- FUNCIONES EXPORTADAS (Las que usa el Router) ---

// Sirve para: Crear una nueva reseña en la base de datos
export async function addReview({ id_producto, id_usuario, calificacion, comentario }) {
  try {
    const { data, error } = await supabase
      .from('resenas')
      .insert([{ id_producto, id_usuario, calificacion, comentario }])
      .select()
      .single();

    if (error) {
      // Si el usuario ya comentó ese producto, Supabase devuelve error 23505
      if (error.code === '23505') throw new Error('Ya has calificado este producto anteriormente.');
      throw error;
    }
    return data;
  } catch (err) {
    throw new Error('Error al crear reseña: ' + err.message);
  }
}

// Sirve para: Ver todas las reseñas de un producto específico (Pantalla de detalle de producto)
export async function getReviewsByProduct(productId) {
  try {
    const { data: reviews, error } = await supabase
      .from('resenas')
      .select('*')
      .eq('id_producto', productId)
      .order('fecha', { ascending: false });

    if (error) throw error;
    return await enrichReviews(reviews); // Enriquecemos con nombres y fotos
  } catch (err) {
    throw new Error('Error obteniendo reseñas del producto: ' + err.message);
  }
}

// Sirve para: Ver el historial de reseñas que ha escrito un usuario (Pantalla "Mis Reseñas")
export async function getReviewsWrittenByUser(userId) {
  try {
    const { data: reviews, error } = await supabase
      .from('resenas')
      .select('*')
      .eq('id_usuario', userId)
      .order('fecha', { ascending: false });

    if (error) throw error;
    return await enrichReviews(reviews);
  } catch (err) {
    throw new Error('Error obteniendo historial de reseñas: ' + err.message);
  }
}

// Sirve para: Ver qué opinan los compradores sobre un VENDEDOR (Perfil del vendedor)
export async function getReviewsReceivedBySeller(sellerId) {
  try {
    // A. Buscamos qué productos vende este señor
    const { data: sellerProducts, error: prodError } = await supabase
      .from('producto')
      .select('id')
      .eq('id_usuario', sellerId); 

    if (prodError) throw prodError;
    
    // Si no ha vendido nada o no tiene productos publicados, no tiene reseñas
    if (!sellerProducts || sellerProducts.length === 0) return [];
    
    const productIds = sellerProducts.map(p => p.id);

    // B. Buscamos todas las reseñas asociadas a esos productos
    const { data: reviews, error: revError } = await supabase
      .from('resenas')
      .select('*')
      .in('id_producto', productIds)
      .order('fecha', { ascending: false });

    if (revError) throw revError;

    return await enrichReviews(reviews);
  } catch (err) {
    throw new Error('Error obteniendo reputación del vendedor: ' + err.message);
  }
}